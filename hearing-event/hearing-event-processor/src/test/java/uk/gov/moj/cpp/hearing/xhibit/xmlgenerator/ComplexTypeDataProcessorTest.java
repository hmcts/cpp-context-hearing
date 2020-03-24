package uk.gov.moj.cpp.hearing.xhibit.xmlgenerator;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import uk.gov.justice.core.courts.DefenceCounsel;
import uk.gov.moj.cpp.external.domain.progression.prosecutioncases.LinkedApplicationsSummary;
import uk.gov.moj.cpp.external.domain.progression.prosecutioncases.ProsecutionCase;
import uk.gov.moj.cpp.hearing.xhibit.ProgressionCaseRetriever;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ComplexTypeDataProcessorTest {

    @Mock
    private ProgressionCaseRetriever progressionCaseRetriever;

    @InjectMocks
    private ComplexTypeDataProcessor complexTypeDataProcessor;

    private static final UUID CASE_ID = randomUUID();

    @Test
    public void shouldGetGetDefenceCouncilFullName() {
        final DefenceCounsel defenceCounsel = DefenceCounsel.defenceCounsel().withFirstName("Violet").withLastName("Jones").build();
        assertThat(complexTypeDataProcessor.getGetDefenceCouncilFullName(defenceCounsel), is("Violet Jones"));
    }

    @Test
    public void shouldGetAppellantDisplayName() {
        final ProsecutionCase prosecutionCase = getProsecutionCase();
        when(progressionCaseRetriever.getProsecutionCaseDetails(CASE_ID)).thenReturn(prosecutionCase);
        assertThat(complexTypeDataProcessor.getAppellantDisplayName(asList(CASE_ID)).get(), is("Joanne Black"));
    }

    @Test
    public void shouldNotGetAppellantDisplayName() {
        final ProsecutionCase prosecutionCase = ProsecutionCase.prosecutionCase().build();
        when(progressionCaseRetriever.getProsecutionCaseDetails(CASE_ID)).thenReturn(prosecutionCase);
        assertThat(complexTypeDataProcessor.getAppellantDisplayName(asList(CASE_ID)).isPresent(), is(false));
    }

    private ProsecutionCase getProsecutionCase() {
        return ProsecutionCase.prosecutionCase().withLinkedApplicationsSummary(
                asList(LinkedApplicationsSummary.linkedApplicationsSummary().withApplicantDisplayName("Joanne Black").withIsAppeal(Boolean.TRUE).withRespondentDisplayNames(asList("1", "2")).build(),
                        LinkedApplicationsSummary.linkedApplicationsSummary().withApplicantDisplayName("Joanne Jones").withIsAppeal(Boolean.TRUE).build()
                )
        ).build();
    }
}