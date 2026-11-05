package uk.gov.moj.cpp.hearing.persist.entity.ha;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

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
