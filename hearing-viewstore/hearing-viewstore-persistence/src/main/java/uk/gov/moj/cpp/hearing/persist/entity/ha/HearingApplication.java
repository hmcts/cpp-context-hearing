package uk.gov.moj.cpp.hearing.persist.entity.ha;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "ha_hearing_application")
public class HearingApplication {

    @Id
    private HearingApplicationKey id;

    @ManyToOne
    @JoinColumn(name = "hearing_id", insertable = false, updatable = false)
    private Hearing hearing;

    public Hearing getHearing() {
        return hearing;
    }

    public void setHearing(Hearing hearing) {
        this.hearing = hearing;
    }

    public HearingApplicationKey getId() {
        return id;
    }

    public void setId(HearingApplicationKey id) {
        this.id = id;
    }
}
