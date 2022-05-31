package uk.gov.moj.cpp.hearing.query.view.response;

import java.util.List;
import java.util.UUID;

@SuppressWarnings("squid:S2384")
public class Application {

    private final UUID applicationId;
    private final List<Person> applicants;
    private final List<Person> respondents;
    private final List<Person> subjects;

    public Application(final ApplicationBuilder builder) {
        this.applicationId = builder.applicationId;
        this.applicants = builder.applicants;
        this.respondents = builder.respondents;
        this.subjects = builder.subjects;
    }

    public UUID getApplicationId() {
        return applicationId;
    }

    public List<Person> getApplicants() {
        return applicants;
    }


    public List<Person> getRespondents() {
        return respondents;
    }

    public List<Person> getSubjects() {
        return subjects;
    }

    public static class ApplicationBuilder {
        private UUID applicationId;

        private List<Person> applicants;
        private List<Person> respondents;
        private List<Person> subjects;


        public ApplicationBuilder withApplicationId(final UUID applicationId) {
            this.applicationId = applicationId;
            return this;
        }

        public ApplicationBuilder withApplicants(final List<Person> applicants) {
            this.applicants = applicants;
            return this;
        }

        public ApplicationBuilder withRespondents(final List<Person> respondents) {
            this.respondents = respondents;
            return this;
        }

        public ApplicationBuilder withSubjects(final List<Person> subjects) {
            this.subjects = subjects;
            return this;
        }


        public Application build() {
            return new Application(this);
        }
    }
}