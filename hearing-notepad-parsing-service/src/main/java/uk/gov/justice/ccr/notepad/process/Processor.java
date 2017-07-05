package uk.gov.justice.ccr.notepad.process;


import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.stream.Collectors.toList;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultType.CURR;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultType.DATE;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultType.DURATION;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultType.INT;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultType.RESULT;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultType.TIME;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultType.TXT;
import static uk.gov.justice.ccr.notepad.view.Part.State.RESOLVED;
import static uk.gov.justice.ccr.notepad.view.Part.State.UNRESOLVED;

import uk.gov.justice.ccr.notepad.process.ResultDefinitionMatchingOutput.MatchingType;
import uk.gov.justice.ccr.notepad.result.cache.ResultCache;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultDefinition;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultPrompt;
import uk.gov.justice.ccr.notepad.view.Part;
import uk.gov.justice.ccr.notepad.view.PromptChoice;
import uk.gov.justice.ccr.notepad.view.ResultChoice;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

/**
 * Processor class will match List of provided values with resulting metadata and return Knowledge
 * back as a result Currently it has three main matching types mentioned in enum
 *
 * {@link ResultDefinitionMatchingOutput.MatchingType} Note : metadata stored in memory all in lowercase
 * and we are converting parts values in lower case before matching with metadata
 */
public class Processor {

    private final Pattern alphaNumericRegex = Pattern.compile("[a-z]+|\\d+");

    @Inject
    ResultDefinitionMatcher resultDefinitionMatcher;

    @Inject
    ResultPromptMatcher resultPromptMatcher;

    @Inject
    Time24HoursMatcher time24HoursMatcher;

    @Inject
    DateMatcher dateMatcher;

    @Inject
    ResultCache resultCache;

    private CurrencyMatcher currencyMatcher = new CurrencyMatcher();


    public void reloadResultCache(final boolean loadFromReadStore, final JsonEnvelope envelope) throws ExecutionException {
        resultCache.reloadCache(loadFromReadStore, envelope);
    }

    public Knowledge processParts(final List<String> partsValues) throws ExecutionException {
        List<String> values = partsValues.stream().map(String::toLowerCase).collect(Collectors.toList());

        Knowledge knowledge = getKnowledge(values);
        //remove all result definition parts from the list
        values.removeAll(knowledge.getResultDefinitionParts().keySet());

        knowledge = processPrompts(knowledge, values);
        return knowledge;
    }

    public Knowledge processResultPrompt(final String resultDefinitionId) throws ExecutionException {
        Knowledge knowledge = new Knowledge();
        List<ResultPrompt> resultPrompts = resultCache.getResultPromptByResultDefinitionId(resultDefinitionId);
        knowledge.setPromptChoices(resultPrompts.stream().map(resultPrompt -> {
            PromptChoice promptChoice = new PromptChoice();
            promptChoice.setCode(resultPrompt.getId());
            promptChoice.setDurationElement(resultPrompt.getDurationElement());
            promptChoice.setLabel(resultPrompt.getLabel());
            promptChoice.setType(resultPrompt.getType());
            promptChoice.setRequired("Y".equalsIgnoreCase(resultPrompt.getMandatory()));
            promptChoice.setFixedList(resultPrompt.getFixedList());
            return promptChoice;
        }).collect(toList()));
        return knowledge;
    }

    private Knowledge getKnowledge(final List<String> values) throws ExecutionException {
        Knowledge knowledge = new Knowledge();
        ResultDefinitionMatchingOutput resultDefinitionMatchingOutput = match(values);
        if (MatchingType.UNKNOWN.equals(resultDefinitionMatchingOutput.getMatchingType())) {
            getAmbiguousResultDefinition(knowledge, values);
        } else {
            knowledge.setThisPerfectMatch(true);
            if (MatchingType.EQUALS.equals(resultDefinitionMatchingOutput.getMatchingType())) {
                knowledge = getEqualsPartsKnowledge(resultDefinitionMatchingOutput, knowledge, resultDefinitionMatcher.findDefinitionExactMatchSynonyms.run(values));
            } else if (MatchingType.SHORT_CODE.equals(resultDefinitionMatchingOutput.getMatchingType())) {
                knowledge = getShortCodePartsKnowledge(resultDefinitionMatchingOutput, knowledge);
            } else if (MatchingType.CONTAINS.equals(resultDefinitionMatchingOutput.getMatchingType())) {
                knowledge = getContainsPartsKnowledge(resultDefinitionMatchingOutput, knowledge, resultDefinitionMatcher.findDefinitionPartialMatchSynonyms.run(values));
            }
        }
        return knowledge;
    }

