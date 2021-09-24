package uk.gov.justice.ccr.notepad.view;


import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newConcurrentMap;

import static uk.gov.justice.ccr.notepad.result.cache.model.ResultType.BOOLEAN;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultType.CURR;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultType.DURATION;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultType.IGNORED;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultType.INT;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultType.TXT;
import static uk.gov.justice.ccr.notepad.view.Part.State.RESOLVED;
import static uk.gov.justice.ccr.notepad.view.Part.State.UNRESOLVED;

import uk.gov.justice.ccr.notepad.process.Knowledge;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;

public class ResultDefinitionViewBuilder {

    private final Pattern alphaNumericRegex = Pattern.compile("[a-z]+|\\d+");

    public String getResultDefinitionIdFromKnowledge(final List<Part> parts, final Knowledge knowledge) {
        final List<String> partValues = parts.stream().map(v -> v.getValue().toString().toLowerCase()).collect(Collectors.toList());
        if (knowledge.isThisPerfectMatch()) {
            final Part firstQualifiedPartInResultDefinition = getFirstQualifiedPartInResultDefinition(partValues, knowledge);
            return firstQualifiedPartInResultDefinition.getCode();
        }
        return null;
    }

    public ResultDefinitionView buildFromKnowledge(final List<Part> parts, final Knowledge knowledge, final List<ChildResultDefinition> childResultDefinitions, final Boolean excludedFromResults, final Boolean booleanResult, final String label, final List<PromptChoice> promptChoices) {
        final ResultDefinitionView resultDefinitionView = new ResultDefinitionView();
        final List<String> partValues = parts.stream().map(v -> v.getValue().toString().toLowerCase()).collect(Collectors.toList());
        if (knowledge.isThisPerfectMatch()) {
            final Part firstQualifiedPartInResultDefinition = getFirstQualifiedPartInResultDefinition(partValues, knowledge);
            final String resultDefinitionId = firstQualifiedPartInResultDefinition.getCode();
            resultDefinitionView.setResultDefinitionId(resultDefinitionId);
            resultDefinitionView.setResultLevel(firstQualifiedPartInResultDefinition.getResultLevel());
            resultDefinitionView.setParts(buildParts(knowledge, partValues, parts));
            resultDefinitionView.setChildResultDefinitions(childResultDefinitions);
            resultDefinitionView.setConditionalMandatory(booleanResult);
            resultDefinitionView.setLabel(label);
            resultDefinitionView.setExcludedFromResults(excludedFromResults);
            resultDefinitionView.setPromptChoices(promptChoices);

        } else {
            resultDefinitionView.setParts(buildAmbiguousParts(knowledge, partValues, parts));
        }
        joinParts(resultDefinitionView.getParts(), knowledge);
        final List<Part> visibleParts = resultDefinitionView.getParts().stream().filter(Part::getVisible).collect(Collectors.toList());
        groupDurationInConsecutiveOrder(visibleParts);
        resultDefinitionView.setParts(visibleParts.stream().filter(Part::getVisible).collect(Collectors.toList()));
        return resultDefinitionView;
    }

    private void joinParts(final List<Part> parts, final Knowledge knowledge) {
        joinDurationParts(parts);
        joinBooleanParts(parts, knowledge);

    }

