package uk.gov.moj.cpp.hearing.command.api;


import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;

import uk.gov.justice.services.core.interceptor.InterceptorChainEntry;
import uk.gov.justice.services.core.interceptor.InterceptorChainEntryProvider;
import uk.gov.moj.cpp.authorisation.interceptor.SynchronousFeatureControlInterceptor;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class HearingCommandApiInterceptorChainProvider implements InterceptorChainEntryProvider {

    @Override
    public String component() {
        return COMMAND_API;
    }

    @Override
    public List<InterceptorChainEntry> interceptorChainTypes() {
        final List<InterceptorChainEntry> pairs = new ArrayList<>();
        pairs.add(new InterceptorChainEntry(5900, SynchronousFeatureControlInterceptor.class));
        return pairs;
    }
}
