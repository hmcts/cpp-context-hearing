package uk.gov.moj.cpp.hearing.it;

import static java.util.Optional.of;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.moj.cpp.hearing.it.UseCases.asDefault;
import static uk.gov.moj.cpp.hearing.it.UseCases.initiateHearingWithNsp;
import static uk.gov.moj.cpp.hearing.it.UseCases.logEvent;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.OPEN_CASE_PROSECUTION_EVENT_DEFINITION_ID;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.initiateHearingTemplateWithParamNoReportingRestriction;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.stubOrganisationalUnit;
import static uk.gov.moj.cpp.hearing.utils.WebDavStub.awaitNewSentXmlForPubDisplay;
import static uk.gov.moj.cpp.hearing.utils.WebDavStub.countSentXmlForPubDisplay;
import static uk.gov.moj.cpp.hearing.utils.WebDavStub.getSentXmlForPubDisplay;

import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.LegalEntityDefendant;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.Organisation;
import uk.gov.justice.core.courts.Person;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.steps.CourtListRestrictionSteps;
import uk.gov.moj.cpp.hearing.steps.PublishCourtListSteps;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.InitiateHearingCommandHelper;

import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.concurrent.NotThreadSafe;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * A prosecution case with both an individual and a company (legal entity) defendant must still
 * produce the Xhibit Public Display file.
 */
@NotThreadSafe
public class PublishCourtListWithCompanyDefendantIT extends AbstractPublishLatestCourtCentreHearingIT {

    private final PublishCourtListSteps publishCourtListSteps = new PublishCourtListSteps();
    private final CourtListRestrictionSteps courtListRestrictionSteps = new CourtListRestrictionSteps();

    private ZonedDateTime eventTime;

    @BeforeEach
    public void setUpTest() {
        // Same courtCentreId/courtRoom2Id bucket as the other publish ITs: start from a clean slate so
        // neither stale hearings nor a stale EXPORT_FAILED/EXPORT_SUCCESSFUL row affects this test.
        cleanDatabase("ha_hearing",
                "ha_case",
                "ha_defendant",
                "ha_hearing_day",
                "ha_hearing_event",
                "court_list_publish_status");
        eventTime = new UtcClock().now().minusMinutes(5L);
    }

    @AfterEach
    public void tearDownTest() {
        // Do not leak an unrestricted hearing into other publish ITs sharing this court centre.
        cleanDatabase("ha_hearing");
    }

    @Test
    public void shouldPublishPublicDisplayWhenCaseHasIndividualAndCompanyDefendants() throws NoSuchAlgorithmException {
        stubOrganisationalUnit(fromString(courtCentreId), "OUCODE");
        final String companyName = "CompanyDefendant Ltd " + randomUUID().toString().substring(0, 8);
        final UUID defenceCounselId = randomUUID();

        // No reporting restriction, otherwise defendant names are omitted and the person/company
        // branch in the transformer is never exercised.
        final InitiateHearingCommand command = initiateHearingTemplateWithParamNoReportingRestriction(
                fromString(courtCentreId), fromString(courtRoom2Id), "CourtRoom 1", eventTime.toLocalDate(),
                defenceCounselId, caseId, of(hearingTypeId));
        command.setHearing(withCompanyDefendantAdded(command.getHearing(), companyName));

        final InitiateHearingCommandHelper hearing = h(initiateHearingWithNsp(getRequestSpec(), command));
        logEvent(randomUUID(), getRequestSpec(), asDefault(), hearing.it(), OPEN_CASE_PROSECUTION_EVENT_DEFINITION_ID,
                false, defenceCounselId, eventTime, null);
        courtListRestrictionSteps.waitForHearingVisible(courtCentreId, eventTime.toLocalDate());

        final int pubDisplayCountBeforePublish = countSentXmlForPubDisplay();

        sendPublishCourtListCommand(buildPublishCourtListJsonString(courtCentreId, "26"), courtCentreId);

        // The status query only returns EXPORT_SUCCESSFUL rows (it is also written after the web page
        // export), so it can pass while the Public Display export fails. Wait for the Public Display
        // file itself and assert explicitly that nothing was recorded as failed.
        publishCourtListSteps.verifyCourtListPublishStatusReturnedWhenQueryingFromAPI(courtCentreId);
        awaitNewSentXmlForPubDisplay(pubDisplayCountBeforePublish);
        publishCourtListSteps.verifyNoExportFailed(courtCentreId);

        final String pubDisplayXml = getSentXmlForPubDisplay();
        final Person personDetails = personDefendantDetails(hearing.getHearing());

        assertThat(pubDisplayXml, containsString("<firstname>" + companyName + "</firstname>"));
        assertThat(pubDisplayXml, containsString("<lastname>" + personDetails.getLastName() + "</lastname>"));
    }

    private static Hearing withCompanyDefendantAdded(final Hearing hearing, final String companyName) {
        final ProsecutionCase prosecutionCase = hearing.getProsecutionCases().get(0);
        final Defendant personDefendant = prosecutionCase.getDefendants().get(0);

        final Defendant companyDefendant = Defendant.defendant()
                .withValuesFrom(personDefendant)
                .withId(randomUUID())
                .withMasterDefendantId(randomUUID())
                .withPersonDefendant(null)
                .withLegalEntityDefendant(LegalEntityDefendant.legalEntityDefendant()
                        .withOrganisation(Organisation.organisation().withName(companyName).build())
                        .build())
                .withOffences(personDefendant.getOffences().stream()
                        .map(offence -> Offence.offence().withValuesFrom(offence).withId(randomUUID()).build())
                        .collect(toList()))
                .build();

        final List<Defendant> defendants = new ArrayList<>(prosecutionCase.getDefendants());
        defendants.add(companyDefendant);

        final List<ProsecutionCase> prosecutionCases = new ArrayList<>(hearing.getProsecutionCases());
        prosecutionCases.set(0, ProsecutionCase.prosecutionCase()
                .withValuesFrom(prosecutionCase)
                .withDefendants(defendants)
                .build());

        return Hearing.hearing()
                .withValuesFrom(hearing)
                .withProsecutionCases(prosecutionCases)
                .build();
    }

    private static Person personDefendantDetails(final Hearing hearing) {
        return hearing.getProsecutionCases().get(0).getDefendants().stream()
                .filter(defendant -> defendant.getPersonDefendant() != null)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Template hearing has no person defendant"))
                .getPersonDefendant()
                .getPersonDetails();
    }
}
