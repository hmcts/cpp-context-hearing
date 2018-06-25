package uk.gov.moj.cpp.hearing.event;

import org.apache.commons.validator.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.Variant;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.event.delegates.GenerateNowsDelegate;
import uk.gov.moj.cpp.hearing.event.delegates.PublishResultsDelegate;
import uk.gov.moj.cpp.hearing.event.delegates.SaveNowVariantsDelegate;
import uk.gov.moj.cpp.hearing.event.delegates.UpdateResultLineStatusDelegate;
import uk.gov.moj.cpp.hearing.event.nows.NowsGenerator;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Nows;

import javax.inject.Inject;
import java.util.List;

import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

@ServiceComponent(EVENT_PROCESSOR)
public class PublishResultsEventProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(PublishResultsEventProcessor.class.getName());

    private final JsonObjectToObjectConverter jsonObjectToObjectConverter;

    private Sender sender;

    private final NowsGenerator nowsGenerator;

    private final GenerateNowsDelegate generateNowsDelegate;

    private final SaveNowVariantsDelegate saveNowVariantsDelegate;

    private final UpdateResultLineStatusDelegate updateResultLineStatusDelegate;

    private final PublishResultsDelegate publishResultsDelegate;

    @Inject
    public PublishResultsEventProcessor(final JsonObjectToObjectConverter jsonObjectToObjectConverter,
                                        final Sender sender,
                                        final NowsGenerator nowsGenerator,
                                        final GenerateNowsDelegate generateNowsDelegate,
                                        final SaveNowVariantsDelegate saveNowVariantsDelegate,
                                        final UpdateResultLineStatusDelegate updateResultLineStatusDelegate,
                                        final PublishResultsDelegate publishResultsDelegate) {
        this.jsonObjectToObjectConverter = jsonObjectToObjectConverter;
        this.sender = sender;
        this.nowsGenerator = nowsGenerator;
        this.generateNowsDelegate = generateNowsDelegate;
        this.saveNowVariantsDelegate = saveNowVariantsDelegate;
        this.updateResultLineStatusDelegate = updateResultLineStatusDelegate;
        this.publishResultsDelegate = publishResultsDelegate;
    }

    @Handles("hearing.results-shared")
    public void resultsShared(final JsonEnvelope event) {
        LOGGER.debug("hearing.results-shared event received {}", event.payloadAsJsonObject());

        final ResultsShared resultsShared = this.jsonObjectToObjectConverter
                .convert(event.payloadAsJsonObject(), ResultsShared.class);

        this.nowsGenerator.setContext(event);

        final List<Nows> nows = nowsGenerator.createNows(resultsShared);

        List<Variant> newVariants = null;

        if (!nows.isEmpty()) {

            newVariants = saveNowVariantsDelegate.saveNowsVariants(sender, event, nows, resultsShared);

            generateNowsDelegate.generateNows(sender, event, nows, resultsShared);
        }

        updateResultLineStatusDelegate.updateResultLineStatus(sender, event, resultsShared);

        publishResultsDelegate.shareResults(sender, event, resultsShared, newVariants);
    }
}
