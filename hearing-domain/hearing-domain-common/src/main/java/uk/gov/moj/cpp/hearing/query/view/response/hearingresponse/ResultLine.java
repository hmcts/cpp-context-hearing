package uk.gov.moj.cpp.hearing.query.view.response.hearingresponse;

public final class ResultLine {

    private final String id;
    private final String lastSharedDateTime;

    private ResultLine(final Builder builder) {
        this.id = builder.id;
        this.lastSharedDateTime = builder.lastSharedDateTime;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getId() {
        return id;
    }

    public String getLastSharedDateTime() {
        return lastSharedDateTime;
    }

    public static final class Builder {

        private String id;
        private String lastSharedDateTime;

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withLastSharedDateTime(String lastSharedDateTime) {
            this.lastSharedDateTime = lastSharedDateTime;
            return this;
        }

        public ResultLine build() {
            return new ResultLine(this);
        }
    }
}