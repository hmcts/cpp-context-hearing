package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import static java.util.Collections.emptyList;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.getValueOfField;
import static uk.gov.moj.cpp.hearing.domain.aggregate.util.PleaTypeUtil.guiltyPleaTypes;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingWithApplicationTemplate;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtApplicationCase;
import uk.gov.justice.core.courts.CourtApplicationParty;
import uk.gov.justice.core.courts.CourtApplicationType;
import uk.gov.justice.core.courts.DelegatedPowers;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.core.courts.LegalEntityDefendant;
import uk.gov.justice.core.courts.MasterDefendant;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.PersonDefendant;
import uk.gov.justice.core.courts.Plea;
import uk.gov.justice.core.courts.PleaModel;
import uk.gov.justice.core.courts.Verdict;
import uk.gov.justice.core.courts.VerdictType;
import uk.gov.justice.domain.annotation.Event;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.moj.cpp.hearing.command.result.SharedResultsCommandResultLineV2;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.ApplicationDetailChanged;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsSharedV3;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Builds a hearing whose application offences carry plea, verdict and conviction date cloned
 * from an earlier hearing (STAT DEC / reopen), and drives it through the HearingAggregate.
 * Contains no real case data - every identifier is random.
 */
final class ClonedValuesFixture {

    static final String REOPEN = "MC80524";
    static final String STAT_DEC = "MC80527";
    static final String STAT_DEC_SJP = "MC80528";

    static final UUID G = fromString("2b3f7c20-8fc1-4fad-9076-df196c24b27e");
    static final UUID STDEC = fromString("e2f6e11b-c3a2-4e76-8d29-fbede4174988");
    static final UUID RFSD = fromString("d3902789-4cc8-4753-a15f-7e26dd39f6ae");
    static final UUID DISM = fromString("14d66587-8fbe-424f-a369-b1144f1684e3");
    static final UUID WDRN = fromString("eb2e4c4f-b738-4a4d-9cce-0572cecb7cb8");

    static final String GUILTY = "GUILTY";
    static final String NOT_GUILTY = "NOT_GUILTY";
    static final String CLONED_VALUES_ADJUSTED = "hearing.events.application-cloned-values-adjusted";
    /** Synthetic stand-in for the reference-data 'Proved SJP' verdict category type, which CHD-3370 AC10 does not depend on. */
    static final String PROVED_SJP = "TEST-PROVED-SJP";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapperProducer().objectMapper();

    enum SubjectType {INDIVIDUAL, COMPANY}

    private UUID originatingHearingId = randomUUID();
    private final HearingAggregate aggregate = new HearingAggregate();
    private final List<CourtApplication> applications = new ArrayList<>();
    private final java.util.Set<UUID> joiningAtShare = new java.util.HashSet<>();
    private final List<Object> appliedEvents = new ArrayList<>();
    private final DelegatedPowers courtClerk = DelegatedPowers.delegatedPowers()
            .withUserId(randomUUID()).withFirstName(STRING.next()).withLastName(STRING.next()).build();

    private Hearing hearing;
    private LocalDate hearingDate;
    private LocalDate applicationReceivedDate;
    private LocalDate originalConvictionDate;
    private int hearingDayCount = 1;
    private boolean caseOffenceWithValues;
    private boolean applicationOffenceSharesCaseOffenceId;
    private boolean clonedFromProvedSjp;
    private ClonedPleaDate clonedPleaDate = ClonedPleaDate.BEFORE_APPLICATION;

    enum ClonedPleaDate {BEFORE_APPLICATION, SAME_DAY_AS_APPLICATION, NONE}

    ClonedValuesFixture withApplication(final String typeCode, final SubjectType subjectType, final int clonedOffences) {
        applications.add(CourtApplication.courtApplication()
                .withId(randomUUID())
                .withType(CourtApplicationType.courtApplicationType().withId(randomUUID()).withCode(typeCode).build())
                .withSubject(subject(subjectType))
                .withCourtApplicationCases(List.of(CourtApplicationCase.courtApplicationCase()
                        .withProsecutionCaseId(randomUUID())
                        .withIsSJP(false)
                        .withCaseStatus("INACTIVE")
                        .withOffences(IntStream.range(0, clonedOffences)
                                .mapToObj(i -> Offence.offence().withId(randomUUID()).withOffenceDefinitionId(randomUUID()).build())
                                .collect(toList()))
                        .build()))
                .build());
        return this;
    }