    private Knowledge processPrompts(final Knowledge knowledge, final List<String> values) throws ExecutionException {
        getResultPromptType(knowledge, values);
        getRemainingPromptTypes(knowledge, values);
        reprocessPromptTypeTXT(knowledge.getResultPromptParts().entrySet().stream().filter(e -> TXT == e.getValue().getType()).collect(Collectors.toMap(Entry::getKey, Entry::getValue)));
        return knowledge;
    }

    private void getAmbiguousResultDefinition(final Knowledge knowledge, final List<String> values) throws ExecutionException {

        Map<String, Set<String>> matchedSynonymWords = resultDefinitionMatcher.findDefinitionExactMatchSynonyms.run(values);
        List<ResultDefinition> resultDefinitions = resultCache.getResultDefinition();
        for (Entry<String, Set<String>> entry : matchedSynonymWords.entrySet()) {
            Set<ResultChoice> resultChoices = newHashSet();
            for (String word : entry.getValue()) {
                List<Long> resultDefinitionIndexes = getIndexes(word);
                for (Long index : resultDefinitionIndexes) {
                    ResultDefinition resultDefinition = resultDefinitions.get(index.intValue());
                    ResultChoice resultChoice = getResultChoice(resultDefinition);
                    resultChoices.add(resultChoice);
                }
            }
            addToKnowledge(knowledge, entry.getKey(), resultChoices);

        }
        Set<ResultDefinition> resultDefinitionsByShortCode = resultDefinitionMatcher.findDefinitionsByShortCodes.run(values);
        resultDefinitionsByShortCode.forEach(resultDefinition -> {
            Set<ResultChoice> resultChoices = newHashSet();
            String partValue = resultDefinition.getShortCode().toLowerCase();
            ResultChoice resultChoice = getResultChoice(resultDefinition);
            resultChoices.add(resultChoice);
            addToKnowledge(knowledge, partValue, resultChoices);
        });

    }

    private void addToKnowledge(Knowledge knowledge, String key, Set<ResultChoice> collect) {
        Part part = new Part();
        part.setValue(key);
        part.setState(UNRESOLVED);
        if (!collect.isEmpty()) {
            part.setResultChoices(new TreeSet<>(collect));
        }
        knowledge.addResultDefinitionParts(key, part);
    }

    /**
     * Reprocessing TXT types for parts type duration with no gaps
     * e.g 2y is 2 years
     */
    void reprocessPromptTypeTXT(final Map<String, Part> txtParts) throws ExecutionException {
        for (Entry<String, Part> part : txtParts.entrySet()) {
            String onlyStringValue = StringUtils.isAlphanumeric(part.getKey()) ? parse(part.getKey()) : null;
            if (onlyStringValue != null) {
                ResultPrompt resultPrompt = resultPromptMatcher.match(Arrays.asList(onlyStringValue)).getResultPrompt();
                if (resultPrompt != null && DURATION == resultPrompt.getType()) {
                    changePartFromTxtToDuration(part, resultPrompt);
                }
            }
        }

    }

    private void changePartFromTxtToDuration(final Entry<String, Part> part, final ResultPrompt resultPrompt) {
        Part p = part.getValue();
        p.setType(resultPrompt.getType());
        p.setOriginalText(part.getKey());
        if (!StringUtils.isEmpty(resultPrompt.getDurationElement())) {
            p.setLabel(resultPrompt.getDurationElement());
        }
    }

    private ResultChoice getResultChoice(final ResultDefinition resultDefinition) {
        ResultChoice resultChoice = new ResultChoice(resultDefinition.getId(), resultDefinition.getLabel());
        resultChoice.setLevel(resultDefinition.getLevel());
        resultChoice.setType(RESULT);
        return resultChoice;
    }

    private void getRemainingPromptTypes(final Knowledge knowledge, final List<String> values) {
        values.forEach(s -> {
                    Part p = new Part();
                    if (dateMatcher.match(s)) {
                        p.setType(DATE);
                    } else if (time24HoursMatcher.match(s)) {
                        p.setType(TIME);
                    } else if (StringUtils.isNumeric(s)) {
                        p.setType(INT);
                    } else if (currencyMatcher.match(s)) {
                        p.setType(CURR);
                        p.setValue(s.replaceAll("£", ""));
                    } else {
                        p.setType(TXT);
                    }
                    p.setState(UNRESOLVED);
                    knowledge.addResultPromptParts(s, p);
                }

        );
        values.removeAll(knowledge.getResultPromptParts().keySet());
    }

    private void getResultPromptType(final Knowledge knowledge, final List<String> values) throws ExecutionException {
        ResultPromptMatchingOutput resultPromptMatchingOutput = resultPromptMatcher.match(values);
        while (resultPromptMatchingOutput != null && resultPromptMatchingOutput.getResultPrompt() != null) {
            getPromptParts(resultPromptMatchingOutput, knowledge, resultPromptMatcher.findPromptSynonyms.run(values));
            values.removeAll(knowledge.getResultPromptParts().keySet());
            resultPromptMatchingOutput = resultPromptMatcher.match(values);
        }

    }

