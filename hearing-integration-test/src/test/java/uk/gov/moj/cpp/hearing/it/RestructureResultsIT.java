package uk.gov.moj.cpp.hearing.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.time.LocalDate.now;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static uk.gov.moj.cpp.hearing.it.UseCases.initiateHearing;
import static uk.gov.moj.cpp.hearing.it.Utilities.EventListener;
import static uk.gov.moj.cpp.hearing.it.Utilities.listenFor;
import static uk.gov.moj.cpp.hearing.it.Utilities.makeCommand;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.givenAUserHasLoggedInAsACourtClerk;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.InitiateHearingCommandHelper;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.MapStringToTypeMatcher.convertStringTo;
import static uk.gov.moj.cpp.hearing.utils.StubNowsReferenceData.setupNowsReferenceData;

import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.JudicialResult;
import uk.gov.justice.core.courts.JudicialResultPrompt;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.result.ShareResultsCommand;
import uk.gov.moj.cpp.hearing.domain.event.result.PublicHearingResulted;
import uk.gov.moj.cpp.hearing.test.FileResourceObjectMapper;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import javax.json.JsonObject;

import org.junit.Before;
import org.junit.Test;

public class RestructureResultsIT extends AbstractIT {

    private FileResourceObjectMapper fileResourceObjectMapper;

    @Before
    public void setup() {
        fileResourceObjectMapper = new FileResourceObjectMapper();
        setupNowsReferenceData(now());
    }

    @Test
    public void shouldRestructureCO() throws IOException {


        final InitiateHearingCommandHelper hearing = shareResults("restructure/CO-UI-payload.json");

        final EventListener publicEventResulted = listenFor("public.hearing.resulted")
                .withFilter(convertStringTo(PublicHearingResulted.class, isBean(PublicHearingResulted.class)
                        .with(PublicHearingResulted::getHearing, isBean(Hearing.class)
                                .with(Hearing::getId, is(hearing.getHearingId()))
                                .with(this::getOffenceLevelResults, hasSize(1))
                                .with(this::getOffenceLevelResults, hasItem(isBean(JudicialResult.class)
                                        .with(JudicialResult::getLabel, is("Community order England / Wales"))
                                        .with(JudicialResult::getJudicialResultPrompts, hasSize(10))
                                ))
                        )
                ));

        publicEventResulted.waitFor();

    }

    @Test
    public void shouldRestructureDIRS() throws IOException {

        final InitiateHearingCommandHelper hearing = shareResults("restructure/DIRS-UI-payload.json");

        final EventListener publicEventResulted = listenFor("public.hearing.resulted")
                .withFilter(convertStringTo(PublicHearingResulted.class, isBean(PublicHearingResulted.class)
                        .with(PublicHearingResulted::getHearing, isBean(Hearing.class)
                                .with(Hearing::getId, is(hearing.getHearingId()))
                                .with(this::getDefendantLevelResults, hasSize(2))
                                .with(this::getDefendantLevelResults, hasItem(isBean(JudicialResult.class)
                                        .with(JudicialResult::getLabel, is("Special measures direction: A video recording be admitted of the evidence in chief of the interview"))))
                                .with(this::getDefendantLevelResults, hasItem(isBean(JudicialResult.class)
                                        .with(JudicialResult::getLabel, is("Special measures direction: Witness to be provided with an aid to commumication"))))
                        )
                ));

        publicEventResulted.waitFor();
        final JsonObject commandPayload = createObjectBuilder().add("resultLinesToBeUpdated", createArrayBuilder()
                .add(createObjectBuilder()
                        .add("resultLineId", "afe667a6-586b-4abc-837a-7212cdc35952")
                        .add("sharedDate", "2020-01-12"))).build();

        String log = commandPayload.toString();

        try (final Utilities.EventListener eventTopic = listenFor("hearing.event.hearing-resultline-shared-dates-updated", "hearing.event")
                .withFilter(isJson(withJsonPath("$.hearingId", is(hearing.getHearingId().toString())
                )))) {

            makeCommand(getRequestSpec(), "hearing.update-resultline-shared-dates")
                    .ofType("application/vnd.hearing.update-resultline-shared-dates+json")
                    .withPayload(log)
                    .withArgs(hearing.getHearingId())
                    .withCppUserId(USER_ID_VALUE_AS_ADMIN)
                    .executeSuccessfully();

            eventTopic.waitFor();
        }
    }

