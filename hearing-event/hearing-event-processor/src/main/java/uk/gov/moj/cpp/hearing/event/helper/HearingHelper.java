package uk.gov.moj.cpp.hearing.event.helper;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtOrder;
import uk.gov.justice.core.courts.CourtOrderOffence;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.Offence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class HearingHelper {

    private HearingHelper() {
        throw new IllegalStateException("Utility class");
    }

    public static List<Offence> getOffencesFromHearing(Hearing hearing) {
        final List<Offence> offences = new ArrayList<>();

        offences.addAll(ofNullable(hearing.getProsecutionCases()).map(Collection::stream).orElseGet(Stream::empty)
                .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream())
                .flatMap(defendant -> defendant.getOffences().stream()).collect(toList()));

        offences.addAll(ofNullable(hearing.getCourtApplications()).map(Collection::stream).orElseGet(Stream::empty)
                .flatMap(ca -> ofNullable(ca.getCourtApplicationCases()).map(Collection::stream).orElseGet(Stream::empty))
                .flatMap(ac -> ofNullable(ac.getOffences()).map(Collection::stream).orElseGet(Stream::empty))
                .collect(toList()));

        offences.addAll(ofNullable(hearing.getCourtApplications()).map(Collection::stream).orElseGet(Stream::empty)
                .flatMap(ca -> ofNullable(ca.getCourtOrder()).map(CourtOrder::getCourtOrderOffences).map(Collection::stream).orElseGet(Stream::empty))
                .map(CourtOrderOffence::getOffence)
                .collect(toList()));

        return offences;
    }

    public static List<Offence> getOffencesFromApplication(final CourtApplication courtApplication) {
        final List<Offence> offences = new ArrayList<>();

        offences.addAll(ofNullable(courtApplication.getCourtApplicationCases()).map(Collection::stream).orElseGet(Stream::empty)
                .flatMap(ac -> ofNullable(ac.getOffences()).map(Collection::stream).orElseGet(Stream::empty))
                .collect(toList()));

        offences.addAll(ofNullable(courtApplication.getCourtOrder()).map(CourtOrder::getCourtOrderOffences).map(Collection::stream).orElseGet(Stream::empty)
                .map(CourtOrderOffence::getOffence)
                .collect(toList()));

        return offences;
    }
}
