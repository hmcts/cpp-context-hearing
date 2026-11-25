package uk.gov.moj.cpp.hearing.domain.event.result;

import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.Plea;
import uk.gov.justice.core.courts.Verdict;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The plea, verdict and conviction date of one application offence at a point in time.
 */
@SuppressWarnings({"squid:S2384", "PMD.BeanMembersShouldSerialize"})
public class ApplicationOffenceValues implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Plea plea;
    private final Verdict verdict;
    private final LocalDate convictionDate;

    @JsonCreator
    public ApplicationOffenceValues(@JsonProperty("plea") final Plea plea,
                                    @JsonProperty("verdict") final Verdict verdict,
                                    @JsonProperty("convictionDate") final LocalDate convictionDate) {
        this.plea = plea;
        this.verdict = verdict;
        this.convictionDate = convictionDate;
    }

    /**
     * Copies the values so later changes to the offence's plea or verdict objects are not seen.
     */
    public static ApplicationOffenceValues of(final Offence offence) {
        return new ApplicationOffenceValues(
                offence.getPlea() == null ? null : Plea.plea().withValuesFrom(offence.getPlea()).build(),
                offence.getVerdict() == null ? null : Verdict.verdict().withValuesFrom(offence.getVerdict()).build(),
                offence.getConvictionDate());
    }

    public Plea getPlea() {
        return plea;
    }

    public Verdict getVerdict() {
        return verdict;
    }

    public LocalDate getConvictionDate() {
        return convictionDate;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ApplicationOffenceValues that = (ApplicationOffenceValues) o;
        return Objects.equals(plea, that.plea)
                && Objects.equals(verdict, that.verdict)
                && Objects.equals(convictionDate, that.convictionDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(plea, verdict, convictionDate);
    }
}
