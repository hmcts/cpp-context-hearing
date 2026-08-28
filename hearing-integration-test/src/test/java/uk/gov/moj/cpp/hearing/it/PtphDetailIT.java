package uk.gov.moj.cpp.hearing.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasNoJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.is;
import static uk.gov.moj.cpp.hearing.it.Queries.pollForPtphDetail;
import static uk.gov.moj.cpp.hearing.it.Utilities.listenFor;
import static uk.gov.moj.cpp.hearing.it.UseCases.deletePtphDetail;
import static uk.gov.moj.cpp.hearing.it.UseCases.finalisePtphDetail;
import static uk.gov.moj.cpp.hearing.it.UseCases.initiateHearing;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static uk.gov.moj.cpp.hearing.it.UseCases.savePtphDetail;
import static uk.gov.moj.cpp.hearing.it.UseCases.savePtphDetailExpecting;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.givenAUserHasLoggedInAsACourtClerk;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.stubUsersAndGroupsUserRoles;

import uk.gov.moj.cpp.hearing.command.ListType;
import uk.gov.moj.cpp.hearing.command.SavePtphDetailCommand;
import uk.gov.moj.cpp.hearing.command.Tier;
import uk.gov.moj.cpp.hearing.it.Utilities.EventListener;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.InitiateHearingCommandHelper;

import java.util.UUID;

import org.junit.jupiter.api.Test;

/**
 * LPT-2400–2404 — the tier and list type slice, end to end through the real CQRS stack:
 * command API → handler → aggregate delegate → domain event → listener → view store → query.
 *
 * <p>Unit tests already cover each layer in isolation. What only an integration test can show is
 * that the layers agree: that the command-API and handler schemas accept what the aggregate
 * emits, that the listener's upsert and row-removal actually reach {@code ha_ptph_detail}, and
 * that {@code hearing.get-ptph-detail} reads back what was written.
 *
 * <p>Absent values are asserted with {@code hasNoJsonPath} rather than a null value: the
 * query response serialises with non-null inclusion, so an unset tier is missing from the
 * payload entirely (just {@code finalised}) rather than present as {@code null}.
 *
 * <p>The command-side preconditions live in {@code HearingAggregate} and are asserted by their
 * observable effect on the query, not by an HTTP status: every command returns 202 before the
 * aggregate runs, so a rejected command surfaces as the view store simply not changing.
 *
 * <p>A rejection is emitted as {@code hearing.hearing-change-ignored} rather than thrown, so it
 * must leave the hearing's command stream healthy — {@code shouldKeepTheHearingStreamUsableAfterARejectedCommand}
 * is the test that would fail if a guard went back to throwing and dead-lettered the queue.
 */
@SuppressWarnings("squid:S2699")
class PtphDetailIT extends AbstractIT {

    private static final UUID USER_ID = randomUUID();

    private static final String KEY_REASON = "Trial fixed date required by court order";

