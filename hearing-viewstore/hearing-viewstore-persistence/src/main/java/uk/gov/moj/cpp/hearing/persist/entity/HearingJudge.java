package uk.gov.moj.cpp.hearing.persist.entity;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "hearing_judge")
public class HearingJudge implements Serializable {

    @Id
    @Column(name = "hearing_id")
    private UUID hearingId;

    @Column(name = "id")
    private String id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "title")
    private String title;

    public HearingJudge() {
        //For JPA
    }

    public HearingJudge(final UUID hearingId,
                        final String id,
                        final String firstName,
                        final String lastName,
                        final String title) {
        this.hearingId = hearingId;
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.title = title;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public String getId() {
        return id;
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
}