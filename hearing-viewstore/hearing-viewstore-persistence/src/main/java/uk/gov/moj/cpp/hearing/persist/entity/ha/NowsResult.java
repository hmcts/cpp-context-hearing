package uk.gov.moj.cpp.hearing.persist.entity.ha;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "nows_result")
public class NowsResult {

    @Id
    @Column(name = "shared_result_id", nullable = false)
    private UUID sharedResultId;

    @Column(name = "sequence")
    private Integer sequence;

    @ManyToOne
    @JoinColumn(name = "nows_id", nullable = false)
    private Nows nows;

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

    public Nows getNows() {
        return nows;
    }

    public void setNows(Nows nows) {
        this.nows = nows;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private UUID sharedResultId;
        private Integer sequence;
        private Nows nows;

        private Builder() {
        }

        public Builder withSharedResultId(UUID sharedResultId) {
            this.sharedResultId = sharedResultId;
            return this;
        }

        public Builder withSequence(Integer sequence) {
            this.sequence = sequence;
            return this;
        }


        public Builder withNows(Nows nows) {
            this.nows = nows;
            return this;
        }

        public NowsResult build() {
            NowsResult nowsMaterial = new NowsResult();
            nowsMaterial.setNows(nows);
            nowsMaterial.setSequence(sequence);
            nowsMaterial.setSharedResultId(sharedResultId);
            return nowsMaterial;
        }
    }
}
