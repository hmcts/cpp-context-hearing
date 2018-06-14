package uk.gov.moj.cpp.hearing.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.result.CourtClerk;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.GenerateNowsCommand;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.NowTypes;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Nows;
import uk.gov.moj.cpp.hearing.message.shareResults.Address;
import uk.gov.moj.cpp.hearing.message.shareResults.Attendee;
import uk.gov.moj.cpp.hearing.message.shareResults.Case;
import uk.gov.moj.cpp.hearing.message.shareResults.CourtCentre;
import uk.gov.moj.cpp.hearing.message.shareResults.DefenceAdvocate;
import uk.gov.moj.cpp.hearing.message.shareResults.Defendant;
import uk.gov.moj.cpp.hearing.message.shareResults.Hearing;
import uk.gov.moj.cpp.hearing.message.shareResults.Interpreter;
import uk.gov.moj.cpp.hearing.message.shareResults.Offence;
import uk.gov.moj.cpp.hearing.message.shareResults.Person;
import uk.gov.moj.cpp.hearing.message.shareResults.Plea;
import uk.gov.moj.cpp.hearing.message.shareResults.Prompt;
import uk.gov.moj.cpp.hearing.message.shareResults.ProsecutionAdvocate;
import uk.gov.moj.cpp.hearing.message.shareResults.ShareResultsMessage;
import uk.gov.moj.cpp.hearing.message.shareResults.SharedResultLine;
import uk.gov.moj.cpp.hearing.message.shareResults.Verdict;

import javax.inject.Inject;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

