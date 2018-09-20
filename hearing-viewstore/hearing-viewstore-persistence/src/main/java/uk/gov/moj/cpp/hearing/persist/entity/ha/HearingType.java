package uk.gov.moj.cpp.hearing.persist.entity.ha;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.UUID;

@SuppressWarnings("squid:S1067")
@Embeddable
public class HearingType {

    @Column(name = "hearing_type_id")
    private UUID id;

    @Column(name = "hearing_type_description")
    private String description;

    public HearingType() {
        //For JPA
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
