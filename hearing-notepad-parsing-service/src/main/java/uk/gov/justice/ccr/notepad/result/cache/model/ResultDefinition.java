package uk.gov.justice.ccr.notepad.result.cache.model;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toCollection;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.collections.CollectionUtils;

public class ResultDefinition {
    private String id;
    private String label;
    private String shortCode;
    private String level;
    private Set<String> keywords;
    private Boolean terminatesOffenceProceedings;
    private Boolean lifeDuration;
    private List<ChildResultDefinition> childResultDefinitions = new ArrayList<>();
    private Boolean publishedAsAPrompt;
    private Boolean excludedFromResults;
    private Boolean alwaysPublished;
    private Boolean urgent;
    private Boolean d20;
    private Boolean rollUpPrompts;
    private Boolean publishedForNows;
    private Boolean conditionalMandatory;
    private String dvlaCode;
    private String resultDefinitionGroup;

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

    public Boolean getTerminatesOffenceProceedings() {
        return terminatesOffenceProceedings;
    }

    public void setTerminatesOffenceProceedings(final Boolean terminatesOffenceProceedings) {
        this.terminatesOffenceProceedings = terminatesOffenceProceedings;
    }

    public Boolean getLifeDuration() {
        return lifeDuration;
    }

    public void setLifeDuration(final Boolean lifeDuration) {
        this.lifeDuration = lifeDuration;
    }

    public Boolean getPublishedAsAPrompt() {
        return publishedAsAPrompt;
    }

    public void setPublishedAsAPrompt(final Boolean publishedAsAPrompt) {
        this.publishedAsAPrompt = publishedAsAPrompt;
    }

    public Boolean getExcludedFromResults() {
        return excludedFromResults;
    }

    public void setExcludedFromResults(final Boolean excludedFromResults) {
        this.excludedFromResults = excludedFromResults;
    }

    public Boolean getAlwaysPublished() {
        return alwaysPublished;
    }

    public void setAlwaysPublished(final Boolean alwaysPublished) {
        this.alwaysPublished = alwaysPublished;
    }

    public Boolean getUrgent() {
        return urgent;
    }

    public void setUrgent(final Boolean urgent) {
        this.urgent = urgent;
    }

    public Boolean getD20() {
        return d20;
    }

    public void setD20(final Boolean d20) {
        this.d20 = d20;
    }

    public Boolean getRollUpPrompts() {
        return rollUpPrompts;
    }

    public void setRollUpPrompts(final Boolean rollUpPrompts) {
        this.rollUpPrompts = rollUpPrompts;
    }

    public Boolean getPublishedForNows() {
        return publishedForNows;
    }

    public void setPublishedForNows(final Boolean publishedForNows) {
        this.publishedForNows = publishedForNows;
    }

    public Boolean getConditonalMandatory() {
        return conditionalMandatory;
    }

    public void setConditonalMandatory(Boolean conditonalMandatory) {
        this.conditionalMandatory = conditonalMandatory;
    }

    public String getDvlaCode() {
        return dvlaCode;
    }

    public void setDvlaCode(final String dvlaCode) {
        this.dvlaCode = dvlaCode;
    }

    public String getResultDefinitionGroup() {
        return resultDefinitionGroup;
    }

    public void setResultDefinitionGroup(final String resultDefinitionGroup) {
        this.resultDefinitionGroup = resultDefinitionGroup;
    }

    @Override
    public String toString() {
        return "ResultDefinition{" +
                "id='" + id + '\'' +
                ", label='" + label + '\'' +
                ", shortCode='" + shortCode + '\'' +
                ", level='" + level + '\'' +
                ", keywords=" + keywords +
                ", terminatesOffenceProceedings=" + terminatesOffenceProceedings +
                ", isLifeDuration=" + lifeDuration +
                ", childResultDefinitions=" + childResultDefinitions +
                ", isPublishedAsAPrompt=" + publishedAsAPrompt +
                ", isExcludedFromResults=" + excludedFromResults +
                ", isAlwaysPublished=" + alwaysPublished +
                ", isUrgent=" + urgent +
                ", isD20=" + d20 +
                ", isRollUpPrompts=" + rollUpPrompts +
                ", isPublishedForNows=" + publishedForNows +
                ", isConditionalMandatory=" + conditionalMandatory +
                ", dvlaCode= " + dvlaCode +
                ", resultDefinitionGroup= " + resultDefinitionGroup +
                '}';
    }

    public static Builder builder() {
        return new ResultDefinition.Builder();
    }

    public List<ChildResultDefinition> getChildResultDefinitions() {
        return childResultDefinitions;
    }

