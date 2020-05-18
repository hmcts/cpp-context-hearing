package uk.gov.justice.ccr.notepad.process;


import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.ccr.notepad.process.ResultDefinitionMatchingOutput.MatchingType;
import uk.gov.justice.ccr.notepad.result.cache.ResultCache;
import uk.gov.justice.ccr.notepad.result.cache.model.ChildResultDefinition;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultDefinition;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultPrompt;
import uk.gov.justice.ccr.notepad.view.Part;
import uk.gov.justice.ccr.notepad.view.PromptChoice;
import uk.gov.justice.ccr.notepad.view.ResultChoice;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultType.*;
import static uk.gov.justice.ccr.notepad.view.Part.State.RESOLVED;
import static uk.gov.justice.ccr.notepad.view.Part.State.UNRESOLVED;

/**
 * Processor class will match List of provided values with resulting metadata and return Knowledge
 * back as a result Currently it has three main matching types mentioned in enum
 * <p>
 * {@link ResultDefinitionMatchingOutput.MatchingType} Note : metadata stored in memory all in
 * lowercase and we are converting parts values in lower case before matching with metadata
 */
public class Processor {

    private static final String HCHOUSE = "HCHOUSE";
    private static final String HCROOM = "HCROOM";
    private static final String ONEOF = "ONEOF";
    private static final Logger LOGGER = LoggerFactory.getLogger(Processor.class);
    private final Pattern alphaNumericRegex = Pattern.compile("[a-z]+|\\d+");
    private final CurrencyMatcher currencyMatcher = new CurrencyMatcher();
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

    public void lazyLoad(final JsonEnvelope envelope, final LocalDate referenceDate) {
        resultCache.lazyLoad(envelope, referenceDate);
    }

    public Knowledge processParts(final List<String> partsValues, final LocalDate referenceDate) {
        final List<String> values = partsValues.stream().map(String::toLowerCase).collect(Collectors.toList());

        final Knowledge knowledge = getKnowledge(values, referenceDate);

        if (checkKnowledgeHavingResultDefinition(knowledge).get()) {
            //remove all result definition parts from the list and only process parse for prompt
            values.removeAll(knowledge.getResultDefinitionParts().keySet());
        } else {
            //in case no result definition found treat all parts as TXT
            addAllPartsAsTxt(knowledge, values);
        }

        return processPrompts(knowledge, values, referenceDate);
    }

    private Optional<Boolean> checkKnowledgeHavingResultDefinition(final Knowledge knowledge) {
        return Optional.of(knowledge.getResultDefinitionParts().size() > 0);
    }

    public Knowledge processResultPrompt(final String resultDefinitionId, final LocalDate referenceDate) {
        final Knowledge knowledge = new Knowledge();
        List<ResultPrompt> resultPrompts = resultCache.getResultPromptByResultDefinitionId(resultDefinitionId, referenceDate);
        LOGGER.debug("resultPrompts unordered:" + resultPrompts);
        resultPrompts = new ResultPromptsOrder().process(resultPrompts);
        LOGGER.debug("resultPrompts ordered:" + resultPrompts);
        knowledge.setPromptChoices(resultPrompts.stream().map(resultPrompt -> {
            PromptChoice promptChoice = new PromptChoice();
            promptChoice.setCode(resultPrompt.getId());
            promptChoice.setDurationElement(resultPrompt.getDurationElement());
            promptChoice.setLabel(resultPrompt.getLabel());
            promptChoice.setType(resultPrompt.getType());
            final String resultPromptRule = resultPrompt.getResultPromptRule();
            promptChoice.setRequired("optional".equals(resultPromptRule) ? FALSE : TRUE);
            setComponentType(promptChoice, resultPromptRule, resultPrompt);
            promptChoice.setFixedList(resultPrompt.getFixedList());
            promptChoice.setDurationSequence(resultPrompt.getDurationSequence());
            promptChoice.setHidden(resultPrompt.getHidden());
            return promptChoice;
        }).collect(toList()));
        return knowledge;
    }

    private void setComponentType(final PromptChoice promptChoice, final String resultPromptRule, final ResultPrompt resultPrompt) {
        if (HCHOUSE.equals(resultPrompt.getReference())) {
            promptChoice.setComponentType(HCHOUSE);
        } else if (HCROOM.equals(resultPrompt.getReference())) {
            promptChoice.setComponentType(HCROOM);
        } else {
            promptChoice.setComponentType("oneOf".equals(resultPromptRule) ? ONEOF : resultPrompt.getType().toString());
        }
    }

    public ResultDefinition retrieveResultDefinitionById(final String resultDefinitionId, final LocalDate orderedDate) {
        return resultCache.getResultDefinitionsById(resultDefinitionId, orderedDate);
    }


