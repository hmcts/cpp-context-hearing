package uk.gov.moj.cpp.hearing.persist.entity.ha;

import uk.gov.justice.core.courts.NotifiedPleaValue;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
public class NotifiedPlea {

    @Column(name = "notified_plea_date")
    private LocalDate notifiedPleaDate;

    @Column(name = "notified_plea_value")
    @Enumerated(EnumType.STRING)
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
