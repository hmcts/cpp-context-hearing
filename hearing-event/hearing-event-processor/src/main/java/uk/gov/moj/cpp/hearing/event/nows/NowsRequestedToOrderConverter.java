package uk.gov.moj.cpp.hearing.event.nows;

import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;

import uk.gov.justice.json.schemas.core.ProsecutionCase;
import uk.gov.moj.cpp.hearing.command.result.Level;
import uk.gov.moj.cpp.hearing.event.nows.order.Address;
import uk.gov.moj.cpp.hearing.event.nows.order.DefendantCaseOffence;
import uk.gov.moj.cpp.hearing.event.nows.order.NowsDocumentOrder;
import uk.gov.moj.cpp.hearing.event.nows.order.OrderCase;
import uk.gov.moj.cpp.hearing.event.nows.order.OrderDefendant;
import uk.gov.moj.cpp.hearing.event.nows.order.OrderPrompt;
import uk.gov.moj.cpp.hearing.event.nows.order.OrderResult;
import uk.gov.moj.cpp.hearing.nows.events.Material;
import uk.gov.moj.cpp.hearing.nows.events.MaterialUserGroup;
import uk.gov.moj.cpp.hearing.nows.events.Now;
import uk.gov.moj.cpp.hearing.nows.events.NowResult;
import uk.gov.moj.cpp.hearing.nows.events.NowType;
import uk.gov.moj.cpp.hearing.nows.events.NowsRequested;
import uk.gov.moj.cpp.hearing.nows.events.Prompt;
import uk.gov.moj.cpp.hearing.nows.events.SharedResultLine;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

@SuppressWarnings({"squid:S1188"})
public class NowsRequestedToOrderConverter {

    private NowsRequestedToOrderConverter() {

    }

    public static Map<NowsDocumentOrder, NowsNotificationDocumentState> convert(NowsRequested nowsRequested) {
        final Map<NowsDocumentOrder, NowsNotificationDocumentState> nowsDocumentOrders = new HashMap<>();

        nowsRequested.getNows().forEach(selectedNow -> {

            NowType matchingNowType = nowsRequested.getNowTypes().stream()
                    .filter(nowType -> nowType.getId().equals(selectedNow.getNowsTypeId()))
                    .findAny()
                    .orElseThrow(() -> new IllegalArgumentException("invalid now type id supplied."));

            selectedNow.getMaterials().forEach(selectedNowMaterial -> {

                String courtClerkName = format("%s %s", nowsRequested.getCourtClerk().getFirstName(), nowsRequested.getCourtClerk().getLastName());
                OrderDefendant orderDefendant = getNowsOrderDefendant(nowsRequested, selectedNow);
                List<String> caseUrns = getCaseUrns(nowsRequested, selectedNow);

                final NowsNotificationDocumentState nowsNotificationDocumentState = new NowsNotificationDocumentState()
                        .setUsergroups(selectedNowMaterial.getUserGroups().stream().map(MaterialUserGroup::getGroup).collect(toList()))
                        .setOriginatingCourtCentreId(nowsRequested.getHearing().getCourtCentre().getId())
                        .setDefendantName(orderDefendant.getName())
                        .setCourtClerkName(courtClerkName)
                        .setCaseUrns(caseUrns)
                        .setNowsTypeId(matchingNowType.getId())
                        .setJurisdiction(matchingNowType.getJurisdiction())
                        .setCourtCentreName(nowsRequested.getHearing().getCourtCentre().getName())
                        .setOrderName(matchingNowType.getDescription())
                        .setPriority(matchingNowType.getPriority())
                        .setMaterialId(selectedNowMaterial.getId());

                final NowsDocumentOrder nowsDocumentOrder = NowsDocumentOrder.builder()
                        .withMaterialId(selectedNowMaterial.getId())
                        .withOrderName(matchingNowType.getDescription())
                        .withNowText(matchingNowType.getStaticText())
                        .withPriority(matchingNowType.getPriority())
                        .withCourtCentreName(nowsRequested.getHearing().getCourtCentre().getName())
                        .withCourtClerkName(courtClerkName)
                        .withOrderDate(findOrderDate(nowsRequested, selectedNowMaterial))
                        .withCaseUrns(caseUrns)
                        .withDefendant(orderDefendant)
                        .withCases(getNowsMaterialOrderCases(nowsRequested, selectedNowMaterial))
                        .withAmended(selectedNowMaterial.isAmended())
                        .build();

                nowsDocumentOrders.put(nowsDocumentOrder, nowsNotificationDocumentState);
            });
        });

        return nowsDocumentOrders;
    }


