package uk.gov.justice.ccr.notepad.result.cache.model;


import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

public class ResultDefinition {
    private final String id = UUID.randomUUID().toString();
    private String label;
    private String shortCode;
    private String level;
    private Set<String> keywords = new TreeSet<>();

    public final String getLabel() {
        return label;
    }

    public final void setLabel(final String value) {
        this.label = value;
    }

    public final String getShortCode() {
        return shortCode;
    }

    public final void setShortCode(final String value) {
        this.shortCode = value;
    }

    public final String getLevel() {
        return level;
    }

    public final void setLevel(final String value) {
        this.level = value;
    }

    public final Set<String> getKeywords() {
        return keywords;
    }

    public final void setKeywords(List<String> keywords) {
        if (!keywords.isEmpty()) {
            this.keywords = new TreeSet<>(keywords.stream().filter(v -> !v.isEmpty()).collect(Collectors.toSet()));
        }
    }

    public final String getId() {
        return id;
    }
}
