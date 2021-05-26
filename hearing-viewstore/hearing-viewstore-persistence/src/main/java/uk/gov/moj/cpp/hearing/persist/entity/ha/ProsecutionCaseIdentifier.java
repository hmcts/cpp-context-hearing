package uk.gov.moj.cpp.hearing.persist.entity.ha;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@SuppressWarnings("squid:S1067")
@Embeddable
public class ProsecutionCaseIdentifier {

    @Column(name = "prosecution_authority_id")
    private UUID prosecutionAuthorityId;

    @Column(name = "prosecution_authority_code")
    private String prosecutionAuthorityCode;

    @Column(name = "prosecution_authority_reference")
    private String prosecutionAuthorityReference;

    @Column(name = "caseurn")
    private String caseURN;

    @Column(name = "prosecutor_authority_name")
    private String prosecutorAuthorityName;

    @Column(name = "prosecutor_authority_address_1")
    private String prosecutorAuthorityAddress1;

    @Column(name = "prosecutor_authority_address_2")
    private String prosecutorAuthorityAddress2;

    @Column(name = "prosecutor_authority_address_3")
    private String prosecutorAuthorityAddress3;

    @Column(name = "prosecutor_authority_address_4")
    private String prosecutorAuthorityAddress4;

    @Column(name = "prosecutor_authority_address_5")
    private String prosecutorAuthorityAddress5;

    @Column(name = "prosecutor_authority_post_code")
    private String prosecutorAuthorityPostCode;

    @Column(name = "prosecutor_authority_email_address")
    private String prosecutorAuthorityEmailAddress;

    @Column(name = "prosecutor_category")
    private String prosecutorCategory;

    public UUID getProsecutionAuthorityId() {
        return prosecutionAuthorityId;
    }

    public void setProsecutionAuthorityId(UUID prosecutionAuthorityId) {
        this.prosecutionAuthorityId = prosecutionAuthorityId;
    }

    public String getCaseURN() {
        return caseURN;
    }

    public void setCaseURN(String caseURN) {
        this.caseURN = caseURN;
    }

    public String getProsecutionAuthorityCode() {
        return prosecutionAuthorityCode;
    }

    public void setProsecutionAuthorityCode(String prosecutionAuthorityCode) {
        this.prosecutionAuthorityCode = prosecutionAuthorityCode;
    }

    public String getProsecutionAuthorityReference() {
        return prosecutionAuthorityReference;
    }

    public void setProsecutionAuthorityReference(String prosecutionAuthorityReference) {
        this.prosecutionAuthorityReference = prosecutionAuthorityReference;
    }

    public String getProsecutorAuthorityName() {
        return prosecutorAuthorityName;
    }

    public void setProsecutorAuthorityName(final String prosecutorAuthorityName) {
        this.prosecutorAuthorityName = prosecutorAuthorityName;
    }

    public String getProsecutorAuthorityAddress1() {
        return prosecutorAuthorityAddress1;
    }

    public void setProsecutorAuthorityAddress1(final String prosecutorAuthorityAddress1) {
        this.prosecutorAuthorityAddress1 = prosecutorAuthorityAddress1;
    }

    public String getProsecutorAuthorityAddress2() {
        return prosecutorAuthorityAddress2;
    }

    public void setProsecutorAuthorityAddress2(final String prosecutorAuthorityAddress2) {
        this.prosecutorAuthorityAddress2 = prosecutorAuthorityAddress2;
    }

    public String getProsecutorAuthorityAddress3() {
        return prosecutorAuthorityAddress3;
    }

    public void setProsecutorAuthorityAddress3(final String prosecutorAuthorityAddress3) {
        this.prosecutorAuthorityAddress3 = prosecutorAuthorityAddress3;
    }

    public String getProsecutorAuthorityAddress4() {
        return prosecutorAuthorityAddress4;
    }

    public void setProsecutorAuthorityAddress4(final String prosecutorAuthorityAddress4) {
        this.prosecutorAuthorityAddress4 = prosecutorAuthorityAddress4;
    }

    public String getProsecutorAuthorityAddress5() {
        return prosecutorAuthorityAddress5;
    }

    public void setProsecutorAuthorityAddress5(final String prosecutorAuthorityAddress5) {
        this.prosecutorAuthorityAddress5 = prosecutorAuthorityAddress5;
    }

    public String getProsecutorAuthorityPostCode() {
        return prosecutorAuthorityPostCode;
    }

    public void setProsecutorAuthorityPostCode(final String prosecutorAuthorityPostCode) {
        this.prosecutorAuthorityPostCode = prosecutorAuthorityPostCode;
    }

    public String getProsecutorAuthorityEmailAddress() {
        return prosecutorAuthorityEmailAddress;
    }

    public void setProsecutorAuthorityEmailAddress(final String prosecutorAuthorityEmailAddress) {
        this.prosecutorAuthorityEmailAddress = prosecutorAuthorityEmailAddress;
    }

    public String getProsecutorCategory() {
        return prosecutorCategory;
    }

    public void setProsecutorCategory(final String prosecutorCategory) {
        this.prosecutorCategory = prosecutorCategory;
    }

}