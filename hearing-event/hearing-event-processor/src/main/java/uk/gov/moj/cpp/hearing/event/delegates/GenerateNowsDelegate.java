package uk.gov.moj.cpp.hearing.event.delegates;

import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.initiate.Case;
import uk.gov.moj.cpp.hearing.command.initiate.Defendant;
import uk.gov.moj.cpp.hearing.command.result.CompletedResultLine;
import uk.gov.moj.cpp.hearing.command.result.CompletedResultLineStatus;
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
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.ResultDefinitions;
import uk.gov.moj.cpp.hearing.event.service.ReferenceDataService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

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

    public void generateNows(final Sender sender, final JsonEnvelope event, final List<Nows> nows, final ResultsShared resultsShared) {

        final GenerateNowsCommand generateNowsCommand = new GenerateNowsCommand()
                .setHearing(
                        translateReferenceData(resultsShared)
                                .setNows(nows)
                                .setNowTypes(findNowDefinitions(
                                        resultsShared.getCompletedResultLines())
                                        .stream()
                                        .map(nowDefinition -> {

                                            String nowText = Stream.concat(
                                                    Stream.of(nowDefinition.getText()),
                                                    nowDefinition.getResultDefinitions().stream()
                                                            .map(ResultDefinitions::getText)
                                            )
                                                    .filter(Objects::nonNull)
                                                    .filter(s -> !s.isEmpty())
                                                    .collect(Collectors.joining("\n"));

                                            String welshText = Stream.concat(
                                                    Stream.of(nowDefinition.getWelshText()),
                                                    nowDefinition.getResultDefinitions().stream()
                                                            .map(ResultDefinitions::getWelshText)
                                            )
                                                    .filter(Objects::nonNull)
                                                    .filter(s -> !s.isEmpty())
                                                    .collect(Collectors.joining("\n"));


                                            return NowTypes.nowTypes()
                                                    .setId(nowDefinition.getId())
                                                    .setStaticText(nowText)
                                                    .setStaticTextWelsh(welshText)
                                                    .setDescription(nowDefinition.getName())
                                                    .setJurisdiction(nowDefinition.getJurisdiction())
                                                    .setPriority(ofNullable(nowDefinition.getUrgentTimeLimitInMinutes()).map(Object::toString).orElse(null))
                                                    .setRank(nowDefinition.getRank())
                                                    .setTemplateName(nowDefinition.getTemplateName());
                                        })
                                        .collect(toList())
                                )

                );

        sender.send(this.enveloper.withMetadataFrom(event, "hearing.command.generate-nows")
                .apply(this.objectToJsonObjectConverter.convert(generateNowsCommand)));
    }

    private Hearing translateReferenceData(final ResultsShared resultsShared) {

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

        final List<SharedResultLines> sharedResultLines = resultsShared.getCompletedResultLines().stream()
                .map(line -> SharedResultLines.sharedResultLines()
                        .setId(line.getId())
                        .setSharedDate(ofNullable(resultsShared.getCompletedResultLinesStatus().get(line.getId())).map(CompletedResultLineStatus::getLastSharedDateTime).orElse(null))
                        .setOrderedDate(line.getOrderedDate())
                        .setLevel(line.getLevel().name())
                        .setCaseId(line.getCaseId())
                        .setDefendantId(line.getDefendantId())
                        .setLabel(line.getResultLabel())
                        .setOffenceId(line.getOffenceId())
                        .setPrompts(
                                line.getPrompts().stream()
                                        .map(pIn -> Prompts.prompts()
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
                                                .setAddress2(defendant.getAddress().getAddress2())
                                                .setAddress3(defendant.getAddress().getAddress3())
                                                .setAddress4(defendant.getAddress().getAddress4())
                                                .setPostCode(defendant.getAddress().getPostCode())))
                                .setCases(defendant.getDefendantCases().stream()
                                        .map(caseIn -> Cases.cases()
                                                .setId(caseIn.getCaseId())
                                                .setUrn(resultsShared.getCases().stream()
                                                        .filter(c -> c.getCaseId().equals(caseIn.getCaseId()))
                                                        .map(Case::getUrn)
                                                        .findFirst()
                                                        .orElse(null))
                                                .setBailStatus(caseIn.getBailStatus())
                                                .setCustodyTimeLimitDate(caseIn.getCustodyTimeLimitDate())
                                                .setOffences(defendant.getOffences().stream()
                                                        .filter(offence -> offence.getCaseId().equals(caseIn.getCaseId()))
                                                        .map(offIn -> Offences.offences()
                                                                .setId(offIn.getId())
                                                                .setCode(offIn.getOffenceCode())
                                                                .setStartDate(offIn.getStartDate())
                                                                .setEndDate(offIn.getEndDate())
                                                                .setConvictionDate(offIn.getConvictionDate())
                                                                .setWording(offIn.getWording())
                                                                .setPlea(ofNullable(resultsShared.getPleas().get(offIn.getId()))
                                                                        .map(p -> Plea.plea()
                                                                                .setId(p.getOffenceId())
                                                                                .setDate(p.getPleaDate())
                                                                                .setValue(p.getValue())
                                                                                .setEnteredHearingId(p.getOriginHearingId())
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

    private String formatSplitJurors(final VerdictUpsert v) {
        return v.getNumberOfJurors() == null || v.getNumberOfSplitJurors() == null ? "" :
                String.format("%s-%s", v.getNumberOfJurors() - v.getNumberOfSplitJurors(), v.getNumberOfSplitJurors());
    }

    private Set<NowDefinition> findNowDefinitions(final List<CompletedResultLine> resultLines) {
        return resultLines.stream()
                .map(resultLine -> referenceDataService.getNowDefinitionByPrimaryResultDefinitionId(
                        resultLine.getOrderedDate(),
                        resultLine.getResultDefinitionId()))
                .filter(Objects::nonNull)
                .collect(toSet());
    }
}