    void getPromptParts(final ResultPromptMatchingOutput resultPromptMatchingOutput, final Knowledge knowledge, final Map<String, Set<String>> values) {
        findQualifiedPartsForResultPrompt(resultPromptMatchingOutput, knowledge, values);
    }

    private Knowledge getShortCodePartsKnowledge(final ResultDefinitionMatchingOutput resultDefinitionMatchingOutput, final Knowledge knowledge) {
        final ResultDefinition resultDefinition = resultDefinitionMatchingOutput.getResultDefinition();
        knowledge.addResultDefinitionParts(resultDefinition.getShortCode().toLowerCase(), getPart(resultDefinition));
        return knowledge;
    }


    Knowledge getContainsPartsKnowledge(final ResultDefinitionMatchingOutput resultDefinitionMatchingOutput, final Knowledge knowledge, final Map<String, Set<String>> values) {
        findQualifiedPartsForResultDefinition(resultDefinitionMatchingOutput, knowledge, values);
        return knowledge;
    }

    Knowledge getEqualsPartsKnowledge(final ResultDefinitionMatchingOutput resultDefinitionMatchingOutput, final Knowledge knowledge, final Map<String, Set<String>> values) {
        findQualifiedPartsForResultDefinition(resultDefinitionMatchingOutput, knowledge, values);
        return knowledge;
    }

    private void findQualifiedPartsForResultDefinition(final ResultDefinitionMatchingOutput resultDefinitionMatchingOutput, final Knowledge knowledge, Map<String, Set<String>> values) {
        final ResultDefinition resultDefinition = resultDefinitionMatchingOutput.getResultDefinition();
        values.entrySet().stream().forEach(stringSetEntry -> resultDefinitionMatchingOutput.getResultDefinition().getKeywords().forEach(s -> {
            if (stringSetEntry.getValue().contains(s)) {
                knowledge.addResultDefinitionParts(stringSetEntry.getKey(), getPart(resultDefinition));
            }
        }));
    }

    private void findQualifiedPartsForResultPrompt(final ResultPromptMatchingOutput resultPromptMatchingOutput, final Knowledge knowledge, Map<String, Set<String>> values) {
        final ResultPrompt resultPrompt = resultPromptMatchingOutput.getResultPrompt();
        values.entrySet().stream().forEach(stringSetEntry -> resultPromptMatchingOutput.getResultPrompt().getKeywords().forEach(s -> {
            if (stringSetEntry.getValue().contains(s)) {
                knowledge.addResultPromptParts(stringSetEntry.getKey(), getPart(resultPrompt));
            }
        }));
    }

    private Part getPart(final ResultDefinition resultDefinition) {
        Part part = new Part();
        setCommonProperty(part, resultDefinition.getId(), resultDefinition.getLabel());
        part.setType(RESULT);
        part.setState(RESOLVED);
        part.setResultLevel(resultDefinition.getLevel());
        return part;
    }

    private void setCommonProperty(final Part part, final String id, final String label) {
        part.setCode(id);
        part.setValue(label);
    }

    private Part getPart(final ResultPrompt resultPrompt) {
        Part part = new Part();
        setCommonProperty(part, resultPrompt.getId(), resultPrompt.getLabel());
        part.setType(resultPrompt.getType());
        part.setState(UNRESOLVED);
        if (!StringUtils.isEmpty(resultPrompt.getDurationElement())) {
            part.setLabel(resultPrompt.getDurationElement());
        } else {
            part.setLabel(resultPrompt.getLabel());
        }
        return part;
    }

    private ResultDefinitionMatchingOutput match(final List<String> partsValues) throws ExecutionException {
        return resultDefinitionMatcher.match(partsValues);
    }

    private List<Long> getIndexes(final String word) throws ExecutionException {
        return resultCache.getResultDefinitionsIndexGroupByKeyword().entrySet()
                .stream()
                .filter(entry -> word.equals(entry.getKey()))
                .map(Entry::getValue)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    /**
     * Part 2y means 2 years, if entered part starts with integer and ends with txt means parts can
     * be duration. this function will returns txt value only.
     */
    private String parse(final String value) {
        Matcher m = alphaNumericRegex.matcher(value);
        List<String> allMatches = newArrayList();
        while (m.find()) {
            allMatches.add(m.group());
        }
        if (allMatches.size() == 2 && StringUtils.isNumeric(allMatches.get(0)) && StringUtils.isAlpha(allMatches.get(1))) {
            return allMatches.get(1);
        }
        return null;
    }
}
