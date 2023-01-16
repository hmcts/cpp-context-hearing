package uk.gov.moj.cpp.hearing.event.service;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import uk.gov.justice.core.courts.LjaDetails;
import uk.gov.justice.core.courts.VerdictType;
import uk.gov.justice.hearing.courts.referencedata.FixedListCollection;
import uk.gov.justice.hearing.courts.referencedata.FixedListResult;
import uk.gov.justice.hearing.courts.referencedata.LocalJusticeAreas;
import uk.gov.justice.hearing.courts.referencedata.OrganisationalUnit;
import uk.gov.justice.hearing.courts.referencedata.Prosecutor;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.event.helper.TreeNode;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.alcohollevel.AlcoholLevelMethod;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.bailstatus.BailStatus;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.AllFixedList;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class NowsReferenceDataServiceImplTest {

    @Mock
    private NowsReferenceCache nowsReferenceCache;

    @Mock
    private LjaReferenceDataLoader ljaReferenceDataLoader;

    @Mock
    private BailStatusReferenceDataLoader bailStatusReferenceDataLoader;

    @Mock
    private FixedListLookup fixedListLookup;

    @Mock
    private ProsecutorDataLoader prosecutorDataLoader;

    @Mock
    private OrganisationalUnitLoader organisationalUnitLoader;

    @Mock
    private VerdictTypesReferenceDataLoader verdictTypesReferenceDataLoader;

    @Mock
    private AlcoholLevelMethodsReferenceDataLoader alcoholLevelMethodsReferenceDataLoader;

    @InjectMocks
    private NowsReferenceDataServiceImpl nowsReferenceDataService;

    @Mock
    private JsonEnvelope jsonEnvelope;

    @Test
    public void shouldGetAllAlcoholLevelMethods() {
        final List<AlcoholLevelMethod> alcoholLevelMethods = Arrays.asList(new AlcoholLevelMethod(randomUUID(), 1, "A", "Blood"));
        when(alcoholLevelMethodsReferenceDataLoader.getAllAlcoholLevelMethods(jsonEnvelope)).thenReturn(alcoholLevelMethods);

        final List<AlcoholLevelMethod> actualAlcoholLevelMethods = nowsReferenceDataService.getAlcoholLevelMethods(jsonEnvelope);

        assertThat(actualAlcoholLevelMethods, is(alcoholLevelMethods));
    }

    @Test
    public void shouldGetAllVerdictTypes() {
        final List<VerdictType> verdictTypes = Arrays.asList(VerdictType.verdictType().withId(randomUUID()).build());
        when(verdictTypesReferenceDataLoader.getAllVerdictTypes(jsonEnvelope)).thenReturn(verdictTypes);

        final List<VerdictType> actualVerdictTypes = nowsReferenceDataService.getVerdictTypes(jsonEnvelope);

        assertThat(actualVerdictTypes, is(verdictTypes));
    }

    @Test
    public void shouldGetAllBailStatuses() {
        final List<BailStatus> bailStatuses = Arrays.asList(new BailStatus());
        when(bailStatusReferenceDataLoader.getAllBailStatuses(jsonEnvelope)).thenReturn(bailStatuses);

        final List<BailStatus> actualBailStatuses = nowsReferenceDataService.getBailStatuses(jsonEnvelope);

        assertThat(actualBailStatuses, is(bailStatuses));
    }

    @Test
    public void shouldGetAllLocalJusticeAreas() {
        final String nationalCourtCode = "Code1";
        final LocalJusticeAreas localJusticeAreas = new LocalJusticeAreas("name", nationalCourtCode, "welshName", Collections.emptyMap());
        when(ljaReferenceDataLoader.getLJAByNationalCourtCode(jsonEnvelope, nationalCourtCode)).thenReturn(localJusticeAreas);

        final LocalJusticeAreas actualLocalJusticeAreas = nowsReferenceDataService.getLJAByNationalCourtCode(jsonEnvelope, nationalCourtCode);

        assertThat(actualLocalJusticeAreas, is(localJusticeAreas));
    }

    @Test
    public void shouldGetLocalJusticeAreaDetails() {
        final UUID courtCentreId = randomUUID();
        final OrganisationalUnit organisationalUnit=OrganisationalUnit.organisationalUnit()
                .withOucode("123ABCD")
                .withIsWelsh(true)
                .withOucodeL3WelshName("Welsh Court Centre")
                .withWelshAddress1("Welsh 1")
                .withWelshAddress2("Welsh 2")
                .withWelshAddress3("Welsh 3")
                .withWelshAddress4("Welsh 4")
                .withWelshAddress5("Welsh 5")
                .withPostcode("LL55 2DF")
                .build();

        final LjaDetails ljaDetails = new LjaDetails(organisationalUnit.getLja(), organisationalUnit.getOucodeL3Name(), organisationalUnit.getOucodeL3WelshName());

        when(ljaReferenceDataLoader.getLjaDetails(jsonEnvelope, courtCentreId)).thenReturn(ljaDetails);

        final LjaDetails actualLjaDetails = nowsReferenceDataService.getLjaDetails(jsonEnvelope, courtCentreId);

        assertThat(actualLjaDetails, is(ljaDetails));
    }

    @Test
    public void shouldGetAllFixedLists() {
        final FixedListResult allFixedList = new FixedListResult(Arrays.asList(new FixedListCollection("cjsQualifier", Collections.emptyList(), "2020-05-01", randomUUID(), "2020-06-01", ZonedDateTime.now())));
        when(fixedListLookup.getAllFixedLists(jsonEnvelope)).thenReturn(allFixedList);

        final FixedListResult actualAllFixedLists = nowsReferenceDataService.getAllFixedLists(jsonEnvelope);

        assertThat(actualAllFixedLists, is(allFixedList));
    }

    @Test
    public void shouldGetAllFixedListForDate() {
        final LocalDate referenceDate = LocalDate.now();
        final AllFixedList allFixedList = new AllFixedList();
        when(nowsReferenceCache.getAllFixedList(jsonEnvelope, referenceDate)).thenReturn(allFixedList);

        final AllFixedList actualAllFixedList = nowsReferenceDataService.getAllFixedList(jsonEnvelope, referenceDate);

        assertThat(actualAllFixedList, is(allFixedList));
    }

    @Test
    public void shouldGetProsecutorById() {
        final UUID prosecutorId = randomUUID();
        final Prosecutor prosecutor = new Prosecutor(null, "fullName", prosecutorId.toString(), "informant@test.hmcts.net", "C1", "OU1");
        when(prosecutorDataLoader.getProsecutorById(jsonEnvelope, prosecutorId)).thenReturn(prosecutor);

        final Prosecutor actualProsecutor = nowsReferenceDataService.getProsecutorById(jsonEnvelope, prosecutorId);

        assertThat(actualProsecutor, is(prosecutor));
    }

    @Test
    public void shouldGetOrganisationUnitById() {
        final UUID organisationUnitId = randomUUID();
        final OrganisationalUnit organisationalUnit = OrganisationalUnit.organisationalUnit()
                .withId(organisationUnitId.toString())
                .build();
        when(organisationalUnitLoader.getOrganisationUnitById(jsonEnvelope, organisationUnitId)).thenReturn(organisationalUnit);

        final OrganisationalUnit actualOrganisationalUnit = nowsReferenceDataService.getOrganisationUnitById(jsonEnvelope, organisationUnitId);

        assertThat(actualOrganisationalUnit, is(organisationalUnit));
    }

    @Test
    public void shouldGetResultDefinitionId() {
        final LocalDate referenceDate = LocalDate.now();
        final UUID resultDefinitionId = randomUUID();
        final ResultDefinition resultDefinition = new ResultDefinition();
        when(nowsReferenceCache.getResultDefinitionById(jsonEnvelope, referenceDate, resultDefinitionId)).thenReturn(new TreeNode(resultDefinitionId, resultDefinition));

        final ResultDefinition actualResultDefinition = nowsReferenceDataService.getResultDefinitionById(jsonEnvelope, referenceDate, resultDefinitionId);

        assertThat(actualResultDefinition, is(resultDefinition));
    }

    @Test
    public void shouldGetResultDefinitionIdTreeNode() {
        final LocalDate referenceDate = LocalDate.now();
        final UUID resultDefinitionId = randomUUID();
        final ResultDefinition resultDefinition = new ResultDefinition();
        final TreeNode resultDefinitionTreeNode = new TreeNode(resultDefinitionId, resultDefinition);
        when(nowsReferenceCache.getResultDefinitionById(jsonEnvelope, referenceDate, resultDefinitionId)).thenReturn(resultDefinitionTreeNode);

        final TreeNode<ResultDefinition> actualResultDefinition = nowsReferenceDataService.getResultDefinitionTreeNodeById(jsonEnvelope, referenceDate, resultDefinitionId);

        assertThat(actualResultDefinition, is(resultDefinitionTreeNode));
    }
}