    /**
     * An application that is not on the hearing when it is initiated and arrives with the share
     * (shareResultForDay's additionalApplications).
     */
    ClonedValuesFixture withApplicationJoiningAtShare(final String typeCode, final SubjectType subjectType, final int clonedOffences) {
        withApplication(typeCode, subjectType, clonedOffences);
        joiningAtShare.add(applications.get(applications.size() - 1).getId());
        return this;
    }

    ClonedValuesFixture withHearingDays(final int count) {
        this.hearingDayCount = count;
        return this;
    }

    ClonedValuesFixture withCaseOffenceCarryingValues() {
        this.caseOffenceWithValues = true;
        return this;
    }

    /** The first application's first offence carries the same id as the case offence. */
    ClonedValuesFixture withApplicationOffenceSharingCaseOffenceId() {
        this.applicationOffenceSharesCaseOffenceId = true;
        return this;
    }

    /** Cloned from a completed SJP case proved in absence: no plea received, verdict Proved SJP, SJP conviction date. */
    ClonedValuesFixture withClonedProvedSjpOffences() {
        this.clonedFromProvedSjp = true;
        return this;
    }

    ClonedValuesFixture withClonedPleaDate(final ClonedPleaDate clonedPleaDate) {
        this.clonedPleaDate = clonedPleaDate;
        return this;
    }

    /**
     * Clones only a plea and verdict that are guilty, with the original conviction date, onto
     * every application offence; then initiates the hearing.
     */
    ClonedValuesFixture initiate() {
        hearing = standardInitiateHearingWithApplicationTemplate(applications.stream()
                .filter(application -> !joiningAtShare.contains(application.getId()))
                .collect(toList())).getHearing();
        final ZonedDateTime firstSittingDay = hearing.getHearingDays().get(0).getSittingDay();
        hearing.setHearingDays(IntStream.range(0, hearingDayCount)
                .mapToObj(i -> HearingDay.hearingDay()
                        .withValuesFrom(hearing.getHearingDays().get(0))
                        .withSittingDay(firstSittingDay.plusDays(i))
                        .build())
                .collect(toList()));
        hearingDate = firstSittingDay.toLocalDate();
        applicationReceivedDate = hearingDate.minusDays(14);
        originalConvictionDate = hearingDate.minusYears(1);

        if (applicationOffenceSharesCaseOffenceId) {
            applications.get(0).getCourtApplicationCases().get(0).getOffences().get(0).setId(caseOffence().getId());
        }

        applications.forEach(application -> {
            application.setApplicationReceivedDate(applicationReceivedDate);
            application.getCourtApplicationCases().get(0).getOffences().forEach(offence -> {
                offence.setPlea(clonedFromProvedSjp ? null : clonedPlea(offence.getId()));
                offence.setVerdict(verdict(offence.getId(), originatingHearingId, originalConvictionDate, clonedFromProvedSjp ? PROVED_SJP : GUILTY));
                offence.setConvictionDate(originalConvictionDate);
            });
        });

        if (caseOffenceWithValues) {
            final Offence caseOffence = caseOffence();
            caseOffence.setPlea(plea(caseOffence.getId(), originatingHearingId, originalConvictionDate, GUILTY));
            caseOffence.setVerdict(verdict(caseOffence.getId(), originatingHearingId, originalConvictionDate, GUILTY));
            caseOffence.setConvictionDate(originalConvictionDate);
        }

        record(aggregate.apply(java.util.stream.Stream.of(new HearingInitiated(hearing))));
        return this;
    }

    List<Object> enterVerdict(final int application, final int offence, final String categoryType) {
        final UUID offenceId = offenceId(application, offence);
        return record(aggregate.updateVerdict(hearing.getId(), verdict(offenceId, null, hearingDate, categoryType), guiltyPleaTypes()));
    }

    /** The same hearing initiated again (re-listing): earlier share state for its applications must not carry over. */
    List<Object> reinitiate() {
        return record(aggregate.apply(java.util.stream.Stream.of(new HearingInitiated(hearing))));
    }

    List<Object> enterCaseOffenceVerdict(final String categoryType) {
        final UUID offenceId = caseOffence().getId();
        return record(aggregate.updateVerdict(hearing.getId(), verdict(offenceId, null, hearingDate, categoryType), guiltyPleaTypes()));
    }

