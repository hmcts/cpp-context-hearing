package uk.gov.moj.cpp.hearing.persist.entity.ha;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class LesserOrAlternativeOffence {

    @Column(name = "lesser_offence_definition_id")
    private UUID lesserOffenceDefinitionId;

    @Column(name = "lesser_offence_code")
    private String lesserOffenceCode;

    @Column(name = "lesser_offence_title")
    private String lesserOffenceTitle;

    @Column(name = "lesser_offence_title_welsh")
    private String lesserOffenceTitleWelsh;

    @Column(name = "lesser_offence_legislation")
    private String lesserOffenceLegislation;

    @Column(name = "lesser_offence_legislation_welsh")
    private String lesserOffenceLegislationWelsh;

    public UUID getLesserOffenceDefinitionId() {
        return lesserOffenceDefinitionId;
    }

    public void setLesserOffenceDefinitionId(UUID lesserOffenceDefinitionId) {
        this.lesserOffenceDefinitionId = lesserOffenceDefinitionId;
    }

    public String getLesserOffenceCode() {
        return lesserOffenceCode;
    }

    public void setLesserOffenceCode(String lesserOffenceCode) {
        this.lesserOffenceCode = lesserOffenceCode;
    }

    public String getLesserOffenceTitle() {
        return lesserOffenceTitle;
    }

    public void setLesserOffenceTitle(String lesserOffenceTitle) {
        this.lesserOffenceTitle = lesserOffenceTitle;
    }

    public String getLesserOffenceLegislation() {
        return lesserOffenceLegislation;
    }

    public void setLesserOffenceLegislation(String lesserOffenceLegislation) {
        this.lesserOffenceLegislation = lesserOffenceLegislation;
    }

    public String getLesserOffenceTitleWelsh() {
        return lesserOffenceTitleWelsh;
    }

    public void setLesserOffenceTitleWelsh(String lesserOffenceTitleWelsh) {
        this.lesserOffenceTitleWelsh = lesserOffenceTitleWelsh;
    }

    public String getLesserOffenceLegislationWelsh() {
        return lesserOffenceLegislationWelsh;
    }

    public void setLesserOffenceLegislationWelsh(String lesserOffenceLegislationWelsh) {
        this.lesserOffenceLegislationWelsh = lesserOffenceLegislationWelsh;
    }
}
