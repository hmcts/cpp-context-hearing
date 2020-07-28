package uk.gov.moj.cpp.hearing.event.delegates;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.when;
import static uk.gov.moj.cpp.hearing.event.relist.RelistTestHelper.COURT_CENTRE_LABEL;
import static uk.gov.moj.cpp.hearing.event.relist.RelistTestHelper.COURT_ROOM_LABEL;
import static uk.gov.moj.cpp.hearing.event.relist.RelistTestHelper.ESTIMATED_DURATION_LABEL;
import static uk.gov.moj.cpp.hearing.event.relist.RelistTestHelper.HEARING_TYPE_LABEL;
import static uk.gov.moj.cpp.hearing.event.relist.RelistTestHelper.arbitraryNextHearingMetaData;
import static uk.gov.moj.cpp.hearing.event.relist.RelistTestHelper.getArbitrarySharedResultWithNextHearingResult;
import static uk.gov.moj.cpp.hearing.event.relist.RelistTestHelper.getArbitrarySharedResultWithNextHearingResultExceptTime;

import uk.gov.justice.core.courts.Address;
import uk.gov.justice.core.courts.CourtCentre;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.HearingType;
import uk.gov.justice.core.courts.NextHearing;
import uk.gov.justice.core.courts.NextHearingDefendant;
import uk.gov.justice.core.courts.NextHearingProsecutionCase;
import uk.gov.justice.core.courts.Prompt;
import uk.gov.justice.core.courts.ResultLine;
import uk.gov.justice.core.courts.Target;
import uk.gov.justice.hearing.courts.referencedata.CourtCentreOrganisationUnit;
import uk.gov.justice.hearing.courts.referencedata.Courtrooms;
import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.HearingAdjourned;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.event.relist.HearingAdjournTransformer;
import uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPrompt;
import uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference;
import uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingResultDefinition;
import uk.gov.moj.cpp.hearing.event.service.CourtHouseReverseLookup;
import uk.gov.moj.cpp.hearing.event.service.HearingTypeReverseLookup;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HearingAdjournTransformerTest {
    @Mock
    private HearingTypeReverseLookup hearingTypeReverseLookup;

    @Mock
    private CourtHouseReverseLookup courtHouseReverseLookup;

    @InjectMocks
    private HearingAdjournTransformer target = new HearingAdjournTransformer();


    private Prompt getPromptByLabel(final List<ResultLine> resultLines, final String label) {
        return resultLines.stream().flatMap(resultLine -> resultLine.getPrompts().stream()).filter(prompt -> label.equals(prompt.getLabel())).findFirst().orElseThrow(
                () -> new RuntimeException("no prompt with label " + label)
        );
    }

    @Test
    public void transformProsecutionCase() {
        transform(rs -> {
        }, true, true);
    }

    @Test
    public void transformApplicationOnly() {
        transform(rs -> {
        }, false, true);
    }

    @Test
    public void transformLinkedApplicationDoNotHavingOffenceResult() {
        transform(rs -> rs.getTargets().get(0).setOffenceId(null), true, true);
    }

    @Test
    public void transformProsecutionCaseWithoutTimeInNextHearing() {
        transform(rs -> {
        }, true, false);
    }

    private void transform(final Consumer<ResultsShared> resultSharedModifier, boolean hasProsecutionCase,
                           boolean timeExistsInNextHearingResult) {

        ResultsShared resultsShared;
        if(timeExistsInNextHearingResult){
            resultsShared = getArbitrarySharedResultWithNextHearingResult();
        }else{
            resultsShared = getArbitrarySharedResultWithNextHearingResultExceptTime();
        }
        resultSharedModifier.accept(resultsShared);
        final Hearing hearing = resultsShared.getHearing();
        final Target firstTarget = resultsShared.getTargets().get(0);

        Map<UUID, NextHearingResultDefinition> nextHearingResultDefinitions = new HashMap<>();

        final ResultLine resultLine = firstTarget.getResultLines().get(0);
        final Prompt promptHest = getPromptByLabel(firstTarget.getResultLines(), ESTIMATED_DURATION_LABEL);
        promptHest.setValue("601 MINUTES");
        final Prompt promptType = getPromptByLabel(firstTarget.getResultLines(), HEARING_TYPE_LABEL);

        final Prompt courtCentrePrompt = getPromptByLabel(firstTarget.getResultLines(), COURT_CENTRE_LABEL);
        final Prompt courtRoomPrompt = getPromptByLabel(firstTarget.getResultLines(), COURT_ROOM_LABEL);

        final NextHearingPrompt estDurationPromptReference = new NextHearingPrompt(promptHest.getId(), NextHearingPromptReference.HEST.name());
        NextHearingResultDefinition nextHearingResultDefinition = new NextHearingResultDefinition(resultLine.getResultDefinitionId(),
                estDurationPromptReference);
        nextHearingResultDefinitions.put(resultLine.getResultDefinitionId(), nextHearingResultDefinition);

        nextHearingResultDefinitions = arbitraryNextHearingMetaData();

        final JsonEnvelope context = Mockito.mock(JsonEnvelope.class);

        final HearingType hearingType = HearingType.hearingType().build();
        when(hearingTypeReverseLookup.getHearingTypeByName(context, promptType.getValue())).thenReturn(hearingType);

        final Courtrooms courtroom = Courtrooms.courtrooms()
                .withCourtroomId(54321)
                .withCourtroomName(courtRoomPrompt.getValue())
                .withId(UUID.randomUUID())
                .withWelshCourtroomName(courtRoomPrompt.getValue() + "Welsh")
                .build();
        final CourtCentreOrganisationUnit courtcentreOrganisationunits = CourtCentreOrganisationUnit.courtCentreOrganisationUnit()
                .withOucodeL3Name(courtCentrePrompt.getValue().toUpperCase())
                .withOucodeL3WelshName(courtCentrePrompt.getValue() + "Welsh")
                .withId(UUID.randomUUID().toString())
                .withCourtrooms(asList(courtroom))
                .withPostcode("AA1 1AA")
                .withAddress1("address1")
                .withAddress2("address2")
                .withAddress3("address3")
                .withAddress4("address4")
                .withAddress5("address5")
                .build();

        when(courtHouseReverseLookup.getCourtCentreByName(context, courtCentrePrompt.getValue())).thenReturn(Optional.of(courtcentreOrganisationunits));
        when(courtHouseReverseLookup.getCourtRoomByRoomName(courtcentreOrganisationunits, courtRoomPrompt.getValue())).thenReturn(Optional.of(courtroom));

        final List<ResultLine> newResultLines = new ArrayList<>();
        newResultLines.addAll(firstTarget.getResultLines());
        firstTarget.setResultLines(newResultLines);
        ResultLine newResultLine = ResultLine.resultLine()
                .withResultLineId(UUID.randomUUID())
                .withResultDefinitionId(UUID.randomUUID())
                .withPrompts(asList(Prompt.prompt().withId(UUID.randomUUID()).build()))
                .build();
        newResultLines.add(newResultLine);


        final ResultLine resultLineStartDate = firstTarget.getResultLines().get(1);
        final Prompt promptStartDate = resultLineStartDate.getPrompts().get(0);
        final LocalDateTime startDate = LocalDateTime.of(2019, 01, 12, 19, 35, 00);
        promptStartDate.setValue(startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        final Prompt promptStartTime = resultLineStartDate.getPrompts().stream().filter(p -> p.getLabel().toLowerCase().startsWith("time")).findFirst().orElse(null);
        promptStartTime.setValue(startDate.format(DateTimeFormatter.ofPattern("HH:mm")));

        final NextHearingPrompt startDatePromptReference = new NextHearingPrompt(promptStartDate.getId(), NextHearingPromptReference.HDATE.name());
        final NextHearingResultDefinition startDateResultDefinition = new NextHearingResultDefinition(resultLineStartDate.getResultDefinitionId(), startDatePromptReference);
//        nextHearingResultDefinitions.put(resultLineStartDate.getResultDefinitionId(), startDateResultDefinition);

        final HearingAdjourned hearingAdjourned = target.transform2Adjournment(context, resultsShared, nextHearingResultDefinitions);

        Assert.assertEquals(hearing.getId(), hearingAdjourned.getAdjournedHearing());
        Assert.assertEquals(1, hearingAdjourned.getNextHearings().size());
        final NextHearing nextHearing = hearingAdjourned.getNextHearings().get(0);
        if (hasProsecutionCase) {
            Assert.assertEquals(1, nextHearing.getNextHearingProsecutionCases().size());
        }
        Assert.assertEquals(hearingType, nextHearing.getType());

        // need to check the court room
        // need to check the court centre

        if (hasProsecutionCase) {
            Assert.assertEquals(1, nextHearing.getNextHearingProsecutionCases().get(0).getDefendants().size());
        }

        final CourtCentre courtCentre = nextHearing.getCourtCentre();
        final Address courtCentreAddress = courtCentre.getAddress();
        Assert.assertEquals(courtCentre.getId().toString(), courtcentreOrganisationunits.getId());
        Assert.assertEquals(courtCentre.getName(), courtcentreOrganisationunits.getOucodeL3Name());
        Assert.assertEquals(courtCentre.getRoomId(), courtroom.getId());
        Assert.assertEquals(courtCentre.getRoomName(), courtroom.getCourtroomName());
        Assert.assertEquals(courtCentre.getWelshRoomName(), courtroom.getWelshCourtroomName());
        Assert.assertEquals(courtCentreAddress.getPostcode(), courtcentreOrganisationunits.getPostcode());
        Assert.assertEquals(courtCentreAddress.getAddress1(), courtcentreOrganisationunits.getAddress1());
        Assert.assertEquals(courtCentreAddress.getAddress2(), courtcentreOrganisationunits.getAddress2());
        Assert.assertEquals(courtCentreAddress.getAddress3(), courtcentreOrganisationunits.getAddress3());
        Assert.assertEquals(courtCentreAddress.getAddress4(), courtcentreOrganisationunits.getAddress4());
        Assert.assertEquals(courtCentreAddress.getAddress5(), courtcentreOrganisationunits.getAddress5());

        Assert.assertEquals(601, nextHearing.getEstimatedMinutes().intValue());
        Assert.assertEquals(ZonedDateTimes.fromString(ZonedDateTime.of(startDate, ZoneId.systemDefault()).toString()), nextHearing.getListedStartDateTime());

        final NextHearingProsecutionCase nextHearingProsecutionCase = nextHearing.getNextHearingProsecutionCases().get(0);
        Assert.assertEquals(hearing.getProsecutionCases().get(0).getId(), nextHearingProsecutionCase.getId());
        final NextHearingDefendant nextHearingDefendant = nextHearingProsecutionCase.getDefendants().get(0);
        final Defendant defendant = hearing.getProsecutionCases().get(0).getDefendants().get(0);

        Assert.assertEquals(defendant.getId(), nextHearingDefendant.getId());
        Assert.assertEquals(defendant.getOffences().size(), nextHearingDefendant.getOffences().size());
        Assert.assertEquals(defendant.getOffences().get(0).getId(), nextHearingDefendant.getOffences().get(0).getId());
        Assert.assertEquals(hearing.getCourtApplications().stream().map(app -> app.getId()).collect(Collectors.toSet()),
                nextHearing.getNextHearingCourtApplicationId().stream().collect(Collectors.toSet()));

    }

}