package uk.gov.moj.cpp.hearing.event.delegates;

import static java.util.stream.Collectors.toList;
import static uk.gov.moj.cpp.hearing.message.shareResults.Variant.variant;

import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.Variant;
import uk.gov.moj.cpp.hearing.command.result.CompletedResultLineStatus;
import uk.gov.moj.cpp.hearing.command.result.CourtClerk;
import uk.gov.moj.cpp.hearing.domain.event.VerdictUpsert;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.NowDefinition;
import uk.gov.moj.cpp.hearing.event.service.ReferenceDataService;
import uk.gov.moj.cpp.hearing.message.shareResults.Address;
import uk.gov.moj.cpp.hearing.message.shareResults.Attendee;
import uk.gov.moj.cpp.hearing.message.shareResults.Case;
import uk.gov.moj.cpp.hearing.message.shareResults.CourtCentre;
import uk.gov.moj.cpp.hearing.message.shareResults.Defendant;
import uk.gov.moj.cpp.hearing.message.shareResults.Hearing;
import uk.gov.moj.cpp.hearing.message.shareResults.Interpreter;
import uk.gov.moj.cpp.hearing.message.shareResults.Offence;
import uk.gov.moj.cpp.hearing.message.shareResults.Person;
import uk.gov.moj.cpp.hearing.message.shareResults.Plea;
import uk.gov.moj.cpp.hearing.message.shareResults.Prompt;
import uk.gov.moj.cpp.hearing.message.shareResults.ShareResultsMessage;
import uk.gov.moj.cpp.hearing.message.shareResults.SharedResultLine;
import uk.gov.moj.cpp.hearing.message.shareResults.Verdict;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

@SuppressWarnings({"squid:S1188", "squid:S1612"})
public class PublishResultsDelegate {

    private final Enveloper enveloper;

    private final ObjectToJsonObjectConverter objectToJsonObjectConverter;

    private final ReferenceDataService referenceDataService;

    @Inject
    public PublishResultsDelegate(final Enveloper enveloper, final ObjectToJsonObjectConverter objectToJsonObjectConverter,
                                  final ReferenceDataService referenceDataService) {
        this.enveloper = enveloper;
        this.objectToJsonObjectConverter = objectToJsonObjectConverter;
        this.referenceDataService = referenceDataService;
    }

    public void setContext(final JsonEnvelope context) {
        referenceDataService.setContext(context);
    }

    public void shareResults(final Sender sender, final JsonEnvelope event, final ResultsShared resultsShared, final List<Variant> newVariants) {

        final List<Variant> variants = Stream.concat(
                resultsShared.getVariantDirectory().stream().map(replaceWithInputs(newVariants)),
                newVariants.stream().filter(isNotInSet(resultsShared.getVariantDirectory()))
        ).collect(toList());

        final ShareResultsMessage shareResultsMessage = ShareResultsMessage.shareResultsMessage()
                .setHearing(Hearing.hearing()
                        .setId(resultsShared.getHearingId())
                        .setHearingType(resultsShared.getHearing().getType())
                        .setCourtCentre(mapCourtCentre(resultsShared))
                        .setAttendees(mapAttendees(resultsShared))
                        .setDefendants(mapDefendants(resultsShared))
                        .setSharedResultLines(mapSharedResultsLines(resultsShared))
                        .setHearingDates(resultsShared.getHearing().getHearingDays())
                        .setStartDateTime(resultsShared.getHearing().getHearingDays().get(0))
                )
                .setVariants(mapVariantDirectory(variants))
                .setSharedTime(resultsShared.getSharedTime());

        sender.send(this.enveloper.withMetadataFrom(event, "public.hearing.resulted")
                .apply(this.objectToJsonObjectConverter.convert(shareResultsMessage)));
    }

    private List<uk.gov.moj.cpp.hearing.message.shareResults.Variant> mapVariantDirectory(final List<Variant> updatedVariantDirectory) {
        return updatedVariantDirectory.stream()
                .map(variant -> {
                            final NowDefinition nowDefinition = referenceDataService.getNowDefinitionById(variant.getReferenceDate(), variant.getKey().getNowsTypeId());
                            return variant()
                                    .setKey(variant.getKey())
                                    .setStatus(variant.getValue().getStatus().toString())
                                    .setMaterialId(variant.getValue().getMaterialId())
                                    .setDescription(nowDefinition.getName())
                                    .setTemplateName(nowDefinition.getTemplateName());
                        }
                )
                .collect(toList());
    }

    private static CourtCentre mapCourtCentre(final ResultsShared input) {
        return CourtCentre.courtCentre()
                .setCourtCentreId(input.getHearing().getCourtCentreId())
                .setCourtCentreName(input.getHearing().getCourtCentreName())
                .setCourtRoomId(input.getHearing().getCourtRoomId())
                .setCourtRoomName(input.getHearing().getCourtRoomName());
    }

    private static List<Attendee> mapAttendees(final ResultsShared input) {

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
                        .map(defenceCounselUpsert -> Attendee.attendee()
                                .setPersonId(defenceCounselUpsert.getPersonId())
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
                        .map(prosecutionCounselUpsert -> Attendee.attendee()
                                .setPersonId(prosecutionCounselUpsert.getPersonId())
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

    private static List<Defendant> mapDefendants(final ResultsShared input) {
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

    private static List<Case> mapCases(final ResultsShared input, final uk.gov.moj.cpp.hearing.command.initiate.Defendant defendant) {
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

    private static List<Offence> mapOffences(final ResultsShared input, final uk.gov.moj.cpp.hearing.command.initiate.Defendant defendant) {
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
                                                .setNumberOfSplitJurors(formatNumberOfSplitJurors(v))
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

    private static String formatNumberOfSplitJurors(final VerdictUpsert v) {
        return v.getNumberOfJurors() != null && v.getNumberOfSplitJurors() != null ?
                String.format("%s-%s",
                        v.getNumberOfJurors() - v.getNumberOfSplitJurors(),
                        v.getNumberOfSplitJurors()
                ) : null;
    }

    private List<SharedResultLine> mapSharedResultsLines(final ResultsShared input) {
        return input.getCompletedResultLines().stream()
                .map(rl -> SharedResultLine.sharedResultLine()
                        .setId(rl.getId())
                        .setLastSharedDateTime(getLastSharedResultDateTime(input.getSharedTime(), input.getCompletedResultLinesStatus().get(rl.getId())))
                        .setOrderedDate(rl.getOrderedDate())
                        .setCourtClerk(getCourtClerkDetails(input.getCourtClerk(), input.getCompletedResultLinesStatus().get(rl.getId())))
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

    private ZonedDateTime getLastSharedResultDateTime(final ZonedDateTime sharedTime, final CompletedResultLineStatus completedResultLineStatus) {
        if (completedResultLineStatus.getLastSharedDateTime() == null) {
            return sharedTime;
        } else {
            return completedResultLineStatus.getLastSharedDateTime();
        }
    }

    private CourtClerk getCourtClerkDetails(final CourtClerk courtClerk, final CompletedResultLineStatus completedResultLineStatus) {
        if (completedResultLineStatus.getCourtClerk() == null) {
            return courtClerk;
        } else {
            return completedResultLineStatus.getCourtClerk();
        }
    }

    private static <T> Function<T, T> replaceWithInputs(final Collection<T> input) {
        return v -> input.stream().filter(p -> p.equals(v)).findFirst().orElse(v);
    }

    private static <T> Predicate<T> isNotInSet(final Collection<T> input) {
        return v -> input.stream().noneMatch(p -> p.equals(v));
    }
}
