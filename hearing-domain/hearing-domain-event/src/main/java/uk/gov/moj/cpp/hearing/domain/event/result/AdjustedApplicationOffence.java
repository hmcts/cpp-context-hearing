package uk.gov.moj.cpp.hearing.domain.event.result;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * One application offence whose shared plea, verdict or conviction date was changed by the system.
 */
@SuppressWarnings({"squid:S2384", "PMD.BeanMembersShouldSerialize"})
public class AdjustedApplicationOffence implements Serializable {

    public static final String PLEA = "plea";
    public static final String VERDICT = "verdict";
    public static final String CONVICTION_DATE = "convictionDate";

    private static final long serialVersionUID = 1L;

    private final UUID offenceId;
    private final List<String> fieldsChanged;
    private final ApplicationOffenceValues previous;
    private final ApplicationOffenceValues adjusted;

    @JsonCreator
    public AdjustedApplicationOffence(@JsonProperty("offenceId") final UUID offenceId,
                                      @JsonProperty("fieldsChanged") final List<String> fieldsChanged,
                                      @JsonProperty("previous") final ApplicationOffenceValues previous,
                                      @JsonProperty("adjusted") final ApplicationOffenceValues adjusted) {
        this.offenceId = offenceId;
        this.fieldsChanged = fieldsChanged;
        this.previous = previous;
        this.adjusted = adjusted;
    }

    public UUID getOffenceId() {
        return offenceId;
    }

    public List<String> getFieldsChanged() {
        return fieldsChanged;
    }

    public ApplicationOffenceValues getPrevious() {
        return previous;
    }

    public ApplicationOffenceValues getAdjusted() {
        return adjusted;
    }
}
