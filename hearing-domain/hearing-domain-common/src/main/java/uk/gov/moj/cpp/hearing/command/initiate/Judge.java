package uk.gov.moj.cpp.hearing.command.initiate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Judge implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;
    private String title;
    private String firstName;
    private String lastName;

    public Judge() {
    }

    @JsonCreator
    public Judge(@JsonProperty("id") final UUID id,
                 @JsonProperty("title") final String title,
                 @JsonProperty("firstName") final String firstName,
                 @JsonProperty("lastName") final String lastName) {
        this.id = id;
        this.title = title;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Judge setId(UUID id) {
        this.id = id;
        return this;
    }

    public Judge setTitle(String title) {
        this.title = title;
        return this;
    }

    public Judge setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public Judge setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public static Judge judge() {
        return new Judge();
    }
}
