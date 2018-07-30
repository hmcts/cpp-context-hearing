package uk.gov.justice.ccr.notepad;


import static java.util.stream.Collectors.toList;

import uk.gov.justice.ccr.notepad.process.Knowledge;
import uk.gov.justice.ccr.notepad.process.Processor;
import uk.gov.justice.ccr.notepad.view.Part;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.time.LocalDate;
import java.util.List;

import javax.inject.Inject;

public class ParsingFacade {

    @Inject
    Processor processor;


    public Knowledge processParts(final List<Part> parts, final LocalDate orderedDate) {
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());
        return processor.processParts(values, orderedDate);
    }

    public Knowledge processPrompt(final String resultDefinitionId, final LocalDate orderedDate) {
        return processor.processResultPrompt(resultDefinitionId, orderedDate);
    }

    public void lazyLoad(final JsonEnvelope envelope, final LocalDate orderedDate) {
        processor.lazyLoad(envelope, orderedDate);
    }

}
