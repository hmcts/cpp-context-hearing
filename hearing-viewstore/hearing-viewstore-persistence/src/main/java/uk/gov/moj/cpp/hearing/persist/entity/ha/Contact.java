package uk.gov.moj.cpp.hearing.persist.entity.ha;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.Objects;

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

    public Contact() {}

    public Contact(Builder builder) {
        this.home = builder.home;
        this.work = builder.work;
        this.mobile = builder.mobile;
        this.primaryEmail = builder.primaryEmail;
        this.secondaryEmail = builder.secondaryEmail;
        this.fax = builder.fax;
    }

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

    public static class Builder {
        private String home;
        private String work;
        private String mobile;
        private String primaryEmail;
        private String secondaryEmail;
        private String fax;

        protected Builder() {}

        public Builder withHome(final String home) {
            this.home = home;
            return this;
        }

        public Builder withWork(final String work) {
            this.work = work;
            return this;
        }

        public Builder withMobile(final String mobile) {
            this.mobile = mobile;
            return this;
        }

        public Builder withPrimaryEmail(String primaryEmail) {
            this.primaryEmail = primaryEmail;
            return this;
        }

        public Builder withSecondaryEmail(String secondaryEmail) {
            this.secondaryEmail = secondaryEmail;
            return this;
        }

        public Builder withFax(String fax) {
            this.fax = fax;
            return this;
        }

        public Contact build() {
            return new Contact(this);
        }

    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.home, this.work, this.mobile, this.primaryEmail, this.secondaryEmail, this.fax);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (null == o || getClass() != o.getClass()) {
            return false;
        }
        final Contact c = (Contact) o;
        return Objects.equals(this.home, c.home)
                && Objects.equals(this.work, c.work)
                && Objects.equals(this.mobile, c.mobile)
                && Objects.equals(this.primaryEmail, c.primaryEmail)
                && Objects.equals(this.secondaryEmail, c.secondaryEmail)
                && Objects.equals(this.fax, c.fax);
    }
}