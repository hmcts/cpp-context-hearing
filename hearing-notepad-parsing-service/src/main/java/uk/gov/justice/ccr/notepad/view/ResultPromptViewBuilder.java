package uk.gov.justice.ccr.notepad.view;


import static com.google.common.collect.Maps.newHashMap;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultType.DURATION;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultType.INT;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultType.TXT;

import uk.gov.justice.ccr.notepad.process.Knowledge;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;


public class ResultPromptViewBuilder {

    protected static final String ONEOF = "ONEOF";
    protected static final String ADDRESS = "ADDRESS";
    protected static final String NAMEADDRESS = "NAMEADDRESS";
    protected static final String TITLE = "TITLE";
    protected static final String ORGANISATION_NAME = "OrganisationName";
    protected static final String FIRST_NAME = "FirstName";
    protected static final String LAST_NAME = "LastName";
    protected static final String ADDRESS_LINE_1 = "AddressLine1";
    protected static final String EMAIL_ADDRESS_1 = "EmailAddress1";
    protected static final String EMAIL_ADDRESS_2 = "EmailAddress2";
    protected static final String POST_CODE = "PostCode";
    public static final String BOTH = "Both";
    public static final String PERSON = "Person";
    public static final String ORGANISATION = "Organisation";

    public ResultPromptView buildFromKnowledge(final Knowledge knowledge) {
        final ResultPromptView resultPromptView = new ResultPromptView();
        final List<Children> childrenList = new ArrayList<>();
        final Map<String, List<Integer>> durationPrompt = newHashMap();
        final Map<String, List<Integer>> childDurationPrompt = newHashMap();
        final Map<String, List<Integer>> nameAddressPrompt = newHashMap();
        final Map<String, List<Integer>> childNameAddressPrompt = new HashMap<>();
        final AtomicInteger index = new AtomicInteger();
        final List<PromptChoice> promptChoices = knowledge.getPromptChoices();
        promptChoices.forEach(promptChoice ->
                createResultPromptsMapForGrouping(childrenList, durationPrompt, childDurationPrompt, nameAddressPrompt, childNameAddressPrompt, index, promptChoice)
        );
        groupDurationPrompt(durationPrompt, promptChoices);
        if (!childrenList.isEmpty() || !childDurationPrompt.isEmpty() || !childNameAddressPrompt.isEmpty()) {
            groupOneOfPrompts(childrenList, promptChoices, childDurationPrompt, childNameAddressPrompt);
        }
        groupAddressAndNameAddressPrompt(nameAddressPrompt, promptChoices);
        resultPromptView.setPromptChoices(knowledge.getPromptChoices().stream().filter(PromptChoice::getVisible)
                .map(promptChoice -> {
                    if (promptChoice.getType().name().equals(promptChoice.getComponentType())) {
                        promptChoice.setComponentType(null);
                    }
                    return promptChoice;
                })
                .collect(Collectors.toList()));
        return resultPromptView;
    }

