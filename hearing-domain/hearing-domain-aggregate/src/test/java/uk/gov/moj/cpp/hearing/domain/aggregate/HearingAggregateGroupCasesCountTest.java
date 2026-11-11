package uk.gov.moj.cpp.hearing.domain.aggregate;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.moj.cpp.hearing.domain.event.CasesUpdatedAfterCaseRemovedFromGroupCases;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

/**
 * CAD-947: the hearing stores the numberOfGroupCases that progression sends when a case is removed from
 * its group. Without a count the stored value is left unchanged.
 */
public class HearingAggregateGroupCasesCountTest {

    private static final UUID HEARING_ID = randomUUID();
    private static final UUID GROUP_ID = randomUUID();
    private static final UUID MASTER_CASE_ID = randomUUID();
    private static final UUID MEMBER_CASE_ID = randomUUID();

    @Test
    public void shouldUseNumberOfGroupCasesFromEventWhenSupplied() {
        final HearingAggregate hearingAggregate = initiateGroupHearing(3, masterCase());

        final CasesUpdatedAfterCaseRemovedFromGroupCases event = removeCase(hearingAggregate, removedCase(MEMBER_CASE_ID), null, 2);

        assertThat(event.getNumberOfGroupCases(), is(2));
        assertThat(hearingAggregate.getHearing().getNumberOfGroupCases(), is(2));
    }

    @Test
    public void shouldUseNumberOfGroupCasesFromEventWhenMasterCaseRemoved() {
        final HearingAggregate hearingAggregate = initiateGroupHearing(3, masterCase());

        final CasesUpdatedAfterCaseRemovedFromGroupCases event = removeCase(hearingAggregate, removedCase(MASTER_CASE_ID), newGroupMaster(MEMBER_CASE_ID), 2);

        assertThat(event.getNumberOfGroupCases(), is(2));
        assertThat(hearingAggregate.getHearing().getNumberOfGroupCases(), is(2));
    }

    @Test
    public void shouldStoreNumberOfGroupCasesFromEventWhenHearingHadNone() {
        final HearingAggregate hearingAggregate = initiateGroupHearing(null, masterCase());

        removeCase(hearingAggregate, removedCase(MEMBER_CASE_ID), null, 2);

        assertThat(hearingAggregate.getHearing().getNumberOfGroupCases(), is(2));
    }

    @Test
    public void shouldLeaveNumberOfGroupCasesUnchangedWhenEventHasNoCount() {
        final HearingAggregate hearingAggregate = initiateGroupHearing(3, masterCase());

        final CasesUpdatedAfterCaseRemovedFromGroupCases event = removeCase(hearingAggregate, removedCase(MEMBER_CASE_ID), null, null);

        assertThat(event.getNumberOfGroupCases(), is(nullValue()));
        assertThat(hearingAggregate.getHearing().getNumberOfGroupCases(), is(3));
    }

    private CasesUpdatedAfterCaseRemovedFromGroupCases removeCase(final HearingAggregate hearingAggregate, final ProsecutionCase removedCase,
                                                                  final ProsecutionCase newGroupMaster, final Integer numberOfGroupCases) {
        final List<Object> events = hearingAggregate
                .updateCasesAfterCaseRemovedFromGroupCases(HEARING_ID, GROUP_ID, removedCase, newGroupMaster, numberOfGroupCases)
                .collect(toList());
        assertThat(events.size(), is(1));
        return (CasesUpdatedAfterCaseRemovedFromGroupCases) events.get(0);
    }

    private HearingAggregate initiateGroupHearing(final Integer numberOfGroupCases, final ProsecutionCase... prosecutionCases) {
        final HearingAggregate hearingAggregate = new HearingAggregate();
        hearingAggregate.initiate(Hearing.hearing()
                .withId(HEARING_ID)
                .withIsGroupProceedings(Boolean.TRUE)
                .withNumberOfGroupCases(numberOfGroupCases)
                .withProsecutionCases(new ArrayList<>(asList(prosecutionCases)))
                .build()).collect(toList());
        return hearingAggregate;
    }

    private static ProsecutionCase masterCase() {
        return prosecutionCase(MASTER_CASE_ID, true, true);
    }

    private static ProsecutionCase newGroupMaster(final UUID caseId) {
        return prosecutionCase(caseId, true, true);
    }

    private static ProsecutionCase removedCase(final UUID caseId) {
        return prosecutionCase(caseId, false, false);
    }

    private static ProsecutionCase prosecutionCase(final UUID caseId, final boolean isGroupMember, final boolean isGroupMaster) {
        return ProsecutionCase.prosecutionCase()
                .withId(caseId)
                .withIsCivil(Boolean.TRUE)
                .withGroupId(GROUP_ID)
                .withIsGroupMember(isGroupMember)
                .withIsGroupMaster(isGroupMaster)
                .withDefendants(asList(Defendant.defendant()
                        .withId(randomUUID())
                        .withOffences(asList(Offence.offence()
                                .withId(randomUUID())
                                .build()))
                        .build()))
                .build();
    }
}
