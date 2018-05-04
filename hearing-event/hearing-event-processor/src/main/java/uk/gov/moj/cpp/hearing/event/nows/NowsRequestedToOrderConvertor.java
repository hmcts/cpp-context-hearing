package uk.gov.moj.cpp.hearing.event.nows;

import org.apache.commons.lang3.StringUtils;
import uk.gov.moj.cpp.hearing.event.nows.domain.DefendantAddress;
import uk.gov.moj.cpp.hearing.event.nows.domain.DefendantCaseOffence;
import uk.gov.moj.cpp.hearing.event.nows.domain.NowOrderPrompt;
import uk.gov.moj.cpp.hearing.event.nows.domain.NowsOrder;
import uk.gov.moj.cpp.hearing.event.nows.domain.NowsOrderCase;
import uk.gov.moj.cpp.hearing.event.nows.domain.NowsOrderCourtCentre;
import uk.gov.moj.cpp.hearing.event.nows.domain.NowsOrderDefendant;
import uk.gov.moj.cpp.hearing.event.nows.domain.NowsOrderResult;
import uk.gov.moj.cpp.hearing.nows.events.Attendee;
import uk.gov.moj.cpp.hearing.nows.events.Case;
import uk.gov.moj.cpp.hearing.nows.events.CourtCentre;
import uk.gov.moj.cpp.hearing.nows.events.Defendant;
import uk.gov.moj.cpp.hearing.nows.events.Now;
import uk.gov.moj.cpp.hearing.nows.events.NowType;
import uk.gov.moj.cpp.hearing.nows.events.NowsRequested;
import uk.gov.moj.cpp.hearing.nows.events.Offence;
import uk.gov.moj.cpp.hearing.nows.events.Prompt;
import uk.gov.moj.cpp.hearing.nows.events.SharedResultLine;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;

public class NowsRequestedToOrderConvertor {
    private static final String COURTCLERK = "COURTCLERK";

    private NowsRequestedToOrderConvertor() {

    }

    public static List<NowsOrder> convert(NowsRequested nowsRequested) {
        List<NowsOrder> nowsOrders = new ArrayList<>();

        nowsRequested.getHearing().getNows().forEach(now -> {
            NowsOrder nowsOrder = NowsOrder.builder()
                    .withOrderName(getNowType(nowsRequested, now).getDescription())
                    .withCourtCentre(getCourtCentreInOrder(nowsRequested))
                    .withCourtClerkName(getCourtClerkName(nowsRequested))
                    .withDefendant(getNowsOrderDefendant(nowsRequested, now))
                    .withCaseUrns(getCaseUrns(nowsRequested, now))
                    .withCases(getDefendantCase(nowsRequested, now))
                    .withDefendantResults(getDefendantResult(nowsRequested, now))
                    .withStaticText(getNowType(nowsRequested, now).getStaticText())
                    .withStaticTextWelsh(getNowType(nowsRequested, now).getStaticTextWelsh())
                    .build();
            nowsOrders.add(nowsOrder);
        });

        return nowsOrders;
    }

    private static List<NowsOrderResult> getDefendantResult(NowsRequested nowsRequested, Now now) {
        Optional<Defendant> defendant = getNowsDefendant(nowsRequested, now);
        if(defendant.isPresent()) {
            List<SharedResultLine> sharedResultLines = nowsRequested.getHearing().getSharedResultLines();
            Stream<SharedResultLine> sharedResultLineList = sharedResultLines.stream().filter(s -> s.getDefendantId().equals(defendant.get().getId()));

            List<NowsOrderResult> nowsOrderResults = new ArrayList<>();
            sharedResultLineList.forEach(sharedResultLine ->
                    nowsOrderResults.add(NowsOrderResult.builder()
                            .withLabel(sharedResultLine.getLabel())
                            .withPrompts(getNowOrderPrompt(sharedResultLine.getPrompts()))
                            .build())
            );
            return nowsOrderResults;
        }
        return new ArrayList<>();
    }

    private static List<NowsOrderCase> getDefendantCase(NowsRequested nowsRequested, Now now) {
        List<Case> cases = new ArrayList<>();
        nowsRequested.getHearing().getDefendants().stream()
                .filter(def -> def.getId().equals(now.getDefendantId())).findFirst().ifPresent(defendant -> cases.addAll(defendant.getCases()));
        List<NowsOrderCase> nowsOrderCases = new ArrayList<>();
        cases.forEach(aCase ->
                nowsOrderCases.add(NowsOrderCase.builder().withUrn(aCase.getUrn())
                        .withDefendantCaseOffences(getDefendantCaseOffences(nowsRequested, aCase))
                        .build())
        );
        return nowsOrderCases;
    }