    public ChildResultDefinitionDetail retrieveChildResultDefinitionDetail(final String resultDefinitionId, final LocalDate referenceDate) {

        final ResultDefinition resultDefinition = resultCache.getResultDefinitionsById(resultDefinitionId, referenceDate);

        ChildResultDefinitionDetail childResultDefinitionDetail = null;
        if (Objects.nonNull(resultDefinition) && isNotEmpty(resultDefinition.getChildResultDefinitions())) {
            childResultDefinitionDetail = new ChildResultDefinitionDetail(resultDefinition,
                    retrieveChildResultDefinitions(resultDefinition, referenceDate));
        }
        return childResultDefinitionDetail;
    }

    private List<ResultDefinition> retrieveChildResultDefinitions(final ResultDefinition resultDefinition, final LocalDate referenceDate) {
        return resultDefinition.getChildResultDefinitions().stream()
                .map(ChildResultDefinition::getChildResultDefinitionId)
                .map(childResultDefinitionId -> resultCache.getResultDefinitionsById(childResultDefinitionId.toString(), referenceDate))
                .collect(toList());
    }

    private Knowledge getKnowledge(final List<String> values, final LocalDate referenceDate) {
        Knowledge knowledge = new Knowledge();
        final ResultDefinitionMatchingOutput resultDefinitionMatchingOutput = match(values, referenceDate);
        if (MatchingType.UNKNOWN.equals(resultDefinitionMatchingOutput.getMatchingType())) {
            getAmbiguousResultDefinition(knowledge, values, referenceDate);
        } else {
            knowledge.setThisPerfectMatch(true);
            if (MatchingType.EQUALS.equals(resultDefinitionMatchingOutput.getMatchingType())) {
                knowledge = getEqualsPartsKnowledge(resultDefinitionMatchingOutput, knowledge, resultDefinitionMatcher.findDefinitionExactMatchSynonyms.run(values, referenceDate));
            } else if (MatchingType.SHORT_CODE.equals(resultDefinitionMatchingOutput.getMatchingType())) {
                knowledge = getShortCodePartsKnowledge(resultDefinitionMatchingOutput, knowledge);
            } else if (MatchingType.CONTAINS.equals(resultDefinitionMatchingOutput.getMatchingType())) {
                knowledge = getContainsPartsKnowledge(resultDefinitionMatchingOutput, knowledge, resultDefinitionMatcher.findDefinitionPartialMatchSynonyms.run(values, referenceDate));
            }
        }
        return knowledge;
    }

    private Knowledge processPrompts(final Knowledge knowledge, final List<String> values, final LocalDate referenceDate) {
        getResultPromptType(knowledge, values, referenceDate);
        getRemainingPromptTypes(knowledge, values);
        reprocessPromptTypeTXT(knowledge.getResultPromptParts().entrySet().stream().filter(e -> TXT == e.getValue().getType()).collect(Collectors.toMap(Entry::getKey, Entry::getValue)), referenceDate);
        return knowledge;
    }

    private void getAmbiguousResultDefinition(final Knowledge knowledge, final List<String> values, final LocalDate referenceDate) {

        final Map<String, Set<String>> matchedSynonymWords = resultDefinitionMatcher.findDefinitionExactMatchSynonyms.run(values, referenceDate);
        final List<ResultDefinition> resultDefinitions = resultCache.getResultDefinitions(referenceDate);
        for (final Entry<String, Set<String>> entry : matchedSynonymWords.entrySet()) {
            final Set<ResultChoice> resultChoices = newHashSet();
            for (final String word : entry.getValue()) {
                final List<Long> resultDefinitionIndexes = getIndexes(word, referenceDate);
                for (final Long index : resultDefinitionIndexes) {
                    final ResultDefinition resultDefinition = resultDefinitions.get(index.intValue());
                    final ResultChoice resultChoice = getResultChoice(resultDefinition);
                    resultChoices.add(resultChoice);
                }
            }
            addToKnowledge(knowledge, entry.getKey(), resultChoices);

        }
        final Set<ResultDefinition> resultDefinitionsByShortCode = resultDefinitionMatcher.findDefinitionsByShortCodes.run(values, referenceDate);
        resultDefinitionsByShortCode.forEach(resultDefinition -> {
            final Set<ResultChoice> resultChoices = newHashSet();
            final String partValue = resultDefinition.getShortCode().toLowerCase();
            final ResultChoice resultChoice = getResultChoice(resultDefinition);
            resultChoices.add(resultChoice);
            addToKnowledge(knowledge, partValue, resultChoices);
        });

    }

    private void addToKnowledge(final Knowledge knowledge, final String key, final Set<ResultChoice> collect) {
        final Part part = new Part();
        part.setValue(key);
        part.setState(UNRESOLVED);
        if (!collect.isEmpty()) {
            part.setResultChoices(new TreeSet<>(collect));
        }
        knowledge.addResultDefinitionParts(key, part);
    }