    /** A verdict passed on from an associated hearing (hearing.command.enrich-update-verdict-with-associated-hearings). */
    List<Object> inheritVerdict(final int application, final int offence, final String categoryType) {
        final UUID offenceId = offenceId(application, offence);
        return record(aggregate.inheritVerdict(hearing.getId(), verdict(offenceId, randomUUID(), hearingDate, categoryType), guiltyPleaTypes()));
    }

    /**
     * Progression adds a cloned offence to an application after the hearing was initiated
     * (hearing.events.application-detail-changed). Returns the new offence's index.
     */
    int addClonedOffenceToApplication(final int application) {
        final CourtApplication current = applications.get(application);
        final Offence added = Offence.offence().withId(randomUUID()).withOffenceDefinitionId(randomUUID()).build();
        added.setPlea(clonedPlea(added.getId()));
        added.setVerdict(verdict(added.getId(), originatingHearingId, originalConvictionDate, GUILTY));
        added.setConvictionDate(originalConvictionDate);
        final List<Offence> offences = new ArrayList<>(current.getCourtApplicationCases().get(0).getOffences());
        offences.add(added);
        final CourtApplication changed = CourtApplication.courtApplication()
                .withValuesFrom(current)
                .withCourtApplicationCases(List.of(CourtApplicationCase.courtApplicationCase()
                        .withValuesFrom(current.getCourtApplicationCases().get(0))
                        .withOffences(offences)
                        .build()))
                .build();
        applications.set(application, changed);
        record(aggregate.apply(java.util.stream.Stream.of(new ApplicationDetailChanged(hearing.getId(), changed))));
        return offences.size() - 1;
    }

    List<Object> enterPlea(final int application, final int offence, final String pleaValue) {
        final UUID offenceId = offenceId(application, offence);
        return record(aggregate.updatePlea(hearing.getId(), PleaModel.pleaModel()
                .withOffenceId(offenceId)
                .withPlea(plea(offenceId, null, hearingDate, pleaValue))
                .build(), guiltyPleaTypes()));
    }

    /**
     * Shares through the delegate, as hearing.command.share-days-results does, and applies the
     * emitted events to the aggregate. Bypasses the aggregate's share guards so a reshare of an
     * already shared target can be exercised directly.
     */
    List<Object> share(final int hearingDayIndex, final List<SharedResultsCommandResultLineV2> resultLines) {
        final ResultsSharedDelegate delegate = getValueOfField(aggregate, "resultsSharedDelegate", ResultsSharedDelegate.class);
        final LocalDate hearingDay = hearing.getHearingDays().get(hearingDayIndex).getSittingDay().toLocalDate();
        return record(aggregate.apply(delegate.shareResultForDay(hearing.getId(), courtClerk, ZonedDateTime.now(), resultLines,
                applicationsJoiningAtShare(), emptyList(), null, hearingDay, 1)));
    }

    /**
     * A share made before CHD-3381 was deployed: the same results, but the hearing is shared as held
     * (no clearing, no adjustment event) - what a historic results-shared-v3 in the stream looks like.
     */
    List<Object> sharePreDeploy(final List<SharedResultsCommandResultLineV2> resultLines) {
        final ResultsSharedDelegate delegate = getValueOfField(aggregate, "resultsSharedDelegate", ResultsSharedDelegate.class);
        final HearingAggregateMomento momento = getValueOfField(aggregate, "momento", HearingAggregateMomento.class);
        final Hearing asHeld = momento.getHearing();
        final ResultsSharedV3 shared = resultsShared(delegate.shareResultForDay(hearing.getId(), courtClerk, ZonedDateTime.now(), resultLines,
                emptyList(), emptyList(), null, hearingDay(0), 1).collect(toList()));
        final ResultsSharedV3 historic = ResultsSharedV3.builder()
                .withHearingId(shared.getHearingId())
                .withSharedTime(shared.getSharedTime())
                .withCourtClerk(shared.getCourtClerk())
                .withHearing(asHeld)
                .withCompletedResultLinesStatus(shared.getCompletedResultLinesStatus())
                .withVariantDirectory(shared.getVariantDirectory())
                .withTargets(shared.getTargets())
                .withSavedTargets(shared.getSavedTargets())
                .withDefendantDetailsChanged(shared.getDefendantDetailsChanged())
                .withIsReshare(shared.getIsReshare())
                .withNewAmendmentResults(shared.getNewAmendmentResults())
                .withHearingDay(shared.getHearingDay())
                .withVersion(shared.getVersion())
                .build();
        return record(aggregate.apply(java.util.stream.Stream.of(historic)));
    }

