package uk.gov.justice.ccr.notepad;


import static java.util.stream.Collectors.toList;

import uk.gov.justice.ccr.notepad.process.Knowledge;
import uk.gov.justice.ccr.notepad.process.Processor;
import uk.gov.justice.ccr.notepad.view.Part;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

public class ParsingFacade {

    @Inject
    Processor processor;


    public Knowledge processParts(final List<Part> parts) throws ExecutionException {
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());
        return processor.processParts(values);
    }

    public Knowledge processPrompt(final String resultDefinitionId) throws ExecutionException {
        return processor.processResultPrompt(resultDefinitionId);
    }

    public void lazyLoad(final JsonEnvelope envelope) throws ExecutionException {
        processor.lazyLoad(envelope);
    }

}
