package uk.gov.moj.cpp.hearing.query.view.service;

import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static uk.gov.justice.hearing.courts.JurisdictionType.CROWN;
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
import uk.gov.justice.core.courts.CourtApplicationParty;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.HearingEvent;
import uk.gov.justice.core.courts.JudicialRole;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.moj.cpp.external.domain.referencedata.CourtRoomMapping;
import uk.gov.moj.cpp.hearing.query.view.referencedata.XhibitCourtRoomMapperCache;
import uk.gov.moj.cpp.hearing.query.view.referencedata.XhibitHearingTypesCache;
import uk.gov.moj.cpp.hearing.query.view.referencedata.XhibitJudiciaryCache;
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
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

@ApplicationScoped
public class HearingListXhibitResponseTransformer {

    private static final String CIRCUIT = "circuit";

    @Inject
    private XhibitCourtRoomMapperCache xhibitCourtRoomMapperCache;

    private static final DateTimeFormatter dateTimeFormatter = ofPattern("yyyy-MM-dd'T'HH:mm'Z'");

    @Inject
    private XhibitHearingTypesCache xhibitHearingTypesCache;

    @Inject
    private ReferenceDataService referenceDataService;

    @Inject
    private XhibitJudiciaryCache xhibitJudiciaryCache;


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
        CourtRoom courtRoom = courtRoomMap.get(courtRoomKey);

        final Set<UUID> activeHearingIds = hearingEventsToHearingMapper.getActiveHearingIds();

        final BigInteger isActiveHearing = activeHearingIds.contains(hearing.getId()) ? ACTIVE.getStatusCode() : INACTIVE.getStatusCode();

        BigInteger hearingprogessValue = ACTIVE.getStatusCode().equals(isActiveHearing) ? INPROGRESS.getProgressCode() : STARTED.getProgressCode();

        final Optional<HearingEvent> latestEventOpt = hearingEventsToHearingMapper.getAllHearingEventBy(hearing.getId());
        if (latestEventOpt.isPresent()) {
            final HearingEvent latestEvent = latestEventOpt.get();
            final UUID latestHearingEventDefinitionId = latestEvent.getHearingEventDefinitionId();
            final boolean finishedHearingDefinitionsId = EventDefinitions.FINISHED.getEventDefinitionsId().equals(latestHearingEventDefinitionId);
            hearingprogessValue = finishedHearingDefinitionsId ? FINISHED.getProgressCode() : hearingprogessValue;
        }

        if (courtRoom == null) {
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

        if (CollectionUtils.isEmpty(hearing.getProsecutionCases())) {
            return cases().withCasesDetails(buildCaseDetailsForStandaloneApplication(hearing, hearingEvent, isActiveHearing, hearingprogessValue)).build();
        }

        return cases().withCasesDetails(buildCaseDetailsForProsecutionCases(hearing, hearingEvent, isActiveHearing, hearingprogessValue)).build();
    }

    private List<CaseDetail> buildCaseDetailsForProsecutionCases(final Hearing hearing, final HearingEvent hearingEvent, final BigInteger isActiveHearing, final BigInteger hearingprogessValue) {
        final List<CaseDetail> caseDetailsList = new ArrayList<>();
        hearing.getProsecutionCases()
                .forEach(prosecutionCase -> {
                    final List<Defendant> defendants = getDefendants(prosecutionCase, StringUtils.isNotEmpty(hearing.getReportingRestrictionReason()));
                    caseDetailsList.add(buildCaseDetail(hearing,
                            hearingEvent,
                            isActiveHearing,
                            defendants,
                            getCaseURN(prosecutionCase),
                            hearingprogessValue));
                });

        return caseDetailsList;
    }

    private String getCaseURN(final ProsecutionCase prosecutionCase) {
        String caseURN = prosecutionCase.getProsecutionCaseIdentifier().getCaseURN();
        if (caseURN == null) {
            caseURN = prosecutionCase.getProsecutionCaseIdentifier().getProsecutionAuthorityReference();
        }
        return caseURN;
    }

    private List<CaseDetail> buildCaseDetailsForStandaloneApplication(final Hearing hearing, final HearingEvent hearingEvent, final BigInteger isActiveHearing, final BigInteger hearingprogessValue) {
        final List<CaseDetail> caseDetailList = new ArrayList<>();

        hearing.getCourtApplications()
                .forEach(courtApplication -> {
                    final List<Defendant> defendants = getDefendantsForStandaloneApplication(courtApplication.getApplicant(), StringUtils.isNotEmpty(hearing.getReportingRestrictionReason()));
                    caseDetailList.add(buildCaseDetail(hearing,
                                        hearingEvent,
                                        isActiveHearing, defendants,
                                        courtApplication.getApplicationReference(),
                                        hearingprogessValue));
                });

        return caseDetailList;
    }

    private List<Defendant> getDefendantsForStandaloneApplication(final CourtApplicationParty applicant, final boolean needToBeOmitted) {
        if (needToBeOmitted || (Objects.isNull(applicant.getPersonDetails()) && Objects.isNull(applicant.getOrganisation()))) {
            return asList(defendant().build());
        }
        if (Objects.nonNull(applicant.getPersonDetails())) {
            return asList(defendant()
                    .withFirstName(applicant.getPersonDetails().getFirstName())
                    .withMiddleName(applicant.getPersonDetails().getMiddleName())
                    .withLastName(applicant.getPersonDetails().getLastName()).build());
        }
        return  asList(defendant().withFirstName(applicant.getOrganisation().getName()).build());
    }

    @SuppressWarnings("squid:S3655")
    private CaseDetail buildCaseDetail(final Hearing hearing,
                                       final HearingEvent hearingEvent,
                                       final BigInteger activecase,
                                       final List<Defendant> defendants,
                                       final String cppUrn,
                                       final BigInteger hearingprogessValue) {
        //get the hearingType by hearingType id from cache
        final String exhibitHearingTypeDescription = xhibitHearingTypesCache.getHearingTypeDescription(hearing.getType().getId());

        final CaseDetail caseDetail  = caseDetail()
                .withActivecase(activecase)
                .withHearingprogress(hearingprogessValue)
                .withCppUrn(cppUrn)
                .withCaseType(CROWN.name()) //TODO: this is wrong --> Single character case type (e.g. A – Appeal, T – Trial, S – Sentence).  Only supplied by XHIBIT cases.
                .withHearingType(exhibitHearingTypeDescription)
                .withDefendants(defendants)
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
                .filter(isCircuitJudge())
                .findFirst();

        if (judicialRole.isPresent()) {
            return xhibitJudiciaryCache.getJudiciaryName(judicialRole.get().getJudicialId());
        }
        return EMPTY;
    }

    private Predicate<JudicialRole> isCircuitJudge() {
        return hearingJudicialRole -> {
            final String judiciaryType = hearingJudicialRole.getJudicialRoleType().getJudiciaryType().toLowerCase();
            return (judiciaryType.contains(CIRCUIT));
        };
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