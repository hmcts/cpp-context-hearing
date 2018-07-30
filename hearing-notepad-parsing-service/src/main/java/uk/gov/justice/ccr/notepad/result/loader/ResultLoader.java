package uk.gov.justice.ccr.notepad.result.loader;


import uk.gov.justice.ccr.notepad.result.cache.model.ResultDefinition;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultDefinitionSynonym;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultPrompt;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultPromptSynonym;

import java.time.LocalDate;
import java.util.List;

public interface ResultLoader {

    List<ResultDefinition> loadResultDefinition(final LocalDate orderedDate);

    List<ResultDefinitionSynonym> loadResultDefinitionSynonym(final LocalDate orderedDate);

    List<ResultPrompt> loadResultPrompt(final LocalDate orderedDate);

    List<ResultPromptSynonym> loadResultPromptSynonym(final LocalDate orderedDate);

}
