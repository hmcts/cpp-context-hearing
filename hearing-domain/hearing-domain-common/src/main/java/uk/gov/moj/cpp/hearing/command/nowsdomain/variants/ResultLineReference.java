package uk.gov.moj.cpp.hearing.command.nowsdomain.variants;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

public class ResultLineReference implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID resultLineId;

    private ZonedDateTime lastSharedTime;

    public static ResultLineReference resultLineReference() {
        return new ResultLineReference();
    }

    public UUID getResultLineId() {
        return this.resultLineId;
    }

    public ResultLineReference setResultLineId(UUID resultLineId) {
        this.resultLineId = resultLineId;
        return this;
    }

    public ZonedDateTime getLastSharedTime() {
        return this.lastSharedTime;
    }

    public ResultLineReference setLastSharedTime(ZonedDateTime lastSharedTime) {
        this.lastSharedTime = lastSharedTime;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ResultLineReference that = (ResultLineReference) o;
        return Objects.equals(resultLineId, that.resultLineId) &&
                Objects.equals(lastSharedTime, that.lastSharedTime);
    }

    @Override
    public int hashCode() {

        return Objects.hash(resultLineId, lastSharedTime);
    }
}
