package uk.gov.moj.cpp.hearing.persist.entity.ex;


import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "nows_material")
public class NowsMaterial {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "defendant_id", nullable = false)
    private UUID defendantId;

    @Column(name = "hearing_id", nullable = false)
    private UUID hearingId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private NowsMaterialStatus status;

    @ElementCollection
    @CollectionTable(
            name = "nows_material_usersgroups",
            joinColumns = @JoinColumn(name = "material_id")
    )
    @Column(name = "user_groups", nullable = false)
    private List<String> userGroups = new ArrayList<>();

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

    public UUID getDefendantId() {
        return defendantId;
    }

    public void setDefendantId(UUID defendantId) {
        this.defendantId = defendantId;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public void setHearingId(UUID hearingId) {
        this.hearingId = hearingId;
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

    public static final class Builder {
        private UUID id;
        private UUID defendantId;
        private NowsMaterialStatus status;
        private UUID hearingId;
        private List<String> userGroups = new ArrayList<>();

        private Builder() {
        }



        public Builder withId(UUID id) {
            this.id = id;
            return this;
        }



        public Builder withDefendantId(UUID defendantId) {
            this.defendantId = defendantId;
            return this;
        }

        public Builder withHearingId(UUID hearingId) {
            this.hearingId = hearingId;
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

        public NowsMaterial build() {
            NowsMaterial nowsMaterial = new NowsMaterial();
            nowsMaterial.setId(id);
            nowsMaterial.setStatus(status);
            nowsMaterial.setDefendantId(defendantId);
            nowsMaterial.setHearingId(hearingId);
            nowsMaterial.setUserGroups(userGroups);
            return nowsMaterial;
        }
    }
}
