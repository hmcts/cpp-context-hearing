package uk.gov.moj.cpp.hearing.persist.entity.ha;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@SuppressWarnings("squid:S1067")
@Embeddable
public class Contact {
    @Column(name = "contact_home")
    private String home;

    @Column(name = "contact_work")
    private String work;

    @Column(name = "contact_mobile")
    private String mobile;

    @Column(name = "contact_primary_email")
    private String primaryEmail;

    @Column(name = "contact_secondary_email")
    private String secondaryEmail;

    @Column(name = "contact_fax")
    private String fax;

    public String getHome() {
        return home;
    }

    public String getWork() {
        return work;
    }

    public String getMobile() {
        return mobile;
    }

    public String getPrimaryEmail() {
        return primaryEmail;
    }

    public String getSecondaryEmail() {
        return secondaryEmail;
    }

    public String getFax() {
        return fax;
    }

    public void setHome(String home) {
        this.home = home;
    }

    public void setWork(String work) {
        this.work = work;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public void setPrimaryEmail(String primaryEmail) {
        this.primaryEmail = primaryEmail;
    }

    public void setSecondaryEmail(String secondaryEmail) {
        this.secondaryEmail = secondaryEmail;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }
}