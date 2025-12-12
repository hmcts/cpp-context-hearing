package uk.gov.moj.cpp.hearing.query.view.response.hearingresponse;

import static java.util.Collections.unmodifiableList;

import java.util.List;

@SuppressWarnings({"squid:S2384"})
public class GetShareResultsV2Response {

    private final List<ResultLine> resultLines;

    public GetShareResultsV2Response(final List<ResultLine> resultLines) {
        this.resultLines = resultLines;
    }

    public static Builder builder() {
        return new Builder();
    }

    public List<ResultLine> getResultLines() {
        return unmodifiableList(resultLines);
    }

    public static final class Builder {

        private List<ResultLine> resultLines;

        public Builder withResultLines(final List<ResultLine> resultLines) {
            this.resultLines = resultLines;
            return this;
        }

        public GetShareResultsV2Response build() {
            return new GetShareResultsV2Response(resultLines);
        }
    }
}
