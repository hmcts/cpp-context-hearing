package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import uk.gov.justice.core.courts.CourtCentre;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.core.courts.HearingLanguage;
import uk.gov.justice.core.courts.HearingType;
import uk.gov.justice.core.courts.JudicialRole;
import uk.gov.justice.core.courts.JurisdictionType;
import uk.gov.moj.cpp.hearing.domain.event.HearingDetailChanged;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventIgnored;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@SuppressWarnings("squid:S00107")
public class HearingDelegate implements Serializable {

    private static final long serialVersionUID = 1L;

    private final HearingAggregateMomento momento;

    public HearingDelegate(HearingAggregateMomento momento) {
        this.momento = momento;
    }

    public void handleHearingInitiated(HearingInitiated hearingInitiated) {
        this.momento.setHearing(hearingInitiated.getHearing());
    }

    public void handleHearingDetailChanged(HearingDetailChanged hearingDetailChanged) {

        if (hearingDetailChanged.getJudiciary() != null && !hearingDetailChanged.getJudiciary().isEmpty()) {
            this.momento.getHearing().setJudiciary(new ArrayList<>(hearingDetailChanged.getJudiciary()));
        }
        if (hearingDetailChanged.getHearingDays() != null && !hearingDetailChanged.getHearingDays().isEmpty()) {
            this.momento.getHearing().setHearingDays(new ArrayList<>(hearingDetailChanged.getHearingDays()));
        }
        this.momento.getHearing().setCourtCentre(hearingDetailChanged.getCourtCentre());
        this.momento.getHearing().setHearingLanguage(hearingDetailChanged.getHearingLanguage());
        this.momento.getHearing().setJurisdictionType(hearingDetailChanged.getJurisdictionType());
        this.momento.getHearing().setReportingRestrictionReason(hearingDetailChanged.getReportingRestrictionReason());
        this.momento.getHearing().setType(hearingDetailChanged.getType());

    }

    public Stream<Object> initiate(final Hearing hearing) {

        return Stream.of(new HearingInitiated(hearing));
    }

    public Stream<Object> updateHearingDetails(final UUID id,
                                               final HearingType type,
                                               final CourtCentre courtCentre,
                                               final JurisdictionType jurisdictionType,
                                               final String reportingRestrictionReason,
                                               final HearingLanguage hearingLanguage,
                                               final List<HearingDay> hearingDays,
                                               final List<JudicialRole> judiciary) {

        if (this.momento.getHearing() == null) {
            return Stream.of(generateHearingIgnoredMessage("Rejecting 'hearing.change-hearing-detail' event as hearing not found", id));
        }

        return Stream.of(new HearingDetailChanged(id, type, courtCentre, jurisdictionType, reportingRestrictionReason, hearingLanguage, hearingDays, judiciary));
    }

    private HearingEventIgnored generateHearingIgnoredMessage(final String reason, final UUID hearingId) {
        return new HearingEventIgnored(hearingId, reason);
    }
}
