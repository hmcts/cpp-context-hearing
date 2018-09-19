package uk.gov.moj.cpp.hearing.persist.entity.ha;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "ha_target")
public class Target {

    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "hearing_id")
    private Hearing hearing;

    @Column(name = "defendant_id")
    private UUID defendantId;

    @Column(name = "draft_result")
    private String draftResult;

    @Column(name = "offence_id")
    private UUID offenceId;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "target", orphanRemoval = true)
    private List<ResultLine> resultLines = new ArrayList<>();

    public Target() {
        //For JPA
    }

    public UUID getId() {
        return id;
    }

    public Hearing getHearing() {
        return hearing;
    }

    public Target setId(UUID id) {
        this.id = id;
        return this;
    }

    public Target setHearing(final Hearing hearing) {
        this.hearing = hearing;
        return this;
    }

    public UUID getDefendantId() {
        return defendantId;
    }

    public Target setDefendantId(final UUID defendantId) {
        this.defendantId = defendantId;
        return this;
    }

    public String getDraftResult() {
        return draftResult;
    }

    public Target setDraftResult(final String draftResult) {
        this.draftResult = draftResult;
        return this;
    }

    public UUID getOffenceId() {
        return offenceId;
    }

    public Target setOffenceId(final UUID offenceId) {
        this.offenceId = offenceId;
        return this;
    }

    public List<ResultLine> getResultLines() {
        return resultLines;
    }

    public Target setResultLines(final List<ResultLine> resultLines) {
        this.resultLines = resultLines;
        return this;
    }

    public static Target target() {
        return new Target();
    }
}