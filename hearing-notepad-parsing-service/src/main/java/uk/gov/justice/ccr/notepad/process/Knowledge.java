package uk.gov.justice.ccr.notepad.process;


import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

import uk.gov.justice.ccr.notepad.view.Part;
import uk.gov.justice.ccr.notepad.view.PromptChoice;

import java.util.List;
import java.util.Map;

public class Knowledge {

    private Map<String, Part> resultDefinitionParts = newHashMap();

    private Map<String, Part> resultPromptParts = newHashMap();

    private List<PromptChoice> promptChoices = newArrayList();

    private boolean isThisPerfectMatch = false;

    public void addResultDefinitionParts(final String key, final Part part) {
        resultDefinitionParts.putIfAbsent(key, part);
    }

    public void addResultPromptParts(final String key, final Part part) {
        resultPromptParts.putIfAbsent(key, part);
    }

    public Map<String, Part> getResultDefinitionParts() {
        return resultDefinitionParts;
    }

    public Map<String, Part> getResultPromptParts() {
        return resultPromptParts;
    }

    public boolean isThisPerfectMatch() {
        return isThisPerfectMatch;
    }

    public final void setThisPerfectMatch(final boolean value) {
        isThisPerfectMatch = value;
    }

    public List<PromptChoice> getPromptChoices() {
        return promptChoices;
    }

    public final void setPromptChoices(final List<PromptChoice> value) {
        promptChoices = value;
    }
}