    @SuppressWarnings({"squid:S1188", "squid:S3776"})
    private static List<OrderCase> getNowsMaterialOrderCases(NowsRequested nowsRequested, Material selectedMaterial) {
        Map<String, OrderCase> orderCaseMap = new HashMap<>();


        for (NowResult selectedNowResult : selectedMaterial.getNowResult()) {

            nowsRequested.getSharedResultLines().stream()
                    .filter(l -> l.getId().equals(selectedNowResult.getSharedResultId()))
                    .findAny()
                    .ifPresent(sharedResultLine -> {

                        List<OrderPrompt> orderPrompts = prepareOrderPrompts(selectedNowResult, sharedResultLine);

                        ProsecutionCase prosecutionCase = nowsRequested.getHearing().getProsecutionCases().stream()
                                .filter(isCaseBelongingToDefendant(sharedResultLine.getDefendantId()))
                                .findAny()
                                .orElseThrow(() -> new IllegalArgumentException("invalid data, could not find case for defendant supplied by result line"));

                        OrderCase orderCase = orderCaseMap.computeIfAbsent(prosecutionCase.getProsecutionCaseIdentifier().getCaseURN(), (urn) -> OrderCase.orderCase().setUrn(urn));

                        if (sharedResultLine.getLevel().equals(Level.CASE.toString())) {
                            orderCase.getCaseResults()
                                    .add(OrderResult.builder()
                                            .withLabel(sharedResultLine.getLabel())
                                            .withPrompts(orderPrompts)
                                            .build()
                                    );

                        }

                        if (sharedResultLine.getLevel().equals(Level.OFFENCE.toString())) {
                            prosecutionCase.getDefendants().stream()
                                    .filter(d -> d.getId().equals(sharedResultLine.getDefendantId()))
                                    .flatMap(d -> d.getOffences().stream())
                                    .filter(o -> o.getId().equals(sharedResultLine.getOffenceId()))
                                    .findAny()
                                    .ifPresent(offence -> orderCase.getDefendantCaseOffences()
                                            .add(DefendantCaseOffence.builder()
                                                    .withConvictionDate(offence.getConvictionDate() != null ? offence.getConvictionDate().toString() : "")
                                                    .withStartDate(offence.getStartDate().toString())
                                                    .withWording(offence.getWording())
                                                    .withResults(singletonList(OrderResult.builder()
                                                            .withLabel(sharedResultLine.getLabel())
                                                            .withPrompts(orderPrompts)
                                                            .build()
                                                    ))
                                                    .build())
                                    );
                        }

                        if (sharedResultLine.getLevel().equals(Level.DEFENDANT.toString())) {
                            orderCase.getDefendantCaseResults()
                                    .add(OrderResult.builder()
                                            .withLabel(sharedResultLine.getLabel())
                                            .withPrompts(orderPrompts)
                                            .build()
                                    );
                        }


                    });
        }

        return new ArrayList<>(orderCaseMap.values());
    }


    @SuppressWarnings("squid:S1192")
    private static OrderDefendant getNowsOrderDefendant(NowsRequested nowsRequested, Now now) {

        return nowsRequested.getHearing().getProsecutionCases().stream()
                .flatMap(c -> c.getDefendants().stream())
                .filter(d -> d.getId().equals(now.getDefendantId()))
                .findAny()
                .map(defendant -> {
                    String defendantName = format("%s %s %s",
                            trimToEmpty(defendant.getPersonDefendant().getPersonDetails().getTitle().toString()),
                            trimToEmpty(defendant.getPersonDefendant().getPersonDetails().getFirstName()), trimToEmpty(defendant.getPersonDefendant().getPersonDetails().getLastName())
                    ).trim();
                    String line1 = format("%s %s",
                            trimToEmpty(defendant.getPersonDefendant().getPersonDetails().getAddress().getAddress1()),
                            trimToEmpty(defendant.getPersonDefendant().getPersonDetails().getAddress().getAddress2())
                    ).trim();
                    String line2 = format("%s %s",
                            trimToEmpty(defendant.getPersonDefendant().getPersonDetails().getAddress().getAddress3()),
                            trimToEmpty(defendant.getPersonDefendant().getPersonDetails().getAddress().getAddress4())
                    ).trim();

                    Address address = Address.builder()
                            .withLine1(line1)
                            .withLine2(line2)
                            .withPostCode(defendant.getPersonDefendant().getPersonDetails().getAddress().getPostcode())
                            .build();

                    return OrderDefendant.builder()
                            .withName(defendantName)
                            .withDateOfBirth(defendant.getPersonDefendant().getPersonDetails().getDateOfBirth())
                            .withAddress(address)
                            .build();
                })
                .orElse(OrderDefendant.builder().build());
    }

    private static String findOrderDate(NowsRequested nowsRequested, Material material) {
        for (NowResult nowResult : material.getNowResult()) {
            Optional<SharedResultLine> sharedResultLine = nowsRequested.getSharedResultLines().stream()
                    .filter(l -> l.getId().equals(nowResult.getSharedResultId()))
                    .findAny();
            if (sharedResultLine.isPresent()) {
                return sharedResultLine.get().getOrderedDate().toString();
            }
        }
        return EMPTY;
    }

    private static List<OrderPrompt> prepareOrderPrompts(NowResult selectedNowResult, SharedResultLine sharedResultLine) {
        List<Prompt> nowResultPrompts = getMatchingPrompts(selectedNowResult, sharedResultLine);
        return nowResultPrompts.stream()
                .map(prompt -> new OrderPrompt(prompt.getLabel(), prompt.getValue()))
                .collect(toList());
    }

    private static List<Prompt> getMatchingPrompts(NowResult selectedNowResult, SharedResultLine sharedResultLine) {
        List<UUID> nowResultPromptLabels = selectedNowResult.getPrompts().stream().map(Prompt::getId).collect(toList());
        return sharedResultLine.getPrompts().stream()
                .filter(prompt -> nowResultPromptLabels.contains(prompt.getId()))
                .collect(toList());
    }

    private static List<String> getCaseUrns(NowsRequested nowsRequested, Now now) {
        return nowsRequested.getHearing().getProsecutionCases().stream()
                .filter(isCaseBelongingToDefendant(now.getDefendantId()))
                .map(c -> c.getProsecutionCaseIdentifier().getCaseURN())
                .collect(toList());
    }

    private static Predicate<ProsecutionCase> isCaseBelongingToDefendant(UUID defendantId) {
        return c -> c.getDefendants().stream().anyMatch(d -> d.getId().equals(defendantId));
    }
}
