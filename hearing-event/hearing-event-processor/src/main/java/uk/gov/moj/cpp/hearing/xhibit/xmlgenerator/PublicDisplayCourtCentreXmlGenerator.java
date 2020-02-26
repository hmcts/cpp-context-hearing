package uk.gov.moj.cpp.hearing.xhibit.xmlgenerator;

import static java.lang.String.format;
import static java.math.BigInteger.ONE;
import static java.math.BigInteger.valueOf;
import static java.time.ZonedDateTime.parse;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Optional.ofNullable;
import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

import uk.gov.justice.core.courts.HearingEvent;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.JudicialRoleTypeEnum;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.CaseDetails;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.Cases;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.Court;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.Courtroom;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.Courtrooms;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.Courtsite;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.Courtsites;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.Currentcourtstatus;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.Datetimestamp;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.DaysOfWeekType;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.Defendant;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.Defendants;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.Floating;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.MonthsOfYearType;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CaseDetail;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CourtSite;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CurrentCourtStatus;
import uk.gov.moj.cpp.hearing.xhibit.CourtCentreGeneratorParameters;
import uk.gov.moj.cpp.hearing.xhibit.XhibitReferenceDataService;
import uk.gov.moj.cpp.hearing.xhibit.XmlUtils;
import uk.gov.moj.cpp.hearing.xhibit.refdatacache.XhibitEventMapperCache;

import java.math.BigInteger;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.JsonObject;

import org.apache.commons.lang3.StringUtils;

@ApplicationScoped
public class PublicDisplayCourtCentreXmlGenerator implements CourtCentreXmlGenerator {

    private static final DateTimeFormatter dateTimeFormatter = ofPattern("HH:mm");
    private static final String TITLE_PREFIX = "titlePrefix";
    private static final String TITLE_SUFFIX = "titleSuffix";
    private static final String FORENAMES = "forenames";
    private static final String SURNAME = "surname";
    private static final String HEARING_QUERY_GET_HEARING_BY_HEARING_ID = "hearing.get.hearing";

    @Inject
    private XmlUtils xmlUtils;

    @Inject
    private PublicDisplayEventGenerator eventGenerator;

    @Inject
    private XhibitEventMapperCache eventMapperCache;

    @Inject
    private XhibitReferenceDataService xhibitReferenceDataService;

    @ServiceComponent(EVENT_PROCESSOR)
    @Inject
    private Requester requester;

    @Inject
    private Enveloper enveloper;

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    private static final uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.ObjectFactory webPageObjectFactory = new uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.ObjectFactory();

    @SuppressWarnings("squid:S3655")
    @Override
    public String generateXml(final CourtCentreGeneratorParameters courtCentreGeneratorParameters) {

        final CurrentCourtStatus cppCurrentCourtStatus = courtCentreGeneratorParameters.getCurrentCourtStatus().get();

        final Currentcourtstatus xhibitCurrentCourtStatus = webPageObjectFactory.createCurrentcourtstatus();

        xhibitCurrentCourtStatus.setPagename(cppCurrentCourtStatus.getCourt().getCourtName().toLowerCase());
        xhibitCurrentCourtStatus.setCourt(getCourt(cppCurrentCourtStatus, courtCentreGeneratorParameters.getEnvelope()));
        xhibitCurrentCourtStatus.setDatetimestamp(getDateTimeStamp(courtCentreGeneratorParameters.getLatestCourtListUploadTime()));

        return xmlUtils.createPublicDisplay(xhibitCurrentCourtStatus);
    }

    private Datetimestamp getDateTimeStamp(final ZonedDateTime latestCourtListUploadTime) {

        final Datetimestamp datetimestamp = webPageObjectFactory.createDatetimestamp();

        datetimestamp.setDayofweek(DaysOfWeekType.valueOf(latestCourtListUploadTime.getDayOfWeek().name()));
        datetimestamp.setDate(latestCourtListUploadTime.getDayOfMonth());
        datetimestamp.setMonth(MonthsOfYearType.valueOf(latestCourtListUploadTime.getMonth().name()));
        datetimestamp.setYear(valueOf(latestCourtListUploadTime.getYear()));
        datetimestamp.setHour(latestCourtListUploadTime.getHour());
        datetimestamp.setMin(latestCourtListUploadTime.getMinute());

        return datetimestamp;
    }

    private Court getCourt(final CurrentCourtStatus cppCurrentCourtStatus, final JsonEnvelope envelope) {

        final Court court = webPageObjectFactory.createCourt();

        court.setCourtname(cppCurrentCourtStatus.getCourt().getCourtName());
        court.setCourtsites(getCourtSites(cppCurrentCourtStatus.getCourt().getCourtSites(), envelope));

        return court;
    }


    private Courtsites getCourtSites(final List<CourtSite> courtSiteList, final JsonEnvelope envelope) {

        final Courtsites courtsites = webPageObjectFactory.createCourtsites();

        courtSiteList.forEach(courtSite -> {

            final Courtsite xhibitCourtSite = webPageObjectFactory.createCourtsite();
            xhibitCourtSite.setCourtsitename(courtSite.getCourtSiteName());
            xhibitCourtSite.setCourtrooms(getCourtRooms(courtSite, envelope));
            final Floating floating = webPageObjectFactory.createFloating();
            xhibitCourtSite.setFloating(floating);

            courtsites.getCourtsite().add(xhibitCourtSite);
        });
        return courtsites;
    }

