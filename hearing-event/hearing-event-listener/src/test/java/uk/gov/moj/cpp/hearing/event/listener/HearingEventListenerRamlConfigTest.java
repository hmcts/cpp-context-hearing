package uk.gov.moj.cpp.hearing.event.listener;


import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.moj.cpp.hearing.domain.event.ApplicantCounselChangeIgnored;
import uk.gov.moj.cpp.hearing.domain.event.CaseDefendantDetailsWithHearings;
import uk.gov.moj.cpp.hearing.domain.event.CompanyRepresentativeChangeIgnored;

import uk.gov.moj.cpp.hearing.domain.event.DefenceCounselChangeIgnored;
import uk.gov.moj.cpp.hearing.domain.event.DefenceWitnessAdded;
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
import uk.gov.moj.cpp.hearing.domain.event.HearingVerdictUpdated;
import uk.gov.moj.cpp.hearing.domain.event.InterpreterIntermediaryChangeIgnored;
import uk.gov.moj.cpp.hearing.domain.event.MagsCourtHearingRecorded;
import uk.gov.moj.cpp.hearing.domain.event.NowsVariantsSavedEvent;
import uk.gov.moj.cpp.hearing.domain.event.OffencePleaUpdated;
import uk.gov.moj.cpp.hearing.domain.event.OffenceVerdictUpdated;
import uk.gov.moj.cpp.hearing.domain.event.ProsecutionCounselChangeIgnored;
import uk.gov.moj.cpp.hearing.domain.event.RegisteredHearingAgainstApplication;
import uk.gov.moj.cpp.hearing.domain.event.RegisteredHearingAgainstCase;
import uk.gov.moj.cpp.hearing.domain.event.RegisteredHearingAgainstDefendant;
import uk.gov.moj.cpp.hearing.domain.event.RegisteredHearingAgainstOffence;
import uk.gov.moj.cpp.hearing.domain.event.RespondentCounselChangeIgnored;
import uk.gov.moj.cpp.hearing.domain.event.SendingSheetCompletedPreviouslyRecorded;
import uk.gov.moj.cpp.hearing.domain.event.SendingSheetCompletedRecorded;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultLinesStatusUpdated;
import uk.gov.moj.cpp.hearing.nows.events.EnforcementError;
import uk.gov.moj.cpp.hearing.nows.events.NowsRequested;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

public class HearingEventListenerRamlConfigTest {
    private static final String PATH_TO_RAML = "src/raml/hearing-event-listener.messaging.raml";
    private static final String COMMAND_NAME = "hearing";
    private static final String CONTENT_TYPE_PREFIX = "application/vnd.";
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
            RegisteredHearingAgainstApplication.class.getAnnotation(Event.class).value(),
            NowsVariantsSavedEvent.class.getAnnotation(Event.class).value(),
            HearingAdjourned.class.getAnnotation(Event.class).value(),
            ProsecutionCounselChangeIgnored.class.getAnnotation(Event.class).value(),
            DefenceCounselChangeIgnored.class.getAnnotation(Event.class).value(),
            ResultLinesStatusUpdated.class.getAnnotation(Event.class).value(),
            EnrichUpdatePleaWithAssociatedHearings.class.getAnnotation(Event.class).value(),
            EnrichUpdateVerdictWithAssociatedHearings.class.getAnnotation(Event.class).value(),
            NowsRequested.class.getAnnotation(Event.class).value(),
            EnforcementError.class.getAnnotation(Event.class).value(),
            RespondentCounselChangeIgnored.class.getAnnotation(Event.class).value(),
            ApplicantCounselChangeIgnored.class.getAnnotation(Event.class).value(),
            HearingInitiateIgnored.class.getAnnotation(Event.class).value(),
            HearingChangeIgnored.class.getAnnotation(Event.class).value(),
            InterpreterIntermediaryChangeIgnored.class.getAnnotation(Event.class).value(),
            CompanyRepresentativeChangeIgnored.class.getAnnotation(Event.class).value()
    );

    private Map<String, String> handlerNames = new HashMap<>();
    private List<String> ramlActionNames;

    @Before
    public void setup() throws IOException {
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
                StagingEnforcementEventListener.class,
                ApplicationResponseSavedEventListener.class,
                RespondentCounselEventListener.class,
                ApplicantCounselEventListener.class,
                AddDefendantEventListener.class,
                InterpreterIntermediaryEventListener.class,
                CompanyRepresentativeEventListener.class));

        final List<String> allLines = FileUtils.readLines(new File(PATH_TO_RAML));

        this.ramlActionNames = allLines.stream()
                .filter(action -> !action.isEmpty())
                .filter(line -> line.contains(CONTENT_TYPE_PREFIX) && line.contains(COMMAND_NAME))
                .map(line -> line.replaceAll("(application/vnd\\.)|(\\+json:)", "").trim())
                .collect(toList());
    }

    @Test
    public void testActionNameAndHandleNameAreSame() {
        assertThat(handlerNames.values(), containsInAnyOrder(this.ramlActionNames.toArray()));
    }

    @Test
    public void testEventsHandledProperly() {
        List<String> eventHandlerNames = new FastClasspathScanner(
                "uk.gov.moj.cpp.hearing.domain.event",
                "uk.gov.moj.cpp.hearing.nows.events",
                "uk.gov.justice.hearing.courts",
                "uk.gov.moj.cpp.hearing.subscription.events")
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
        assertThat(this.ramlActionNames, containsInAnyOrder(eventHandlerNames.toArray()));
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
