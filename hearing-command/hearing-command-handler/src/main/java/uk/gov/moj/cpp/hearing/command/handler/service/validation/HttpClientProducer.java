package uk.gov.moj.cpp.hearing.command.handler.service.validation;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

@ApplicationScoped
public class HttpClientProducer {

    @Produces
    @ApplicationScoped
    public HttpClient createHttpClient() {
        return HttpClientBuilder.create().build();
    }
}
