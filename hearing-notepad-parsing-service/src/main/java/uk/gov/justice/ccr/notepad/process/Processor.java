package uk.gov.justice.ccr.notepad.process;


import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
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
import uk.gov.justice.ccr.notepad.result.cache.model.ChildResultDefinition;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultDefinition;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultPrompt;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultPromptDynamicListNameAddress;
import uk.gov.justice.ccr.notepad.view.AddressParts;
import uk.gov.justice.ccr.notepad.view.NameAddress;
import uk.gov.justice.ccr.notepad.view.Part;
import uk.gov.justice.ccr.notepad.view.PromptChoice;
import uk.gov.justice.ccr.notepad.view.ResultChoice;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Processor class will match List of provided values with resulting metadata and return Knowledge
 * back as a result Currently it has three main matching types mentioned in enum
 * <p>
 * {@link ResultDefinitionMatchingOutput.MatchingType} Note : metadata stored in memory all in
 * lowercase and we are converting parts values in lower case before matching with metadata
 */
public class Processor {
    @Inject
    private ResultDefinitionMatcher resultDefinitionMatcher;
    @Inject
    private ResultPromptMatcher resultPromptMatcher;
    @Inject
    private Time24HoursMatcher time24HoursMatcher;
    @Inject
    private DateMatcher dateMatcher;
    @Inject
    private ResultCache resultCache;
    @Inject
    private FindDefinitionExactMatchSynonyms findDefinitionExactMatchSynonyms;
    @Inject
    private FindDefinitionsByShortCodes findDefinitionsByShortCodes;
    @Inject
    private FindPromptSynonyms findPromptSynonyms;

    private final Pattern alphaNumericRegex = Pattern.compile("[a-z]+|\\d+");
    private final CurrencyMatcher currencyMatcher = new CurrencyMatcher();

    private static final String HCHOUSE = "HCHOUSE";
    private static final String HCROOM = "HCROOM";
    private static final String ONEOF = "ONEOF";
    private static final Logger LOGGER = LoggerFactory.getLogger(Processor.class);

    public void lazyLoad(final JsonEnvelope envelope, final LocalDate referenceDate) {
        resultCache.lazyLoad(envelope, referenceDate);
    }

    public Knowledge processParts(final List<String> partsValues, final LocalDate referenceDate) {
        final List<String> values = partsValues.stream().map(String::toLowerCase).collect(Collectors.toList());
        final Knowledge knowledge = getKnowledge(values, referenceDate);
        final Map<String, Part> resultDefinitionParts = knowledge.getResultDefinitionParts();

        if (nonNull(resultDefinitionParts) && resultDefinitionParts.size() > 0) {
            //remove all result definition parts from the list and only process parse for prompt
            values.removeAll(resultDefinitionParts.keySet());
        } else {
            //in case no result definition found treat all parts as TXT
            addAllPartsAsTxt(knowledge, values);
        }

        return processPrompts(knowledge, values, referenceDate);
    }

    public Knowledge processResultPrompt(final String resultDefinitionId, final LocalDate referenceDate) {
        final Knowledge knowledge = new Knowledge();
        List<ResultPrompt> resultPrompts = resultCache.getResultPromptByResultDefinitionId(resultDefinitionId, referenceDate);
        LOGGER.debug("resultPrompts unordered: {}", resultPrompts);
        resultPrompts = new ResultPromptsOrder().process(resultPrompts);
        LOGGER.debug("resultPrompts ordered: {}", resultPrompts);
        knowledge.setPromptChoices(resultPrompts.stream().map(this::getPromptChoice).collect(toList()));
        return knowledge;
    }

    private PromptChoice getPromptChoice(final ResultPrompt resultPrompt) {
        final PromptChoice promptChoice = new PromptChoice();
        promptChoice.setCode(resultPrompt.getId());
        promptChoice.setDurationElement(resultPrompt.getDurationElement());
        promptChoice.setWelshDurationElement(resultPrompt.getWelshDurationElement());
        promptChoice.setLabel(resultPrompt.getLabel());
        promptChoice.setType(resultPrompt.getType());
        promptChoice.setPromptRef(resultPrompt.getReference());
        promptChoice.setPromptOrder(resultPrompt.getPromptOrder());
        final String resultPromptRule = resultPrompt.getResultPromptRule();
        promptChoice.setRequired("optional".equals(resultPromptRule) ? FALSE : TRUE);
        setComponentType(promptChoice, resultPromptRule, resultPrompt);
        promptChoice.setFixedList(resultPrompt.getFixedList());
        setNameAddressList(resultPrompt, promptChoice);
        promptChoice.setComponentLabel(resultPrompt.getComponentLabel());
        promptChoice.setAddressType(resultPrompt.getAddressType());
        promptChoice.setListLabel(resultPrompt.getListLabel());
        promptChoice.setPartName(resultPrompt.getPartName());
        promptChoice.setDurationSequence(resultPrompt.getDurationSequence());
        promptChoice.setHidden(resultPrompt.getHidden());
        promptChoice.setNameEmail(resultPrompt.getNameEmail());
        return promptChoice;
    }

