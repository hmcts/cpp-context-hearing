package uk.gov.moj.cpp.hearing.persist.entity.ha;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "ha_witness")
public class Witness {

    @EmbeddedId
    private HearingSnapshotKey id;

    @ManyToOne
    @JoinColumn(name = "hearing_id", insertable = false, updatable = false)
    private Hearing hearing;

    //bi-directional many-to-one association to ACase
    @ManyToOne
    @JoinColumn(name = "case_id")
    private LegalCase legalCase;

    @Column(name = "type")
    private String type;

    @Column(name="classification")
    private String classification;

    @Column(name = "person_id")
    private UUID personId;

    @Column(name = "title")
    private String title;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "nationality")
    private String nationality;

    @Column(name = "gender")
    private String gender;

    @Column(name = "work_telephone")
    private String workTelephone;

    @Column(name = "home_telephone")
    private String homeTelephone;

    @Column(name = "mobile_telephone")
    private String mobileTelephone;

    @Column(name = "fax")
    private String fax;

    @Column(name = "email")
    private String email;

    @ManyToMany(mappedBy = "defendantWitnesses", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Defendant> defendants = new ArrayList<>();

    public Witness() {

    }

    public Witness(final Builder builder) {
        this.id = builder.id;
        this.hearing = builder.hearing;
        this.legalCase = builder.legalCase;
        this.type = builder.type;
        this.classification = builder.classification;
        this.personId = builder.personId;
        this.title = builder.title;
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.dateOfBirth = builder.dateOfBirth;
        this.nationality = builder.nationality;
        this.gender = builder.gender;
        this.workTelephone = builder.workTelephone;
        this.homeTelephone = builder.homeTelephone;
        this.mobileTelephone = builder.mobileTelephone;
        this.fax = builder.fax;
        this.email = builder.email;
        this.defendants = builder.defendants;
    }

    public HearingSnapshotKey getId() {
        return id;
    }

    public Hearing getHearing() {
        return hearing;
    }

    public UUID getPersonId() {
        return personId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public String getNationality() {
        return nationality;
    }

    public String getGender() {
        return gender;
    }

    public String getWorkTelephone() {
        return workTelephone;
    }

    public String getHomeTelephone() {
        return homeTelephone;
    }

    public String getMobileTelephone() {
        return mobileTelephone;
    }

    public String getFax() {
        return fax;
    }

    public String getEmail() {
        return email;
    }

    public LegalCase getLegalCase() {
        return legalCase;
    }

    public String getType() {
        return type;
    }

    public String getClassification() {
        return classification;
    }

    public String getTitle() {
        return title;
    }

    public List<Defendant> getDefendants() {
        if(defendants == null){
            defendants = new ArrayList<>();
        }
        return defendants;
    }


    public void setType(final String type) {
        this.type = type;
    }

    public void setClassification(final String classification) {
        this.classification = classification;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }


    public static class Builder {

        private HearingSnapshotKey id;

        private Hearing hearing;

        private LegalCase legalCase;

        private String type;

        private String classification;

        private UUID personId;

        private String title;

        private String firstName;

        private String lastName;

        private LocalDate dateOfBirth;

        private String nationality;

        private String gender;

        private String workTelephone;

        private String homeTelephone;

        private String mobileTelephone;

        private String fax;

        private String email;

        private List<Defendant> defendants;

        protected Builder() {
        }

        public Builder withId(final HearingSnapshotKey id) {
            this.id = id;
            return this;
        }

        public Builder withHearing(final Hearing hearing) {
            this.hearing = hearing;
            return this;
        }

        public Builder withLegalCase(final LegalCase legalCase){
            this.legalCase = legalCase;
            return this;
        }

        public Builder withPersonId(final UUID personId) {
            this.personId = personId;
            return this;
        }

        public Builder withTitle(final String title){
            this.title = title;
            return this;
        }

        public Builder withFirstName(final String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder withLastName(final String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder withDateOfBirth(final LocalDate dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
            return this;
        }

        public Builder withNationality(final String nationality) {
            this.nationality = nationality;
            return this;
        }

        public Builder withGender(final String gender) {
            this.gender = gender;
            return this;
        }

        public Builder withWorkTelephone(final String workTelephone) {
            this.workTelephone = workTelephone;
            return this;
        }

        public Builder withHomeTelephone(final String homeTelephone) {
            this.homeTelephone = homeTelephone;
            return this;
        }

        public Builder withMobileTelephone(final String mobileTelephone) {
            this.mobileTelephone = mobileTelephone;
            return this;
        }

        public Builder withFax(final String fax) {
            this.fax = fax;
            return this;
        }

        public Builder withEmail(final String email) {
            this.email = email;
            return this;
        }

        public Builder withType(final String type) {
            this.type = type;
            return this;
        }

        public Builder withClassification(final String classification) {
            this.classification = classification;
            return this;
        }

        public Builder withDefendants(final List<Defendant> defendants){
            this.defendants = defendants;
            return this;
        }

        public Witness build() {
            return new Witness(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (null == o || getClass() != o.getClass()) {
            return false;
        }
        return Objects.equals(this.id, ((Witness) o).id);
    }
}