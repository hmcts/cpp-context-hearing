package uk.gov.moj.cpp.hearing.persist.entity.ha;


import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "nows")
public class Nows {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "defendant_id", nullable = false)
    private UUID defendantId;

    @Column(name = "hearing_id", nullable = false)
    private UUID hearingId;

    @Column(name = "nows_type_id", nullable = false)
    private UUID nowsTypeId;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "nows", orphanRemoval = true)
    private List<NowsMaterial> material = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "nows", orphanRemoval = true)
    private List<NowsResult> nowResult = new ArrayList<>();


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

    public UUID getNowsTypeId() {
        return nowsTypeId;
    }

    public void setNowsTypeId(UUID nowsTypeId) {
        this.nowsTypeId = nowsTypeId;
    }

    public List<NowsMaterial> getMaterial() {
        return material;
    }

    public void setMaterial(List<NowsMaterial> material) {
        this.material = material;
    }

    public List<NowsResult> getNowResult() {
        return nowResult;
    }

    public void setNowResult(List<NowsResult> nowResult) {
        this.nowResult = nowResult;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private UUID id;
        private UUID defendantId;
        private UUID hearingId;
        private UUID nowsTypeId;
        List<NowsMaterial> material = new ArrayList<>();
        List<NowsResult> nowResult = new ArrayList<>();

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

        public Builder withNowsTypeId(UUID nowsTypeId) {
            this.nowsTypeId = nowsTypeId;
            return this;
        }

        public Builder withMaterial(List<NowsMaterial> material) {
            this.material = material;
            return this;
        }

        public Builder withNowResult(List<NowsResult> nowResult) {
            this.nowResult = nowResult;
            return this;
        }

        public Nows build() {
            Nows nows = new Nows();
            nows.setId(id);
            nows.setNowsTypeId(nowsTypeId);
            nows.setDefendantId(defendantId);
            nows.setHearingId(hearingId);
            nows.setMaterial(material);
            nows.setNowResult(nowResult);
            return nows;
        }
    }
}
