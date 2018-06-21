package uk.gov.moj.cpp.hearing.event.nows;

import org.junit.Test;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.moj.cpp.hearing.event.nows.order.NowsDocumentOrder;
import uk.gov.moj.cpp.hearing.event.nows.order.OrderCase;
import uk.gov.moj.cpp.hearing.nows.events.Defendant;
import uk.gov.moj.cpp.hearing.nows.events.NowsRequested;
import uk.gov.moj.cpp.hearing.nows.events.SharedResultLine;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class NowsRequestedToOrderConvertorTest {

    @Test
    public void testConvertion() throws IOException {
        final InputStream is = NowsRequestedToOrderConvertorTest.class.getResourceAsStream("/data/hearing.events.nows-requested.json");
        NowsRequested nowsRequested = new ObjectMapperProducer().objectMapper().readValue(is, NowsRequested.class);
        List<NowsDocumentOrder> nowsDocumentOrders = NowsRequestedToOrderConvertor.convert(nowsRequested);

        NowsDocumentOrder nowsDocumentOrder = nowsDocumentOrders.get(0);
        assertThat(nowsRequested.getHearing().getNows().get(0).getMaterial().get(0).getId(), is(nowsDocumentOrder.getMaterialId()));
        assertThat(nowsRequested.getHearing().getNowTypes().get(0).getStaticText(), is(nowsDocumentOrder.getNowText()));
        assertThat(nowsRequested.getHearing().getNowTypes().get(0).getPriority(), is(nowsDocumentOrder.getPriority()));
        assertThat(nowsRequested.getHearing().getNowTypes().get(0).getDescription(), is(nowsDocumentOrder.getOrderName()));
        assertThat(nowsRequested.getHearing().getCourtCentre().getCourtCentreName(), is(nowsDocumentOrder.getCourtCentreName()));
        assertThat("Cherie Blair", is(nowsDocumentOrder.getCourtClerkName()));
        assertThat(getSharedResultLines(nowsRequested).get(0).getOrderedDate(), is(nowsDocumentOrder.getOrderDate()));

        assertThat(getDefendants(nowsRequested).get(0).getCases().get(0).getUrn(), is(nowsDocumentOrder.getCaseUrns().get(0)));
        assertThat(getDefendants(nowsRequested).get(0).getCases().get(1).getUrn(), is(nowsDocumentOrder.getCaseUrns().get(1)));
        assertThat("Mr David  LLOYD", is(nowsDocumentOrder.getDefendant().getName()));
        assertThat(getDefendants(nowsRequested).get(0).getPerson().getAddress().getPostCode(), is(nowsDocumentOrder.getDefendant().getAddress().getPostCode()));
//defendantCaseResults
        assertThat(getDefendants(nowsRequested).get(0).getCases().get(0).getUrn(), is(getOrderCase(nowsDocumentOrder).getDefendantCaseResults().get(0).getUrn()));
        assertThat(getSharedResultLines(nowsRequested).get(0).getPrompts().get(0).getLabel(), is(getOrderCase(nowsDocumentOrder).getDefendantCaseResults().get(0).getPrompts().get(0).getLabel()));
        assertThat(getSharedResultLines(nowsRequested).get(0).getPrompts().get(0).getValue(), is(getOrderCase(nowsDocumentOrder).getDefendantCaseResults().get(0).getPrompts().get(0).getValue()));

//defendantCaseOffences
        assertThat(getSharedResultLines(nowsRequested).get(2).getLabel(), is(getOrderCase(nowsDocumentOrder).getDefendantCaseOffences().get(0).getResults().get(0).getLabel()));
        assertThat(getSharedResultLines(nowsRequested).get(2).getPrompts().get(0).getLabel(), is(getOrderCase(nowsDocumentOrder).getDefendantCaseOffences().get(0).getResults().get(0).getPrompts().get(0).getLabel()));
        assertThat(getSharedResultLines(nowsRequested).get(2).getPrompts().get(0).getValue(), is(getOrderCase(nowsDocumentOrder).getDefendantCaseOffences().get(0).getResults().get(0).getPrompts().get(0).getValue()));
//caseResults
        assertThat(getSharedResultLines(nowsRequested).get(1).getLabel(), is(getOrderCase(nowsDocumentOrder).getDefendantCaseOffences().get(0).getResults().get(0).getLabel()));
        assertThat(getSharedResultLines(nowsRequested).get(1).getPrompts().get(0).getLabel(), is(getOrderCase(nowsDocumentOrder).getCaseResults().get(0).getPrompts().get(0).getLabel()));
        assertThat(getSharedResultLines(nowsRequested).get(1).getPrompts().get(0).getValue(), is(getOrderCase(nowsDocumentOrder).getCaseResults().get(0).getPrompts().get(0).getValue()));

    }

    private OrderCase getOrderCase(NowsDocumentOrder nowsDocumentOrder) {
        return nowsDocumentOrder.getCases().get(0);
    }

    private List<Defendant> getDefendants(NowsRequested nowsRequested) {
        return nowsRequested.getHearing().getDefendants();
    }

    private List<SharedResultLine> getSharedResultLines(NowsRequested nowsRequested) {
        return nowsRequested.getHearing().getSharedResultLines();
    }


}