    private void createResultPromptsMapForGrouping(List<Children> childrenList, Map<String, List<Integer>> durationPrompt, Map<String, List<Integer>> childDurationPrompt, Map<String, List<Integer>> nameAddressPrompt, Map<String, List<Integer>> childNameAddressPrompt, AtomicInteger index, PromptChoice promptChoice) {
        final String label = promptChoice.getLabel();
        final String code = promptChoice.getCode();
        final String promptRef = promptChoice.getPromptRef();
        final int currentIndex = index.getAndIncrement();
        if (DURATION == promptChoice.getType() && !ONEOF.equals(promptChoice.getComponentType())) {
            durationPrompt.putIfAbsent(label, Lists.newArrayList());
            durationPrompt.get(label).add(currentIndex);
            promptChoice.setVisible(false);
        }
        if (ONEOF.equals(promptChoice.getComponentType())) {
            if (promptChoice.getType() == DURATION) {
                childDurationPrompt.putIfAbsent(label, Lists.newArrayList());
                childDurationPrompt.get(label).add(currentIndex);
                promptChoice.setVisible(false);
            } else if (promptChoice.getType() == ResultType.NAMEADDRESS) {
                childNameAddressPrompt.putIfAbsent(code, Lists.newArrayList());
                childNameAddressPrompt.get(code).add(currentIndex);
                promptChoice.setVisible(false);
            } else {
                childrenList.add(new Children()
                        .withLabel(promptChoice.getLabel())
                        .withCode(promptChoice.getCode())
                        .withPromptRef(promptRef)
                        .withType(promptChoice.getType())
                        .withFixedList(promptChoice.getFixedList())
                        .withChildrenList(promptChoice.getChildren()));

                promptChoice.setVisible(false);
            }
        }
        if (ADDRESS.equals(promptChoice.getComponentType()) || NAMEADDRESS.equals(promptChoice.getComponentType())) {
            nameAddressPrompt.putIfAbsent(code, Lists.newArrayList());
            nameAddressPrompt.get(code).add(currentIndex);
            promptChoice.setVisible(false);
        }
        setNameEmailAndAddressListToNullIfNotNameAddressType(promptChoice);
    }

    private void groupDurationPrompt(final Map<String, List<Integer>> durationPrompt, final List<PromptChoice> promptChoices) {
        durationPrompt.forEach((s, integers) -> {
            final PromptChoice promptChoice = promptChoices.stream().filter(v -> v.getLabel().equals(s)).findFirst().get();
            integers.forEach(integer -> {
                final PromptChoice pc = promptChoices.get(integer);
                promptChoice.addChildren(new Children(pc.getCode(),pc.getDurationElement(), pc.getPromptRef(), pc.getWelshDurationElement(), INT));
                promptChoice.setVisible(true);
            });

        });
    }

    private void groupAddressAndNameAddressPrompt(final Map<String, List<Integer>> addressPrompt, final List<PromptChoice> promptChoices) {
        addressPrompt.forEach((s, integers) -> {
            final PromptChoice promptChoice = promptChoices.stream()
                    .filter(v-> nonNull(v.getCode()))
                    .filter(v -> v.getCode().equals(s))
                    .findFirst().get();
            integers.forEach(integer -> setNameAddressFields(promptChoices, promptChoice, integer));
            setParentPromptProperties(promptChoice);
            sortChildrenBySequence(promptChoice.getChildren());
        });
    }

    private void setNameAddressFields(final List<PromptChoice> promptChoices, final PromptChoice promptChoiceParent, final Integer integer) {
        final PromptChoice pc = promptChoices.get(integer);
        if(!containsIgnoreCase(pc.getLabel(), TITLE)) {
            addChildrenAndSetVisible(promptChoiceParent, pc);

            if (NAMEADDRESS.equals(promptChoiceParent.getType().name())) {
                promptChoiceParent.setPromptRef(pc.getReferenceDataKey());
            }
            if (CollectionUtils.isNotEmpty(pc.getNameAddressList())) {
                promptChoiceParent.setNameAddressList(pc.getNameAddressList());
                pc.setNameAddressList(null);
            }
            if (pc.getNameEmail() != null) {
                promptChoiceParent.setNameEmail(pc.getNameEmail());
                pc.setNameEmail(null);
            }
        }
    }

    private void setParentPromptRef(final PromptChoice promptChoiceParent) {
        promptChoiceParent.getChildren()
                .stream()
                .filter(e->Arrays.asList(POST_CODE,EMAIL_ADDRESS_1).contains(e.getPartName()))
                .forEach(children -> {
                    final String promtRef = children.getPromptRef().replace(children.getPartName(),"");
                    promptChoiceParent.setPromptRef(promtRef);
                    promptChoiceParent.setComponentLabel(promtRef);

                });
    }

