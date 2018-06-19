package uk.gov.moj.cpp.hearing.event.nows;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Test;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.moj.cpp.hearing.event.nows.order.NowsDocumentOrder;
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
        List<NowsDocumentOrder> nowsDocumentOrders = NowsRequestedToOrderConvertor.convert(nowsRequested);
        NowsDocumentOrder nowsDocumentOrder = nowsDocumentOrders.get(0);
        assertThat("Imprisonment Order", is(nowsDocumentOrder.getOrderName()));
        assertThat("Liverpool Crown Court", is(nowsDocumentOrder.getCourtCentreName()));
        assertThat("Cherie Blair", is(nowsDocumentOrder.getCourtClerkName()));
        assertThat("URN12345", is(nowsDocumentOrder.getCaseUrns().get(0)));
        assertThat("Mr David  LLOYD", is(nowsDocumentOrder.getDefendant().getName()));
        assertThat("W1T 1JY", is(nowsDocumentOrder.getDefendant().getAddress().getPostCode()));
        assertThat("URN12345", is(nowsDocumentOrder.getCases().get(0).getDefendantCaseResults().get(0).getUrn()));
        assertThat("2016-06-21", is(nowsDocumentOrder.getCases().get(0).getDefendantCaseOffences().get(0).getStartDate()));
        assertThat("Imprisonment", is(nowsDocumentOrder.getCases().get(0).getDefendantCaseOffences().get(0).getResults().get(0).getLabel()));
        assertThat("Imprisonment duration", is(nowsDocumentOrder.getCases().get(0).getDefendantCaseOffences().get(0).getResults().get(0).getPrompts().get(0).getLabel()));

    }


}
