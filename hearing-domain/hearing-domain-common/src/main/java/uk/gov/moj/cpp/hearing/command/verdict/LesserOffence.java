package uk.gov.moj.cpp.hearing.command.verdict;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class LesserOffence implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID offenceDefinitionId;
    private String offenceCode;
    private String title;
    private String legislation;

    public LesserOffence() {
    }

    @JsonCreator
    protected LesserOffence(@JsonProperty("offenceDefinitionId") final UUID offenceDefinitionId,
                            @JsonProperty("offenceCode") final String offenceCode,
                            @JsonProperty("title") final String title,
                            @JsonProperty("legislation") final String legislation) {
        super();
        this.offenceDefinitionId = offenceDefinitionId;
        this.offenceCode = offenceCode;
        this.title = title;
        this.legislation = legislation;
    }

    public UUID getOffenceDefinitionId() {
        return offenceDefinitionId;
    }

    public String getOffenceCode() {
        return offenceCode;
    }

    public String getTitle() {
        return title;
    }

    public String getLegislation() {
        return legislation;
    }

    public LesserOffence setOffenceDefinitionId(UUID offenceDefinitionId) {
        this.offenceDefinitionId = offenceDefinitionId;
        return this;
    }

    public LesserOffence setOffenceCode(String offenceCode) {
        this.offenceCode = offenceCode;
        return this;
    }

    public LesserOffence setTitle(String title) {
        this.title = title;
        return this;
    }

    public LesserOffence setLegislation(String legislation) {
        this.legislation = legislation;
        return this;
    }

    public static LesserOffence lesserOffence() {
        return new LesserOffence();
    }
}