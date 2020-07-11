package uk.gov.moj.cpp.hearing.command.result;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SharedResultsCommandPrompt {
    private UUID id;
    private String label;
    private String fixedListCode;
    private String value;
    private String welshValue;
    private String welshLabel;
    private String promptRef;

    @JsonCreator
    public SharedResultsCommandPrompt(
            @JsonProperty("id") final UUID id,
            @JsonProperty("label") final String label,
            @JsonProperty("fixedListCode") final String fixedListCode,
            @JsonProperty("value") final String value,
            @JsonProperty("welshValue") final String welshValue,
            @JsonProperty("welshLabel") final String welshLabel,
            @JsonProperty("promptRef") final String promptRef
    ) {
        this.id = id;
        this.label = label;
        this.fixedListCode = fixedListCode;
        this.value = value;
        this.welshValue = welshValue;
        this.welshLabel = welshLabel;
        this.promptRef = promptRef;
    }


    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getFixedListCode() {
        return fixedListCode;
    }

    public void setFixedListCode(String fixedListCode) {
        this.fixedListCode = fixedListCode;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getWelshValue() {
        return welshValue;
    }

    public void setWelshValue(String welshValue) {
        this.welshValue = welshValue;
    }

    public String getWelshLabel() {
        return welshLabel;
    }

    public void setWelshLabel(final String welshLabel) {
        this.welshLabel = welshLabel;
    }

    public String getPromptRef() {
        return promptRef;
    }

    public void setPromptRef(String promptRef) {
        this.promptRef = promptRef;
    }

}

