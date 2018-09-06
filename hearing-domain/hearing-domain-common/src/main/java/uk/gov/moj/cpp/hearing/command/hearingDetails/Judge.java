package uk.gov.moj.cpp.hearing.command.hearingDetails;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.io.Serializable;
import java.util.UUID;

public class Judge implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;

    private String title;

    private String firstName;

    private String lastName;

    public Judge() {
    }

    @JsonCreator
    public Judge(UUID id, String title, String firstName, String lastName) {
        this.id = id;
        this.title = title;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public UUID getId() {
        return id;
    }

    public Judge setId(UUID id) {
        this.id = id;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public Judge setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getFirstName() {
        return firstName;
    }

    public Judge setFirstName(String firstName) {
        this.firstName = firstName;
        return this;

    }

    public String getLastName() {
        return lastName;
    }

    public Judge setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public static Judge judge() {
        return new Judge();
    }
}