    private void groupDurationInConsecutiveOrder(final List<Part> parts) {
        final Map<Integer, List<Integer>> groupDurationIndexesInSequence = newConcurrentMap();
        final AtomicInteger indexIncrementer = new AtomicInteger();
        final List<Integer> tempIndexBucket = newArrayList();

        parts.forEach(part -> {
            final int index = indexIncrementer.getAndIncrement();
            if (DURATION == part.getType()) {
                final int previousIndex = index - 1;
                if (groupDurationIndexesInSequence.containsKey(previousIndex)) {
                    tempIndexBucket.add(previousIndex);
                }
                if (!tempIndexBucket.isEmpty() && groupDurationIndexesInSequence.containsKey(tempIndexBucket.get(0)) && tempIndexBucket.contains(previousIndex)) {
                    final List<Integer> sequenceDurationIndexes = groupDurationIndexesInSequence.get(tempIndexBucket.get(0));
                    tempIndexBucket.add(index);
                    sequenceDurationIndexes.add(index);
                } else {
                    groupDurationIndexesInSequence.putIfAbsent(index, newArrayList());
                    tempIndexBucket.clear();
                }
            }
        });
        groupDurationIndexesInSequence.forEach((integer, integers) -> {
            final Part partToEnrich = parts.get(integer);
            integers.forEach(invisibleIndexes -> {
                final Part part = parts.get(invisibleIndexes);
                partToEnrich.setOriginalText(partToEnrich.getOriginalText().concat(" ").concat(part.getOriginalText()));
                final List values = (List) partToEnrich.getValue();
                values.addAll((List) part.getValue());
                part.setVisible(false);
            });
        });
    }

    private void joinBooleanParts(final List<Part> parts, final Knowledge knowledge) {
        final Map<String, Part> booleanParts = Maps.newHashMap();
        knowledge.getResultPromptParts().forEach((s, part) -> {
            if (BOOLEAN == part.getType()) {
                booleanParts.putIfAbsent(part.getCode(), part);
            }
        });
        //Ignore all parts first
        booleanParts.forEach((s, part) -> parts.stream().filter(p -> part.getLabel().equals(p.getLabel())).forEach(this::ignoreParts));
        //filter first part and update state to resolved
        booleanParts.forEach((s, part) -> {
            final Optional<Part> partToChange = parts.stream().filter(v -> part.getLabel().equals(v.getLabel())).findFirst();
            partToChange.ifPresent(p -> {
                p.setValue(true);
                p.setType(BOOLEAN);
                p.setState(UNRESOLVED);
            });
        });
    }

    private void joinDurationParts(final List<Part> parts) {
        final List<Integer> indexesOfTypeInt = newArrayList();
        for (int i = 0; i < parts.size(); i++) {
            final Part part = parts.get(i);
            if (INT == part.getType()) {
                indexesOfTypeInt.add(i);
            }
        }
        final List<Integer> indexesOfTypeDuration = newArrayList();
        for (int i = 0; i < parts.size(); i++) {
            if (DURATION == parts.get(i).getType()) {
                indexesOfTypeDuration.add(i);
            }
        }
        indexesOfTypeDuration.forEach(index -> {
            final List<PartValue> values = newArrayList();
            final Part typeDuration = parts.get(index);
            if (indexesOfTypeInt.contains(index - 1)) {
                addTypeDurationWithGap(parts, index, values, typeDuration);
            } else if (typeDuration.getOriginalText() != null && parse(typeDuration.getOriginalText()) != null) {
                addTypeDurationWithNoGap(values, typeDuration);

            } else {
                typeDuration.setType(TXT);
                typeDuration.setLabel(null);
            }

        });
    }

    /**
     * Adding value to duration part when there is no gap between parts
     * e.g 2y is 2 years and '2' is int
     */
    private void addTypeDurationWithNoGap(final List<PartValue> values, final Part typeDuration) {
        final Integer integerValue = parse(typeDuration.getValueAsString());
        typeDuration.setOriginalText(typeDuration.getValueAsString());
        final PartValue value = new PartValue();
        value.setType(INT);
        value.setLabel(typeDuration.getLabel());
        value.setValue(integerValue);
        values.add(value);
        typeDuration.setValue(values);
        typeDuration.setLabel("");
    }

    /**
     * Adding value to duration part when there is gap between parts
     * e.g 2 y is 2 years and 2 is int and '2 y' should grouped as single part,
     */
    private void addTypeDurationWithGap(final List<Part> parts, final Integer index, final List<PartValue> values, final Part typeDuration) {
        final Part typeInt = parts.get(index - 1);
        typeInt.setVisible(false);
        typeDuration.setOriginalText(typeInt.getValue() + " " + typeDuration.getValue());
        final PartValue value = new PartValue();
        value.setType(typeInt.getType());
        value.setLabel(typeDuration.getLabel());
        value.setValue(Integer.parseInt(typeInt.getValue().toString()));
        values.add(value);
        typeDuration.setValue(values);
        typeDuration.setLabel("");
    }

