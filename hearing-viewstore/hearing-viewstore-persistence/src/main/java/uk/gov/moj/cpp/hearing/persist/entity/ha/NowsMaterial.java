package uk.gov.moj.cpp.hearing.persist.entity.ha;


import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "ha_nows_material")
public class NowsMaterial {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private NowsMaterialStatus status;

    @ElementCollection
    @CollectionTable(
            name = "ha_nows_material_usergroups",
            joinColumns = @JoinColumn(name = "material_id")
    )
    @Column(name = "user_groups", nullable = false)
    private List<String> userGroups = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "nows_id", nullable = false)
    Nows nows;

    @Column(name = "language")
    private String language;

    public NowsMaterialStatus getStatus() {
        return status;
    }

    public void setStatus(NowsMaterialStatus status) {
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public List<String> getUserGroups() {
        return userGroups;
    }

    public void setUserGroups(List<String> userGroups) {
        this.userGroups = userGroups;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Nows getNows() {
        return nows;
    }

    public void setNows(Nows nows) {
        this.nows = nows;
    }

    public static final class Builder {
        private UUID id;
        private NowsMaterialStatus status;
        private List<String> userGroups = new ArrayList<>();
        private String language;
        private Nows nows;

        private Builder() {
        }

        public Builder withId(UUID id) {
            this.id = id;
            return this;
        }

        public Builder withLanguage(String language) {
            this.language = language;
            return this;
        }

        public Builder withUserGroups(List<String> userGroups) {
            this.userGroups = userGroups;
            return this;
        }

        public Builder withStatus(NowsMaterialStatus status) {
            this.status = status;
            return this;
        }

        public Builder withNows(Nows nows) {
            this.nows = nows;
            return this;
        }

        public NowsMaterial build() {
            NowsMaterial nowsMaterial = new NowsMaterial();
            nowsMaterial.setId(id);
            nowsMaterial.setStatus(status);
            nowsMaterial.setLanguage(language);
            nowsMaterial.setUserGroups(userGroups);
            nowsMaterial.setNows(nows);
            return nowsMaterial;
        }
    }
}
