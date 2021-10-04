package uk.gov.moj.cpp.hearing.domain.aggregate.util;

import java.time.LocalDate;
import uk.gov.justice.core.courts.PleaModel;

public class PleaTestData {

    PleaModel beforeValue;
    PleaModel payload;
    LocalDate convictionDate;
    boolean convictionDataAdded;
    boolean convictionDataRemoved;

    public PleaTestData(final PleaModel beforeValue, final PleaModel payload, final LocalDate convictionDate, final boolean convictionDataAdded, final boolean convictionDataRemoved) {
        this.beforeValue = beforeValue;
        this.payload = payload;
        this.convictionDate = convictionDate;
        this.convictionDataAdded = convictionDataAdded;
        this.convictionDataRemoved = convictionDataRemoved;
    }

    public PleaModel getBeforeValue() {
        return beforeValue;
    }

    public PleaModel getPayload() {
        return payload;
    }

    public LocalDate getConvictionDate() {
        return convictionDate;
    }

    public boolean isConvictionDataAdded() {
        return convictionDataAdded;
    }

    public boolean isConvictionDataRemoved() {
        return convictionDataRemoved;
    }
}