    /**
     * Reprocessing TXT types for parts type duration with no gaps e.g 2y is 2 years
     */
    void reprocessPromptTypeTXT(final Map<String, Part> txtParts, final LocalDate referenceDate) {
        for (final Entry<String, Part> part : txtParts.entrySet()) {
            final String onlyStringValue = StringUtils.isAlphanumeric(part.getKey()) ? parse(part.getKey()) : null;
            if (onlyStringValue != null) {
                final ResultPrompt resultPrompt = resultPromptMatcher.match(Arrays.asList(onlyStringValue), referenceDate).getResultPrompt();
                if (resultPrompt != null && DURATION == resultPrompt.getType()) {
                    changePartFromTxtToDuration(part, resultPrompt);
                }
            }
        }

    }

    private void changePartFromTxtToDuration(final Entry<String, Part> part, final ResultPrompt resultPrompt) {
        final Part p = part.getValue();
        p.setType(resultPrompt.getType());
        p.setOriginalText(part.getKey());
        if (!StringUtils.isEmpty(resultPrompt.getDurationElement())) {
            p.setLabel(resultPrompt.getDurationElement());
        }
    }

    private ResultChoice getResultChoice(final ResultDefinition resultDefinition) {
        final ResultChoice resultChoice = new ResultChoice(resultDefinition.getId(), resultDefinition.getLabel());
        resultChoice.setLevel(resultDefinition.getLevel());
        resultChoice.setType(RESULT);
        return resultChoice;
    }

    private void getRemainingPromptTypes(final Knowledge knowledge, final List<String> values) {
        values.forEach(s -> {
                    final Part p = new Part();
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

    private void addAllPartsAsTxt(final Knowledge knowledge, final List<String> values) {
        values.forEach(s -> {
                    final Part p = new Part();
                    p.setType(TXT);
                    p.setState(UNRESOLVED);
                    knowledge.addResultPromptParts(s, p);
                }

        );
        values.removeAll(knowledge.getResultPromptParts().keySet());
    }

    private void getResultPromptType(final Knowledge knowledge, final List<String> values, final LocalDate referenceDate) {
        ResultPromptMatchingOutput resultPromptMatchingOutput = resultPromptMatcher.match(values, referenceDate);
        while (resultPromptMatchingOutput != null && resultPromptMatchingOutput.getResultPrompt() != null) {
            getPromptParts(resultPromptMatchingOutput, knowledge, resultPromptMatcher.findPromptSynonyms.run(values, referenceDate));
            values.removeAll(knowledge.getResultPromptParts().keySet());
            resultPromptMatchingOutput = resultPromptMatcher.match(values, referenceDate);
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

    private void findQualifiedPartsForResultDefinition(final ResultDefinitionMatchingOutput resultDefinitionMatchingOutput, final Knowledge knowledge, final Map<String, Set<String>> values) {
        final ResultDefinition resultDefinition = resultDefinitionMatchingOutput.getResultDefinition();
        values.entrySet().stream().forEach(stringSetEntry -> resultDefinitionMatchingOutput.getResultDefinition().getKeywords().forEach(s -> {
            if (stringSetEntry.getValue().contains(s)) {
                knowledge.addResultDefinitionParts(stringSetEntry.getKey(), getPart(resultDefinition));
            }
        }));
    }

    private void findQualifiedPartsForResultPrompt(final ResultPromptMatchingOutput resultPromptMatchingOutput, final Knowledge knowledge, final Map<String, Set<String>> values) {
        final ResultPrompt resultPrompt = resultPromptMatchingOutput.getResultPrompt();
        values.entrySet().stream().forEach(stringSetEntry -> resultPromptMatchingOutput.getResultPrompt().getKeywords().forEach(s -> {
            if (stringSetEntry.getValue().contains(s)) {
                knowledge.addResultPromptParts(stringSetEntry.getKey(), getPart(resultPrompt));
            }
        }));
    }

    private Part getPart(final ResultDefinition resultDefinition) {
        final Part part = new Part();
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
        final Part part = new Part();
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

    private ResultDefinitionMatchingOutput match(final List<String> partsValues, final LocalDate referenceDate) {
        return resultDefinitionMatcher.match(partsValues, referenceDate);
    }

    private List<Long> getIndexes(final String word, final LocalDate referenceDate) {
        return resultCache.getResultDefinitionsIndexGroupByKeyword(referenceDate).entrySet()
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
        final Matcher m = alphaNumericRegex.matcher(value);
        final List<String> allMatches = newArrayList();
        while (m.find()) {
            allMatches.add(m.group());
        }
        if (allMatches.size() == 2 && StringUtils.isNumeric(allMatches.get(0)) && StringUtils.isAlpha(allMatches.get(1))) {
            return allMatches.get(1);
        }
        return null;
    }
}
