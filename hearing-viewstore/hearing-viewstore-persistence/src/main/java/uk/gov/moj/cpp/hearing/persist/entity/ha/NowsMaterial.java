package uk.gov.moj.cpp.hearing.persist.entity.ha;


import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "ha_nows_material")
public class NowsMaterial {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "status", nullable = false)
    private String status;

    @ElementCollection
    @CollectionTable(
            name = "ha_nows_material_usergroups",
            joinColumns = @JoinColumn(name = "material_id")
    )
    @Column(name = "user_groups", nullable = false)
    private Set<String> userGroups = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "nows_id", nullable = false)
    Nows nows;


    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "nowsMaterial", orphanRemoval = true)
    private Set<NowsResult> nowResult = new HashSet<>();


    @Column(name = "language")
    private String language;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Set<String> getUserGroups() {
        return userGroups;
    }

    public void setUserGroups(Set<String> userGroups) {
        this.userGroups = userGroups;
    }

    public Nows getNows() {
        return nows;
    }

    public void setNows(Nows nows) {
        this.nows = nows;
    }

    public Set<NowsResult> getNowResult() {
        return nowResult;
    }

    public void setNowResult(Set<NowsResult> nowResult) {
        this.nowResult = nowResult;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }


    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        Nows nows;
        private UUID id;
        private String status;
        private Set<String> userGroups = new HashSet<>();
        private Set<NowsResult> nowResult = new HashSet<>();
        private String language;

        private Builder() {
        }

        public Builder withId(UUID id) {
            this.id = id;
            return this;
        }

        public Builder withStatus(String status) {
            this.status = status;
            return this;
        }

        public Builder withUserGroups(Set<String> userGroups) {
            this.userGroups = userGroups;
            return this;
        }

        public Builder withNows(Nows nows) {
            this.nows = nows;
            return this;
        }

        public Builder withNowResult(Set<NowsResult> nowResult) {
            this.nowResult = nowResult;
            return this;
        }

        public Builder withLanguage(String language) {
            this.language = language;
            return this;
        }

        public NowsMaterial build() {
            NowsMaterial nowsMaterial = new NowsMaterial();
            nowsMaterial.setId(id);
            nowsMaterial.setStatus(status);
            nowsMaterial.setUserGroups(userGroups);
            nowsMaterial.setNows(nows);
            nowsMaterial.setNowResult(nowResult);
            nowsMaterial.setLanguage(language);
            return nowsMaterial;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NowsMaterial that = (NowsMaterial) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id);
    }
}
