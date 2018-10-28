package uk.gov.moj.cpp.hearing.command.result;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class SharedResultsCommandPrompt {
    private UUID id;
    private String label;
    private String fixedListCode;
    private String value;
    private String welshValue;

    @JsonCreator
    public SharedResultsCommandPrompt(
            @JsonProperty("id") final UUID id,
            @JsonProperty("label") final String label,
            @JsonProperty("fixedListCode") final String fixedListCode,
            @JsonProperty("value") final String value,
            @JsonProperty("welshValue") final String welshValue
    ) {
        this.id = id;
        this.label = label;
        this.fixedListCode = fixedListCode;
        this.value = value;
        this.welshValue = welshValue;
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
}

