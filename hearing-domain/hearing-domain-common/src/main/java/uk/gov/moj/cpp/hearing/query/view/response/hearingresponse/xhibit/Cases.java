package uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit;

import java.io.Serializable;
import java.util.List;

public class Cases implements Serializable {
    private static final long serialVersionUID = 8063429769834087909L;

    private List<CaseDetail> casesDetails;

    public Cases(final List<CaseDetail> casesDetails) {
        this.casesDetails = casesDetails;
    }

    public List<CaseDetail> getCasesDetails() {
        return casesDetails;
    }

    public static Builder cases() {
        return new Cases.Builder();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj){
            return true;
        }
        if (obj == null || getClass() != obj.getClass()){
            return false;
        }
        final Cases that = (Cases) obj;

        return java.util.Objects.equals(this.casesDetails, that.casesDetails);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(casesDetails);
    }

    @Override
    public String toString() {
        return "Cases{" +
                "casesDetails='" + casesDetails + "'" +
                "}";
    }

    public Cases setCasesDetails(List<CaseDetail> casesDetails) {
        this.casesDetails = casesDetails;
        return this;
    }

    public static class Builder {
        private List<CaseDetail> casesDetails;

        public Builder withCasesDetails(final List<CaseDetail> casesDetails) {
            this.casesDetails = casesDetails;
            return this;
        }

        public Cases build() {
            return new Cases(casesDetails);
        }
    }
}
