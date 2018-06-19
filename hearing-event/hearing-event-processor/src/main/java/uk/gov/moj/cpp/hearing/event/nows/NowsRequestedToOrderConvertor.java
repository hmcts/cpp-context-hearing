package uk.gov.moj.cpp.hearing.event.nows;

import uk.gov.moj.cpp.hearing.command.result.Level;
import uk.gov.moj.cpp.hearing.event.nows.order.Address;
import uk.gov.moj.cpp.hearing.event.nows.order.DefendantCaseOffence;
import uk.gov.moj.cpp.hearing.event.nows.order.NowsDocumentOrder;
import uk.gov.moj.cpp.hearing.event.nows.order.OrderCase;
import uk.gov.moj.cpp.hearing.event.nows.order.OrderDefendant;
import uk.gov.moj.cpp.hearing.event.nows.order.OrderPrompt;
import uk.gov.moj.cpp.hearing.event.nows.order.OrderResult;
import uk.gov.moj.cpp.hearing.nows.events.Attendee;
import uk.gov.moj.cpp.hearing.nows.events.Case;
import uk.gov.moj.cpp.hearing.nows.events.Defendant;
import uk.gov.moj.cpp.hearing.nows.events.Material;
import uk.gov.moj.cpp.hearing.nows.events.Now;
import uk.gov.moj.cpp.hearing.nows.events.NowResult;
import uk.gov.moj.cpp.hearing.nows.events.NowType;
import uk.gov.moj.cpp.hearing.nows.events.NowsRequested;
import uk.gov.moj.cpp.hearing.nows.events.Offence;
import uk.gov.moj.cpp.hearing.nows.events.Prompt;
import uk.gov.moj.cpp.hearing.nows.events.SharedResultLine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.EMPTY;

public class NowsRequestedToOrderConvertor {
    private static final String COURTCLERK = "COURTCLERK";

    private NowsRequestedToOrderConvertor() {

    }

    /**
     * Converts NowsRequested to List<NowsDocumentOrder>
     *
     * @param nowsRequested
     * @return listNowsDocumentOrder
     */
    public static List<NowsDocumentOrder> convert(NowsRequested nowsRequested) {
        List<NowsDocumentOrder> nowsDocumentOrders = new ArrayList<>();
        nowsRequested.getHearing().getNows().forEach(selectedNow -> {
            NowType matchingNowType = getMatchingNowType(nowsRequested, selectedNow);
            selectedNow.getMaterial().forEach(selectedNowMaterial -> {

                NowsDocumentOrder nowsDocumentOrder = NowsDocumentOrder.builder()
                        .withOrderName(matchingNowType.getDescription())
                        .withCourtCentreName(getCourtCentreName(nowsRequested))
                        .withCourtClerkName(getCourtClerkName(nowsRequested))
                        .withOrderDate(getOrderDate(nowsRequested, selectedNowMaterial))
                        .withDefendant(getNowsOrderDefendant(nowsRequested, selectedNow))
                        .withCaseUrns(getCaseUrns(nowsRequested, selectedNow))
                        .withCases(getNowsMaterialOrderCases(nowsRequested, selectedNowMaterial))
                        .withNowText(matchingNowType.getStaticText())
                        .withPriority(matchingNowType.getPriority())
                        .withMaterialId(selectedNowMaterial.getId())
                        .build();
                nowsDocumentOrders.add(nowsDocumentOrder);
            });


        });

        return nowsDocumentOrders;
    }


