package uk.gov.moj.cpp.hearing.persist.entity.ha;



import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "ha_hearing_witness")
public class Witness {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;


    @Column(name = "name", nullable = false)
    private String name;




    @ManyToOne
    @JoinColumn(name = "hearing_id")
    private Hearing hearing;

    public Witness() {
    }

    public Witness(UUID id, String name, Hearing hearing) {
        this.id = id;
        this.name = name;
        this.hearing = hearing;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Hearing getHearing() {
        return hearing;
    }

    public void setHearing(Hearing hearing) {
        this.hearing = hearing;
    }
}
