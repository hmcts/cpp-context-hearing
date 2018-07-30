package uk.gov.moj.cpp.hearing.persist.entity.ha;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "ha_nows_result")
public class NowsResult {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "shared_result_id", nullable = false)
    private UUID sharedResultId;

    @Column(name = "sequence")
    private Integer sequence;

    @ManyToOne
    @JoinColumn(name = "nows_material_id", nullable = false)
    private NowsMaterial nowsMaterial;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getSharedResultId() {
        return sharedResultId;
    }

    public void setSharedResultId(UUID sharedResultId) {
        this.sharedResultId = sharedResultId;
    }

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }

    public NowsMaterial getNowsMaterial() {
        return nowsMaterial;
    }

    public void setNowsMaterial(NowsMaterial nowsMaterial) {
        this.nowsMaterial = nowsMaterial;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private UUID id;
        private UUID sharedResultId;
        private Integer sequence;
        private NowsMaterial nowsMaterial;

        private Builder() {
        }

        public Builder withId(UUID id) {
            this.id = id;
            return this;
        }

        public Builder withSharedResultId(UUID sharedResultId) {
            this.sharedResultId = sharedResultId;
            return this;
        }

        public Builder withSequence(Integer sequence) {
            this.sequence = sequence;
            return this;
        }

        public Builder withNowsMaterial(NowsMaterial nowsMaterial) {
            this.nowsMaterial = nowsMaterial;
            return this;
        }

        public NowsResult build() {
            NowsResult nowsResult = new NowsResult();
            nowsResult.setId(id);
            nowsResult.setSharedResultId(sharedResultId);
            nowsResult.setSequence(sequence);
            nowsResult.setNowsMaterial(nowsMaterial);
            return nowsResult;
        }
    }
}
