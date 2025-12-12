package uk.gov.moj.cpp.hearing.persist.entity.ha;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "ha_hearing_youth_court_defendants")
public class HearingYouthCourtDefendants {

    @Id
    private HearingYouthCourDefendantsKey id;

    @ManyToOne
    @JoinColumn(name = "hearing_id", insertable = false, updatable = false)
    private Hearing hearing;

    public HearingYouthCourtDefendants() {
    }

    public HearingYouthCourtDefendants(HearingYouthCourDefendantsKey id) {
        this.id = id;
    }

    public Hearing getHearing() {
        return hearing;
    }

    public void setHearing(Hearing hearing) {
        this.hearing = hearing;
    }

    public HearingYouthCourDefendantsKey getId() {
        return id;
    }

    public void setId(HearingYouthCourDefendantsKey id) {
        this.id = id;
    }


}