    List<Object> share(final List<SharedResultsCommandResultLineV2> resultLines) {
        return share(0, resultLines);
    }

    private List<Object> record(final java.util.stream.Stream<Object> events) {
        final List<Object> emitted = events.collect(toList());
        appliedEvents.addAll(emitted);
        return emitted;
    }

    private List<CourtApplication> applicationsJoiningAtShare() {
        return applications.stream().filter(application -> joiningAtShare.contains(application.getId())).collect(toList());
    }

    List<Object> appliedEvents() {
        return appliedEvents;
    }

    SharedResultsCommandResultLineV2 applicationResult(final int application, final UUID resultDefinitionId) {
        return applicationResult(application, resultDefinitionId, randomUUID(), false, null);
    }

    SharedResultsCommandResultLineV2 applicationResult(final int application, final UUID resultDefinitionId, final UUID resultLineId,
                                                       final boolean deleted, final ZonedDateTime amendmentDate) {
        return SharedResultsCommandResultLineV2.sharedResultsCommandResultLine()
                .withApplicationIds(applicationId(application))
                .withResultLineId(resultLineId)
                .withResultDefinitionId(resultDefinitionId)
                .withLevel("CASE")
                .withPrompts(emptyList())
                .withIsComplete(true)
                .withIsDeleted(deleted)
                .withAmendmentDate(amendmentDate)
                .withOrderedDate(hearingDate)
                .build();
    }

    /** A result on the case offence only, with no application-level result. */
    SharedResultsCommandResultLineV2 caseOffenceResult() {
        return SharedResultsCommandResultLineV2.sharedResultsCommandResultLine()
                .withOffenceId(caseOffence().getId())
                .withResultLineId(randomUUID())
                .withResultDefinitionId(randomUUID())
                .withLevel("OFFENCE")
                .withPrompts(emptyList())
                .withIsComplete(true)
                .withOrderedDate(hearingDate)
                .build();
    }

    /** A sentence-type result on an application offence (no plea, verdict or application result). */
    SharedResultsCommandResultLineV2 applicationOffenceResult(final int application, final int offence) {
        return SharedResultsCommandResultLineV2.sharedResultsCommandResultLine()
                .withApplicationIds(applicationId(application))
                .withOffenceId(offenceId(application, offence))
                .withResultLineId(randomUUID())
                .withResultDefinitionId(randomUUID())
                .withLevel("OFFENCE")
                .withPrompts(emptyList())
                .withIsComplete(true)
                .withOrderedDate(hearingDate)
                .build();
    }

    static ResultsSharedV3 resultsShared(final List<Object> events) {
        return events.stream().filter(ResultsSharedV3.class::isInstance).map(ResultsSharedV3.class::cast)
                .reduce((first, second) -> second).orElseThrow(() -> new AssertionError("no ResultsSharedV3 emitted"));
    }

    static int indexOf(final List<Object> events, final Class<?> eventClass) {
        return IntStream.range(0, events.size()).filter(i -> eventClass.isInstance(events.get(i))).findFirst().orElse(-1);
    }

    /** Events named hearing.events.application-cloned-values-adjusted, as JSON (no compile-time dependency on the class). */
    static List<JsonNode> clonedValuesAdjusted(final List<Object> events) {
        return events.stream()
                .filter(e -> e.getClass().isAnnotationPresent(Event.class)
                        && CLONED_VALUES_ADJUSTED.equals(e.getClass().getAnnotation(Event.class).value()))
                .map(e -> (JsonNode) OBJECT_MAPPER.valueToTree(e))
                .collect(toList());
    }

    static int indexOfClonedValuesAdjusted(final List<Object> events) {
        return IntStream.range(0, events.size())
                .filter(i -> events.get(i).getClass().isAnnotationPresent(Event.class)
                        && CLONED_VALUES_ADJUSTED.equals(events.get(i).getClass().getAnnotation(Event.class).value()))
                .findFirst().orElse(-1);
    }

    static JsonNode toJson(final Object value) {
        return OBJECT_MAPPER.valueToTree(value);
    }

    Offence sharedApplicationOffence(final ResultsSharedV3 resultsShared, final int application, final int offence) {
        return applicationOffence(resultsShared.getHearing(), application, offence);
    }

