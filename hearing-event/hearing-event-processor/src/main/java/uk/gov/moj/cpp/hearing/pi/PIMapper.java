package uk.gov.moj.cpp.hearing.pi;

import static java.util.Objects.nonNull;

import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.hearing.courts.referencedata.OrganisationalUnit;
import uk.gov.moj.cpp.hearing.common.ReferenceDataLoader;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventLogged;
import uk.gov.moj.cpp.hearing.json.schema.event.CourtRoom;
import uk.gov.moj.cpp.hearing.json.schema.event.Defendant;
import uk.gov.moj.cpp.hearing.json.schema.event.Hearing;
import uk.gov.moj.cpp.hearing.json.schema.event.HearingEvent;
import uk.gov.moj.cpp.hearing.json.schema.event.LiveStatusPublished;
import uk.gov.moj.cpp.hearing.json.schema.event.Session;
import uk.gov.moj.cpp.hearing.json.schema.event.Sitting;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

public class PIMapper {
    private static final String LIST_TYPE = "CROWN_LCSU";

    @Inject
    ReferenceDataLoader referenceDataLoader;

    public LiveStatusPublished transformFrom(final HearingEventLogged hearingEventLogged,
                                             List<ProsecutionCase> prosecutionCase) {
        return LiveStatusPublished.liveStatusPublished()
                .withVenueId(getOuCode(hearingEventLogged.getCourtCentre().getId()))
                .withListType(LIST_TYPE) // meta data
                .withCourtId(hearingEventLogged.getCourtCentre().getId().toString())
                .withCourtCentreName(hearingEventLogged.getCourtCentre().getName())
                .withCourtRooms(buildCourtRooms(hearingEventLogged, prosecutionCase))
                .build();
    }

    private List<CourtRoom> buildCourtRooms(final HearingEventLogged hearingEventLogged, List<ProsecutionCase> prosecutionCase) {
        final List<CourtRoom> courtRooms = new ArrayList<>();
        final CourtRoom courtRoom = CourtRoom.courtRoom().withCourtRoomName(hearingEventLogged.getCourtCentre().getRoomName())
                .withRoomId(hearingEventLogged.getCourtCentre().getRoomId().toString())
                .withSessions(buildSessions(hearingEventLogged, prosecutionCase))
                .build();
        courtRooms.add(courtRoom);
        return courtRooms;
    }

    private List<Session> buildSessions(final HearingEventLogged hearingEventLogged, List<ProsecutionCase> prosecutionCase) {
        final List<Session> sessions = new ArrayList<>();
        final Session session = Session.session().withSittings(buildSittings(hearingEventLogged, prosecutionCase)).build();
        sessions.add(session);
        return sessions;
    }

    private List<Sitting> buildSittings(final HearingEventLogged hearingEventLogged, List<ProsecutionCase> prosecutionCase) {
        final List<Sitting> sittings = new ArrayList<>();
        final Sitting sitting = Sitting.sitting()
                .withHearing(buildHearings(hearingEventLogged, prosecutionCase))
                .build();
        sittings.add(sitting);
        return sittings;
    }

    private List<Hearing> buildHearings(final HearingEventLogged hearingEventLogged, List<ProsecutionCase> prosecutionCase) {
        final List<Hearing> hearings = new ArrayList<>();
        final Hearing hearing = Hearing.hearing().withHearingType(hearingEventLogged.getHearingType().getDescription())
                .withCaseNumber(buildCaseURNs(hearingEventLogged))
                .withStartTime(hearingEventLogged.getEventTime().toString())
                .withHearingEvents(buildHearingEvents(hearingEventLogged))
                .withDefendants(buildDefandants(prosecutionCase.get(0).getDefendants()))
                .build();
        hearings.add(hearing);
        return hearings;
    }

    private List<Defendant> buildDefandants(final List<uk.gov.justice.core.courts.Defendant> defendants) {

        final List<Defendant> defendantList = new ArrayList<>();

        defendants.forEach(defendant -> {
            final Defendant defendant1 = Defendant.defendant()
                    .withArrestSummonsNumber(defendant.getPersonDefendant().getArrestSummonsNumber())
                    .withAddress(defendant.getPersonDefendant().getPersonDetails().getAddress())
                    //.withAge(defendant.getPersonDefendant().getPersonDetails()) //TBD - age
                    .withBailStatus(nonNull(defendant.getPersonDefendant().getBailStatus()) ? defendant.getPersonDefendant().getBailStatus().getDescription() : StringUtils.EMPTY)
                    .withDateOfBirth(nonNull(defendant.getPersonDefendant().getPersonDetails().getDateOfBirth()) ? defendant.getPersonDefendant().getPersonDetails().getDateOfBirth().toString() : StringUtils.EMPTY)
                    .withFirstName(defendant.getPersonDefendant().getPersonDetails().getFirstName())
                    .withGender(defendant.getPersonDefendant().getPersonDetails().getGender().name())
                    .withLastName(defendant.getPersonDefendant().getPersonDetails().getLastName())
                    //.withNationality(defendant.getPersonDefendant().getPersonDetails().getNationalityId().) //TBD
                    //.withOffences() //TBD
                    .withOrganisationName(nonNull(defendant.getPersonDefendant().getEmployerOrganisation()) ? defendant.getPersonDefendant().getEmployerOrganisation().getName() : StringUtils.EMPTY)
                    .build();

            defendantList.add(defendant1);
        });

        return defendantList;
    }

    private List<HearingEvent> buildHearingEvents(final HearingEventLogged hearingEventLogged) {
        final List<HearingEvent> hearingEvents = new ArrayList<>();
        final HearingEvent hearingEvent = HearingEvent.hearingEvent()
                .withHearingEvent(hearingEventLogged.getRecordedLabel())
                .withHearingEventTime(hearingEventLogged.getEventTime())
                .build();
        hearingEvents.add(hearingEvent);
        return hearingEvents;
    }


    private List<String> buildCaseURNs(final HearingEventLogged hearingEventLogged) {
        final List<String> caseURNs = new ArrayList<>();
        caseURNs.add(hearingEventLogged.getCaseURN());
        return caseURNs;
    }

    private String getOuCode(final UUID courtCentreId) {
        final OrganisationalUnit organisationalUnit = referenceDataLoader.getOrganisationUnitById(courtCentreId);
        return organisationalUnit.getOucode();
    }
}
