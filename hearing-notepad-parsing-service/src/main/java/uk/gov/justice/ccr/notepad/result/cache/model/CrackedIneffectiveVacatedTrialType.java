package uk.gov.justice.ccr.notepad.result.cache.model;


public class CrackedIneffectiveVacatedTrialType {

    private String id;
    private String reasonShortDescription;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getReasonShortDescription() {
        return reasonShortDescription;
    }

    public void setReasonShortDescription(String reasonShortDescription) {
        this.reasonShortDescription = reasonShortDescription;
    }

    @Override
    public String toString() {
        return "CrackedIneffectiveVacatedTrialType{" +
                "id='" + id + '\'' +
                ", reasonShortDescription='" + reasonShortDescription + '\'' +
                '}';
    }
}
