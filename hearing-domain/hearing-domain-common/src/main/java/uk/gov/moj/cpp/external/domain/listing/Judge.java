package uk.gov.moj.cpp.external.domain.listing;

import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(value = Include.NON_NULL)
public class Judge implements Serializable {

    private final String id;
    private final String title;
    private final String firstName;
    private final String lastName;

    @JsonCreator
    public Judge(@JsonProperty(value = "id") final String id,
                 @JsonProperty(value = "title") final String title,
                 @JsonProperty(value = "firstName") final String firstName,
                 @JsonProperty(value = "lastName") final String lastName) {
        this.id = id;
        this.title = title;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getId() {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Judge)) {
            return false;
        }
        Judge judge = (Judge) o;
        return Objects.equals(getId(), judge.getId()) &&
                Objects.equals(getTitle(), judge.getTitle()) &&
                Objects.equals(getFirstName(), judge.getFirstName()) &&
                Objects.equals(getLastName(), judge.getLastName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getTitle(), getFirstName(), getLastName());
    }
}