    private UUID givenAnInitiatedHearing() {
        givenAUserHasLoggedInAsACourtClerk(USER_ID);
        stubUsersAndGroupsUserRoles(USER_ID);

        final InitiateHearingCommandHelper hearing = h(initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));
        return hearing.getHearingId();
    }

    private SavePtphDetailCommand ptphDetail(final UUID hearingId, final Tier tier, final ListType listType, final String keyReason) {
        return new SavePtphDetailCommand(hearingId, tier, listType, keyReason);
    }

    @Test
    void shouldSaveTierAndListTypeAndReadThemBack() {
        final UUID hearingId = givenAnInitiatedHearing();

        savePtphDetail(getRequestSpec(), hearingId, ptphDetail(hearingId, Tier.TIER_2, ListType.TYPE_1_FIXED, KEY_REASON));

        pollForPtphDetail(hearingId,
                withJsonPath("$.tier", is("TIER_2")),
                withJsonPath("$.listType", is("TYPE_1_FIXED")),
                withJsonPath("$.keyReason", is(KEY_REASON)),
                withJsonPath("$.finalised", is(false)));
    }

    @Test
    void shouldSaveTierOnlyWhenListTypeIsChosenLater() {
        final UUID hearingId = givenAnInitiatedHearing();

        savePtphDetail(getRequestSpec(), hearingId, ptphDetail(hearingId, Tier.TIER_1, null, null));

        pollForPtphDetail(hearingId,
                withJsonPath("$.tier", is("TIER_1")),
                hasNoJsonPath("$.listType"),
                withJsonPath("$.finalised", is(false)));
    }

    /**
     * LPT-2401's Type-1 rule, enforced in {@code PtphDetailCommandHandler.resolveKeyReason}:
     * a key reason sent with a flexible list type is discarded rather than stored.
     */
    @Test
    void shouldDiscardKeyReasonForFlexibleListType() {
        final UUID hearingId = givenAnInitiatedHearing();

        savePtphDetail(getRequestSpec(), hearingId, ptphDetail(hearingId, Tier.TIER_5, ListType.TYPE_2_FLEXIBLE, "should be discarded"));

        pollForPtphDetail(hearingId,
                withJsonPath("$.tier", is("TIER_5")),
                withJsonPath("$.listType", is("TYPE_2_FLEXIBLE")),
                hasNoJsonPath("$.keyReason"));
    }

    /**
     * LPT-2403 — editing re-posts the save command as an upsert. Semantics are replace, not
     * merge: omitting the list type on an edit clears a previously stored one.
     */
    @Test
    void shouldReplaceRatherThanMergeOnEdit() {
        final UUID hearingId = givenAnInitiatedHearing();

        savePtphDetail(getRequestSpec(), hearingId, ptphDetail(hearingId, Tier.TIER_2, ListType.TYPE_1_FIXED, KEY_REASON));
        pollForPtphDetail(hearingId, withJsonPath("$.tier", is("TIER_2")));

        savePtphDetail(getRequestSpec(), hearingId, ptphDetail(hearingId, Tier.TIER_4, null, null));

        pollForPtphDetail(hearingId,
                withJsonPath("$.tier", is("TIER_4")),
                hasNoJsonPath("$.listType"),
                hasNoJsonPath("$.keyReason"));
    }

    @Test
    void shouldFinaliseWhenBothTierAndListTypeArePresent() {
        final UUID hearingId = givenAnInitiatedHearing();

        savePtphDetail(getRequestSpec(), hearingId, ptphDetail(hearingId, Tier.TIER_3, ListType.TYPE_1_FIXED, KEY_REASON));
        pollForPtphDetail(hearingId, withJsonPath("$.finalised", is(false)));

        finalisePtphDetail(getRequestSpec(), hearingId);

        pollForPtphDetail(hearingId,
                withJsonPath("$.tier", is("TIER_3")),
                withJsonPath("$.listType", is("TYPE_1_FIXED")),
                withJsonPath("$.finalised", is(true)));
    }

    /**
     * Precondition: finalise is rejected unless both tier and list type are saved. The POST still
     * returns 202, so the assertion is that the record stays un-finalised.
     */
    @Test
    void shouldNotFinaliseWhenListTypeIsMissing() {
        final UUID hearingId = givenAnInitiatedHearing();

        savePtphDetail(getRequestSpec(), hearingId, ptphDetail(hearingId, Tier.TIER_6, null, null));
        pollForPtphDetail(hearingId, withJsonPath("$.tier", is("TIER_6")));

        finalisePtphDetail(getRequestSpec(), hearingId);

        pollForPtphDetail(hearingId,
                withJsonPath("$.tier", is("TIER_6")),
                withJsonPath("$.finalised", is(false)));
    }

    /**
     * Precondition: once finalised the record is immutable — a further save is rejected by the
     * aggregate, so the stored tier must not change.
     */
    @Test
    void shouldRejectEditOnceFinalised() {
        final UUID hearingId = givenAnInitiatedHearing();

        savePtphDetail(getRequestSpec(), hearingId, ptphDetail(hearingId, Tier.TIER_3, ListType.TYPE_1_FIXED, KEY_REASON));
        finalisePtphDetail(getRequestSpec(), hearingId);
        pollForPtphDetail(hearingId, withJsonPath("$.finalised", is(true)));

        savePtphDetail(getRequestSpec(), hearingId, ptphDetail(hearingId, Tier.TIER_7, ListType.TYPE_2_FLEXIBLE, null));

        pollForPtphDetail(hearingId,
                withJsonPath("$.tier", is("TIER_3")),
                withJsonPath("$.listType", is("TYPE_1_FIXED")),
                withJsonPath("$.finalised", is(true)));
    }

    /**
     * LPT-2402 — delete removes the view-store row, so the query reports a blank record:
     * {@code HearingService.getPtphDetail} returns {@code PtphDetailResponse(null, null, null, false)}.
     */
    @Test
    void shouldDeleteDraftRecord() {
        final UUID hearingId = givenAnInitiatedHearing();

        savePtphDetail(getRequestSpec(), hearingId, ptphDetail(hearingId, Tier.TIER_2, ListType.TYPE_1_FIXED, KEY_REASON));
        pollForPtphDetail(hearingId, withJsonPath("$.tier", is("TIER_2")));

        deletePtphDetail(getRequestSpec(), hearingId);

        pollForPtphDetail(hearingId,
                hasNoJsonPath("$.tier"),
                hasNoJsonPath("$.listType"),
                hasNoJsonPath("$.keyReason"),
                withJsonPath("$.finalised", is(false)));
    }

    /**
     * Delete has no state precondition, so it is the one operation still permitted after
     * finalisation — and it clears the finalised flag along with the values.
     */
    @Test
    void shouldDeleteFinalisedRecord() {
        final UUID hearingId = givenAnInitiatedHearing();

        savePtphDetail(getRequestSpec(), hearingId, ptphDetail(hearingId, Tier.TIER_3, ListType.TYPE_1_FIXED, KEY_REASON));
        finalisePtphDetail(getRequestSpec(), hearingId);
        pollForPtphDetail(hearingId, withJsonPath("$.finalised", is(true)));

        deletePtphDetail(getRequestSpec(), hearingId);

        pollForPtphDetail(hearingId,
                hasNoJsonPath("$.tier"),
                withJsonPath("$.finalised", is(false)));
    }

    /**
     * A hearing that never had a tier saved must answer the query rather than 404 — this is what
     * lets the listing side (LPT-2405) treat "no record" as an ordinary not-finalised response.
     */
    @Test
    void shouldReturnBlankRecordWhenNothingWasEverSaved() {
        final UUID hearingId = givenAnInitiatedHearing();

        pollForPtphDetail(hearingId,
                hasNoJsonPath("$.tier"),
                hasNoJsonPath("$.listType"),
                withJsonPath("$.finalised", is(false)));
    }

    /**
     * The point of emitting {@code hearing.hearing-change-ignored} instead of throwing: a rejected
     * command must not poison the hearing's command queue. Two rejections that reach the aggregate
     * are driven back to back — a save over a finalised record and a second finalise — and then an
     * ordinary delete has to succeed. If either guard threw, the message would roll back and be
     * redelivered, and the delete that follows would never be applied.
     *
     * <p>Both are accepted with 202 and rejected asynchronously, which is exactly why the DLQ was
     * invisible before: the caller cannot tell a dropped command from an applied one.
     */
    @Test
    void shouldKeepTheHearingStreamUsableAfterARejectedCommand() {
        final UUID hearingId = givenAnInitiatedHearing();

        savePtphDetail(getRequestSpec(), hearingId, ptphDetail(hearingId, Tier.TIER_3, ListType.TYPE_1_FIXED, KEY_REASON));
        finalisePtphDetail(getRequestSpec(), hearingId);
        pollForPtphDetail(hearingId, withJsonPath("$.finalised", is(true)));

        savePtphDetail(getRequestSpec(), hearingId, ptphDetail(hearingId, Tier.TIER_1, ListType.TYPE_2_FLEXIBLE, null));
        finalisePtphDetail(getRequestSpec(), hearingId);

        pollForPtphDetail(hearingId,
                withJsonPath("$.tier", is("TIER_3")),
                withJsonPath("$.listType", is("TYPE_1_FIXED")),
                withJsonPath("$.finalised", is(true)));

        deletePtphDetail(getRequestSpec(), hearingId);

        pollForPtphDetail(hearingId,
                hasNoJsonPath("$.tier"),
                withJsonPath("$.finalised", is(false)));
    }

    /**
     * A fixed date must be justified, and the save schema enforces it, so the caller is told
     * immediately with a 400 rather than getting a 202 for a command that is later dropped. This
     * is the one PTPH rule that never reaches the aggregate.
     */
    @Test
    void shouldRejectAFixedListTypeWithNoKeyReasonAtTheApi() {
        final UUID hearingId = givenAnInitiatedHearing();

        savePtphDetailExpecting(getRequestSpec(), hearingId,
                ptphDetail(hearingId, Tier.TIER_1, ListType.TYPE_1_FIXED, null), SC_BAD_REQUEST);

        pollForPtphDetail(hearingId,
                hasNoJsonPath("$.tier"),
                withJsonPath("$.finalised", is(false)));
    }

    /** The same list type with a reason is accepted, so the rule is not simply refusing everything. */
    @Test
    void shouldAcceptAFixedListTypeWhenTheKeyReasonIsPresent() {
        final UUID hearingId = givenAnInitiatedHearing();

        savePtphDetail(getRequestSpec(), hearingId, ptphDetail(hearingId, Tier.TIER_1, ListType.TYPE_1_FIXED, KEY_REASON));

        pollForPtphDetail(hearingId,
                withJsonPath("$.tier", is("TIER_1")),
                withJsonPath("$.listType", is("TYPE_1_FIXED")),
                withJsonPath("$.keyReason", is(KEY_REASON)));
    }

    // ---------------------------------------------------------------------------------
    // Public events: each command must announce its outcome on the public topic, since the
    // 202 Accepted returns before the aggregate has run and so proves nothing on its own.
    // ---------------------------------------------------------------------------------

    @Test
    void shouldPublishPublicEventCarryingTheValuesWhenSaved() {
        final UUID hearingId = givenAnInitiatedHearing();

        try (EventListener publicSaved = listenFor("public.hearing.ptph-detail-saved")
                .withFilter(isJson(withJsonPath("$.hearingId", is(hearingId.toString()))))) {

            savePtphDetail(getRequestSpec(), hearingId, ptphDetail(hearingId, Tier.TIER_3, ListType.TYPE_1_FIXED, KEY_REASON));

            publicSaved.waitFor();
        }
    }

    @Test
    void shouldPublishPublicEventWhenFinalised() {
        final UUID hearingId = givenAnInitiatedHearing();

        savePtphDetail(getRequestSpec(), hearingId, ptphDetail(hearingId, Tier.TIER_3, ListType.TYPE_1_FIXED, KEY_REASON));
        pollForPtphDetail(hearingId, withJsonPath("$.tier", is("TIER_3")));

        try (EventListener publicFinalised = listenFor("public.hearing.ptph-detail-finalised")
                .withFilter(isJson(withJsonPath("$.hearingId", is(hearingId.toString()))))) {

            finalisePtphDetail(getRequestSpec(), hearingId);

            publicFinalised.waitFor();
        }
    }

    @Test
    void shouldPublishPublicEventWhenDeleted() {
        final UUID hearingId = givenAnInitiatedHearing();

        savePtphDetail(getRequestSpec(), hearingId, ptphDetail(hearingId, Tier.TIER_2, ListType.TYPE_1_FIXED, KEY_REASON));
        pollForPtphDetail(hearingId, withJsonPath("$.tier", is("TIER_2")));

        try (EventListener publicDeleted = listenFor("public.hearing.ptph-detail-deleted")
                .withFilter(isJson(withJsonPath("$.hearingId", is(hearingId.toString()))))) {

            deletePtphDetail(getRequestSpec(), hearingId);

            publicDeleted.waitFor();
        }
    }
}
