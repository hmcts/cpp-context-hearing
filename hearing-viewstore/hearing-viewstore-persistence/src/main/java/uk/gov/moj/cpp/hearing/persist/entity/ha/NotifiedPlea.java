package uk.gov.moj.cpp.hearing.persist.entity.ha;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import uk.gov.justice.json.schemas.core.NotifiedPleaValue;

import java.time.LocalDate;

@Embeddable
public class NotifiedPlea {

    @Column(name = "notified_plea_date")
    private LocalDate notifiedPleaDate;

    @Column(name = "notified_plea_value")
    private NotifiedPleaValue notifiedPleaValue;

    public LocalDate getNotifiedPleaDate() {
        return notifiedPleaDate;
    }

    public void setNotifiedPleaDate(LocalDate notifiedPleaDate) {
        this.notifiedPleaDate = notifiedPleaDate;
    }

    public NotifiedPleaValue getNotifiedPleaValue() {
        return notifiedPleaValue;
    }

    public void setNotifiedPleaValue(NotifiedPleaValue notifiedPleaValue2) {
        this.notifiedPleaValue = notifiedPleaValue2;
    }
}