    private void addChildrenAndSetVisible(final PromptChoice promptChoiceParent, final PromptChoice pc) {
        final Children child = new Children()
                .withCode(pc.getCode())
                .withLabel(pc.getLabel())
                .withPromptRef(pc.getPromptRef())
                .withType(TXT)
                .withPartName(pc.getPartName())
                .withAddressType(pc.getAddressType())
                .withRequired(pc.getRequired())
                .withMinLength(pc.getMinLength())
                .withMaxLength(pc.getMaxLength());
        promptChoiceParent.setVisible(true);
        promptChoiceParent.setAddressType(pc.getAddressType());

        if (isNameAddressAndIsNameEmail(promptChoiceParent, pc)) {
            child.withRequired(Arrays.asList(ORGANISATION_NAME,EMAIL_ADDRESS_1).contains(pc.getPartName()));
            if (Arrays.asList(ORGANISATION_NAME,EMAIL_ADDRESS_1,EMAIL_ADDRESS_2).contains(pc.getPartName())) {
                promptChoiceParent.addChildren(child);
            }
            return;
        }

        if (isNameAddressAndNotNameEmail(promptChoiceParent, pc)) {

            if (Arrays.asList(ORGANISATION_NAME,FIRST_NAME,LAST_NAME,ADDRESS_LINE_1).contains(pc.getPartName()) && Arrays.asList(BOTH).contains(pc.getAddressType())) {
                child.withRequired(true);
            } else if (Arrays.asList(FIRST_NAME,LAST_NAME,ADDRESS_LINE_1).contains(pc.getPartName()) && Arrays.asList(PERSON).contains(pc.getAddressType())) {
                child.withRequired(true);
            } else {
                child.withRequired(Arrays.asList(ORGANISATION_NAME,ADDRESS_LINE_1).contains(pc.getPartName()) && Arrays.asList(ORGANISATION).contains(pc.getAddressType()));
            }
        }

        if (POST_CODE.equals(pc.getPartName())){
            child.withRequired(true);
        }


        promptChoiceParent.addChildren(child);
    }

    private boolean isNameAddressAndIsNameEmail(final PromptChoice promptChoice, final PromptChoice pc) {
        return NAMEADDRESS.equals(promptChoice.getType().name()) && pc.getNameEmail() != null && TRUE.equals(pc.getNameEmail());
    }

    private boolean isNameAddressAndNotNameEmail(final PromptChoice promptChoice, final PromptChoice pc) {
        return NAMEADDRESS.equals(promptChoice.getType().name())&& pc.getNameEmail() != null && FALSE.equals(pc.getNameEmail());
    }

    private void setParentPromptProperties(PromptChoice promptChoice) {
        if (promptChoice.getType() == ResultType.NAMEADDRESS) {
            promptChoice.setLabel(promptChoice.getComponentLabel());
            setParentPromptRef(promptChoice);
        } else if (ADDRESS.equals(promptChoice.getType().name())) {
            setParentPromptRef(promptChoice);
            promptChoice.setAddressType(null);
        }  else {
            promptChoice.setAddressType(null);
            promptChoice.setPromptRef(null);
        }
        promptChoice.setPartName(null);
        promptChoice.setMinLength(null);
        promptChoice.setMaxLength(null);
    }

    private void setNameEmailAndAddressListToNullIfNotNameAddressType(PromptChoice promptChoice) {
        if(!ADDRESS.equals(promptChoice.getType().name()) && !NAMEADDRESS.equals(promptChoice.getType().name())) {
            promptChoice.setNameEmail(null);
            promptChoice.setNameAddressList(null);
        }
    }

    private void sortChildrenBySequence(final List<Children> childrenList) {
        final List<Children> sortedChildrenList = childrenList.stream().sorted(Comparator.comparing(Children::getSequence)).collect(toList());
        childrenList.clear();
        childrenList.addAll(sortedChildrenList);
    }


