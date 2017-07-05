package uk.gov.justice.ccr.notepad.view;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "resultCode",
        "promptChoices"
})
public class ResultPromptView {

    private List<PromptChoice> promptChoices = new ArrayList<>();

    public List<PromptChoice> getPromptChoices() {
        return null != promptChoices ? new ArrayList<>(promptChoices) : null;
    }

    public void setPromptChoices(List<PromptChoice> promptChoices) {
        this.promptChoices = new ArrayList<>(promptChoices);
    }

}
