package uk.gov.justice.ccr.notepad.view;


import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import uk.gov.justice.ccr.notepad.process.Knowledge;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.google.common.collect.Maps.newHashMap;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultType.DURATION;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultType.INT;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultType.TXT;

public class ResultPromptViewBuilder {

    protected static final String ONEOF = "ONEOF";
    protected static final String ADDRESS = "ADDRESS";
    protected static final String NAMEADDRESS = "NAMEADDRESS";
    protected static final String TITLE = "TITLE";

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
        resultPromptView.setPromptChoices(knowledge.getPromptChoices().stream().filter(PromptChoice::getVisible).collect(Collectors.toList()));
        return resultPromptView;
    }

    private void createResultPromptsMapForGrouping(List<Children> childrenList, Map<String, List<Integer>> durationPrompt, Map<String, List<Integer>> childDurationPrompt, Map<String, List<Integer>> nameAddressPrompt, Map<String, List<Integer>>  childNameAddressPrompt, AtomicInteger index, PromptChoice promptChoice) {
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
            } else if(promptChoice.getType() == ResultType.NAMEADDRESS){
                childNameAddressPrompt.putIfAbsent(code, Lists.newArrayList());
                childNameAddressPrompt.get(code).add(currentIndex);
                promptChoice.setVisible(false);
            } else {
                childrenList.add(new Children(promptChoice.getLabel(), promptChoice.getCode(),promptRef, promptChoice.getType(), promptChoice.getFixedList(), promptChoice.getChildren()));
                promptChoice.setVisible(false);
            }
        }
        if(ADDRESS.equals(promptChoice.getComponentType()) || NAMEADDRESS.equals(promptChoice.getComponentType())){
            nameAddressPrompt.putIfAbsent(code, Lists.newArrayList());
            nameAddressPrompt.get(code).add(currentIndex);
            promptChoice.setVisible(false);
        }
    }

    private void groupDurationPrompt(final Map<String, List<Integer>> durationPrompt, final List<PromptChoice> promptChoices) {
        durationPrompt.forEach((s, integers) -> {
            final PromptChoice promptChoice = promptChoices.stream().filter(v -> v.getLabel().equals(s)).findFirst().get();
            integers.forEach(integer -> {
                final PromptChoice pc = promptChoices.get(integer);
                promptChoice.addChildren(new Children(pc.getCode(),pc.getDurationElement(), pc.getPromptRef(), INT, pc.getWelshDurationElement()));
                promptChoice.setVisible(true);
            });
        });
    }

    private void groupAddressAndNameAddressPrompt(final Map<String, List<Integer>> addressPrompt, final List<PromptChoice> promptChoices) {
        addressPrompt.forEach((s,integers) -> {
            final PromptChoice promptChoice = promptChoices.stream()
                    .filter(v -> v.getCode().equals(s))
                    .findFirst().get();
            integers.forEach(integer -> setNameAdressFields(promptChoices, promptChoice, integer));
            sortChildrenBySequence(promptChoice.getChildren());
        });

    }

    private void setNameAdressFields(final List<PromptChoice> promptChoices, final PromptChoice promptChoice, final Integer integer) {
        final PromptChoice pc = promptChoices.get(integer);
        if(!containsIgnoreCase(pc.getLabel(), TITLE)) {
            promptChoice.addChildren(new Children(pc.getLabel(), pc.getPromptRef(), TXT, pc.getPromptOrder()));
            promptChoice.setVisible(true);
            if(CollectionUtils.isNotEmpty(pc.getNameAddressList())) {
                promptChoice.setNameAddressList(pc.getNameAddressList());
                pc.setNameAddressList(null);
            }
            if(pc.getNameEmail() !=null) {
                promptChoice.setNameEmail(pc.getNameEmail());
                pc.setNameEmail(null);
            }
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

              final Children nameAddressChildren = new Children(s , "", promptChoice.getPromptRef(), ResultType.NAMEADDRESS);
              promptChoice.addChildren(nameAddressChildren);
              integers.forEach(integer -> {
                  final PromptChoice pc = promptChoices.get(integer);
                  if(!containsIgnoreCase(pc.getLabel(), TITLE)) {
                      nameAddressChildren.addChildrenList(new Children(pc.getLabel(), pc.getPromptRef(), TXT));
                      setAttributesFromChildren(nameAddressChildren, pc);
                  }

              });

              sortChildrenBySequence(nameAddressChildren.getChildrenList());
          });
        }
    }

    private void setAttributesFromChildren(final Children nameAddressChildren, PromptChoice pc) {
        if(isBlank(nameAddressChildren.getLabel())) {
            nameAddressChildren.setLabel(pc.getComponentLabel());
        }
        if(isBlank(nameAddressChildren.getListLabel())) {
            nameAddressChildren.setListLabel(pc.getListLabel());
        }
        if(isBlank(nameAddressChildren.getAddressType())) {
            nameAddressChildren.setAddressType(pc.getAddressType());
        }
        if(CollectionUtils.isNotEmpty(pc.getNameAddressList())) {
            nameAddressChildren.setNameAddressList(pc.getNameAddressList());
            pc.setNameAddressList(null);
        }
        if(pc.getNameEmail() != null) {
            nameAddressChildren.setNameEmail(pc.getNameEmail());
        }
    }

    private void groupDurationPromptWithInOneOff(List<PromptChoice> promptChoices, Map<String, List<Integer>> childDurationPrompt, PromptChoice promptChoice) {
        if (childDurationPrompt != null && !childDurationPrompt.isEmpty()) {
            childDurationPrompt.forEach((s, integers) -> {
                final Children durationChildren = new Children(promptChoice.getCode(), s, promptChoice.getPromptRef(), DURATION);
                promptChoice.addChildren(durationChildren);
                integers.forEach(integer -> {
                    final PromptChoice pc = promptChoices.get(integer);
                    durationChildren.addChildrenList(new Children(pc.getCode(), pc.getDurationElement(), pc.getPromptRef(), INT, promptChoice.getWelshDurationElement()));
                });
            });
        }
    }
}