@SuppressWarnings({"squid:S1188", "squid:S2221"})
@ServiceComponent(EVENT_PROCESSOR)
public class PublishResultsEventProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(PublishResultsEventProcessor.class.getName());

    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    private final Enveloper enveloper;

    private final Sender sender;

    private NowsDataProcessor nowsDataProcessor;

    @Inject
    public PublishResultsEventProcessor(final Enveloper enveloper, final Sender sender, final JsonObjectToObjectConverter jsonObjectToObjectConverter,
                                        final ObjectToJsonObjectConverter objectToJsonObjectConverter, final NowsDataProcessor nowsDataProcessor) {
        this.enveloper = enveloper;
        this.sender = sender;
        this.jsonObjectToObjectConverter = jsonObjectToObjectConverter;
        this.objectToJsonObjectConverter = objectToJsonObjectConverter;
        this.nowsDataProcessor = nowsDataProcessor;
    }

    @Handles("hearing.results-shared")
    public void resultsShared(final JsonEnvelope event) {
        LOGGER.debug("hearing.results-shared event received {}", event.payloadAsJsonObject());

        final ResultsShared input = this.jsonObjectToObjectConverter
                .convert(event.payloadAsJsonObject(), ResultsShared.class);

        nowsDataProcessor.setContext(event);

        try {
            final List<Nows> nows = nowsDataProcessor.createNows(input);

            if (!nows.isEmpty()) {
                GenerateNowsCommand generateNowsCommand = new GenerateNowsCommand()
                        .setHearing(
                                nowsDataProcessor.translateReferenceData(input)
                                        .setNows(nows)
                                        .setNowTypes(
                                                nowsDataProcessor.findNowDefinitions(input.getCompletedResultLines())
                                                        .stream()
                                                        .map(resultDefinition -> NowTypes.nowTypes()
                                                                .setId(resultDefinition.getId())
                                                                .setDescription(resultDefinition.getName())
                                                                .setJurisdiction(resultDefinition.getJurisdiction())
                                                                .setPriority(ofNullable(resultDefinition.getUrgentTimeLimitInMinutes()).map(Object::toString).orElse(null))
                                                                .setRank(resultDefinition.getRank())
                                                                .setStaticText(resultDefinition.getText())
                                                                .setStaticTextWelsh(resultDefinition.getWelshText())
                                                                .setTemplateName(resultDefinition.getTemplateName()))
                                                        .collect(toList())
                                        )

                        );
                this.sender.send(this.enveloper.withMetadataFrom(event, "hearing.command.generate-nows.v2")
                        .apply(this.objectToJsonObjectConverter.convert(generateNowsCommand)));
            }
        } catch (Exception e) {
            LOGGER.error("NOWS processing generated exception", e);
        }

        ShareResultsMessage shareResultsMessage = ShareResultsMessage.shareResultsMessage()
                .setHearing(Hearing.hearing()
                        .setId(input.getHearingId())
                        .setHearingType(input.getHearing().getType())
                        .setCourtCentre(mapCourtCentre(input))
                        .setAttendees(mapAttendees(input))
                        .setDefendants(mapDefendants(input))
                        .setSharedResultLines(mapSharedResultsLines(input))
                        .setStartDateTime(input.getHearing().getHearingDays().get(0))
                )
                .setSharedTime(ZonedDateTime.now());

        this.sender.send(this.enveloper.withMetadataFrom(event, "public.hearing.resulted")
                .apply(this.objectToJsonObjectConverter.convert(shareResultsMessage)));
    }

    private static CourtCentre mapCourtCentre(ResultsShared input) {
        return CourtCentre.courtCentre()
                .setCourtCentreId(input.getHearing().getCourtCentreId())
                .setCourtCentreName(input.getHearing().getCourtCentreName())
                .setCourtRoomId(input.getHearing().getCourtRoomId())
                .setCourtRoomName(input.getHearing().getCourtRoomName());
    }

    private static List<Attendee> mapAttendees(ResultsShared input) {

        final List<Attendee> attendees = new ArrayList<>();

        final CourtClerk courtClerk = input.getCourtClerk();

        attendees.add(Attendee.attendee()
                .setPersonId(courtClerk.getId())
                .setFirstName(courtClerk.getFirstName())
                .setLastName(courtClerk.getLastName())
                .setTitle("")
                .setType("COURTCLERK"));

        attendees.add(Attendee.attendee()
                .setPersonId(input.getHearing().getJudge().getId())
                .setFirstName(input.getHearing().getJudge().getFirstName())
                .setLastName(input.getHearing().getJudge().getLastName())
                .setTitle(input.getHearing().getJudge().getTitle())
                .setType("JUDGE"));

        attendees.addAll(
                input.getDefenceCounsels().values().stream()
                        .map(defenceCounselUpsert -> DefenceAdvocate.defenceAdvocate()
                                .setFirstName(defenceCounselUpsert.getFirstName())
                                .setLastName(defenceCounselUpsert.getLastName())
                                .setTitle(defenceCounselUpsert.getTitle())
                                .setStatus(defenceCounselUpsert.getStatus())
                                .setType("DEFENCEADVOCATE")
                                .setDefendantIds(defenceCounselUpsert.getDefendantIds())
                        )
                        .collect(toList())
        );

        attendees.addAll(
                input.getProsecutionCounsels().values().stream()
                        .map(prosecutionCounselUpsert -> ProsecutionAdvocate.prosecutionAdvocate()
                                .setPersonId(prosecutionCounselUpsert.getPersonId())
                                //NOTYET - which cases do the prosecution counsellors handle?
                                .setCaseIds(input.getCases().stream().map(c -> c.getCaseId()).collect(toList()))
                                .setFirstName(prosecutionCounselUpsert.getFirstName())
                                .setLastName(prosecutionCounselUpsert.getLastName())
                                .setTitle(prosecutionCounselUpsert.getTitle())
                                .setStatus(prosecutionCounselUpsert.getStatus())
                                .setType("PROSECUTIONADVOCATE")
                        )
                        .collect(toList())
        );


        return attendees;
    }

    private static List<Defendant> mapDefendants(ResultsShared input) {
        return input.getHearing().getDefendants().stream()
                .map(defendant -> Defendant.defendant()
                        .setId(defendant.getId())
                        .setPerson(Person.person()
                                .setId(defendant.getPersonId())
                                .setFirstName(defendant.getFirstName())
                                .setLastName(defendant.getLastName())
                                .setAddress(Address.address()
                                        .setAddress1(defendant.getAddress().getAddress1())
                                        .setAddress2(defendant.getAddress().getAddress2())
                                        .setAddress3(defendant.getAddress().getAddress3())
                                        .setAddress4(defendant.getAddress().getAddress4())
                                        .setPostCode(defendant.getAddress().getPostCode())
                                )
                                .setDateOfBirth(defendant.getDateOfBirth())
                                .setGender(defendant.getGender())
                                .setNationality(defendant.getNationality())
                                .setHomeTelephone("")
                                .setWorkTelephone("")
                                .setMobile("")
                                .setFax("")
                                .setEmail("")
                        )
                        .setDefenceOrganisation(defendant.getDefenceOrganisation())
                        .setInterpreter(Interpreter.interpreter()
                                .setLanguage(defendant.getInterpreter().getLanguage())
                        )
                        .setCases(mapCases(input, defendant))
                )
                .collect(Collectors.toList());
    }

    private static List<Case> mapCases(ResultsShared input, uk.gov.moj.cpp.hearing.command.initiate.Defendant defendant) {
        return defendant.getDefendantCases().stream()
                .map(defendantCase -> Case.legalCase()
                        .setId(defendantCase.getCaseId())
                        .setUrn(input.getCases().stream()
                                .filter(c -> c.getCaseId().equals(defendantCase.getCaseId()))
                                .map(uk.gov.moj.cpp.hearing.command.initiate.Case::getUrn)
                                .findFirst()
                                .orElse(null))
                        .setBailStatus(defendantCase.getBailStatus())
                        .setCustodyTimeLimitDate(defendantCase.getCustodyTimeLimitDate())
                        .setOffences(mapOffences(input, defendant)))
                .collect(Collectors.toList());
    }

    private static List<Offence> mapOffences(ResultsShared input, uk.gov.moj.cpp.hearing.command.initiate.Defendant defendant) {
        return defendant.getOffences().stream()
                .map(o -> Offence.offence()
                        .setId(o.getId())
                        .setCode(o.getOffenceCode())
                        .setConvictionDate(o.getConvictionDate())
                        .setStartDate(o.getStartDate())
                        .setEndDate(o.getEndDate())
                        .setWording(o.getWording())
                        .setPlea(
                                input.getPleas().values().stream()
                                        .filter(p -> p.getOffenceId().equals(o.getId()))
                                        .map(p -> Plea.plea()
                                                .setId(p.getOffenceId())
                                                .setValue(p.getValue())
                                                .setDate(p.getPleaDate())
                                                .setEnteredHearingId(p.getOriginHearingId()))
                                        .findFirst()
                                        .orElse(null)
                        )
                        .setVerdict(
                                input.getVerdicts().values().stream()
                                        .filter(v -> v.getOffenceId().equals(o.getId()))
                                        .map(v -> Verdict.verdict()
                                                .setVerdictCategory(v.getCategory())
                                                .setEnteredHearingId(v.getHearingId())
                                                .setNumberOfJurors(v.getNumberOfJurors())
                                                .setNumberOfSplitJurors(String.format("%s-%s",
                                                        v.getNumberOfJurors() - v.getNumberOfSplitJurors(),
                                                        v.getNumberOfSplitJurors()
                                                ))
                                                // guilty of lesser offence.
                                                .setUnanimous(v.getUnanimous())
                                                .setVerdictDate(v.getVerdictDate())
                                                .setVerdictDescription(v.getDescription())
                                        )
                                        .findFirst()
                                        .orElse(null)
                        )
                )
                .collect(Collectors.toList());
    }

    private List<SharedResultLine> mapSharedResultsLines(ResultsShared input) {
        return input.getCompletedResultLines().stream()
                .map(rl -> SharedResultLine.sharedResultLine()
                        .setId(rl.getId())
                        .setLastSharedResultId(rl.getLastSharedResultId())
                        .setCaseId(rl.getCaseId())
                        .setDefendantId(rl.getDefendantId())
                        .setOffenceId(rl.getOffenceId())
                        .setLabel(rl.getResultLabel())
                        .setLevel(rl.getLevel().name())
                        .setPrompts(rl.getPrompts().stream()
                                .map(p -> Prompt.prompt()
                                        .setId(p.getId())
                                        .setLabel(p.getLabel())
                                        .setValue(p.getValue()))
                                .collect(Collectors.toList())
                        ))
                .collect(Collectors.toList());
    }
}
