package uk.gov.moj.cpp.hearing.command.handler.service.validation;

public class PromptDto {

    private final String promptRef;
    private final String promptValue;

    public PromptDto(final String promptRef, final String promptValue) {
        this.promptRef = promptRef;
        this.promptValue = promptValue;
    }

    public String getPromptRef() { return promptRef; }
    public String getPromptValue() { return promptValue; }
}
