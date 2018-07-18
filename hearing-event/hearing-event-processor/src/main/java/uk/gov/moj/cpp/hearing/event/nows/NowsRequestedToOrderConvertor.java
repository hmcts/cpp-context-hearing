package uk.gov.moj.cpp.hearing.event.nows;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;

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
import uk.gov.moj.cpp.hearing.nows.events.MaterialUserGroup;
import uk.gov.moj.cpp.hearing.nows.events.Now;
import uk.gov.moj.cpp.hearing.nows.events.NowResult;
import uk.gov.moj.cpp.hearing.nows.events.NowType;
import uk.gov.moj.cpp.hearing.nows.events.NowsRequested;
import uk.gov.moj.cpp.hearing.nows.events.Offence;
import uk.gov.moj.cpp.hearing.nows.events.Prompt;
import uk.gov.moj.cpp.hearing.nows.events.SharedResultLine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings({"squid:S1188"})
public class NowsRequestedToOrderConvertor {
    private static final String COURTCLERK = "CourtClerk";

    private NowsRequestedToOrderConvertor() {

    }

    /**
     * Converts NowsRequested to Map<NowsDocumentOrder, NowsNotificationDocumentState>
     *
     * @param nowsRequested
     * @return mapNowsDocumentOrder
     */
    public static Map<NowsDocumentOrder, NowsNotificationDocumentState> convert(NowsRequested nowsRequested) {
        final Map<NowsDocumentOrder, NowsNotificationDocumentState> nowsDocumentOrders = new HashMap<>();
        nowsRequested.getHearing().getNows().forEach(selectedNow -> {
            Optional<NowType> matchingNowType = getMatchingNowType(nowsRequested, selectedNow);
            selectedNow.getMaterials().forEach(selectedNowMaterial -> {
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
                        .withAmended(selectedNowMaterial.isAmended())
                        .build();
                final NowsNotificationDocumentState nowsNotificationDocumentState = new NowsNotificationDocumentState()
                        .setUsergroups(selectedNowMaterial.getUserGroups().stream().map(MaterialUserGroup::getGroup).collect(Collectors.toList()))
                        .setOriginatingCourtCentreId(UUID.fromString(nowsRequested.getHearing().getCourtCentre().getCourtCentreId()))
                        .setDefendantName(nowsDocumentOrder.getDefendant().getName())
                        .setCourtClerkName(nowsDocumentOrder.getCourtClerkName())
                        .setCaseUrns(nowsDocumentOrder.getCaseUrns())
                        .setNowsTypeId(UUID.fromString(matchingNowType.get().getId()))
                        .setJurisdiction(matchingNowType.get().getJurisdiction());
                nowsDocumentOrders.put(nowsDocumentOrder, nowsNotificationDocumentState);
            });
        });

        return nowsDocumentOrders;
    }


    @SuppressWarnings({"squid:S1188", "squid:S3776"})
    private static List<OrderCase> getNowsMaterialOrderCases(NowsRequested nowsRequested, Material selectedMaterial) {
        Map<String, OrderCase> orderCaseMap = new HashMap<>();
        Map<String, SharedResultLine> sharedResultLineIdMap = prepareSharedResultLineIdMap(nowsRequested);
        selectedMaterial.getNowResult().stream().forEach(selectedNowResult -> {
            if (sharedResultLineIdMap.containsKey(selectedNowResult.getSharedResultId())) {
                SharedResultLine sharedResultLine = sharedResultLineIdMap.get(selectedNowResult.getSharedResultId());
                List<OrderPrompt> orderPrompts = prepareOrderPrompts(selectedNowResult, sharedResultLine);
                getMatchingCase(nowsRequested, sharedResultLine).ifPresent(defendantCase -> {
                            OrderCase orderCase = new OrderCase();
                            orderCase.setUrn(defendantCase.getUrn());
                            if (orderCaseMap.containsKey(defendantCase.getUrn())) {
                                orderCase = orderCaseMap.get(defendantCase.getUrn());
                            } else {
                                orderCaseMap.put(defendantCase.getUrn(), orderCase);
                            }
                            if (sharedResultLine.getLevel().equals(Level.CASE.toString())) {
                                orderCase.getDefendantCaseResults().add(OrderResult.builder().withLabel(sharedResultLine.getLabel()).withPrompts(orderPrompts).build());

                            }
                            if (sharedResultLine.getLevel().equals(Level.OFFENCE.toString())) {
                                Offence offence = getMatchingOffence(nowsRequested, sharedResultLine).orElse(new Offence());
                                DefendantCaseOffence defendantCaseOffence = DefendantCaseOffence.builder().withConvictionDate(offence.getConvictionDate())
                                        .withStartDate(offence.getStartDate())
                                        .withConvictionDate(offence.getConvictionDate())
                                        .withWording(offence.getWording())
                                        .withResults(Arrays.asList(OrderResult.builder().withLabel(sharedResultLine.getLabel()).withPrompts(orderPrompts).build())).build();
                                orderCase.getDefendantCaseOffences().add(defendantCaseOffence);

                            }
                            if (sharedResultLine.getLevel().equals(Level.DEFENDANT.toString())) {
                                orderCase.getCaseResults().add(OrderResult.builder().withLabel(sharedResultLine.getLabel()).withPrompts(orderPrompts).build());
                            }
                        }
                );
            }
        });

        return orderCaseMap.values().stream().collect(Collectors.toList());
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
            String defendantName = format("%s %s %s", trimToEmpty(defendant.getPerson().getTitle()), trimToEmpty(defendant.getPerson().getFirstName()), trimToEmpty(defendant.getPerson().getLastName())).trim();
            String line1 = format("%s %s", trimToEmpty(defendant.getPerson().getAddress().getAddress1()), trimToEmpty(defendant.getPerson().getAddress().getAddress2()));
            String line2 = format("%s %s", trimToEmpty(defendant.getPerson().getAddress().getAddress3()), trimToEmpty(defendant.getPerson().getAddress().getAddress4()));

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
                .filter(attende -> attende.getType().equalsIgnoreCase(COURTCLERK))
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
