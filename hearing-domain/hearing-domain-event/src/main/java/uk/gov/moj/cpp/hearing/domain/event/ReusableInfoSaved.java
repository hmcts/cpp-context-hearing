package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.moj.cpp.hearing.command.ReusableInfo;
import uk.gov.moj.cpp.hearing.command.ReusableInfoResults;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@SuppressWarnings({"squid:S2384", "squid:S1948","PMD.BeanMembersShouldSerialize"})
@Event("hearing.event.reusable-info-saved")
public class ReusableInfoSaved implements Serializable {

    private UUID hearingId;
    private List<ReusableInfo> promptList;
    private List<ReusableInfoResults> resultsList;
    private static final long serialVersionUID = 1L;

    public ReusableInfoSaved(final UUID hearingId, final List<ReusableInfo> promptList, final List<ReusableInfoResults> resultsList) {
        this.hearingId = hearingId;
        this.promptList = promptList;
        this.resultsList = resultsList;
    }

    public List<ReusableInfo> getPromptList() {
        return promptList;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public List<ReusableInfoResults> getResultsList() {
        return resultsList;
    }

    public static Builder reusableInfoSaved() {
        return new Builder();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ReusableInfoSaved that = (ReusableInfoSaved) o;
        return Objects.equals(hearingId, that.hearingId) &&
                Objects.equals(promptList, that.promptList) &&
                Objects.equals(resultsList, that.resultsList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hearingId, promptList, resultsList);
    }

    @Override
    public String toString() {
        return "ReusableInfoCached{" +
                "hearingId=" + hearingId +
                ", promptList=" + promptList +
                ", resultsList=" + resultsList +
                '}';
    }

    public static class Builder {
        private UUID hearingId;
        private List<ReusableInfo> promptList;
        private List<ReusableInfoResults> resultsList;

        public Builder withHearingId(final UUID hearingId) {
            this.hearingId = hearingId;
            return this;
        }

        public Builder withPromptList(final List<ReusableInfo> promptList) {
            this.promptList = promptList;
            return this;
        }

        public Builder withResultsList(final List<ReusableInfoResults> resultsList) {
            this.resultsList = resultsList;
            return this;
        }

        public ReusableInfoSaved build() {
            return new ReusableInfoSaved(hearingId, promptList, resultsList);
        }
    }
}
