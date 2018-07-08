package uk.gov.moj.cpp.hearing.event;

import static java.util.Collections.emptyList;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.Variant;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.event.delegates.AdjournHearingDelegate;
import uk.gov.moj.cpp.hearing.event.delegates.GenerateNowsDelegate;
import uk.gov.moj.cpp.hearing.event.delegates.PublishResultsDelegate;
import uk.gov.moj.cpp.hearing.event.delegates.SaveNowVariantsDelegate;
import uk.gov.moj.cpp.hearing.event.delegates.UpdateResultLineStatusDelegate;
import uk.gov.moj.cpp.hearing.event.nows.NowsGenerator;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Nows;

import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(EVENT_PROCESSOR)
public class PublishResultsEventProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(PublishResultsEventProcessor.class.getName());

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;
    @Inject
    private NowsGenerator nowsGenerator;
    @Inject
    private GenerateNowsDelegate generateNowsDelegate;
    @Inject
    private SaveNowVariantsDelegate saveNowVariantsDelegate;
    @Inject
    private UpdateResultLineStatusDelegate updateResultLineStatusDelegate;
    @Inject
    private PublishResultsDelegate publishResultsDelegate;
    @Inject
    private AdjournHearingDelegate adjournHearingDelegate;
    @Inject
    private Sender sender;

    @Handles("hearing.results-shared")
    public void resultsShared(final JsonEnvelope event) {
        LOGGER.info("hearing.results-shared event received {}", event.payloadAsJsonObject());

        final ResultsShared resultsShared = this.jsonObjectToObjectConverter
                .convert(event.payloadAsJsonObject(), ResultsShared.class);

        adjournHearingDelegate.execute(resultsShared, event);

        this.nowsGenerator.setContext(event);

        final List<Nows> nows = nowsGenerator.createNows(resultsShared);

        List<Variant> newVariants = emptyList();

        if (!nows.isEmpty()) {

            newVariants = saveNowVariantsDelegate.saveNowsVariants(sender, event, nows, resultsShared);

            generateNowsDelegate.generateNows(sender, event, nows, resultsShared);
        }

        updateResultLineStatusDelegate.updateResultLineStatus(sender, event, resultsShared);

        publishResultsDelegate.shareResults(sender, event, resultsShared, newVariants);
    }
}