    private static List<DefendantCaseOffence> getDefendantCaseOffences(NowsRequested nowsRequested, Case aCase) {
        List<DefendantCaseOffence> defendantCaseOffences = new ArrayList<>();
        aCase.getOffences().forEach(offence ->
                defendantCaseOffences.add(DefendantCaseOffence.builder().withConvictionDate(offence.getConvictionDate())
                        .withStartDate(offence.getStartDate())
                        .withWording(offence.getWording())
                        .withResults(getNowOrderResult(nowsRequested, offence))
                        .build())
        );
        return defendantCaseOffences;
    }

    private static List<NowsOrderResult> getNowOrderResult(NowsRequested nowsRequested, Offence offence) {
        List<NowsOrderResult> nowsOrderResults = new ArrayList<>();
        List<SharedResultLine> sharedResultLines = nowsRequested.getHearing().getSharedResultLines();
        Stream<SharedResultLine> sharedResultLineList = sharedResultLines.stream().filter(s -> s.getOffenceId().equals(offence.getId()));

        sharedResultLineList.forEach(sharedResultLine ->
                nowsOrderResults.add(NowsOrderResult.builder()
                        .withLabel(sharedResultLine.getLabel())
                        .withPrompts(getNowOrderPrompt(sharedResultLine.getPrompts()))
                        .build())
        );
        return nowsOrderResults;
    }

    private static List<NowOrderPrompt> getNowOrderPrompt(List<Prompt> prompts) {
        return prompts.stream().map(prompt -> new NowOrderPrompt(prompt.getLabel(), prompt.getValue())).collect(Collectors.toList());
    }

    private static List<String> getCaseUrns(NowsRequested nowsRequested, Now now) {
        Optional<Defendant> defendant = getNowsDefendant(nowsRequested, now);
        if (defendant.isPresent()) {
            return defendant.get().getCases().stream().map(aCase -> aCase.getUrn()).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    private static NowsOrderDefendant getNowsOrderDefendant(NowsRequested nowsRequested, Now now) {
        Optional<Defendant> defendantNows = getNowsDefendant(nowsRequested, now);
        if (defendantNows.isPresent()) {
            Defendant defendant = defendantNows.get();
            String defendantName = format("%s %s  %s", defendant.getPerson().getTitle(), defendant.getPerson().getFirstName(), defendant.getPerson().getLastName());
            DefendantAddress defendantAddress = DefendantAddress.builder()
                    .withAddress1(defendant.getPerson().getAddress().getAddress1())
                    .withAddress2(defendant.getPerson().getAddress().getAddress2())
                    .withAddress3(defendant.getPerson().getAddress().getAddress3())
                    .withAddress4(defendant.getPerson().getAddress().getAddress4())
                    .withPostCode(defendant.getPerson().getAddress().getPostCode())
                    .build();
            return NowsOrderDefendant.builder()
                    .withName(defendantName)
                    .withDateOfBirth(defendant.getPerson().getDateOfBirth())
                    .withAddress(defendantAddress)
                    .build();
        }
        return NowsOrderDefendant.builder().build();
    }

    private static Optional<Defendant> getNowsDefendant(NowsRequested nowsRequested, Now now) {
        List<Defendant> defendants = nowsRequested.getHearing().getDefendants();
        return defendants.stream().filter(def -> def.getId().equals(now.getDefendantId())).findFirst();
    }

    private static NowType getNowType(NowsRequested nowsRequested, Now now) {
        List<NowType> nowTypes = nowsRequested.getHearing().getNowTypes();
        return nowTypes.stream().filter(nowType -> nowType.getId().equals(now.getNowsTypeId())).findFirst().orElse(new NowType());
    }

    private static String getCourtClerkName(NowsRequested nowsRequested) {
        Optional<Attendee> attendee = nowsRequested.getHearing().getAttendees().stream()
                .filter(attende -> attende.getType().equals(COURTCLERK)).findFirst();
        if (attendee.isPresent()) {
            return format("%s %s", attendee.get().getFirstName(), attendee.get().getLastName());
        }
        return StringUtils.EMPTY;
    }

    private static NowsOrderCourtCentre getCourtCentreInOrder(NowsRequested nowsRequested) {
        CourtCentre courtCentre = nowsRequested.getHearing().getCourtCentre();
        return NowsOrderCourtCentre.builder().withCourtCentreName(courtCentre.getCourtCentreName()).withCourtRoomName(courtCentre.getCourtRoomName()).build();
    }


}
