package uk.gov.moj.cpp.hearing.event.delegates;

import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.initiate.Defendant;
import uk.gov.moj.cpp.hearing.command.result.CompletedResultLine;
import uk.gov.moj.cpp.hearing.domain.event.VerdictUpsert;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Address;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Attendees;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Cases;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.CourtCentre;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Defendants;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.GenerateNowsCommand;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Hearing;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Interpreter;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.NowTypes;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Nows;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Offences;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Person;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Plea;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Prompts;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.SharedResultLines;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Verdict;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.NowDefinition;
import uk.gov.moj.cpp.hearing.event.service.ReferenceDataService;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@SuppressWarnings("squid:S1188")
public class GenerateNowsDelegate {

    private static final String DEFENCE_COUNSEL_ATTENDEE_TYPE = "DefenseCounsel";
    private static final String PROSECUTION_COUNSEL_ATTENDEE_TYPE = "ProsecutionCounsel";
    private static final String COURTCLERK_ATTENDEE_TYPE = "CourtClerk";

    private final Enveloper enveloper;

    private final ObjectToJsonObjectConverter objectToJsonObjectConverter;

    private final ReferenceDataService referenceDataService;

    @Inject
    public GenerateNowsDelegate(final Enveloper enveloper,
                                final ObjectToJsonObjectConverter objectToJsonObjectConverter,
                                final ReferenceDataService referenceDataService) {
        this.enveloper = enveloper;
        this.objectToJsonObjectConverter = objectToJsonObjectConverter;
        this.referenceDataService = referenceDataService;
    }

    public void generateNows(Sender sender, JsonEnvelope event, List<Nows> nows, ResultsShared resultsShared) {

        final LocalDate referenceDate = resultsShared.getHearing().getHearingDays().stream()
                .map(ZonedDateTime::toLocalDate)
                .min(Comparator.comparing(LocalDate::toEpochDay))
                .orElse(null);

        final GenerateNowsCommand generateNowsCommand = new GenerateNowsCommand()
                .setHearing(
                        translateReferenceData(resultsShared)
                                .setNows(nows)
                                .setNowTypes(findNowDefinitions(referenceDate, resultsShared.getCompletedResultLines())
                                        .stream()
                                        .map(resultDefinition -> NowTypes.nowTypes()
                                                .setId(resultDefinition.getId())
                                                .setStaticText("Static Text N/A")
                                                .setStaticTextWelsh("Welsh Static Text N/A")
                                                .setDescription(resultDefinition.getName())
                                                .setJurisdiction(resultDefinition.getJurisdiction())
                                                .setPriority(ofNullable(resultDefinition.getUrgentTimeLimitInMinutes()).map(Object::toString).orElse(null))
                                                .setRank(resultDefinition.getRank())
                                                .setTemplateName(resultDefinition.getTemplateName()))
                                        .collect(toList())
                                )

                );

        sender.send(this.enveloper.withMetadataFrom(event, "hearing.command.generate-nows")
                .apply(this.objectToJsonObjectConverter.convert(generateNowsCommand)));
    }