    Offence sharedCaseOffence(final ResultsSharedV3 resultsShared) {
        final UUID caseOffenceId = caseOffence().getId();
        return resultsShared.getHearing().getProsecutionCases().stream()
                .flatMap(pc -> pc.getDefendants().stream())
                .flatMap(d -> d.getOffences().stream())
                .filter(o -> o.getId().equals(caseOffenceId))
                .findFirst().orElseThrow(() -> new AssertionError("case offence missing from shared hearing"));
    }

    /** The application offence as held in aggregate state (work order design D1: stays as entered). */
    Offence aggregateApplicationOffence(final int application, final int offence) {
        final HearingAggregateMomento momento = getValueOfField(aggregate, "momento", HearingAggregateMomento.class);
        return applicationOffence(momento.getHearing(), application, offence);
    }

    /** A fresh aggregate rebuilt by replaying every event applied so far. */
    ClonedValuesFixture replayed() {
        final ClonedValuesFixture copy = new ClonedValuesFixture();
        copy.applications.addAll(applications);
        copy.hearing = hearing;
        copy.hearingDate = hearingDate;
        copy.originatingHearingId = originatingHearingId;
        copy.originalConvictionDate = originalConvictionDate;
        appliedEvents.forEach(copy.aggregate::apply);
        return copy;
    }

    private Offence applicationOffence(final Hearing source, final int application, final int offence) {
        final UUID applicationId = applicationId(application);
        final UUID offenceId = offenceId(application, offence);
        return source.getCourtApplications().stream()
                .filter(ca -> ca.getId().equals(applicationId))
                .flatMap(ca -> ca.getCourtApplicationCases().stream())
                .flatMap(cac -> cac.getOffences().stream())
                .filter(o -> o.getId().equals(offenceId))
                .findFirst().orElseThrow(() -> new AssertionError("application offence missing"));
    }

    private Offence caseOffence() {
        return hearing.getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0);
    }

    UUID hearingId() {
        return hearing.getId();
    }

    UUID applicationId(final int application) {
        return applications.get(application).getId();
    }

    UUID offenceId(final int application, final int offence) {
        return applications.get(application).getCourtApplicationCases().get(0).getOffences().get(offence).getId();
    }

    LocalDate hearingDate() {
        return hearingDate;
    }

    LocalDate hearingDay(final int index) {
        return hearing.getHearingDays().get(index).getSittingDay().toLocalDate();
    }

    LocalDate originalConvictionDate() {
        return originalConvictionDate;
    }

    UUID originatingHearingId() {
        return originatingHearingId;
    }

    private Plea clonedPlea(final UUID offenceId) {
        switch (clonedPleaDate) {
            case SAME_DAY_AS_APPLICATION:
                return plea(offenceId, originatingHearingId, applicationReceivedDate, GUILTY);
            case NONE:
                return plea(offenceId, originatingHearingId, null, GUILTY);
            default:
                return plea(offenceId, originatingHearingId, originalConvictionDate, GUILTY);
        }
    }

    private static Plea plea(final UUID offenceId, final UUID originatingHearingId, final LocalDate pleaDate, final String pleaValue) {
        return Plea.plea()
                .withOffenceId(offenceId)
                .withOriginatingHearingId(originatingHearingId)
                .withPleaDate(pleaDate)
                .withPleaValue(pleaValue)
                .build();
    }

    private static Verdict verdict(final UUID offenceId, final UUID originatingHearingId, final LocalDate verdictDate, final String categoryType) {
        return Verdict.verdict()
                .withOffenceId(offenceId)
                .withOriginatingHearingId(originatingHearingId)
                .withVerdictDate(verdictDate)
                .withVerdictType(VerdictType.verdictType()
                        .withId(randomUUID())
                        .withCategoryType(categoryType)
                        .withCategory(categoryType)
                        .build())
                .build();
    }

    private static CourtApplicationParty subject(final SubjectType subjectType) {
        final MasterDefendant.Builder masterDefendant = MasterDefendant.masterDefendant().withMasterDefendantId(randomUUID());
        if (subjectType == SubjectType.COMPANY) {
            masterDefendant.withLegalEntityDefendant(LegalEntityDefendant.legalEntityDefendant().build());
        } else {
            masterDefendant.withPersonDefendant(PersonDefendant.personDefendant().build());
        }
        return CourtApplicationParty.courtApplicationParty().withId(randomUUID()).withMasterDefendant(masterDefendant.build()).build();
    }
}
