package uk.gov.moj.cpp.hearing.domain.event;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

public class ResultLineSharedDateInfo implements Serializable {
    private static final long serialVersionUID = 8664585796322018307L;
    private UUID resultLineId;
    private LocalDate sharedDate;

    public ResultLineSharedDateInfo() {
    }

    public ResultLineSharedDateInfo(final UUID resultLineId, final LocalDate sharedDate) {
        this.resultLineId = resultLineId;
        this.sharedDate = sharedDate;
    }

    public UUID getResultLineId() {
        return resultLineId;
    }

    public LocalDate getSharedDate() {
        return sharedDate;
    }

}