    private void groupOneOfPrompts(final List<Children> childrenList, final List<PromptChoice> promptChoices, final Map<String, List<Integer>> childDurationPrompt, final Map<String, List<Integer>> childNameAddressPrompt) {
        final Optional<PromptChoice> promptChoiceOptional = promptChoices.stream().filter(v -> ONEOF.equals(v.getComponentType())).findFirst();
        promptChoiceOptional.ifPresent(promptChoice -> {
            childrenList.forEach(promptChoice::addChildren);
            groupDurationPromptWithInOneOff(promptChoices, childDurationPrompt, promptChoice);
            groupNameAddressPromptWithInOneOff(promptChoices, childNameAddressPrompt, promptChoice);
            promptChoice.setVisible(true);
        });
    }

    private void groupNameAddressPromptWithInOneOff(List<PromptChoice> promptChoices, Map<String, List<Integer>> childNameAddressPrompt, PromptChoice promptChoice) {
        if(childNameAddressPrompt !=null && !childNameAddressPrompt.isEmpty()) {
            childNameAddressPrompt.forEach((s, integers)-> {
                final Children nameAddressChildren = new Children(s,"",null,ResultType.NAMEADDRESS,promptChoice.getPartName());
                final PromptChoice nameAddressParent = new PromptChoice();
                nameAddressParent.setNameEmail(promptChoice.getNameEmail());
                nameAddressParent.setType(ResultType.NAMEADDRESS);
                promptChoice.addChildren(nameAddressChildren);
                integers.forEach(integer -> {
                    final PromptChoice pc = promptChoices.get(integer);
                    if(!containsIgnoreCase(pc.getLabel(), TITLE)) {
                        addChildrenAndSetVisible(nameAddressParent,pc);
                        setAttributesFromChildren(nameAddressChildren, pc);
                    }
                    nameAddressChildren.withChildrenList(nameAddressParent.getChildren());

                });

                sortChildrenBySequence(nameAddressChildren.getChildrenList());
                final List<Children> childrenList = nameAddressChildren.getChildrenList();
                childrenList.stream().limit(1).forEach(children -> nameAddressChildren.setPromptRef(Optional.ofNullable(children.getPartName())
                        .map(partName -> children.getPromptRef().replace(partName, ""))
                        .orElse(nameAddressChildren.getPromptRef())));
            });
        }
    }

    private void setAttributesFromChildren(final Children nameAddressChildren, PromptChoice pc) {
        if (isBlank(nameAddressChildren.getLabel())) {
            nameAddressChildren.setLabel(pc.getComponentLabel());
        }
        if (isBlank(nameAddressChildren.getListLabel())) {
            nameAddressChildren.setListLabel(pc.getListLabel());
        }
        if (isBlank(nameAddressChildren.getAddressType())) {
            nameAddressChildren.setAddressType(pc.getAddressType());
        }
        if (CollectionUtils.isNotEmpty(pc.getNameAddressList())) {
            nameAddressChildren.setNameAddressList(pc.getNameAddressList());
            pc.setNameAddressList(null);
        }
        if (pc.getNameEmail() != null) {
            nameAddressChildren.setNameEmail(pc.getNameEmail());
        }
        nameAddressChildren.setCode(null);
        nameAddressChildren.setPartName(null);
    }

    private void groupDurationPromptWithInOneOff(List<PromptChoice> promptChoices, Map<String, List<Integer>> childDurationPrompt, PromptChoice promptChoice) {
        if (childDurationPrompt != null && !childDurationPrompt.isEmpty()) {
            childDurationPrompt.forEach((s, integers) -> {
                final Children durationChildren = new Children(promptChoice.getCode(), s, promptChoice.getPromptRef(), null, DURATION);
                promptChoice.addChildren(durationChildren);
                integers.forEach(integer -> {
                    final PromptChoice pc = promptChoices.get(integer);
                    durationChildren.addChildrenList(new Children(pc.getCode(), pc.getDurationElement(), pc.getPromptRef(), promptChoice.getWelshDurationElement(), INT));
                });
            });
        }
    }
}