    @Test
    public void shouldRestructureSendToCCOnCB() throws IOException {


        final InitiateHearingCommandHelper hearing = shareResults("restructure/UI-payload-shortCode-SendToCCOnCB.json");

        final EventListener publicEventResulted = listenFor("public.hearing.resulted")
                .withFilter(convertStringTo(PublicHearingResulted.class, isBean(PublicHearingResulted.class)
                        .with(PublicHearingResulted::getHearing, isBean(Hearing.class)
                                .with(Hearing::getId, is(hearing.getHearingId()))
                                .with(this::getOffenceLevelResults, hasSize(1))
                                .with(this::getOffenceLevelResults, hasItem(isBean(JudicialResult.class)
                                                .with(JudicialResult::getLabel, is("Send To Crown Court On Conditional Bail"))
                                                .with(JudicialResult::getJudicialResultPrompts, hasSize(1))
                                                .with(JudicialResult::getJudicialResultPrompts, hasItem(isBean(JudicialResultPrompt.class)
                                                        .with(JudicialResultPrompt::getLabel, is("Bail condition: Assessments/Reports - participate in any assistance or treatment considered appropriate for misuse of class A drugs"))))
                                        )
                                ))
                ));

        publicEventResulted.waitFor();
    }

    private InitiateHearingCommandHelper shareResults(final String payloadLocation) throws IOException {
        final ShareResultsCommand shareResultsCommand = fileResourceObjectMapper.convertFromFile(payloadLocation, ShareResultsCommand.class);
        shareResultsCommand
                .getResultLines()
                .stream()
                .forEach(rl -> rl.setOrderedDate(now()));

        final Optional<UUID> defendantId = shareResultsCommand
                .getResultLines()
                .stream()
                .filter(rl -> null == rl.getParentResultLineIds())
                .map(rl -> rl.getDefendantId())
                .findFirst();

        final Optional<UUID> offenceId = shareResultsCommand
                .getResultLines()
                .stream()
                .filter(rl -> null == rl.getParentResultLineIds())
                .map(rl -> rl.getOffenceId())
                .findFirst();

        final InitiateHearingCommand initiateHearingCommandTemplate = standardInitiateHearingTemplate();

        initiateHearingCommandTemplate.getHearing().getProsecutionCases().get(0).getDefendants().get(0).setId(defendantId.get());
        initiateHearingCommandTemplate.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).setId(offenceId.get());

        final InitiateHearingCommandHelper hearing = h(initiateHearing(getRequestSpec(), initiateHearingCommandTemplate));

        hearing.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).setId(offenceId.get());

        hearing.getHearing().getProsecutionCases().forEach(prosecutionCase -> stubLjaDetails(hearing.getHearing().getCourtCentre(), prosecutionCase.getProsecutionCaseIdentifier().getProsecutionAuthorityId()));

        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());


        makeCommand(getRequestSpec(), "hearing.share-results")
                .ofType("application/vnd.hearing.share-results+json")
                .withArgs(hearing.getHearingId())
                .withPayload(shareResultsCommand)
                .executeSuccessfully();
        return hearing;
    }

    private List<JudicialResult> getOffenceLevelResults(final Hearing hearing) {
        return hearing
                .getProsecutionCases()
                .stream()
                .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream())
                .flatMap(defendant -> defendant.getOffences().stream())
                .flatMap(o -> ofNullable(o.getJudicialResults()).map(Collection::stream).orElseGet(Stream::empty))
                .collect(toList());
    }

    private List<JudicialResult> getDefendantLevelResults(final Hearing hearing) {
        return hearing
                .getProsecutionCases()
                .stream()
                .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream())
                .flatMap(d -> ofNullable(d.getDefendantCaseJudicialResults()).map(Collection::stream).orElseGet(Stream::empty))
                .collect(toList());
    }
}
