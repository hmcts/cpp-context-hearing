package uk.gov.moj.cpp.hearing.query.api.service.progression;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.external.domain.progression.prosecutioncases.LinkedApplicationsSummary;
import uk.gov.moj.cpp.external.domain.progression.prosecutioncases.ProsecutionCase;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ProgressionServiceTest {
    @Mock(answer = RETURNS_DEEP_STUBS)
    private Requester requester;

    @Spy
    private UtcClock utcClock;

    @InjectMocks
    private ProgressionService progressionService;

    private static final UUID CASE_ID = randomUUID();
    private static final UUID APPLICATION_ID_1 = randomUUID();
    private static final UUID APPLICATION_ID_2 = randomUUID();

    @Test
    public void shouldRetrieveProsecutionCaseByCaseId() {
        final ProsecutionCase prosecutionCase = createProsecutionCase();

        when(requester.requestAsAdmin(any(JsonEnvelope.class), eq(ProsecutionCase.class)).payload()).thenReturn(prosecutionCase);

        final ProsecutionCase retrievedProsecutionCase = progressionService.getProsecutionCaseDetails(CASE_ID);

        assertThat(retrievedProsecutionCase.getLinkedApplicationsSummary().size(), is(2));
        assertThat(retrievedProsecutionCase.getLinkedApplicationsSummary().get(0).getApplicationId(), is(APPLICATION_ID_1));
        assertThat(retrievedProsecutionCase.getLinkedApplicationsSummary().get(1).getApplicationId(), is(APPLICATION_ID_2));
        verify(requester, times(1)).requestAsAdmin(any(JsonEnvelope.class), eq(ProsecutionCase.class));
    }

    private ProsecutionCase createProsecutionCase() {
        return ProsecutionCase.prosecutionCase().withLinkedApplicationsSummary(
                asList(LinkedApplicationsSummary.linkedApplicationsSummary().withApplicationId(APPLICATION_ID_1).build(),
                        LinkedApplicationsSummary.linkedApplicationsSummary().withApplicationId(APPLICATION_ID_2).build()
                )
        ).build();
    }
}
