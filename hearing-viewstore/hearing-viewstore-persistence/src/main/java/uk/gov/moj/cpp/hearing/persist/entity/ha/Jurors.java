package uk.gov.moj.cpp.hearing.persist.entity.ha;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class Jurors {

    @Column(name = "number_of_jurors")
    private Integer numberOfJurors;

    @Column(name = "number_of_split_jurors")
    private Integer numberOfSplitJurors;

    @Column(name = "unanimous")
    private Boolean unanimous;

    public Integer getNumberOfJurors() {
        return numberOfJurors;
    }

    public void setNumberOfJurors(Integer numberOfJurors) {
        this.numberOfJurors = numberOfJurors;
    }

    public Integer getNumberOfSplitJurors() {
        return numberOfSplitJurors;
    }

    public void setNumberOfSplitJurors(Integer numberOfSplitJurors) {
        this.numberOfSplitJurors = numberOfSplitJurors;
    }

    public Boolean getUnanimous() {
        return unanimous;
    }

    public void setUnanimous(Boolean unanimous) {
        this.unanimous = unanimous;
    }


}
