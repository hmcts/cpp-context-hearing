package uk.gov.moj.cpp.hearing.xhibit.pojo;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

public class PublishCourtListRequestParameters {

    private String courtCentreId;
    private String createdTime;

    public PublishCourtListRequestParameters(final String courtCentreId,
                                             final String createdTime) {

        this.courtCentreId = courtCentreId;
        this.createdTime = createdTime;
    }

    public String getCourtCentreId() {
        return courtCentreId;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, SHORT_PREFIX_STYLE);
    }
}
