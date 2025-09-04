package uk.gov.moj.cpp.hearing.event.listener;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;

import uk.gov.justice.services.messaging.Envelope;
import uk.gov.moj.cpp.hearing.domain.event.ApplicationFinalisedOnTargetUpdated;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Target;
import uk.gov.moj.cpp.hearing.repository.TargetRepository;

import java.util.UUID;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TargetUpdatedEventListenerTest {

    @InjectMocks
    private TargetUpdatedEventListener targetUpdatedEventListener;

    @Mock
    private TargetRepository targetRepository;

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldHandleApplicationFinalisedOnTargetUpdated(final boolean applicationFinalisedUpdate) {
        final ArgumentCaptor<Target> argumentCaptor = ArgumentCaptor.forClass(Target.class);
        final UUID hearingId = UUID.randomUUID();
        final UUID id = UUID.randomUUID();
        final ApplicationFinalisedOnTargetUpdated applicationFinalisedOnTargetUpdated = ApplicationFinalisedOnTargetUpdated.builder()
                .withHearingId(hearingId)
                .withId(id)
                .withApplicationFinalised(applicationFinalisedUpdate)
                .build();
        final Envelope<ApplicationFinalisedOnTargetUpdated> event = Envelope.envelopeFrom(
                metadataWithRandomUUID("hearing.events.application-finalised-on-target-updated"),
                applicationFinalisedOnTargetUpdated);
        final Target target = new Target();
        final HearingSnapshotKey hearingSnapshotKey = new HearingSnapshotKey(id, hearingId);
        when(targetRepository.findBy(hearingSnapshotKey)).thenReturn(target);

        targetUpdatedEventListener.handleApplicationFinalisedOnTargetUpdated(event);

        verify(targetRepository).save(argumentCaptor.capture());
        target.setApplicationFinalised(applicationFinalisedUpdate);
        assertThat(argumentCaptor.getValue(), is(target));
    }
}
