package uk.gov.moj.cpp.hearing.command.sessiontime;

import java.util.UUID;

public class CourtSessionJudiciary {
    private UUID judiciaryId;
    private String judiciaryName;
    private boolean benchChairman;

    public UUID getJudiciaryId() {
        return judiciaryId;
    }

    public void setJudiciaryId(UUID judiciaryId) {
        this.judiciaryId = judiciaryId;
    }

    public String getJudiciaryName() {
        return judiciaryName;
    }

    public void setJudiciaryName(String judiciaryName) {
        this.judiciaryName = judiciaryName;
    }

    public boolean isBenchChairman() {
        return benchChairman;
    }

    public void setBenchChairman(boolean benchChairman) {
        this.benchChairman = benchChairman;
    }

    @Override
    @SuppressWarnings({"squid:S00121", "squid:S00122"})
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CourtSessionJudiciary)) return false;

        final CourtSessionJudiciary judiciary = (CourtSessionJudiciary) o;

        if (isBenchChairman() != judiciary.isBenchChairman()) return false;
        if (getJudiciaryId() != null ? !getJudiciaryId().equals(judiciary.getJudiciaryId()) : judiciary.getJudiciaryId() != null)
            return false;
        return getJudiciaryName() != null ? getJudiciaryName().equals(judiciary.getJudiciaryName()) : judiciary.getJudiciaryName() == null;
    }

    @Override
    public int hashCode() {
        int result = getJudiciaryId() != null ? getJudiciaryId().hashCode() : 0;
        result = 31 * result + (getJudiciaryName() != null ? getJudiciaryName().hashCode() : 0);
        result = 31 * result + (isBenchChairman() ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Judiciary{" +
                "judiciaryId=" + judiciaryId +
                ", judiciaryName='" + judiciaryName + '\'' +
                ", benchChairman=" + benchChairman +
                '}';
    }

}