    private void setNameAddressList(final ResultPrompt resultPrompt, final PromptChoice promptChoice) {
        if (resultPrompt.getNameAddressList() != null) {
            final Set<ResultPromptDynamicListNameAddress> nameAddressList = resultPrompt.getNameAddressList();
            if (resultPrompt.getNameEmail() != null && resultPrompt.getNameEmail().booleanValue()) {
                final Set<ResultPromptDynamicListNameAddress> nameEmailOnlyAddressList = nameAddressList.stream().map(nameAddress ->
                        ResultPromptDynamicListNameAddress.resultPromptDynamicListNameAddressBuilder()
                                .withName(nameAddress.getName())
                                .withEmailAddress1(nameAddress.getEmailAddress1())
                                .withEmailAddress2(nameAddress.getEmailAddress2())
                                .build()
                ).collect(toSet());
                promptChoice.setNameAddressList(getNameAddressList(nameEmailOnlyAddressList));
            } else {
                promptChoice.setNameAddressList(getNameAddressList(nameAddressList));
            }
        }
    }

    private Set<NameAddress> getNameAddressList(final Set<ResultPromptDynamicListNameAddress> dynamicListNameAddressSet) {
        return dynamicListNameAddressSet.stream().map(resultPromptDynamicListNameAddress -> NameAddress.nameAddress()
                .withLabel(resultPromptDynamicListNameAddress.getName())
                .withAddressParts(buildAddressParts(resultPromptDynamicListNameAddress))
                .build()).collect(toSet());
    }

