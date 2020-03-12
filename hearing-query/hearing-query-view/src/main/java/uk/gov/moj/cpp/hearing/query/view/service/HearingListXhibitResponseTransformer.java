package uk.gov.moj.cpp.hearing.query.view.service;

import static java.time.format.DateTimeFormatter.ofPattern;
import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static uk.gov.justice.hearing.courts.JurisdictionType.CROWN;
import static uk.gov.moj.cpp.JudicialRoleTypeEnum.CIRCUIT_JUDGE;
import static uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CaseDetail.caseDetail;
import static uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.Cases.cases;
import static uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.Court.court;
import static uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CourtRoom.courtRoom;
import static uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CourtSite.courtSite;
import static uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CurrentCourtStatus.currentCourtStatus;
import static uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.Defendant.defendant;
import static uk.gov.moj.cpp.hearing.query.view.service.CaseStatusCode.ACTIVE;
import static uk.gov.moj.cpp.hearing.query.view.service.CaseStatusCode.INACTIVE;
import static uk.gov.moj.cpp.hearing.query.view.service.ProgessStatusCode.FINISHED;
import static uk.gov.moj.cpp.hearing.query.view.service.ProgessStatusCode.INPROGRESS;
import static uk.gov.moj.cpp.hearing.query.view.service.ProgessStatusCode.STARTED;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.HearingEvent;
import uk.gov.justice.core.courts.JudicialRole;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.moj.cpp.external.domain.referencedata.CourtRoomMapping;
import uk.gov.moj.cpp.hearing.query.view.referencedata.XhibitCourtRoomMapperCache;
import uk.gov.moj.cpp.hearing.query.view.referencedata.XhibitHearingTypesCache;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CaseDetail;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.Cases;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.Court;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CourtRoom;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CourtSite;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CurrentCourtStatus;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.Defendant;

import java.math.BigInteger;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

@ApplicationScoped
public class HearingListXhibitResponseTransformer {

    @Inject
    private XhibitCourtRoomMapperCache xhibitCourtRoomMapperCache;

    private static final DateTimeFormatter dateTimeFormatter = ofPattern("yyyy-MM-dd'T'HH:mm'Z'");

    @Inject
    private XhibitHearingTypesCache xhibitHearingTypesCache;

    @Inject
    private ReferenceDataService referenceDataService;

    public CurrentCourtStatus transformFrom(final HearingEventsToHearingMapper hearingEventsToHearingMapper) {
        return currentCourtStatus()
                .withCourt(getCourt(hearingEventsToHearingMapper))
                .build();
    }

    private Court getCourt(final HearingEventsToHearingMapper hearingEventsToHearingMapper) {
        final Map<UUID, CourtSite> courtSiteMap = new HashMap<>();
        return court()
                //Logically all hearings will belong to single court centre, therefore we pick up the first one
                .withCourtName(hearingEventsToHearingMapper.getHearingList().get(0).getCourtCentre().getName())
                .withCourtSites(getCourtSites(hearingEventsToHearingMapper, courtSiteMap))
                .build();
    }

    private List<CourtSite> getCourtSites(final HearingEventsToHearingMapper hearingEventsToHearingMapper, final Map<UUID, CourtSite> courtSiteMap) {
        return hearingEventsToHearingMapper.getHearingList()
                .stream()
                .map(hearing -> getCourtSite(hearingEventsToHearingMapper, hearing, courtSiteMap))
                .distinct()
                .collect(toList());
    }

    private CourtSite getCourtSite(final HearingEventsToHearingMapper hearingEventsToHearingMapper, final Hearing hearing, final Map<UUID, CourtSite> courtSiteMap) {
        final CourtRoomMapping courtRoomMapping = xhibitCourtRoomMapperCache.getXhibitCourtRoomForCourtCentreAndRoomId(hearing.getCourtCentre().getId(), hearing.getCourtCentre().getRoomId());
        CourtSite courtSite = courtSiteMap.get(courtRoomMapping.getCrestCourtSiteUUID()) ;
        if(courtSite == null) {
            courtSite = courtSite()
                    .withId(courtRoomMapping.getCrestCourtSiteUUID())
                    .withCourtSiteName(courtRoomMapping.getCrestCourtSiteName())
                    .withCourtRooms(new ArrayList<>())
                    .build();
        }
        courtSite.getCourtRooms().addAll(getCourtRoomsForCourtSite(hearingEventsToHearingMapper, courtRoomMapping.getCrestCourtSiteUUID()));
        return courtSite;
    }