    private Courtrooms getCourtRooms(final CourtSite courtSite, final JsonEnvelope envelope) {
        final Courtrooms courtrooms = webPageObjectFactory.createCourtrooms();

        courtSite.getCourtRooms().forEach(courtRoom -> courtrooms.getCourtroom().add(getCourtRoom(courtRoom, envelope)));

        return courtrooms;
    }

    private Courtroom getCourtRoom(final uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CourtRoom ccpCourtRoom, final JsonEnvelope envelope) {
        final Courtroom xhibitCourtRoom = webPageObjectFactory.createCourtroom();

        xhibitCourtRoom.setCourtroomname(ccpCourtRoom.getCourtRoomName());
        xhibitCourtRoom.setCases(getCases(ccpCourtRoom.getCases(), envelope));
        return xhibitCourtRoom;
    }


    private Cases getCases(final uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.Cases cppCases, final JsonEnvelope envelope) {
        final Cases xhibitCases = webPageObjectFactory.createCases();

        cppCases.getCasesDetails()
                .forEach(xhibitCase -> xhibitCases.getCaseDetails().add(addCaseDetails(xhibitCase, envelope)));
        return xhibitCases;
    }


    @SuppressWarnings("squid:S1172")
    private CaseDetails addCaseDetails(final uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CaseDetail cppCaseDetail, final JsonEnvelope envelope) {
        final CaseDetails xhibitCaseDetails = new CaseDetails();
        final HearingEvent hearingEvent = cppCaseDetail.getHearingEvent();
        xhibitCaseDetails.setCppurn(cppCaseDetail.getCppUrn());
        xhibitCaseDetails.setCasenumber(ONE);
        xhibitCaseDetails.setHearingprogress(cppCaseDetail.getHearingprogress());
        xhibitCaseDetails.setCasetype(cppCaseDetail.getCaseType());
        xhibitCaseDetails.setActivecase(cppCaseDetail.getActivecase());
        xhibitCaseDetails.setHearingtype(cppCaseDetail.getHearingType());
        xhibitCaseDetails.setDefendants(getDefendants(cppCaseDetail));

        if (null == hearingEvent) {
            final String dateTime = dateTimeFormatter.format(parse(cppCaseDetail.getNotBeforeTime()));
            xhibitCaseDetails.setNotbeforetime(dateTime);
            xhibitCaseDetails.setTimestatusset(dateTime);
            xhibitCaseDetails.setHearingprogress(BigInteger.ZERO);
        } else {
            xhibitCaseDetails.setCurrentstatus(eventGenerator.generate(cppCaseDetail));
            xhibitCaseDetails.setTimestatusset(hearingEvent.getEventTime().format(dateTimeFormatter));
            exposeJudgeNameIfPresent(hearingEvent.getHearingId(), xhibitCaseDetails, envelope);
        }

        return xhibitCaseDetails;
    }

    private void exposeJudgeNameIfPresent(final UUID hearingId, final CaseDetails xhibitCaseDetails, final JsonEnvelope envelope) {
        final JsonObject queryParameters = createObjectBuilder()
                .add("hearingId", hearingId.toString())
                .build();

        final JsonEnvelope requestEnvelope = enveloper.withMetadataFrom(envelope, HEARING_QUERY_GET_HEARING_BY_HEARING_ID).apply(queryParameters);
        final JsonEnvelope jsonEnvelope = requester.requestAsAdmin(requestEnvelope);

        if (!jsonEnvelope.payloadAsJsonObject().isEmpty()) {
            final Optional<HearingDetailsResponse> hearingDetailsResponseOptional = ofNullable(jsonObjectToObjectConverter.convert(jsonEnvelope.payloadAsJsonObject(), HearingDetailsResponse.class));
            if (hearingDetailsResponseOptional.isPresent()) {
                hearingDetailsResponseOptional.get().getHearing().getJudiciary()
                        .stream()
                        .filter(judicialRole -> Objects.nonNull(judicialRole.getJudicialRoleType()))
                        .filter(judicialRole -> !JudicialRoleTypeEnum.MAGISTRATE.toString().equalsIgnoreCase(judicialRole.getJudicialRoleType().getJudiciaryType()))
                        .map(Optional::ofNullable)
                        .findFirst().ifPresent(judicialRolePresent -> {
                            final JsonObject judiciary = xhibitReferenceDataService.getJudiciary(envelope, judicialRolePresent.get().getJudicialId());
                            xhibitCaseDetails.setJudgename(format("%s %s %s %s", judiciary.getString(TITLE_PREFIX, StringUtils.EMPTY), judiciary.getString(FORENAMES), judiciary.getString(SURNAME), judiciary.getString(TITLE_SUFFIX, StringUtils.EMPTY)));
                        });
            }
        }
    }

    private Defendants getDefendants(final CaseDetail cases) {
        final Defendants exhibitDefendants = webPageObjectFactory.createDefendants();

        cases.getDefendants()
                .forEach(cppDefendant -> {

                    final Defendant exhibitDefendant = webPageObjectFactory.createDefendant();
                    exhibitDefendant.setFirstname(cppDefendant.getFirstName());
                    exhibitDefendant.setMiddlename(cppDefendant.getMiddleName());
                    exhibitDefendant.setLastname(cppDefendant.getLastName());
                    exhibitDefendants.getDefendant().add(exhibitDefendant);
                });
        return exhibitDefendants;
    }
}


