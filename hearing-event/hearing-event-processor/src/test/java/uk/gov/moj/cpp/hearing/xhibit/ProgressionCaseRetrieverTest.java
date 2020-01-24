package uk.gov.moj.cpp.hearing.xhibit;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.external.domain.progression.prosecutioncases.LinkedApplicationsSummary;
import uk.gov.moj.cpp.external.domain.progression.prosecutioncases.ProsecutionCase;

import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProgressionCaseRetrieverTest {

    @Mock(answer = RETURNS_DEEP_STUBS)
    private Requester requester;

    @Spy
    private UtcClock utcClock;

    @InjectMocks
    private ProgressionCaseRetriever prosecutionCaseRetriever;

    private static final String APPELLANT_NAME_1 = "John Socks";
    private static final String APPELLANT_NAME_2 = "Jack Socks";
    private static final UUID CASE_ID = randomUUID();
    private static final List<String> RESPONDENT_NAMES = asList("1", "2");

    @Test
    public void shouldRetrieveProsecutionCaseByCaseId() {
        final ProsecutionCase prosecutionCase = getProsecutionCase();

        when(requester.request(any(JsonEnvelope.class), eq(ProsecutionCase.class)).payload()).thenReturn(prosecutionCase);

        final ProsecutionCase retrievedProsecutionCase = prosecutionCaseRetriever.getProsecutionCaseDetails(CASE_ID);

        assertThat(retrievedProsecutionCase.getLinkedApplicationsSummary().size(), is(2));
        assertThat(retrievedProsecutionCase.getLinkedApplicationsSummary().get(0).getApplicantDisplayName(), is(APPELLANT_NAME_1));
        assertThat(retrievedProsecutionCase.getLinkedApplicationsSummary().get(0).getIsAppeal(), is(Boolean.TRUE));
        assertThat(retrievedProsecutionCase.getLinkedApplicationsSummary().get(0).getRespondentDisplayNames(), is(RESPONDENT_NAMES));
        assertThat(retrievedProsecutionCase.getLinkedApplicationsSummary().get(1).getApplicantDisplayName(), is(APPELLANT_NAME_2));
        assertThat(retrievedProsecutionCase.getLinkedApplicationsSummary().get(1).getIsAppeal(), is(Boolean.TRUE));
        assertThat(retrievedProsecutionCase.getLinkedApplicationsSummary().get(1).getRespondentDisplayNames(), is(nullValue()));
        verify(requester, times(1)).request(any(JsonEnvelope.class), eq(ProsecutionCase.class));
    }

    private ProsecutionCase getProsecutionCase() {
        return ProsecutionCase.prosecutionCase().withLinkedApplicationsSummary(
                asList(LinkedApplicationsSummary.linkedApplicationsSummary().withApplicantDisplayName(APPELLANT_NAME_1).withIsAppeal(Boolean.TRUE).withRespondentDisplayNames(RESPONDENT_NAMES).build(),
                        LinkedApplicationsSummary.linkedApplicationsSummary().withApplicantDisplayName(APPELLANT_NAME_2).withIsAppeal(Boolean.TRUE).build()
                )
        ).build();
    }
}