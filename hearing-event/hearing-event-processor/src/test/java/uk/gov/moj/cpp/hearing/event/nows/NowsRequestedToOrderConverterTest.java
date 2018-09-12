package uk.gov.moj.cpp.hearing.event.nows;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.NowsRequestedTemplates.nowsRequestedTemplate;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;

import org.junit.Test;
import uk.gov.justice.json.schemas.core.Person;
import uk.gov.moj.cpp.hearing.event.nows.order.Address;
import uk.gov.moj.cpp.hearing.event.nows.order.DefendantCaseOffence;
import uk.gov.moj.cpp.hearing.event.nows.order.NowsDocumentOrder;
import uk.gov.moj.cpp.hearing.event.nows.order.OrderCase;
import uk.gov.moj.cpp.hearing.event.nows.order.OrderDefendant;
import uk.gov.moj.cpp.hearing.event.nows.order.OrderPrompt;
import uk.gov.moj.cpp.hearing.event.nows.order.OrderResult;
import uk.gov.moj.cpp.hearing.nows.events.MaterialUserGroup;
import uk.gov.moj.cpp.hearing.nows.events.NowsRequested;
import uk.gov.moj.cpp.hearing.nows.events.Prompt;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class NowsRequestedToOrderConverterTest {

    @Test
    public void testConversion() throws IOException {

        NowsRequested input = nowsRequestedTemplate();

        Map<NowsDocumentOrder, NowsNotificationDocumentState> nowsDocumentOrders = NowsRequestedToOrderConverter.convert(input);

        NowsDocumentOrder nowsDocumentOrder = nowsDocumentOrders.keySet().iterator().next();
        NowsNotificationDocumentState nowsNotificationDocumentState = nowsDocumentOrders.values().iterator().next();

        Person personDetails = input.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getPersonDefendant().getPersonDetails();

        String defendantName = format("%s %s %s", trimToEmpty(personDetails.getTitle().toString()), trimToEmpty(personDetails.getFirstName()), trimToEmpty(personDetails.getLastName())).trim();
        String line1 = format("%s %s", trimToEmpty(personDetails.getAddress().getAddress1()), trimToEmpty(personDetails.getAddress().getAddress2())).trim();
        String line2 = format("%s %s", trimToEmpty(personDetails.getAddress().getAddress3()), trimToEmpty(personDetails.getAddress().getAddress4())).trim();


        assertThat(nowsDocumentOrder, isBean(NowsDocumentOrder.class)
                .with(NowsDocumentOrder::getMaterialId, is(input.getNows().get(0).getMaterials().get(0).getId()))
                .with(NowsDocumentOrder::isAmended, is(input.getNows().get(0).getMaterials().get(0).isAmended()))

                .with(NowsDocumentOrder::getPriority, is(input.getNowTypes().get(0).getPriority()))
                .with(NowsDocumentOrder::getOrderName, is(input.getNowTypes().get(0).getDescription()))

                .with(NowsDocumentOrder::getCourtCentreName, is(input.getHearing().getCourtCentre().getName()))

                .with(NowsDocumentOrder::getCourtClerkName, is(format("%s %s", input.getCourtClerk().getFirstName(), input.getCourtClerk().getLastName())))
                .with(NowsDocumentOrder::getOrderDate, is(input.getSharedResultLines().get(0).getOrderedDate().toString()))

                .with(NowsDocumentOrder::getDefendant, isBean(OrderDefendant.class)
                        .with(OrderDefendant::getName, is(defendantName))
                        .with(OrderDefendant::getDateOfBirth, is(personDetails.getDateOfBirth()))
                        .with(OrderDefendant::getAddress, isBean(Address.class)
                                .with(Address::getLine1, is(line1))
                                .with(Address::getLine2, is(line2))
                                .with(Address::getPostCode, is(personDetails.getAddress().getPostcode()))
                        )
                )
                .with(NowsDocumentOrder::getCaseUrns, hasItems(input.getHearing().getProsecutionCases().get(0).getProsecutionCaseIdentifier().getCaseURN()))
                .with(NowsDocumentOrder::getCases, first(isBean(OrderCase.class)
                        .with(OrderCase::getUrn, is(input.getHearing().getProsecutionCases().get(0).getProsecutionCaseIdentifier().getCaseURN()))


                        .with(OrderCase::getCaseResults, first(isBean(OrderResult.class)
                                .with(OrderResult::getLabel, is(input.getSharedResultLines().get(0).getLabel()))
                                .with(OrderResult::getPrompts, first(isBean(OrderPrompt.class)
                                        .with(OrderPrompt::getLabel, is(input.getSharedResultLines().get(0).getPrompts().get(0).getLabel()))
                                        .with(OrderPrompt::getValue, is(input.getSharedResultLines().get(0).getPrompts().get(0).getValue()))
                                ))
                        ))

                        .with(OrderCase::getDefendantCaseResults, first(isBean(OrderResult.class)
                                .with(OrderResult::getLabel, is(input.getSharedResultLines().get(1).getLabel()))
                                .with(OrderResult::getPrompts, first(isBean(OrderPrompt.class)
                                        .with(OrderPrompt::getLabel, is(input.getSharedResultLines().get(1).getPrompts().get(0).getLabel()))
                                        .with(OrderPrompt::getValue, is(input.getSharedResultLines().get(1).getPrompts().get(0).getValue()))
                                ))
                        ))
                        .with(OrderCase::getDefendantCaseOffences, first(isBean(DefendantCaseOffence.class)
                                .with(DefendantCaseOffence::getWording, is(input.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getWording()))
                                .with(DefendantCaseOffence::getStartDate, is(input.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getStartDate().toString()))
                                .with(DefendantCaseOffence::getConvictionDate, is(""))

                                .with(DefendantCaseOffence::getResults, first(isBean(OrderResult.class)
                                        .with(OrderResult::getLabel, is(input.getSharedResultLines().get(2).getLabel()))
                                        .with(OrderResult::getPrompts, first(isBean(OrderPrompt.class)
                                                .with(OrderPrompt::getLabel, is(input.getSharedResultLines().get(2).getPrompts().get(0).getLabel()))
                                                .with(OrderPrompt::getValue, is(input.getSharedResultLines().get(2).getPrompts().get(0).getValue()))
                                        ))
                                ))

                        ))
                ))
                .with(NowsDocumentOrder::getNowText, is(input.getNowTypes().get(0).getStaticText()))
        );

        assertThat(nowsNotificationDocumentState, isBean(NowsNotificationDocumentState.class)
                .with(NowsNotificationDocumentState::getOriginatingCourtCentreId, is(input.getHearing().getCourtCentre().getId()))
                .with(NowsNotificationDocumentState::getUsergroups, hasItems(input.getNows().get(0).getMaterials().get(0).getUserGroups().get(0).getGroup()))
                .with(NowsNotificationDocumentState::getNowsTypeId, is(input.getNows().get(0).getNowsTypeId()))
                .with(NowsNotificationDocumentState::getCaseUrns,  hasItems(input.getHearing().getProsecutionCases().get(0).getProsecutionCaseIdentifier().getCaseURN()))
                .with(NowsNotificationDocumentState::getCourtClerkName, is(format("%s %s", input.getCourtClerk().getFirstName(), input.getCourtClerk().getLastName())))
                .with(NowsNotificationDocumentState::getDefendantName, is(defendantName))
                .with(NowsNotificationDocumentState::getJurisdiction, is(input.getNowTypes().get(0).getJurisdiction()))
                .with(NowsNotificationDocumentState::getCourtCentreName, is(input.getHearing().getCourtCentre().getName()))
                .with(NowsNotificationDocumentState::getOrderName,  is(input.getNowTypes().get(0).getDescription()))
                .with(NowsNotificationDocumentState::getPriority,  is(input.getNowTypes().get(0).getPriority()))
                .with(NowsNotificationDocumentState::getMaterialId, is(input.getNows().get(0).getMaterials().get(0).getId()))
        );
    }


}
