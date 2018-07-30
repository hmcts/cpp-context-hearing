package uk.gov.moj.cpp.hearing.persist.entity.ha;

import java.util.Objects;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "ha_attendee")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
        discriminatorType = DiscriminatorType.STRING,
        name = "type"
)
public class Attendee {

    @EmbeddedId
    private HearingSnapshotKey id;

    @ManyToOne
    @JoinColumn(name = "hearing_id", insertable = false, updatable = false)
    private Hearing hearing;

    @Column(name = "person_id")
    private java.util.UUID personId;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "title")
    private String title;

    public Attendee() {
    }

    public Attendee(final Attendee.Builder builder) {
        this.id = builder.id;
        this.personId = builder.personId;
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.title = builder.title;
    }

    public HearingSnapshotKey getId() {
        return id;
    }

    public Hearing getHearing() {
        return hearing;
    }

    public java.util.UUID getPersonId() {
        return personId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getTitle() {
        return title;
    }

    public Attendee setHearing(Hearing hearing) {
        this.hearing = hearing;
        return this;
    }

    public Attendee setPersonId(UUID personId) {
        this.personId = personId;
        return this;
    }

    public Attendee setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public Attendee setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public Attendee setTitle(String title) {
        this.title = title;
        return this;
    }

    public static abstract class Builder {

        private HearingSnapshotKey id;
        private java.util.UUID personId;
        private String firstName;
        private String lastName;
        private String title;

        protected Builder() {
        }

        public Attendee.Builder withId(HearingSnapshotKey id) {
            this.id = id;
            return this;
        }

        public Attendee.Builder withPersonId(final java.util.UUID personId) {
            this.personId = personId;
            return this;
        }

        public Attendee.Builder withFirstName(final String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Attendee.Builder withLastName(final String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Attendee.Builder withTitle(final String title) {
            this.title = title;
            return this;
        }

        public abstract <T> T build();
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
        return Objects.equals(this.id, ((Attendee) o).id);
    }
}