    public void setChildResultDefinitions(final List<ChildResultDefinition> childResultDefinitions) {
        if (childResultDefinitions != null) {
            this.childResultDefinitions = new ArrayList<>(childResultDefinitions);
        } else {
            this.childResultDefinitions = null;
        }
    }


    public static class Builder {

        private String id;
        private String label;
        private String shortCode;
        private String level;
        private Set<String> keywords;
        private Boolean terminatesOffenceProceedings;
        private Boolean isLifeDuration;
        private Boolean isPublishedAsAPrompt;
        private Boolean isExcludedFromResults;
        private Boolean isAlwaysPublished;
        private Boolean isUrgent;
        private Boolean isD20;
        private Boolean isRollUpPrompts;
        private Boolean isPublishedForNows;
        private Boolean isConditonalMandatory;
        private List<ChildResultDefinition> isChildResultDefinitions;
        private String dvlaCode;
        private String resultDefinitionGroup;

        public Builder withId(final String id) {
            this.id = id;
            return this;
        }

        public Builder withLabel(final String label) {
            this.label = label;
            return this;
        }

        public Builder withShortCode(final String shortCode) {
            this.shortCode = shortCode;
            return this;
        }

        public Builder withLevel(final String level) {
            this.level = level;
            return this;
        }

        public Builder withKeywords(final Set<String> keywords) {
            this.keywords = new HashSet<>(keywords);
            return this;
        }

        public Builder withTerminatesOffenceProceedings(final Boolean terminatesOffenceProceedings) {
            this.terminatesOffenceProceedings = terminatesOffenceProceedings;
            return this;
        }

        public Builder withLifeDuration(final Boolean lifeDuration) {
            isLifeDuration = lifeDuration;
            return this;
        }

        public Builder withPublishedAsAPrompt(final Boolean publishedAsAPrompt) {
            isPublishedAsAPrompt = publishedAsAPrompt;
            return this;
        }

        public Builder withExcludedFromResults(final Boolean excludedFromResults) {
            isExcludedFromResults = excludedFromResults;
            return this;
        }

        public Builder withAlwaysPublished(final Boolean alwaysPublished) {
            isAlwaysPublished = alwaysPublished;
            return this;
        }

        public Builder withUrgent(final Boolean urgent) {
            isUrgent = urgent;
            return this;
        }

        public Builder withD20(final Boolean d20) {
            isD20 = d20;
            return this;
        }

        public Builder withRollUpPrompts(final Boolean rollUpPrompts) {
            isRollUpPrompts = rollUpPrompts;
            return this;
        }

        public Builder withPublishedForNows(final Boolean publishedForNows) {
            isPublishedForNows = publishedForNows;
            return this;
        }

        public Builder withChildResultDefinitions(final List<ChildResultDefinition> childResultDefinitions) {
            isChildResultDefinitions = new ArrayList<>(childResultDefinitions);
            return this;
        }

        public Builder withConditionalMandatory(final Boolean conditionalMandatory) {
            isConditonalMandatory = conditionalMandatory;
            return this;
        }

        public Builder withDvlaCode(final String dvlaCode) {
            this.dvlaCode = dvlaCode;
            return this;
        }

        public Builder withResultDefinitionGroup(final String resultDefinitionGroup) {
            this.resultDefinitionGroup = resultDefinitionGroup;
            return this;
        }

        public ResultDefinition build() {
            final ResultDefinition resultDefinition = new ResultDefinition();
            resultDefinition.setId(this.id);
            resultDefinition.setLabel(this.label);
            resultDefinition.setShortCode(this.shortCode);
            resultDefinition.setLevel(this.level);
            resultDefinition.setKeywords(this.keywords != null ? new ArrayList<>(this.keywords) : emptyList());
            resultDefinition.setTerminatesOffenceProceedings(this.terminatesOffenceProceedings);
            resultDefinition.setLifeDuration(this.isLifeDuration);
            resultDefinition.setPublishedAsAPrompt(this.isPublishedAsAPrompt);
            resultDefinition.setExcludedFromResults(this.isExcludedFromResults);
            resultDefinition.setAlwaysPublished(this.isAlwaysPublished);
            resultDefinition.setUrgent(this.isUrgent);
            resultDefinition.setD20(this.isD20);
            resultDefinition.setRollUpPrompts(this.isRollUpPrompts);
            resultDefinition.setPublishedForNows(this.isPublishedForNows);
            resultDefinition.setConditonalMandatory(this.isConditonalMandatory);
            resultDefinition.setChildResultDefinitions(CollectionUtils.isNotEmpty(this.isChildResultDefinitions) ? new ArrayList<>(this.isChildResultDefinitions) : emptyList());
            resultDefinition.setDvlaCode(this.dvlaCode);
            resultDefinition.setResultDefinitionGroup(this.resultDefinitionGroup);
            return resultDefinition;
        }
    }
}
