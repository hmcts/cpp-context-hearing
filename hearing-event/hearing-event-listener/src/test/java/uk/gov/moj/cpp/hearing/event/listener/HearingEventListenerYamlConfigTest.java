package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import org.junit.Before;
import org.junit.Test;
import uk.gov.justice.domain.annotation.Event;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.moj.cpp.hearing.domain.event.ApplicantCounselChangeIgnored;
import uk.gov.moj.cpp.hearing.domain.event.BookProvisionalHearingSlots;
import uk.gov.moj.cpp.hearing.domain.event.CaseDefendantDetailsWithHearings;
import uk.gov.moj.cpp.hearing.domain.event.CaseDefendantsUpdated;
import uk.gov.moj.cpp.hearing.domain.event.CaseMarkersEnrichedWithAssociatedHearings;
import uk.gov.moj.cpp.hearing.domain.event.CompanyRepresentativeChangeIgnored;
import uk.gov.moj.cpp.hearing.domain.event.DefenceCounselChangeIgnored;
import uk.gov.moj.cpp.hearing.domain.event.DefenceWitnessAdded;
import uk.gov.moj.cpp.hearing.domain.event.DefendantCaseWithdrawnOrDismissed;
import uk.gov.moj.cpp.hearing.domain.event.DefendantLegalAidStatusUpdated;
import uk.gov.moj.cpp.hearing.domain.event.DefendantOffenceResultsUpdated;
import uk.gov.moj.cpp.hearing.domain.event.EnrichUpdatePleaWithAssociatedHearings;
import uk.gov.moj.cpp.hearing.domain.event.EnrichUpdateVerdictWithAssociatedHearings;
import uk.gov.moj.cpp.hearing.domain.event.FoundHearingsForDeleteOffence;
import uk.gov.moj.cpp.hearing.domain.event.FoundHearingsForEditOffence;
import uk.gov.moj.cpp.hearing.domain.event.FoundHearingsForNewOffence;
import uk.gov.moj.cpp.hearing.domain.event.FoundPleaForHearingToInherit;
import uk.gov.moj.cpp.hearing.domain.event.FoundVerdictForHearingToInherit;
import uk.gov.moj.cpp.hearing.domain.event.HearingAdjourned;
import uk.gov.moj.cpp.hearing.domain.event.HearingChangeIgnored;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventIgnored;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiateIgnored;
import uk.gov.moj.cpp.hearing.domain.event.HearingMarkedAsDuplicateForCase;
import uk.gov.moj.cpp.hearing.domain.event.HearingMarkedAsDuplicateForDefendant;
import uk.gov.moj.cpp.hearing.domain.event.HearingMarkedAsDuplicateForOffence;
import uk.gov.moj.cpp.hearing.domain.event.HearingVerdictUpdated;
import uk.gov.moj.cpp.hearing.domain.event.MasterDefendantIdAdded;
import uk.gov.moj.cpp.hearing.domain.event.OutstandingFinesRequested;
import uk.gov.moj.cpp.hearing.domain.event.InterpreterIntermediaryChangeIgnored;
import uk.gov.moj.cpp.hearing.domain.event.MagsCourtHearingRecorded;
import uk.gov.moj.cpp.hearing.domain.event.NowsVariantsSavedEvent;
import uk.gov.moj.cpp.hearing.domain.event.OffencePleaUpdated;
import uk.gov.moj.cpp.hearing.domain.event.OffenceVerdictUpdated;
import uk.gov.moj.cpp.hearing.domain.event.OutstandingFinesQueried;
import uk.gov.moj.cpp.hearing.domain.event.ProsecutionCounselChangeIgnored;
import uk.gov.moj.cpp.hearing.domain.event.RegisteredHearingAgainstCase;
import uk.gov.moj.cpp.hearing.domain.event.RegisteredHearingAgainstDefendant;
import uk.gov.moj.cpp.hearing.domain.event.RegisteredHearingAgainstOffence;
import uk.gov.moj.cpp.hearing.domain.event.RespondentCounselChangeIgnored;
import uk.gov.moj.cpp.hearing.domain.event.SendingSheetCompletedPreviouslyRecorded;
import uk.gov.moj.cpp.hearing.domain.event.SendingSheetCompletedRecorded;
import uk.gov.moj.cpp.hearing.domain.event.TargetRemoved;
import uk.gov.moj.cpp.hearing.domain.event.result.MultipleDraftResultsSaved;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultLinesStatusUpdated;
import uk.gov.moj.cpp.hearing.domain.event.result.SaveDraftResultFailed;
import uk.gov.moj.cpp.hearing.event.listener.util.SubscriptionsDescriptorLoader;
import uk.gov.moj.cpp.hearing.nows.events.EnforcementError;
import uk.gov.moj.cpp.hearing.nows.events.NowsRequested;
import uk.gov.moj.cpp.hearing.nows.events.PendingNowsRequested;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HearingEventListenerYamlConfigTest {

    private static final Path PATH_TO_YAML = Paths.get("src/yaml/subscriptions-descriptor.yaml");

    private final List<String> handlerNamesToIgnore = asList(
            FoundVerdictForHearingToInherit.class.getAnnotation(Event.class).value(),
            FoundPleaForHearingToInherit.class.getAnnotation(Event.class).value(),
            OffencePleaUpdated.class.getAnnotation(Event.class).value(),
            OffenceVerdictUpdated.class.getAnnotation(Event.class).value(),
            DefenceWitnessAdded.class.getAnnotation(Event.class).value(),
            HearingEventIgnored.class.getAnnotation(Event.class).value(),
            HearingVerdictUpdated.class.getAnnotation(Event.class).value(),
            SendingSheetCompletedRecorded.class.getAnnotation(Event.class).value(),
            SendingSheetCompletedPreviouslyRecorded.class.getAnnotation(Event.class).value(),
            MagsCourtHearingRecorded.class.getAnnotation(Event.class).value(),
            CaseDefendantDetailsWithHearings.class.getAnnotation(Event.class).value(),
            RegisteredHearingAgainstDefendant.class.getAnnotation(Event.class).value(),
            FoundHearingsForNewOffence.class.getAnnotation(Event.class).value(),
            FoundHearingsForEditOffence.class.getAnnotation(Event.class).value(),
            FoundHearingsForDeleteOffence.class.getAnnotation(Event.class).value(),
            RegisteredHearingAgainstOffence.class.getAnnotation(Event.class).value(),
            RegisteredHearingAgainstCase.class.getAnnotation(Event.class).value(),
            NowsVariantsSavedEvent.class.getAnnotation(Event.class).value(),
            HearingAdjourned.class.getAnnotation(Event.class).value(),
            ProsecutionCounselChangeIgnored.class.getAnnotation(Event.class).value(),
            DefenceCounselChangeIgnored.class.getAnnotation(Event.class).value(),
            ResultLinesStatusUpdated.class.getAnnotation(Event.class).value(),
            EnrichUpdatePleaWithAssociatedHearings.class.getAnnotation(Event.class).value(),
            EnrichUpdateVerdictWithAssociatedHearings.class.getAnnotation(Event.class).value(),
            EnforcementError.class.getAnnotation(Event.class).value(),
            RespondentCounselChangeIgnored.class.getAnnotation(Event.class).value(),
            ApplicantCounselChangeIgnored.class.getAnnotation(Event.class).value(),
            HearingInitiateIgnored.class.getAnnotation(Event.class).value(),
            HearingChangeIgnored.class.getAnnotation(Event.class).value(),
            InterpreterIntermediaryChangeIgnored.class.getAnnotation(Event.class).value(),
            CompanyRepresentativeChangeIgnored.class.getAnnotation(Event.class).value(),
            DefendantOffenceResultsUpdated.class.getAnnotation(Event.class).value(),
            DefendantCaseWithdrawnOrDismissed.class.getAnnotation(Event.class).value(),
            CaseMarkersEnrichedWithAssociatedHearings.class.getAnnotation(Event.class).value(),
            DefendantLegalAidStatusUpdated.class.getAnnotation(Event.class).value(),
            CaseDefendantsUpdated.class.getAnnotation(Event.class).value(),
            CaseMarkersEnrichedWithAssociatedHearings.class.getAnnotation(Event.class).value(),
            OutstandingFinesQueried.class.getAnnotation(Event.class).value(),
            OutstandingFinesRequested.class.getAnnotation(Event.class).value(),
            BookProvisionalHearingSlots.class.getAnnotation(Event.class).value(),
            SaveDraftResultFailed.class.getAnnotation(Event.class).value(),
            MasterDefendantIdAdded.class.getAnnotation(Event.class).value(),
            HearingMarkedAsDuplicateForCase.class.getAnnotation(Event.class).value(),
            HearingMarkedAsDuplicateForDefendant.class.getAnnotation(Event.class).value(),
            HearingMarkedAsDuplicateForOffence.class.getAnnotation(Event.class).value(),
            PendingNowsRequested.class.getAnnotation(Event.class).value(),
            NowsRequested.class.getAnnotation(Event.class).value(),
            MultipleDraftResultsSaved.class.getAnnotation(Event.class).value()

    );

    private final Map<String, String> handlerNames = new HashMap<>();
    private List<String> yamlEventNames;

    @Before
    public void setup() throws MalformedURLException {

        handlerNames.putAll(getMethodsToHandlerNamesMapFor(HearingEventListener.class,
                InitiateHearingEventListener.class,
                PleaUpdateEventListener.class,
                VerdictUpdateEventListener.class,
                HearingLogEventListener.class,
                ProsecutionCounselEventListener.class,
                NowsGeneratedEventListener.class,
                CaseDefendantDetailsUpdatedEventListener.class,
                UpdateOffencesForDefendantEventListener.class,
                ChangeHearingDetailEventListener.class,
                HearingCaseNoteSavedEventListener.class,
                SubscriptionsUploadEventListener.class,
                DefendantAttendanceEventListener.class,
                DefenceCounselEventListener.class,
                ApplicationResponseSavedEventListener.class,
                RespondentCounselEventListener.class,
                ApplicantCounselEventListener.class,
                AddDefendantEventListener.class,
                CaseEjectedEventListener.class,
                CourtApplicationEjectedEventListener.class,
                InterpreterIntermediaryEventListener.class,
                CompanyRepresentativeEventListener.class,
                DefendantLegalAidStatusUpdateEventListener.class,
                CaseDefendantsUpdateListener.class,
                PublishCourtListEventListener.class,
                CaseMarkerEventListener.class,
                SessionTimeEventListener.class,
                HearingVacatedTrialDetailChangeEventListener.class,
                TargetRemoved.class,
                HearingDaysWithoutCourtCenterCorrectedEventListener.class,
                ApprovalRequestedEventListener.class,
                ValidateResultAmendmentsRequestedEventListener.class,
                CpsProsecutorUpdatedEventListener.class));

        yamlEventNames = new SubscriptionsDescriptorLoader(PATH_TO_YAML).eventNames();
    }

    @Test
    public void testActionNameAndHandleNameAreSame() {
        assertThat(handlerNames.values(), containsInAnyOrder(yamlEventNames.toArray()));
    }

    @Test
    public void testEventsHandledProperly() {
        final List<String> eventHandlerNames = new FastClasspathScanner(
                "uk.gov.moj.cpp.hearing.domain.event",
                "uk.gov.moj.cpp.hearing.nows.events",
                "uk.gov.justice.hearing.courts",
                "uk.gov.moj.cpp.hearing.subscription.events",
                "uk.gov.moj.cpp.hearing.publishing.events")

                .scan().getNamesOfClassesWithAnnotation(Event.class)
                .stream().map(className -> {
                    try {
                        return Class.forName(className).getAnnotation(Event.class).value();
                    } catch (final ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(toList());
        eventHandlerNames.removeAll(this.handlerNamesToIgnore);
        assertThat(yamlEventNames, containsInAnyOrder(eventHandlerNames.toArray()));
    }

    private Map<String, String> getMethodsToHandlerNamesMapFor(final Class<?>... commandApiClasses) {
        final Map<String, String> methodToHandlerNamesMap = new HashMap<>();
        for (final Class<?> commandApiClass : commandApiClasses) {
            for (final Method method : commandApiClass.getMethods()) {
                final Handles handles = method.getAnnotation(Handles.class);
                if (handles != null) {
                    methodToHandlerNamesMap.put(method.getName(), handles.value());
                }
            }
        }
        return methodToHandlerNamesMap;
    }

}