    private List<CourtRoom> getCourtRoomsForCourtSite(final HearingEventsToHearingMapper hearingEventsToHearingMapper, final UUID crestCourtSiteId ) {
        final Map<UUID, CourtRoom> courtRoomMap = new HashMap<>();
        return hearingEventsToHearingMapper.getHearingList()
                .stream()
                .filter(hearing -> isHearingForCourtSite(crestCourtSiteId, hearing))
                .map(hearing -> getCourtRoom(hearingEventsToHearingMapper, hearing, courtRoomMap ))
                .distinct()
                .collect(toList());
    }

    private boolean isHearingForCourtSite(final UUID crestCourtSiteId, final Hearing hearing) {
        return xhibitCourtRoomMapperCache.getXhibitCourtRoomForCourtCentreAndRoomId(hearing.getCourtCentre().getId(), hearing.getCourtCentre().getRoomId()).getCrestCourtSiteUUID().equals(crestCourtSiteId);
    }

    private CourtRoom getCourtRoom(final HearingEventsToHearingMapper hearingEventsToHearingMapper,
                                   final Hearing hearing,
                                   final Map<UUID, CourtRoom> courtRoomMap) {
        final UUID courtRoomKey = hearing.getCourtCentre().getRoomId();
        final CourtRoomMapping courtRoomMapping = xhibitCourtRoomMapperCache.getXhibitCourtRoomForCourtCentreAndRoomId(hearing.getCourtCentre().getId(), hearing.getCourtCentre().getRoomId());
        CourtRoom courtRoom = courtRoomMap.get(courtRoomKey) ;

        final Set<UUID> activeHearingIds = hearingEventsToHearingMapper.getActiveHearingIds();

        final BigInteger isActiveHearing = activeHearingIds.contains(hearing.getId()) ? ACTIVE.getStatusCode() : INACTIVE.getStatusCode();

        BigInteger hearingprogessValue = ACTIVE.getStatusCode().equals(isActiveHearing) ? INPROGRESS.getProgressCode() : STARTED.getProgressCode();

        final Map<UUID, UUID> hearingIdAndEventDefinitionIds = hearingEventsToHearingMapper.getHearingIdAndEventDefinitionIds();
        final UUID hearingEventDefinitionId = hearingIdAndEventDefinitionIds.get(hearing.getId());
        final boolean finishedHearingDefinitionsId = EventDefinitions.FINISHED.getEventDefinitionsId().equals(hearingEventDefinitionId);

        hearingprogessValue = finishedHearingDefinitionsId ? FINISHED.getProgressCode() : hearingprogessValue;

        if(courtRoom == null) {
            final Cases cases = getCases(hearing, hearingEventsToHearingMapper.getAllHearingEventBy(hearing.getId()).orElse(null), isActiveHearing, hearingprogessValue);
            courtRoom = courtRoom()
                    .withCourtRoomName(courtRoomMapping.getCrestCourtRoomName())
                    .withCases(cases)
                    .withHearingEvent(hearingEventsToHearingMapper.getAllHearingEventBy(hearing.getId()).orElse(null))
                    .withDefenceCouncil(hearing.getDefenceCounsels())
                    .withLinkedCaseIds(getLinkedCaseIds(hearing.getCourtApplications()))
                    .build();
            courtRoom.setCourtRoomId(courtRoomKey);
            courtRoomMap.put(courtRoomKey, courtRoom);
        } else {
            final Optional<HearingEvent> hearingEventBy = hearingEventsToHearingMapper.getAllHearingEventBy(hearing.getId());
            final List<CaseDetail> casesDetails = getCases(hearing, hearingEventBy.orElse(null), isActiveHearing, hearingprogessValue).getCasesDetails();

            courtRoom.getCases().getCasesDetails().addAll(casesDetails);
            courtRoom.getLinkedCaseIds().addAll(getLinkedCaseIds(hearing.getCourtApplications()));
        }
        return courtRoom;
    }

