package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtOrderOffence;
import uk.gov.justice.core.courts.Offence;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateAdded;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateRemoved;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

@SuppressWarnings("squid:S00112")
public class ConvictionDateDelegate implements Serializable {

    private static final long serialVersionUID = 1L;

    private final HearingAggregateMomento momento;

    public ConvictionDateDelegate(final HearingAggregateMomento momento) {
        this.momento = momento;
    }

    public void handleConvictionDateAdded(final ConvictionDateAdded convictionDateAdded) {
        final UUID convictionDateId = ofNullable(convictionDateAdded.getOffenceId()).orElse(convictionDateAdded.getCourtApplicationId());

        updateConvictionDate(convictionDateAdded.getOffenceId(), convictionDateAdded.getConvictionDate(), convictionDateAdded.getCourtApplicationId());
        this.momento.getConvictionDates().put(convictionDateId, convictionDateAdded.getConvictionDate());
    }

    public void handleConvictionDateRemoved(final ConvictionDateRemoved convictionDateRemoved) {
        final UUID convictionDateId = ofNullable(convictionDateRemoved.getOffenceId()).orElse(convictionDateRemoved.getCourtApplicationId());
        updateConvictionDate(convictionDateRemoved.getOffenceId(), null, convictionDateRemoved.getCourtApplicationId());
        this.momento.getConvictionDates().remove(convictionDateId);
    }

    private void updateConvictionDate(final UUID offenceId, final LocalDate convictionDate, UUID courtApplicationId) {

        if(courtApplicationId != null && offenceId == null){
            final CourtApplication courtApplication = this.momento.getHearing().getCourtApplications().stream()
                    .filter(ca -> ca.getId().equals(courtApplicationId))
                    .findFirst().orElse(null);
            if(courtApplication == null){
                throw new RuntimeException("Invalid Application id on conviction date message");
            }
            courtApplication.setConvictionDate(convictionDate);
        }else {
            Offence offence = ofNullable(this.momento.getHearing().getProsecutionCases()).orElse(emptyList()).stream()
                    .flatMap(ps -> ps.getDefendants().stream())
                    .flatMap(d -> d.getOffences().stream())
                    .filter(o -> o.getId().equals(offenceId))
                    .findFirst().orElse(null);

            if (offence == null) {
                offence = ofNullable(this.momento.getHearing().getCourtApplications()).orElse(emptyList()).stream()
                        .flatMap(ca -> ofNullable(ca.getCourtApplicationCases()).orElse(emptyList()).stream())
                        .flatMap(c -> ofNullable(c.getOffences()).map(Collection::stream).orElseGet(Stream::empty))
                        .filter(o -> o.getId().equals(offenceId))
                        .findFirst().orElse(null);
            }

            if (offence == null) {
                offence = ofNullable(this.momento.getHearing().getCourtApplications()).orElse(emptyList()).stream()
                        .map(CourtApplication::getCourtOrder)
                        .filter( Objects::nonNull)
                        .flatMap(o -> o.getCourtOrderOffences().stream())
                        .map(CourtOrderOffence::getOffence)
                        .filter(o -> o.getId().equals(offenceId))
                        .findFirst().orElse(null);
            }

            if (offence != null) {
                offence.setConvictionDate(convictionDate);
            } else {
                throw new RuntimeException("Invalid offence id on conviction date message");
            }
        }
    }
}
