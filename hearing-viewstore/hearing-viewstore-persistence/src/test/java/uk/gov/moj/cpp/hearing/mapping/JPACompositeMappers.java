package uk.gov.moj.cpp.hearing.mapping;

final class JPACompositeMappers {

    public static final IndicatedPleaJPAMapper INDICATED_PLEA_JPA_MAPPER = new IndicatedPleaJPAMapper(
            new AllocationDecisionJPAMapper());
    public static final PleaJPAMapper PLEA_JPA_MAPPER = new PleaJPAMapper(new DelegatedPowersJPAMapper());
    public static final VerdictJPAMapper VERDICT_JPA_MAPPER = new VerdictJPAMapper(new JurorsJPAMapper(),
            new LesserOrAlternativeOffenceJPAMapper(), new VerdictTypeJPAMapper());
    public static final OffenceJPAMapper OFFENCE_JPA_MAPPER = new OffenceJPAMapper(new NotifiedPleaJPAMapper(),
            INDICATED_PLEA_JPA_MAPPER, PLEA_JPA_MAPPER, new OffenceFactsJPAMapper(), VERDICT_JPA_MAPPER);
    public static final OrganisationJPAMapper ORGANISATION_JPA_MAPPER = new OrganisationJPAMapper(
            new AddressJPAMapper(), new ContactNumberJPAMapper());
    public static final PersonJPAMapper PERSON_JPA_MAPPER = new PersonJPAMapper(new AddressJPAMapper(),
            new ContactNumberJPAMapper());
    public static final AssociatedPersonJPAMapper ASSOCIATED_PERSON_JPA_MAPPER = new AssociatedPersonJPAMapper(PERSON_JPA_MAPPER);
    public static final PersonDefendantJPAMapper PERSON_DEFENDANT_JPA_MAPPER = new PersonDefendantJPAMapper(
            new DefendantAliasesJPAMapper(), ORGANISATION_JPA_MAPPER, PERSON_JPA_MAPPER);
    public static final DefendantJPAMapper DEFENDANT_JPA_MAPPER = new DefendantJPAMapper(ASSOCIATED_PERSON_JPA_MAPPER,
            ORGANISATION_JPA_MAPPER, OFFENCE_JPA_MAPPER, PERSON_DEFENDANT_JPA_MAPPER);
    public static final ProsecutionCaseJPAMapper PROSECUTION_CASE_JPA_MAPPER = new ProsecutionCaseJPAMapper(
            new ProsecutionCaseIdentifierJPAMapper(), DEFENDANT_JPA_MAPPER);
    public static final HearingCaseNoteJPAMapper HEARING_CASE_NOTE_JPA_MAPPER = new HearingCaseNoteJPAMapper();
    public static final HearingDefenceCounselJPAMapper HEARING_DEFENCE_COUNSEL_JPA_MAPPER = new HearingDefenceCounselJPAMapper();
    private static final ResultLineJPAMapper RESULT_LINE_JPA_MAPPER = new ResultLineJPAMapper(new PromptJPAMapper(),
            new DelegatedPowersJPAMapper());
    private static final TargetJPAMapper TARGET_JPA_MAPPER = new TargetJPAMapper(RESULT_LINE_JPA_MAPPER);
    public static final HearingJPAMapper HEARING_JPA_MAPPER = new HearingJPAMapper(new CourtCentreJPAMapper(),
            HEARING_DEFENCE_COUNSEL_JPA_MAPPER, new DefendantAttendanceJPAMapper(), new DefendantReferralReasonJPAMapper(),
            HEARING_CASE_NOTE_JPA_MAPPER, new HearingDayJPAMapper(), new JudicialRoleJPAMapper(),
            PROSECUTION_CASE_JPA_MAPPER, new HearingProsecutionCounselJPAMapper(), TARGET_JPA_MAPPER,
            new HearingTypeJPAMapper());
}
