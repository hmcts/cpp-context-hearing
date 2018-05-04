package uk.gov.moj.cpp.hearing.event.nows;

import org.junit.Test;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.moj.cpp.hearing.event.nows.domain.NowsOrder;
import uk.gov.moj.cpp.hearing.nows.events.NowsRequested;

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
        List<NowsOrder> nowsOrders = NowsRequestedToOrderConvertor.convert(nowsRequested);
        NowsOrder nowsOrder = nowsOrders.get(0);
        assertThat("Imprisonment Order", is(nowsOrder.getOrderName()));
        assertThat("Liverpool Crown Court", is(nowsOrder.getCourtCentre().getCourtCentreName()));
        assertThat("Cherie Blair", is(nowsOrder.getCourtClerkName()));
        assertThat("URN12345", is(nowsOrder.getCaseUrns().get(0)));
        assertThat("Mr David  LLOYD", is(nowsOrder.getDefendant().getName()));
        assertThat("W1T 1JY", is(nowsOrder.getDefendant().getAddress().getPostCode()));
        assertThat("URN12345", is(nowsOrder.getCases().get(0).getUrn()));
        assertThat("2016-06-21", is(nowsOrder.getCases().get(0).getDefendantCaseOffences().get(0).getStartDate()));
        assertThat("Imprisonment", is(nowsOrder.getCases().get(0).getDefendantCaseOffences().get(0).getResults().get(0).getLabel()));
        assertThat("Imprisonment duration", is(nowsOrder.getCases().get(0).getDefendantCaseOffences().get(0).getResults().get(0).getPrompts().get(0).getLabel()));
    }
}