    private List<UUID> getLinkedCaseIds(final List<CourtApplication> courtApplications) {
        return ofNullable(courtApplications).orElse(Collections.emptyList())
                .stream().map(CourtApplication::getLinkedCaseId).collect(toList());
    }

    private Cases getCases(final Hearing hearing,
                           final HearingEvent hearingEvent,
                           final BigInteger isActiveHearing,
                           final BigInteger hearingprogessValue) {

        final List<CaseDetail> caseDetailsList = new ArrayList<>();
        hearing.getProsecutionCases()
                .forEach(prosecutionCase ->
                        caseDetailsList.add(buildCaseDetail(hearing, hearingEvent, prosecutionCase, isActiveHearing, hearingprogessValue)));
        return cases().withCasesDetails(caseDetailsList).build();
    }

    @SuppressWarnings("squid:S3655")
    private CaseDetail buildCaseDetail(final Hearing hearing, final HearingEvent hearingEvent, final ProsecutionCase prosecutionCase,
                                       final BigInteger activecase, final BigInteger hearingprogessValue) {

        //get the hearingType by hearingType id from cache
        final String exhibitHearingTypeDescription = xhibitHearingTypesCache.getHearingTypeDescription(hearing.getType().getId());


        final CaseDetail caseDetail  = caseDetail()
                .withActivecase(activecase)
                .withHearingprogress(hearingprogessValue)
                .withCppUrn(prosecutionCase.getProsecutionCaseIdentifier().getCaseURN())
                .withCaseType(CROWN.name()) //TODO: this is wrong --> Single character case type (e.g. A – Appeal, T – Trial, S – Sentence).  Only supplied by XHIBIT cases.
                .withHearingType(exhibitHearingTypeDescription)
                .withDefendants(getDefendants(prosecutionCase, StringUtils.isNotEmpty(hearing.getReportingRestrictionReason())))
                .withJudgeName(getJudgeName(hearing))
                .withHearingEvent(hearingEvent)
                .withNotBeforeTime(dateTimeFormatter.format(hearing.getHearingDays().stream().max((x, y) -> x.getSittingDay().compareTo(y.getSittingDay())).get().getSittingDay()))
                .build();
        caseDetail.setLinkedCaseIds(getLinkedCaseIds(hearing.getCourtApplications()));
        caseDetail.setDefenceCounsels(hearing.getDefenceCounsels());
        return caseDetail;
    }

    private String getJudgeName(final Hearing hearing) {
        final Optional<JudicialRole> judicialRole = hearing
                .getJudiciary()
                .stream()
                .filter(hearingJudicialRole -> hearingJudicialRole.getJudicialRoleType().getJudiciaryType().equals(CIRCUIT_JUDGE.name()))
                .findFirst();

        if (judicialRole.isPresent()) {
            return judicialRole.get().getTitle().concat(" ").concat(judicialRole.get().getLastName());
        }
        return EMPTY;
    }

    private List<Defendant> getDefendants(final ProsecutionCase prosecutionCase, final boolean needToBeOmitted) {

        return prosecutionCase.getDefendants()
                .stream()
                .map(prosecutionCaseDefendant -> needToBeOmitted ? defendant().build() : defendant()
                        .withFirstName(prosecutionCaseDefendant.getPersonDefendant().getPersonDetails().getFirstName())
                        .withMiddleName(prosecutionCaseDefendant.getPersonDefendant().getPersonDetails().getMiddleName())
                        .withLastName(prosecutionCaseDefendant.getPersonDefendant().getPersonDetails().getLastName())
                        .build())
                .collect(toList());
    }
}