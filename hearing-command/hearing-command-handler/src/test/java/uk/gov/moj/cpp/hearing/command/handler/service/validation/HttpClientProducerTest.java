package uk.gov.moj.cpp.hearing.command.handler.service.validation;

import org.junit.jupiter.api.Test;

class HttpClientProducerTest {

    @Test
    void close_is_safe_when_no_client_was_created() {
        new HttpClientProducer().close();
    }
}
