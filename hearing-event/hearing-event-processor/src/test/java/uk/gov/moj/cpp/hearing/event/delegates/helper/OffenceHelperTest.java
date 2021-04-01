package uk.gov.moj.cpp.hearing.event.delegates.helper;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.HEARING_RESULTS_SHARED_COURT_APPLICATION_WITH_INDICATED_PLEA_JSON;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.HEARING_RESULTS_SHARED_WITH_INDICATED_PLEA_JSON;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.HEARING_RESULTS_SHARED_WITH_OFFENCE_FACTS_JSON;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.HEARING_RESULTS_SHARED_WITH_VERDICT_TYPE_JSON;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtOrderOffence;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.IndicatedPlea;
import uk.gov.justice.core.courts.IndicatedPleaValue;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.OffenceFacts;
import uk.gov.justice.core.courts.Plea;
import uk.gov.justice.core.courts.Source;
import uk.gov.justice.core.courts.VerdictType;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.alcohollevel.AlcoholLevelMethod;
import uk.gov.moj.cpp.hearing.event.service.ReferenceDataService;
import uk.gov.moj.cpp.hearing.test.FileResourceObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class OffenceHelperTest {

    @Mock
    private ReferenceDataService referenceDataService;

    @Mock
    private JsonEnvelope context;

    @InjectMocks
    private OffenceHelper offenceHelper;

    protected static final FileResourceObjectMapper fileResourceObjectMapper = new FileResourceObjectMapper();

    private List<VerdictType> allVerdictTypes = asList(
            VerdictType.verdictType()
                    .withVerdictCode("VC01")
                    .withCategory("Guilty")
                    .withCategoryType("GUILTY_BY_JURY_CONVICTED")
                    .withDescription("Guilty")
                    .withId(UUID.fromString("c4ca4238-a0b9-3382-8dcc-509a6f75849b"))
                    .withSequence(1)
                    .build(),

            VerdictType.verdictType()
                    .withVerdictCode("VC13")
                    .withCategory("Not Guilty")
                    .withCategoryType("NOT_GUILTY")
                    .withDescription("Not guilty by reason of insanity")
                    .withId(UUID.fromString("c51ce410-c124-310e-8db5-e4b97fc2af39"))
                    .withSequence(13)
                    .build());

    private List<AlcoholLevelMethod> allAlcoholLevelMethod = asList(
            new AlcoholLevelMethod(randomUUID(), 1, "A", "Blood"),
            new AlcoholLevelMethod(randomUUID(), 2, "B", "Breath"));

    @Before
    public void setUp() throws IOException {
        when(referenceDataService.getVerdictTypes(context)).thenReturn(allVerdictTypes);
        when(referenceDataService.getAlcoholLevelMethods(context)).thenReturn(allAlcoholLevelMethod);
    }

    @Test
    public void shareResultsWithEnrichedVerdictType() throws IOException {
        final ResultsShared resultsShared = fileResourceObjectMapper.convertFromFile(HEARING_RESULTS_SHARED_WITH_VERDICT_TYPE_JSON, ResultsShared.class);
        offenceHelper.enrichOffence(context, resultsShared.getHearing());
        final Optional<Defendant> defendant = resultsShared.getHearing().getProsecutionCases().stream()
                .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream()).findFirst();
        assertThat(defendant.isPresent(), is(true));

        final VerdictType verdictType = defendant.get().getOffences().get(0).getVerdict().getVerdictType();
        assertThat(verdictType.getCategory(), is("Not Guilty"));
        assertThat(verdictType.getCategoryType(), is("NOT_GUILTY"));
        assertThat(verdictType.getDescription(), is("Not guilty by reason of insanity"));
        assertThat(verdictType.getId().toString(), is("c51ce410-c124-310e-8db5-e4b97fc2af39"));
        assertThat(verdictType.getVerdictCode(), is("VC13"));
        assertThat(verdictType.getSequence(), is(13));
    }

    @Test
    public void shareResultsWithEnrichedAlcoholMethodLevels() throws IOException {
        final ResultsShared resultsShared = fileResourceObjectMapper.convertFromFile(HEARING_RESULTS_SHARED_WITH_OFFENCE_FACTS_JSON, ResultsShared.class);
        offenceHelper.enrichOffence(context, resultsShared.getHearing());

        final Optional<Defendant> defendant = resultsShared.getHearing().getProsecutionCases().stream()
                .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream()).findFirst();
        assertThat(defendant.isPresent(), is(true));

        final OffenceFacts offenceFacts = defendant.get().getOffences().get(0).getOffenceFacts();
        assertThat(offenceFacts.getAlcoholReadingAmount(), is(99));
        assertThat(offenceFacts.getAlcoholReadingMethodCode(), is("B"));
        assertThat(offenceFacts.getAlcoholReadingMethodDescription(), is("Breath"));
        assertThat(offenceFacts.getVehicleMake(), is("Ford"));
        assertThat(offenceFacts.getVehicleCode().name(), is("PASSENGER_CARRYING_VEHICLE"));
        assertThat(offenceFacts.getVehicleRegistration(), is("A123 LIU"));
    }

    @Test
    public void shouldSetIndicatedPleaOnShareResults() throws IOException {
        final ResultsShared resultsShared = fileResourceObjectMapper.convertFromFile(HEARING_RESULTS_SHARED_WITH_INDICATED_PLEA_JSON, ResultsShared.class);
        offenceHelper.enrichOffence(context, resultsShared.getHearing());

        final Optional<Defendant> defendant = resultsShared.getHearing().getProsecutionCases().stream()
                .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream()).findFirst();
        assertThat(defendant.isPresent(), is(true));

        final Plea plea = defendant.get().getOffences().get(0).getPlea();
        assertThat(defendant.get().getOffences().get(0).getModeOfTrial(), is("Either Way"));
        assertThat(plea.getPleaDate().toString(), is("2018-06-24"));
        assertThat(plea.getPleaValue(), is("INDICATED_GUILTY"));
        assertThat(plea.getOffenceId().toString(), is("47ecee20-0215-11ea-9bbd-b1f5a4493d17"));
        assertThat(plea.getOriginatingHearingId().toString(), is("31048c0d-e937-49fb-bfc5-02abc56d3f6a"));

        final IndicatedPlea indicatedPlea = defendant.get().getOffences().get(0).getIndicatedPlea();
        assertThat(indicatedPlea.getIndicatedPleaDate().toString(), is("2018-06-24"));
        assertThat(indicatedPlea.getIndicatedPleaValue(), is(IndicatedPleaValue.INDICATED_GUILTY));
        assertThat(indicatedPlea.getOffenceId().toString(), is("47ecee20-0215-11ea-9bbd-b1f5a4493d17"));
        assertThat(indicatedPlea.getOriginatingHearingId().toString(), is("31048c0d-e937-49fb-bfc5-02abc56d3f6a"));
        assertThat(indicatedPlea.getSource(), is(Source.IN_COURT));
    }

    @Test
    public void shouldSetEnrichedVerdictTypeForCourtApplication() throws IOException {
        final ResultsShared resultsShared = fileResourceObjectMapper.convertFromFile(HEARING_RESULTS_SHARED_COURT_APPLICATION_WITH_INDICATED_PLEA_JSON, ResultsShared.class);
        offenceHelper.enrichOffence(context, resultsShared.getHearing());

        final Optional<Offence> offence = ofNullable(resultsShared.getHearing().getCourtApplications()).orElse(emptyList()).stream()
                .flatMap(ca -> ofNullable(ca.getCourtApplicationCases()).orElse(emptyList()).stream())
                .flatMap(c -> c.getOffences().stream())
                .filter(Objects::nonNull).findFirst();
        assertThat(offence.isPresent(), is(true));

        final VerdictType verdictType = offence.get().getVerdict().getVerdictType();
        assertThat(verdictType.getCategory(), is("Not Guilty"));
        assertThat(verdictType.getCategoryType(), is("NOT_GUILTY"));
        assertThat(verdictType.getDescription(), is("Not guilty by reason of insanity"));
        assertThat(verdictType.getId().toString(), is("c51ce410-c124-310e-8db5-e4b97fc2af39"));
        assertThat(verdictType.getVerdictCode(), is("VC13"));
        assertThat(verdictType.getSequence(), is(13));
    }

    @Test
    public void shouldSetEnrichedAlcoholMethodLevelsForCourtApplication() throws IOException {
        final ResultsShared resultsShared = fileResourceObjectMapper.convertFromFile(HEARING_RESULTS_SHARED_COURT_APPLICATION_WITH_INDICATED_PLEA_JSON, ResultsShared.class);
        offenceHelper.enrichOffence(context, resultsShared.getHearing());

        final Optional<Offence> offence = ofNullable(resultsShared.getHearing().getCourtApplications()).orElse(emptyList()).stream()
                .flatMap(ca -> ofNullable(ca.getCourtApplicationCases()).orElse(emptyList()).stream())
                .flatMap(c -> c.getOffences().stream())
                .filter(Objects::nonNull).findFirst();
        assertThat(offence.isPresent(), is(true));

        final OffenceFacts offenceFacts = offence.get().getOffenceFacts();
        assertThat(offenceFacts.getAlcoholReadingAmount(), is(99));
        assertThat(offenceFacts.getAlcoholReadingMethodCode(), is("B"));
        assertThat(offenceFacts.getAlcoholReadingMethodDescription(), is("Breath"));
        assertThat(offenceFacts.getVehicleMake(), is("Ford"));
        assertThat(offenceFacts.getVehicleCode().name(), is("PASSENGER_CARRYING_VEHICLE"));
        assertThat(offenceFacts.getVehicleRegistration(), is("A123 LIU"));
    }

    @Test
    public void shouldEnrichedOffenceDataFromCourtApplicationOrders() throws IOException {
        final ResultsShared resultsShared = fileResourceObjectMapper.convertFromFile(HEARING_RESULTS_SHARED_COURT_APPLICATION_WITH_INDICATED_PLEA_JSON, ResultsShared.class);
        offenceHelper.enrichOffence(context, resultsShared.getHearing());

        final Optional<Offence> offence = ofNullable(resultsShared.getHearing().getCourtApplications()).orElse(emptyList()).stream()
                .map(CourtApplication::getCourtOrder)
                .filter(Objects::nonNull)
                .flatMap(o -> o.getCourtOrderOffences().stream())
                .map(CourtOrderOffence::getOffence)
                .findFirst();

        assertThat(offence.isPresent(), is(true));

        final OffenceFacts offenceFacts = offence.get().getOffenceFacts();
        assertThat(offenceFacts.getAlcoholReadingAmount(), is(99));
        assertThat(offenceFacts.getAlcoholReadingMethodCode(), is("B"));
        assertThat(offenceFacts.getAlcoholReadingMethodDescription(), is("Breath"));
        assertThat(offenceFacts.getVehicleMake(), is("Ford"));
        assertThat(offenceFacts.getVehicleCode().name(), is("PASSENGER_CARRYING_VEHICLE"));
        assertThat(offenceFacts.getVehicleRegistration(), is("A123 LIU"));
    }

    @Test
    public void shouldSetCourtApplicationIndicatedPleaOnShareResults() throws IOException {
        final ResultsShared resultsShared = fileResourceObjectMapper.convertFromFile(HEARING_RESULTS_SHARED_COURT_APPLICATION_WITH_INDICATED_PLEA_JSON, ResultsShared.class);
        offenceHelper.enrichOffence(context, resultsShared.getHearing());

        final Optional<Offence> offence = ofNullable(resultsShared.getHearing().getCourtApplications()).orElse(emptyList()).stream()
                .flatMap(ca -> ofNullable(ca.getCourtApplicationCases()).orElse(emptyList()).stream())
                .flatMap(c -> c.getOffences().stream())
                .filter(Objects::nonNull).findFirst();
        assertThat(offence.isPresent(), is(true));

        final Plea plea = offence.get().getPlea();
        assertThat(offence.get().getModeOfTrial(), is("Either Way"));
        assertThat(plea.getPleaDate().toString(), is("2020-06-24"));
        assertThat(plea.getPleaValue(), is("INDICATED_GUILTY"));
        assertThat(plea.getOffenceId().toString(), is("47ecee20-0215-11ea-9bbd-b1f5a4493d17"));
        assertThat(plea.getOriginatingHearingId().toString(), is("31048c0d-e937-49fb-bfc5-02abc56d3f6a"));

        final IndicatedPlea indicatedPlea = offence.get().getIndicatedPlea();
        assertThat(indicatedPlea.getIndicatedPleaDate().toString(), is("2020-06-24"));
        assertThat(indicatedPlea.getIndicatedPleaValue(), is(IndicatedPleaValue.INDICATED_GUILTY));
        assertThat(indicatedPlea.getOffenceId().toString(), is("47ecee20-0215-11ea-9bbd-b1f5a4493d17"));
        assertThat(indicatedPlea.getOriginatingHearingId().toString(), is("31048c0d-e937-49fb-bfc5-02abc56d3f6a"));
        assertThat(indicatedPlea.getSource(), is(Source.IN_COURT));
    }
}
