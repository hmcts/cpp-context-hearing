package uk.gov.moj.cpp.hearing.mapping;

final class JPACompositeMappers {

    public static final IndicatedPleaJPAMapper INDICATED_PLEA_JPA_MAPPER = new IndicatedPleaJPAMapper();
    public static final CourtIndicatedSentenceJPAMapper COURT_INDICATED_SENTENCE_JPA_MAPPER = new CourtIndicatedSentenceJPAMapper();
    public static final AllocationDecisionJPAMapper ALLOCATION_DECISION_JPA_MAPPER = new AllocationDecisionJPAMapper(
            COURT_INDICATED_SENTENCE_JPA_MAPPER);
    public static final LaaApplnReferenceJPAMapper LAA_APPLN_REFERENCE_JPA_MAPPER = new LaaApplnReferenceJPAMapper();
    public static final PleaJPAMapper PLEA_JPA_MAPPER = new PleaJPAMapper(new DelegatedPowersJPAMapper(), new LesserOrAlternativeOffenceForPleaJPAMapper());
    public static final VerdictJPAMapper VERDICT_JPA_MAPPER = new VerdictJPAMapper(new JurorsJPAMapper(),
            new LesserOrAlternativeOffenceJPAMapper(), new VerdictTypeJPAMapper());
    public static final ReportingRestrictionJPAMapper REPORTING_RESTRICTION_JPA_MAPPER = new ReportingRestrictionJPAMapper();
    public static final OffenceJPAMapper OFFENCE_JPA_MAPPER = new OffenceJPAMapper(new NotifiedPleaJPAMapper(),
            INDICATED_PLEA_JPA_MAPPER, PLEA_JPA_MAPPER, new OffenceFactsJPAMapper(), VERDICT_JPA_MAPPER, ALLOCATION_DECISION_JPA_MAPPER,
            LAA_APPLN_REFERENCE_JPA_MAPPER, REPORTING_RESTRICTION_JPA_MAPPER);
    public static final OrganisationJPAMapper ORGANISATION_JPA_MAPPER = new OrganisationJPAMapper(
            new AddressJPAMapper(), new ContactNumberJPAMapper());
    public static final PersonJPAMapper PERSON_JPA_MAPPER = new PersonJPAMapper(new AddressJPAMapper(),
            new ContactNumberJPAMapper(), new EthnicityJPAMapper());
    public static final AssociatedPersonJPAMapper ASSOCIATED_PERSON_JPA_MAPPER = new AssociatedPersonJPAMapper(PERSON_JPA_MAPPER);
    public static final PersonDefendantJPAMapper PERSON_DEFENDANT_JPA_MAPPER = new PersonDefendantJPAMapper(
            ORGANISATION_JPA_MAPPER, PERSON_JPA_MAPPER,new CustodialEstablishmentJPAMapper());
    public static final DefenceOrganisationJPAMapper DEFENCE_ORGANISATION_JPA_MAPPER = new DefenceOrganisationJPAMapper(new AddressJPAMapper(), new ContactNumberJPAMapper());
    public static final AssociatedDefenceOrganisationJPAMapper ASSOCIATED_DEFENCE_ORGANISATION_JPA_MAPPER = new AssociatedDefenceOrganisationJPAMapper(DEFENCE_ORGANISATION_JPA_MAPPER);
    public static final DefendantJPAMapper DEFENDANT_JPA_MAPPER = new DefendantJPAMapper(ASSOCIATED_PERSON_JPA_MAPPER,
            ORGANISATION_JPA_MAPPER, OFFENCE_JPA_MAPPER, PERSON_DEFENDANT_JPA_MAPPER, ASSOCIATED_DEFENCE_ORGANISATION_JPA_MAPPER);
    public static final ProsecutionCaseJPAMapper PROSECUTION_CASE_JPA_MAPPER = new ProsecutionCaseJPAMapper(
            new ProsecutionCaseIdentifierJPAMapper(), DEFENDANT_JPA_MAPPER, new CaseMarkerJPAMapper(), new CpsProsecutorJPAMapper());
    public static final HearingCaseNoteJPAMapper HEARING_CASE_NOTE_JPA_MAPPER = new HearingCaseNoteJPAMapper();
    public static final HearingDefenceCounselJPAMapper HEARING_DEFENCE_COUNSEL_JPA_MAPPER = new HearingDefenceCounselJPAMapper();
    private static final ResultLineJPAMapper RESULT_LINE_JPA_MAPPER = new ResultLineJPAMapper(new PromptJPAMapper(),
            new DelegatedPowersJPAMapper());
}
