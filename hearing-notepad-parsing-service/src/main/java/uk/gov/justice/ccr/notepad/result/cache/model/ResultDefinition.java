package uk.gov.justice.ccr.notepad.result.cache.model;

import static java.util.stream.Collectors.toCollection;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

public class ResultDefinition {
    private String id;
    private String label;
    private String shortCode;
    private String level;
    private Set<String> keywords;

    public ResultDefinition() {

    }

    public ResultDefinition(final String id, final String label, final String shortCode,
                            final String level, final Set<String> keywords) {
        this.id = id;
        this.label = label;
        this.shortCode = shortCode;
        this.level = level;
        this.keywords = keywords;
    }

    public final String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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
        return Optional.ofNullable(keywords).orElse(new HashSet<>());
    }

    public final void setKeywords(List<String> keywords) {
        if (!keywords.isEmpty()) {
            this.keywords = keywords.stream().filter(v -> !v.isEmpty()).distinct().collect(toCollection(TreeSet::new));
        }
    }

    @Override
    public String toString() {
        return "ResultDefinition{" +
                "id='" + id + '\'' +
                ", label='" + label + '\'' +
                ", shortCode='" + shortCode + '\'' +
                ", level='" + level + '\'' +
                ", keywords=" + keywords +
                '}';
    }
}
