package uk.gov.moj.cpp.hearing.event.nows;

import uk.gov.moj.cpp.hearing.command.result.Level;
import uk.gov.moj.cpp.hearing.event.nows.order.Address;
import uk.gov.moj.cpp.hearing.event.nows.order.DefendantCaseOffence;
import uk.gov.moj.cpp.hearing.event.nows.order.NowsDocumentOrder;
import uk.gov.moj.cpp.hearing.event.nows.order.OrderCase;
import uk.gov.moj.cpp.hearing.event.nows.order.OrderDefendant;
import uk.gov.moj.cpp.hearing.event.nows.order.OrderPrompt;
import uk.gov.moj.cpp.hearing.event.nows.order.OrderResult;
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
import java.util.UUID;
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
            Optional<NowType> matchingNowType = getMatchingNowType(nowsRequested, selectedNow);
            selectedNow.getMaterial().forEach(selectedNowMaterial -> {

                NowsDocumentOrder nowsDocumentOrder = NowsDocumentOrder.builder()
                        .withMaterialId(selectedNowMaterial.getId())
                        .withOrderName(matchingNowType.map(NowType::getDescription).orElse(EMPTY))
                        .withNowText(matchingNowType.map(NowType::getStaticText).orElse(EMPTY))
                        .withPriority(matchingNowType.map(NowType::getPriority).orElse(EMPTY))
                        .withCourtCentreName(getCourtCentreName(nowsRequested))
                        .withCourtClerkName(getCourtClerkName(nowsRequested))
                        .withOrderDate(getOrderDate(nowsRequested, selectedNowMaterial))
                        .withCaseUrns(getCaseUrns(nowsRequested, selectedNow))
                        .withDefendant(getNowsOrderDefendant(nowsRequested, selectedNow))
                        .withCases(getNowsMaterialOrderCases(nowsRequested, selectedNowMaterial))
                        .build();
                nowsDocumentOrders.add(nowsDocumentOrder);
            });


        });

        return nowsDocumentOrders;
    }


    @SuppressWarnings({"squid:S1188", "squid:S3776"})
    private static List<OrderCase> getNowsMaterialOrderCases(NowsRequested nowsRequested, Material selectedMaterial) {

        List<OrderResult> defendantCaseResults = new ArrayList<>();
        List<DefendantCaseOffence> defendantCaseOffences = new ArrayList<>();
        List<OrderResult> caseResults = new ArrayList<>();

        Map<String, SharedResultLine> sharedResultLineIdMap = prepareSharedResultLineIdMap(nowsRequested);
        selectedMaterial.getNowResult().stream().forEach(selectedNowResult -> {
            if (sharedResultLineIdMap.containsKey(selectedNowResult.getSharedResultId())) {
                SharedResultLine sharedResultLine = sharedResultLineIdMap.get(selectedNowResult.getSharedResultId());
                List<OrderPrompt> orderPrompts = prepareOrderPrompts(selectedNowResult, sharedResultLine);

                if (sharedResultLine.getLevel().equals(Level.CASE.toString())) {
                    getMatchingCase(nowsRequested, sharedResultLine).ifPresent(defendantCase ->
                            defendantCaseResults.add(OrderResult.builder().withUrn(defendantCase.getUrn()).withLabel(sharedResultLine.getLabel()).withPrompts(orderPrompts).build())
                    );
                }
                if (sharedResultLine.getLevel().equals(Level.OFFENCE.toString())) {
                    getMatchingOffence(nowsRequested, sharedResultLine).ifPresent(offence -> {
                        DefendantCaseOffence defendantCaseOffence = DefendantCaseOffence.builder().withConvictionDate(offence.getConvictionDate())
                                .withStartDate(offence.getStartDate())
                                .withWording(offence.getWording())
                                .withResults(Arrays.asList(OrderResult.builder().withLabel(sharedResultLine.getLabel()).withPrompts(orderPrompts).build())).build();
                        defendantCaseOffences.add(defendantCaseOffence);
                    });

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

    private static List<String> getCaseUrns(NowsRequested nowsRequested, Now now) {
        return getMatchingDefendant(nowsRequested, now.getDefendantId())
                .map(Defendant::getCases)
                .orElse(new ArrayList<>()).stream()
                .map(Case::getUrn)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("squid:S1192")
    private static OrderDefendant getNowsOrderDefendant(NowsRequested nowsRequested, Now now) {
        Optional<Defendant> matchingDefendantForNows = getMatchingDefendant(nowsRequested, now.getDefendantId());
        if (matchingDefendantForNows.isPresent()) {
            Defendant defendant = matchingDefendantForNows.get();
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

    private static Optional<NowType> getMatchingNowType(NowsRequested nowsRequested, Now now) {
        List<NowType> nowTypes = nowsRequested.getHearing().getNowTypes();
        return nowTypes.stream().filter(nowType -> nowType.getId().equals(now.getNowsTypeId())).findFirst();
    }

    private static String getCourtClerkName(NowsRequested nowsRequested) {
        return nowsRequested.getHearing().getAttendees().stream()
                .filter(attende -> attende.getType().equals(COURTCLERK))
                .findFirst()
                .map(attendee -> format("%s %s", attendee.getFirstName(), attendee.getLastName()))
                .orElse(EMPTY);
    }

    private static String getCourtCentreName(NowsRequested nowsRequested) {
        return nowsRequested.getHearing().getCourtCentre().getCourtCentreName();
    }

    private static String getOrderDate(NowsRequested nowsRequested, Material material) {
        Map<String, SharedResultLine> stringSharedResultLineMap = prepareSharedResultLineIdMap(nowsRequested);
        return material.getNowResult().stream()
                .filter(nowResult -> stringSharedResultLineMap.containsKey(nowResult.getSharedResultId()))
                .findFirst()
                .map(nowResult -> stringSharedResultLineMap.get(nowResult.getSharedResultId()).getOrderedDate())
                .orElse(EMPTY);
    }

    private static Optional<Defendant> getMatchingDefendant(NowsRequested nowsRequested, String defendantId) {
        return nowsRequested.getHearing().getDefendants().stream().filter(defendant -> defendant.getId().equals(defendantId))
                .findFirst();
    }

    private static Optional<Offence> getMatchingOffence(NowsRequested nowsRequested, SharedResultLine sharedResultLine) {
        return getMatchingCase(nowsRequested, sharedResultLine)
                .map(Case::getOffences)
                .orElse(new ArrayList<>())
                .stream()
                .filter(o -> o.getId().equals(sharedResultLine.getOffenceId()))
                .findFirst();
    }

    private static Optional<Case> getMatchingCase(NowsRequested nowsRequested, SharedResultLine sharedResultLine) {
        return getMatchingDefendant(nowsRequested, sharedResultLine.getDefendantId())
                .map(Defendant::getCases)
                .orElse(new ArrayList<>())
                .stream()
                .filter(aCase -> aCase.getId().equals(sharedResultLine.getCaseId()))
                .findFirst();

    }

    private static List<Prompt> getMatchingPrompts(NowResult selectedNowResult, SharedResultLine sharedResultLine) {
        List<UUID> nowResultPromptLabels = selectedNowResult.getPrompts().stream().map(Prompt::getId).collect(Collectors.toList());
        return sharedResultLine.getPrompts().stream().filter(prompt -> nowResultPromptLabels.contains(prompt.getId())).collect(Collectors.toList());
    }


    private static Map<String, SharedResultLine> prepareSharedResultLineIdMap(NowsRequested nowsRequested) {
        return nowsRequested.getHearing().getSharedResultLines().stream().collect(Collectors.toMap(SharedResultLine::getId, Function.identity()));
    }


}
