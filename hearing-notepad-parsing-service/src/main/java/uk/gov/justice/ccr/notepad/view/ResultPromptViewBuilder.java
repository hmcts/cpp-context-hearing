package uk.gov.justice.ccr.notepad.view;


import static com.google.common.collect.Maps.newHashMap;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultType.DURATION;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultType.INT;

import uk.gov.justice.ccr.notepad.process.Knowledge;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

public class ResultPromptViewBuilder {

    protected static final String ONEOF = "ONEOF";

    public ResultPromptView buildFromKnowledge(final Knowledge knowledge) {
        final ResultPromptView resultPromptView = new ResultPromptView();
        final List<Children> childrenList = new ArrayList<>();
        final Map<String, List<Integer>> durationPrompt = newHashMap();
        final Map<String, List<Integer>> childDurationPrompt = newHashMap();
        final AtomicInteger index = new AtomicInteger();
        final List<PromptChoice> promptChoices = knowledge.getPromptChoices();
        promptChoices.forEach(promptChoice -> {
            final String label = promptChoice.getLabel();
            final int currentIndex = index.getAndIncrement();
            if (DURATION == promptChoice.getType() && !ONEOF.equals(promptChoice.getComponentType())) {
                durationPrompt.putIfAbsent(label, Lists.newArrayList());
                durationPrompt.get(label).add(currentIndex);
                promptChoice.setVisible(false);
            }
            if (ONEOF.equals(promptChoice.getComponentType())) {
                if (promptChoice.getType() != DURATION) {
                    childrenList.add(new Children(promptChoice.getLabel(), promptChoice.getType(), promptChoice.getFixedList(), promptChoice.getChildren(), promptChoice.getCode()));
                    promptChoice.setVisible(false);
                } else {
                    childDurationPrompt.putIfAbsent(label, Lists.newArrayList());
                    childDurationPrompt.get(label).add(currentIndex);
                    promptChoice.setVisible(false);
                }
            }
        });

        groupDurationPrompt(durationPrompt, promptChoices);
        if (!childrenList.isEmpty() || !childDurationPrompt.isEmpty()) {
            groupOneOfPrompts(childrenList, promptChoices, childDurationPrompt);
        }
        resultPromptView.setPromptChoices(knowledge.getPromptChoices().stream().filter(PromptChoice::getVisible).collect(Collectors.toList()));
        return resultPromptView;
    }

    private void groupDurationPrompt(final Map<String, List<Integer>> durationPrompt, final List<PromptChoice> promptChoices) {
        durationPrompt.forEach((s, integers) -> {
            final PromptChoice promptChoice = promptChoices.stream().filter(v -> v.getLabel().equals(s)).findFirst().get();
            integers.forEach(integer -> {
                final PromptChoice pc = promptChoices.get(integer);
                promptChoice.addChildren(new Children(pc.getDurationElement(), INT));
                promptChoice.setVisible(true);
            });
        });
    }

    private void groupOneOfPrompts(final List<Children> childrenList, final List<PromptChoice> promptChoices, final Map<String, List<Integer>> childDurationPrompt) {
        final Optional<PromptChoice> promptChoiceOptional = promptChoices.stream().filter(v -> ONEOF.equals(v.getComponentType())).findFirst();
        promptChoiceOptional.ifPresent(promptChoice -> {
            childrenList.forEach(promptChoice::addChildren);
            if (childDurationPrompt != null && !childDurationPrompt.isEmpty()) {
                childDurationPrompt.forEach((s, integers) -> {
                    final Children durationChildren = new Children(s, DURATION);
                    promptChoice.addChildren(durationChildren);
                    integers.forEach(integer -> {
                        final PromptChoice pc = promptChoices.get(integer);
                        durationChildren.addChildrenList(new Children(pc.getDurationElement(), INT));
                    });
                });
            }
            promptChoice.setVisible(true);
        });
    }
}