    private AddressParts buildAddressParts(final ResultPromptDynamicListNameAddress resultPromptDynamicListNameAddress) {

        return AddressParts.addressParts()
                .withName(resultPromptDynamicListNameAddress.getName())
                .withFirstName(resultPromptDynamicListNameAddress.getFirstName())
                .withMiddleName(resultPromptDynamicListNameAddress.getMiddleName())
                .withLastName(resultPromptDynamicListNameAddress.getLastName())
                .withAddress1(resultPromptDynamicListNameAddress.getAddressLine1())
                .withAddress2(resultPromptDynamicListNameAddress.getAddressLine2())
                .withAddress3(resultPromptDynamicListNameAddress.getAddressLine3())
                .withAddress4(resultPromptDynamicListNameAddress.getAddressLine4())
                .withAddress5(resultPromptDynamicListNameAddress.getAddressLine5())
                .withPostCode(resultPromptDynamicListNameAddress.getPostcode())
                .withEmail1(resultPromptDynamicListNameAddress.getEmailAddress1())
                .withEmail2(resultPromptDynamicListNameAddress.getEmailAddress2())
                .build();

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
        if (nonNull(resultDefinition) && isNotEmpty(resultDefinition.getChildResultDefinitions())) {
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

    @SuppressWarnings("squid:S4165")
    private Knowledge getKnowledge(final List<String> values, final LocalDate referenceDate) {
        final ResultDefinitionMatchingOutput resultDefinitionMatchingOutput = resultDefinitionMatcher.match(values, referenceDate);
        final MatchingType matchingType = resultDefinitionMatchingOutput.getMatchingType();
        Knowledge knowledge = new Knowledge();

        if (MatchingType.UNKNOWN.equals(matchingType)) {
            getAmbiguousResultDefinition(knowledge, values, referenceDate);
        } else {
            knowledge.setThisPerfectMatch(true);

            if (MatchingType.EQUALS.equals(matchingType)) {
                final Map<String, Set<String>> setMap = findDefinitionExactMatchSynonyms.run(values, referenceDate);
                knowledge = addResulDefinitionParts(resultDefinitionMatchingOutput, knowledge, setMap);
            } else if (MatchingType.SHORT_CODE.equals(resultDefinitionMatchingOutput.getMatchingType())) {
                knowledge = addResulDefinitionParts(resultDefinitionMatchingOutput, knowledge);
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
        final Map<String, Set<String>> matchedSynonymWords = findDefinitionExactMatchSynonyms.run(values, referenceDate);
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
        final Set<ResultDefinition> resultDefinitionsByShortCode = findDefinitionsByShortCodes.run(values, referenceDate);
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

    private void changePartFromTxtToDuration(final Entry<String, Part> part, final ResultPrompt resultPrompt) {
        final Part p = part.getValue();
        p.setType(resultPrompt.getType());
        p.setOriginalText(part.getKey());
        if (!StringUtils.isEmpty(resultPrompt.getDurationElement())) {
            p.setLabel(resultPrompt.getDurationElement());
        }
    }

    private void getResultPromptType(final Knowledge knowledge, final List<String> values, final LocalDate referenceDate) {
        ResultPromptMatchingOutput resultPromptMatchingOutput = resultPromptMatcher.match(values, referenceDate, Optional.of(knowledge));
        while (nonNull(resultPromptMatchingOutput) && nonNull(resultPromptMatchingOutput.getResultPrompt())) {
            addResultPromptParts(resultPromptMatchingOutput, knowledge, findPromptSynonyms.run(values, referenceDate));
            values.removeAll(knowledge.getResultPromptParts().keySet());
            resultPromptMatchingOutput = resultPromptMatcher.match(values, referenceDate, Optional.of(knowledge));
        }
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

    /**
     * Reprocessing TXT types for parts type duration with no gaps e.g 2y is 2 years
     */
    private void reprocessPromptTypeTXT(final Map<String, Part> txtParts, final LocalDate referenceDate) {
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

    private ResultChoice getResultChoice(final ResultDefinition resultDefinition) {
        final ResultChoice resultChoice = new ResultChoice(resultDefinition.getId(), resultDefinition.getLabel());
        resultChoice.setShortCode(resultDefinition.getShortCode());
        resultChoice.setLevel(resultDefinition.getLevel());
        resultChoice.setType(RESULT);
        return resultChoice;
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

    private Knowledge addResulDefinitionParts(final ResultDefinitionMatchingOutput resultDefinitionMatchingOutput, final Knowledge knowledge) {
        final ResultDefinition resultDefinition = resultDefinitionMatchingOutput.getResultDefinition();
        knowledge.addResultDefinitionParts(resultDefinition.getShortCode().toLowerCase(), generatePart(resultDefinition));
        return knowledge;
    }

    private Knowledge addResulDefinitionParts(final ResultDefinitionMatchingOutput resultDefinitionMatchingOutput, final Knowledge knowledge, final Map<String, Set<String>> values) {
        final ResultDefinition resultDefinition = resultDefinitionMatchingOutput.getResultDefinition();
        values.entrySet().stream().forEach(stringSetEntry -> resultDefinitionMatchingOutput.getResultDefinition().getKeywords().forEach(s -> {
            if (stringSetEntry.getValue().contains(s)) {
                knowledge.addResultDefinitionParts(stringSetEntry.getKey(), generatePart(resultDefinition));
            }
        }));
        return knowledge;
    }

    private void addResultPromptParts(final ResultPromptMatchingOutput resultPromptMatchingOutput, final Knowledge knowledge, final Map<String, Set<String>> values) {
        final ResultPrompt resultPrompt = resultPromptMatchingOutput.getResultPrompt();
        values.entrySet().stream().forEach(stringSetEntry -> resultPromptMatchingOutput.getResultPrompt().getKeywords().forEach(s -> {
            if (stringSetEntry.getValue().contains(s)) {
                knowledge.addResultPromptParts(stringSetEntry.getKey(), generatePart(resultPrompt));
            }
        }));
    }

    private Part generatePart(final ResultDefinition resultDefinition) {
        final Part part = new Part();
        part.setCode(resultDefinition.getId());
        part.setValue(resultDefinition.getLabel());
        part.setType(RESULT);
        part.setState(RESOLVED);
        part.setResultLevel(resultDefinition.getLevel());
        return part;
    }

    private Part generatePart(final ResultPrompt resultPrompt) {
        final Part part = new Part();
        part.setCode(resultPrompt.getId());
        part.setValue(resultPrompt.getLabel());
        part.setType(resultPrompt.getType());
        part.setState(UNRESOLVED);
        if (!StringUtils.isEmpty(resultPrompt.getDurationElement())) {
            part.setLabel(resultPrompt.getDurationElement());
        } else {
            part.setLabel(resultPrompt.getLabel());
        }
        return part;
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
