package uk.gov.justice.ccr.notepad.view;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "resultLineId",
        "originalText",
        "resultCode",
        "resultLevel",
        "parts"

})
public class ResultDefinitionView {

    private String originalText;

    private String resultLineId = UUID.randomUUID().toString();

    private String resultCode;

    private String resultLevel;

    private List<Part> parts = new ArrayList<>();

    public String getOriginalText() {
        return originalText;
    }

    public void setOriginalText(String originalText) {
        this.originalText = originalText;
    }

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultLevel() {
        return resultLevel;
    }

    public void setResultLevel(String resultLevel) {
        this.resultLevel = resultLevel;
    }

    public List<Part> getParts() {
        return parts;
    }

    public void setParts(List<Part> parts) {
        this.parts = parts;
    }

    public String getResultLineId() {
        return resultLineId;
    }
}
