package uk.gov.moj.cpp.hearing.command.verdict;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Jurors implements Serializable {

    private static final long serialVersionUID = 1L;
    private Integer numberOfJurors;
    private Integer numberOfSplitJurors;
    private Boolean unanimous;

    public Jurors() {
    }

    @JsonCreator
    protected Jurors(@JsonProperty("numberOfJurors") final Integer numberOfJurors,
                     @JsonProperty("numberOfSplitJurors") final Integer numberOfSplitJurors,
                     @JsonProperty("unanimous") final Boolean unanimous) {
        this.numberOfJurors = numberOfJurors;
        this.numberOfSplitJurors = numberOfSplitJurors;
        this.unanimous = unanimous;
    }

    public Integer getNumberOfJurors() {
        return numberOfJurors;
    }

    public Integer getNumberOfSplitJurors() {
        return numberOfSplitJurors;
    }

    public Boolean getUnanimous() {
        return unanimous;
    }

    public Jurors setNumberOfJurors(Integer numberOfJurors) {
        this.numberOfJurors = numberOfJurors;
        return this;
    }

    public Jurors setNumberOfSplitJurors(Integer numberOfSplitJurors) {
        this.numberOfSplitJurors = numberOfSplitJurors;
        return this;
    }

    public Jurors setUnanimous(Boolean unanimous) {
        this.unanimous = unanimous;
        return this;
    }

    public static Jurors jurors() {
        return new Jurors();
    }
}