package uk.gov.justice.ccr.notepad.result.loader;


import uk.gov.justice.ccr.notepad.result.cache.model.ResultDefinition;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultDefinitionSynonym;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultPrompt;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultPromptSynonym;

import java.util.List;

public interface ResultLoader {

    List<ResultDefinition> loadResultDefinition();

    List<ResultDefinitionSynonym> loadResultDefinitionSynonym();

    List<ResultPrompt> loadResultPrompt();

    List<ResultPromptSynonym> loadResultPromptSynonym();

}