    private Hearing translateReferenceData(ResultsShared resultsShared) {

        final List<Attendees> attendees = new ArrayList<>();

        resultsShared.getDefenceCounsels().forEach((id, defenseCounsel) ->
                attendees.add(
                        Attendees.attendees()
                                .setType(DEFENCE_COUNSEL_ATTENDEE_TYPE)
                                .setFirstName(defenseCounsel.getFirstName())
                                .setLastName(defenseCounsel.getLastName())
                )

        );

        resultsShared.getProsecutionCounsels().forEach((id, prosecutionCounsel) ->
                attendees.add(
                        Attendees.attendees()
                                .setType(PROSECUTION_COUNSEL_ATTENDEE_TYPE)
                                .setFirstName(prosecutionCounsel.getFirstName())
                                .setLastName(prosecutionCounsel.getLastName())
                )
        );

        attendees.add(
                Attendees.attendees()
                        .setType(COURTCLERK_ATTENDEE_TYPE)
                        .setFirstName(resultsShared.getCourtClerk().getFirstName())
                        .setLastName(resultsShared.getCourtClerk().getLastName())
        );

        final List<SharedResultLines> sharedResultLines = resultsShared.getCompletedResultLines().stream().map(
                completedRl -> SharedResultLines.sharedResultLines()
                        .setId(completedRl.getId())
                        .setLevel(completedRl.getLevel().name())
                        .setCaseId(completedRl.getCaseId())
                        .setDefendantId(completedRl.getDefendantId())
                        .setLabel(completedRl.getResultLabel())
                        .setOffenceId(completedRl.getOffenceId())
                        .setPrompts(
                                completedRl.getPrompts().stream().map(
                                        pIn -> Prompts.prompts()
                                                .setId(pIn.getId())
                                                .setLabel(pIn.getLabel())
                                                .setValue(pIn.getValue())
                                ).collect(Collectors.toList())
                        )
        ).collect(Collectors.toList());

        final uk.gov.moj.cpp.hearing.command.initiate.Hearing hearingIn = resultsShared.getHearing();

        return Hearing.hearing()
                .setId(resultsShared.getHearing().getId())
                .setHearingDates(hearingIn.getHearingDays())
                .setCourtCentre(CourtCentre.courtCentre()
                        .setCourtCentreId(hearingIn.getCourtCentreId())
                        .setCourtCentreName(hearingIn.getCourtCentreName())
                        .setCourtRoomId(hearingIn.getCourtRoomId())
                        .setCourtRoomName(hearingIn.getCourtRoomName()))
                .setDefendants(resultsShared.getHearing().getDefendants().stream()
                        .map(defendant -> Defendants.defendants()
                                .setId(defendant.getId())
                                .setPerson(Person.person()
                                        .setId(defendant.getPersonId())
                                        .setFirstName(defendant.getFirstName())
                                        .setLastName(defendant.getLastName())
                                        .setDateOfBirth(defendant.getDateOfBirth().toString())
                                        .setNationality(defendant.getNationality())
                                        .setGender(defendant.getGender())
                                        .setAddress(Address.address()
                                                .setAddress1(defendant.getAddress().getAddress1())
                                                .setPostCode(defendant.getAddress().getPostCode())))
                                .setCases(defendant.getDefendantCases().stream()
                                        .map(caseIn -> Cases.cases()
                                                .setId(caseIn.getCaseId())
                                                .setOffences(defendant.getOffences().stream()
                                                        .filter(offence -> offence.getCaseId().equals(caseIn.getCaseId()))
                                                        .map(offIn -> Offences.offences()
                                                                .setId(offIn.getId())
                                                                .setCode(offIn.getOffenceCode())
                                                                .setStartDate(offIn.getStartDate())
                                                                .setEndDate(offIn.getEndDate())
                                                                .setConvictionDate(offIn.getConvictionDate())
                                                                .setWording(offIn.getWording())
                                                                .setPlea(ofNullable(offIn.getPlea())
                                                                        .map(p -> Plea.plea()
                                                                                .setId(p.getId())
                                                                                .setDate(p.getPleaDate())
                                                                                .setValue(p.getValue())
                                                                        )
                                                                        .orElse(null)
                                                                )
                                                                .setVerdict(resultsShared.getVerdicts().values().stream()
                                                                        .filter(v -> v.getOffenceId().equals(offIn.getId()))
                                                                        .map(v -> Verdict.verdict()
                                                                                .setTypeId(v.getVerdictTypeId())
                                                                                .setEnteredHearingId(v.getHearingId())
                                                                                .setNumberOfJurors(v.getNumberOfJurors())
                                                                                .setNumberOfSplitJurors(formatSplitJurors(v))
                                                                                .setUnanimous(v.getUnanimous())
                                                                                .setVerdictCategory(v.getCategory())
                                                                                .setVerdictDate(v.getVerdictDate())
                                                                                .setVerdictDescription(v.getDescription())
                                                                        )
                                                                        .findFirst()
                                                                        .orElse(null)
                                                                )
                                                        )
                                                        .collect(toList())
                                                ))
                                        .collect(toList()))
                                .setInterpreter(of(defendant)
                                        .map(Defendant::getInterpreter)
                                        .map(i -> Interpreter.interpreter().setLanguage(i.getLanguage()))
                                        .orElse(null))
                        )
                        .collect(toList())
                )
                .setAttendees(attendees)
                .setSharedResultLines(sharedResultLines);
    }

    private String formatSplitJurors(VerdictUpsert v) {
        return v.getNumberOfJurors() == null || v.getNumberOfSplitJurors() == null ? "" :
                String.format("%s-%s", v.getNumberOfJurors() - v.getNumberOfSplitJurors(), v.getNumberOfSplitJurors());
    }

    private Set<NowDefinition> findNowDefinitions(final LocalDate referenceDate, final List<CompletedResultLine> resultLines) {
        return resultLines.stream()
                .map(resultLine -> referenceDataService.getNowDefinitionByPrimaryResultDefinitionId(referenceDate, resultLine.getResultDefinitionId()))
                .filter(Objects::nonNull)
                .collect(toSet());
    }
}
