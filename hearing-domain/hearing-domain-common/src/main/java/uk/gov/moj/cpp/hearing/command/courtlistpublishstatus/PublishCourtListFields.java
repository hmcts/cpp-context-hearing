package uk.gov.moj.cpp.hearing.command.courtlistpublishstatus;

public enum PublishCourtListFields {

    COURT_CENTRE_ID("courtCentreId"),
    CREATED_TIME("createdTime");

    public String getInternalName() {
        return internalName;
    }

    final String internalName;

    PublishCourtListFields(final String name) {
        this.internalName = name;
    }

}