    private List<Part> buildParts(final Knowledge knowledge, final List<String> partValues, final List<Part> parts) {
        final int firstQualifiedPartInResultDefinitionIndex = getIndex(partValues, knowledge);

        for (int i = 0; i < parts.size(); i++) {
            final Part part = parts.get(i);
            final String partValue = partValues.get(i);
            if (i == firstQualifiedPartInResultDefinitionIndex) {
                final Part partFromKnowledge = getFirstQualifiedPartInResultDefinition(partValues, knowledge);
                copyValues(part, partFromKnowledge);
            } else if (knowledge.getResultDefinitionParts().get(partValue) != null) {
                ignoreParts(part);
            } else if (knowledge.getResultPromptParts().get(partValue) != null) {
                enrichPartForPrompt(part, knowledge.getResultPromptParts().get(partValue));
            }
        }
        return parts;
    }

    private List<Part> buildAmbiguousParts(final Knowledge knowledge, final List<String> partValues, final List<Part> parts) {
        for (int i = 0; i < parts.size(); i++) {
            final String partValue = partValues.get(i);
            if (knowledge.getResultDefinitionParts().get(partValue) != null) {
                copyValues(parts.get(i), knowledge.getResultDefinitionParts().get(partValue));
            } else if (knowledge.getResultPromptParts().get(partValue) != null) {
                enrichPartForPrompt(parts.get(i), knowledge.getResultPromptParts().get(partValue));
            }
        }
        return parts;
    }

    private void copyValues(final Part part, final Part partFromKnowledge) {
        if (!part.getValueAsString().equalsIgnoreCase(partFromKnowledge.getValueAsString())) {
            part.setValue(partFromKnowledge.getValue());
        }
        part.setType(partFromKnowledge.getType());
        part.setState(partFromKnowledge.getState());
        part.setResultChoices(partFromKnowledge.getResultChoices());
        part.setHidden(partFromKnowledge.getHidden());
    }

    private void enrichPartForPrompt(final Part part, final Part partFromKnowledge) {
        part.setType(partFromKnowledge.getType());
        part.setState(partFromKnowledge.getState());
        part.setOriginalText(partFromKnowledge.getOriginalText());
        if (!StringUtils.isEmpty(partFromKnowledge.getLabel())) {
            part.setLabel(partFromKnowledge.getLabel());
        }
        final Object partValue = part.getValue();
        if (INT == part.getType() && StringUtils.isNumeric(partValue.toString())) {
            part.setValue(Integer.parseInt(partValue.toString()));
        } else if (CURR == part.getType()) {
            part.setValue(partFromKnowledge.getValue());
        }
    }

    private void ignoreParts(final Part part) {
        part.setType(IGNORED);
        part.setState(RESOLVED);

    }

    private Part getFirstQualifiedPartInResultDefinition(final List<String> partValues, final Knowledge knowledge) {
        return knowledge.getResultDefinitionParts().get(partValues.get(getIndex(partValues, knowledge)));
    }

    private int getIndex(final List<String> partValues, final Knowledge knowledge) {
        int result = 0;
        for (int i = 0; i < partValues.size(); i++) {
            if (knowledge.getResultDefinitionParts().keySet().contains(partValues.get(i))) {
                result = i;
                break;
            }
        }
        return result;
    }

    /**
     * Part 2y means 2 years, if entered part starts with integer and ends with txt means parts can
     * be duration. this function will returns Integer value only in case it is integer.
     */
    private Integer parse(String value) {
        final Matcher m = alphaNumericRegex.matcher(value.toLowerCase());
        final List<String> allMatches = newArrayList();
        Integer result = null;
        while (m.find()) {
            allMatches.add(m.group());
        }
        if (allMatches.size() == 2) {
            final String txt = allMatches.get(0);
            if (StringUtils.isNumeric(txt)) {
                result = Integer.valueOf(txt);
            }
        }
        return result;
    }
}