    @SuppressWarnings({"squid:S1188","squid:S3776"})
    private static List<OrderCase> getNowsMaterialOrderCases(NowsRequested nowsRequested, Material selectedMaterial) {

        List<OrderResult> defendantCaseResults = new ArrayList<OrderResult>();
        List<DefendantCaseOffence> defendantCaseOffences = new ArrayList<DefendantCaseOffence>();
        List<OrderResult> caseResults = new ArrayList<OrderResult>();

        Map<String, SharedResultLine> sharedResultLineIdMap = prepareSharedResultLineIdMap(nowsRequested);
        selectedMaterial.getNowResult().stream().forEach(selectedNowResult -> {
            if (sharedResultLineIdMap.containsKey(selectedNowResult.getSharedResultId())) {
                SharedResultLine sharedResultLine = sharedResultLineIdMap.get(selectedNowResult.getSharedResultId());
                List<OrderPrompt> orderPrompts = prepareOrderPrompts(selectedNowResult, sharedResultLine);
                if (sharedResultLine.getLevel().equals(Level.CASE.toString())) {
                    Optional<Case> defendantCase = getMatchingCase(nowsRequested, sharedResultLine);
                    if (defendantCase.isPresent()) {
                        defendantCaseResults.add(OrderResult.builder().withUrn(defendantCase.get().getUrn()).withLabel(sharedResultLine.getLabel()).withPrompts(orderPrompts).build());
                    }
                }
                if (sharedResultLine.getLevel().equals(Level.OFFENCE.toString())) {
                    Optional<Offence> offence = getMatchingOffence(nowsRequested, sharedResultLine);
                    if (offence.isPresent()) {
                        DefendantCaseOffence defendantCaseOffence = DefendantCaseOffence.builder().withConvictionDate(offence.get().getConvictionDate())
                                .withStartDate(offence.get().getStartDate())
                                .withWording(offence.get().getWording())
                                .withResults(Arrays.asList(OrderResult.builder().withLabel(sharedResultLine.getLabel()).withPrompts(orderPrompts).build())).build();
                        defendantCaseOffences.add(defendantCaseOffence);
                    }
                }
                if (sharedResultLine.getLevel().equals(Level.DEFENDANT.toString())) {
                    caseResults.add(OrderResult.builder().withLabel(sharedResultLine.getLabel()).withPrompts(orderPrompts).build());
                }
            }

        });


        List<OrderCase> nowsMaterialOrderCases = new ArrayList<>();
        nowsMaterialOrderCases.add(OrderCase.builder()
                .withDefendantCaseResults(defendantCaseResults)
                .withDefendantCaseOffences(defendantCaseOffences)
                .withCaseResults(caseResults)
                .build());
        return nowsMaterialOrderCases;
    }

    private static List<OrderPrompt> prepareOrderPrompts(NowResult selectedNowResult, SharedResultLine sharedResultLine) {
        List<Prompt> nowResultPrompts = getMatchingPrompts(selectedNowResult, sharedResultLine);
        return nowResultPrompts.stream().map(prompt -> new OrderPrompt(prompt.getLabel(), prompt.getValue())).collect(Collectors.toList());
    }

    private static List<Prompt> getMatchingPrompts(NowResult selectedNowResult, SharedResultLine sharedResultLine) {
        List<String> nowResultPromptLabels = selectedNowResult.getPrompts().stream().map(prompt -> prompt.getLabel()).collect(Collectors.toList());
        return sharedResultLine.getPrompts().stream().filter(prompt -> nowResultPromptLabels.contains(prompt.getLabel())).collect(Collectors.toList());
    }


    private static Optional<Offence> getMatchingOffence(NowsRequested nowsRequested, SharedResultLine sharedResultLine) {
        Optional<Case> matchingCase = getMatchingCase(nowsRequested, sharedResultLine);
        if (matchingCase.isPresent()) {
            return matchingCase.get().getOffences().stream().filter(o -> o.getId().equals(sharedResultLine.getOffenceId()))
                    .findFirst();
        }
        return Optional.empty();
    }

    private static Optional<Case> getMatchingCase(NowsRequested nowsRequested, SharedResultLine sharedResultLine) {
        Optional<Defendant> matchingDefendant = getMatchingDefendant(nowsRequested, sharedResultLine);
        if (matchingDefendant.isPresent()) {
            return matchingDefendant.get().getCases().stream().filter(aCase -> aCase.getId().equals(sharedResultLine.getCaseId()))
                    .findFirst();
        }
        return Optional.empty();
    }

