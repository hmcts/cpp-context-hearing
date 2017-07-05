package uk.gov.justice.ccr.notepad.view;


import static com.google.common.collect.Maps.newHashMap;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultType.DURATION;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultType.INT;

import uk.gov.justice.ccr.notepad.process.Knowledge;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

public class ResultPromptViewBuilder {

    public ResultPromptView buildFromKnowledge(final Knowledge knowledge) {
        ResultPromptView resultPromptView = new ResultPromptView();
        Map<String, List<Integer>> durationPrompt = newHashMap();
        AtomicInteger index = new AtomicInteger();
        List<PromptChoice> promptChoices = knowledge.getPromptChoices();
        promptChoices.forEach(promptChoice -> {
            String label = promptChoice.getLabel();
            int currentIndex = index.getAndIncrement();
            if (DURATION == promptChoice.getType()) {
                durationPrompt.putIfAbsent(label, Lists.newArrayList());
                durationPrompt.get(label).add(currentIndex);
                promptChoice.setVisible(false);
            }
        });
        groupDurationPrompt(durationPrompt, promptChoices);
        resultPromptView.setPromptChoices(knowledge.getPromptChoices().stream().filter(PromptChoice::getVisible).collect(Collectors.toList()));
        return resultPromptView;
    }

    private void groupDurationPrompt(final Map<String, List<Integer>> durationPrompt, final List<PromptChoice> promptChoices) {
        durationPrompt.forEach((s, integers) -> {
            PromptChoice promptChoice = promptChoices.stream().filter(v -> v.getLabel().equals(s)).findFirst().get();
            integers.forEach(integer -> {
                PromptChoice pc = promptChoices.get(integer);
                promptChoice.addChildren(new Children(pc.getDurationElement(), INT));
                promptChoice.setVisible(true);
            });
        });
    }
}
