package uk.gov.moj.cpp.hearing.listing.domain.referencedata;

import java.io.Serializable;
import java.util.UUID;

@SuppressWarnings("PMD.BeanMembersShouldSerialize")
public class Judiciary implements Serializable {

    private UUID id;

    private String titleSuffix;

    private String titlePrefix;

    private String titleJudicialPrefix;

    private String surname;

    private String forenames;

    private String requestedName;

    private String judiciaryType;

    public Judiciary(final UUID id, final String titleSuffix, final String titlePrefix, final String titleJudicialPrefix,
                     final String surname, final String forenames, final String requestedName, final String judiciaryType) {
        this.id = id;
        this.titleSuffix = titleSuffix;
        this.titlePrefix = titlePrefix;
        this.titleJudicialPrefix = titleJudicialPrefix;
        this.surname = surname;
        this.forenames = forenames;
        this.requestedName = requestedName;
        this.judiciaryType = judiciaryType;
    }

    public Judiciary() {
    }

    public UUID getId() {
        return id;
    }

    public String getTitlePrefix() {
        return titlePrefix;
    }

    public String getTitleJudicialPrefix() {
        return titleJudicialPrefix;
    }

    public String getTitleSuffix() {
        return titleSuffix;
    }

    public String getSurname() {
        return surname;
    }

    public String getForenames() {
        return forenames;
    }

    public String getRequestedName() {
        return requestedName;
    }

    public String getJudiciaryType() {
        return judiciaryType;
    }


    public static class Builder {
        private UUID id;
        private String titleSuffix;
        private String titlePrefix;
        private String titleJudicialPrefix;
        private String surname;
        private String forenames;
        private String requestedName;
        private String judiciaryType;

        public Judiciary.Builder withId(final UUID id) {
            this.id = id;
            return this;
        }

        public Judiciary.Builder withTitlePrefix(final String titlePrefix) {
            this.titlePrefix = titlePrefix;
            return this;
        }

        public Judiciary.Builder withTitleJudicialPrefix(final String titleJudicialPrefix) {
            this.titleJudicialPrefix = titleJudicialPrefix;
            return this;
        }

        public Judiciary.Builder withTitleSuffix(final String titleSuffix) {
            this.titleSuffix = titleSuffix;
            return this;
        }

        public Judiciary.Builder withSurname(final String surname) {
            this.surname = surname;
            return this;
        }

        public Judiciary.Builder withForenames(final String forenames) {
            this.forenames = forenames;
            return this;
        }

        public Judiciary.Builder withRequestedName(final String requestedName) {
            this.requestedName = requestedName;
            return this;
        }

        public Judiciary.Builder withJudiciaryType(final String judiciaryType) {
            this.judiciaryType = judiciaryType;
            return this;
        }

        public Judiciary build() {
            return new Judiciary(id, titleSuffix, titlePrefix, titleJudicialPrefix, surname, forenames, requestedName, judiciaryType);
        }
    }
}