    private static Optional<Defendant> getMatchingDefendant(NowsRequested nowsRequested, SharedResultLine sharedResultLine) {
        return nowsRequested.getHearing().getDefendants().stream().filter(defendant -> defendant.getId().equals(sharedResultLine.getDefendantId()))
                .findFirst();
    }

    private static Map<String, SharedResultLine> prepareSharedResultLineIdMap(NowsRequested nowsRequested) {
        return nowsRequested.getHearing().getSharedResultLines().stream().collect(Collectors.toMap(SharedResultLine::getId, Function.identity()));
    }


    private static List<String> getCaseUrns(NowsRequested nowsRequested, Now now) {
        Optional<uk.gov.moj.cpp.hearing.nows.events.Defendant> defendant = getMatchingDefendantForNows(nowsRequested, now);
        if (defendant.isPresent()) {
            return defendant.get().getCases().stream().map(aCase -> aCase.getUrn()).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    private static OrderDefendant getNowsOrderDefendant(NowsRequested nowsRequested, Now now) {
        Optional<uk.gov.moj.cpp.hearing.nows.events.Defendant> matchingDefendantForNows = getMatchingDefendantForNows(nowsRequested, now);
        if (matchingDefendantForNows.isPresent()) {
            uk.gov.moj.cpp.hearing.nows.events.Defendant defendant = matchingDefendantForNows.get();
            String defendantName = format("%s %s  %s", defendant.getPerson().getTitle(), defendant.getPerson().getFirstName(), defendant.getPerson().getLastName());
            String line1 = format("%s %s", defendant.getPerson().getAddress().getAddress1(), defendant.getPerson().getAddress().getAddress2());
            String line2 = format("%s %s", defendant.getPerson().getAddress().getAddress3(), defendant.getPerson().getAddress().getAddress4());

            Address address = Address.builder()
                    .withLine1(line1)
                    .withLine2(line2)
                    .withPostCode(defendant.getPerson().getAddress().getPostCode())
                    .build();
            return OrderDefendant.builder()
                    .withName(defendantName)
                    .withDateOfBirth(defendant.getPerson().getDateOfBirth())
                    .withAddress(address)
                    .build();
        }
        return OrderDefendant.builder().build();
    }

    private static Optional<uk.gov.moj.cpp.hearing.nows.events.Defendant> getMatchingDefendantForNows(NowsRequested nowsRequested, Now now) {
        List<uk.gov.moj.cpp.hearing.nows.events.Defendant> defendants = nowsRequested.getHearing().getDefendants();
        return defendants.stream().filter(def -> def.getId().equals(now.getDefendantId())).findFirst();
    }

    private static NowType getMatchingNowType(NowsRequested nowsRequested, Now now) {
        List<NowType> nowTypes = nowsRequested.getHearing().getNowTypes();
        //TODO: Matching of nows type needs to be done on Nows typeId
        return nowTypes.stream().filter(nowType -> nowType.getTemplateName().equals(now.getNowsTemplateName())).findFirst().orElse(new NowType());
    }

    private static String getCourtClerkName(NowsRequested nowsRequested) {
        Optional<Attendee> attendee = nowsRequested.getHearing().getAttendees().stream()
                .filter(attende -> attende.getType().equals(COURTCLERK)).findFirst();
        if (attendee.isPresent()) {
            return format("%s %s", attendee.get().getFirstName(), attendee.get().getLastName());
        }
        return EMPTY;
    }

    private static String getCourtCentreName(NowsRequested nowsRequested) {
        return nowsRequested.getHearing().getCourtCentre().getCourtCentreName();
    }

    private static String getOrderDate(NowsRequested nowsRequested, Material material) {
        List<String> materialSharedResultIds = material.getNowResult().stream().map(nowResult -> nowResult.getSharedResultId()).collect(Collectors.toList());
        Optional<SharedResultLine> sharedResultLine = nowsRequested.getHearing().getSharedResultLines().stream().filter(s -> materialSharedResultIds.contains(s.getId())).findFirst();
        if (sharedResultLine.isPresent()) {
            return sharedResultLine.get().getOrderedDate();
        }
        return EMPTY;
    }


}
