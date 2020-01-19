package uk.gov.moj.cpp.hearing.event.nows.mapper;

import static java.util.Objects.isNull;
import uk.gov.justice.core.courts.ResultPrompt;
import uk.gov.justice.core.courts.SharedResultLine;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class AbstractStagingEnforcementMapper {

    protected static final String EMPTY_STRING = "";
    protected static final String REGEX_ONLY_NUMBERS = "[^\\d*.\\d+|\\d+\\.\\d*$]";
    protected static final String INCOMING_PROMPT_DATE_FORMAT = "yyyy-MM-dd";
    protected static final String OUTGOING_PROMPT_DATE_FORMAT = "dd MMM yyyy";

    private final List<SharedResultLine> sharedResultLines;

    AbstractStagingEnforcementMapper(final List<SharedResultLine> sharedResultLines) {
        this.sharedResultLines = sharedResultLines;
    }

    /*
    search sharedREsultsLines for promptmatching reference, exclude prompts with references not in
    promptRefsList.
    */
    protected String getPromptValue(List<UUID> promptRefsList, String reference) {
        return getPromptValue(promptRefsList, reference, null);
    }

    /*
    search sharedREsultsLines for promptmatching reference, exclude prompts with references not in
    promptRefsList.
    */
    protected String getPromptValue(List<UUID> promptRefsList, String reference, UUID sharedResultLineId) {

        return sharedResultLines.stream()
                .filter(r -> (isNull(r.getIsDeleted()) || !r.getIsDeleted()))
                .filter(sharedResultLine -> sharedResultLine.getPrompts()!=null)
                .filter(sharedResultLine->sharedResultLineId==null || sharedResultLineId.equals(sharedResultLine.getId()))
                .flatMap(sharedResultLine -> sharedResultLine.getPrompts().stream())
                .filter(sharedPrompt -> reference.equals(sharedPrompt.getPromptReference()))
                .filter(sharedPrompt -> promptRefsList.stream().anyMatch(uuid->sharedPrompt.getId().equals(uuid)))
                .map(ResultPrompt::getValue)
                .findFirst()
                .orElse(null);
    }

    protected BigDecimal getStringAsDecimal(final String value) {
        return new BigDecimal(value.replaceAll(REGEX_ONLY_NUMBERS, EMPTY_STRING));
    }
}
