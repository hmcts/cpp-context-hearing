package uk.gov.moj.cpp.hearing.it;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static uk.gov.moj.cpp.hearing.it.Queries.getHearingPollForMatch;
import static uk.gov.moj.cpp.hearing.it.UseCases.initiateHearing;
import static uk.gov.moj.cpp.hearing.it.UseCases.updateOffences;
import static uk.gov.moj.cpp.hearing.it.UseCases.updatePleaNoAdditionalCheck;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.CaseDefendantOffencesChangedCommandTemplates.addOffencesForDefendantTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.CaseDefendantOffencesChangedCommandTemplates.deleteOffencesForDefendantTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.CaseDefendantOffencesChangedCommandTemplates.updateOffencesForDefendantArguments;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.CaseDefendantOffencesChangedCommandTemplates.updateOffencesForDefendantTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.UpdatePleaCommandTemplates.updatePleaTemplate;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;

import uk.gov.justice.core.courts.AllocationDecision;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.ReportingRestriction;
import uk.gov.moj.cpp.hearing.command.offence.UpdateOffencesForDefendantCommand;
import uk.gov.moj.cpp.hearing.domain.updatepleas.UpdatePleaCommand;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.InitiateHearingCommandHelper;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.UpdateOffencesForDefendantCommandHelper;

import java.util.UUID;

import org.junit.jupiter.api.Test;

@SuppressWarnings("squid:S2699")
class UpdateOffencesForDefendantIT extends AbstractIT {

    @Test
    void caseDefendantOffencesCUpdated_addRemoveUpdateOperations() throws Exception {

        final InitiateHearingCommandHelper hearingOne = h(initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));

        final UpdateOffencesForDefendantCommand updateOffencesForDefendantCommand = updateOffencesForDefendantTemplate(
                updateOffencesForDefendantArguments(
                        hearingOne.getFirstCase().getId(),
                        hearingOne.getFirstDefendantForFirstCase().getId()
                )
                        .setOffencesToUpdate(singletonList(hearingOne.getFirstOffenceIdForFirstDefendant()))
        );

        final UpdateOffencesForDefendantCommandHelper offenceUpdates = h(updateOffences(updateOffencesForDefendantCommand));

        getHearingPollForMatch(hearingOne.getHearingId(), isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingOne.getHearingId()))
                        .with(Hearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                .with(ProsecutionCase::getId, is(hearingOne.getFirstCase().getId()))
                                .with(ProsecutionCase::getDefendants, first(isBean(Defendant.class)
                                        .with(Defendant::getId, is(hearingOne.getFirstDefendantForFirstCase().getId()))
                                        .with(Defendant::getOffences, hasItem(isBean(Offence.class)
                                                .with(Offence::getId, is(offenceUpdates.getFirstOffenceFromUpdatedOffences().getId()))
                                                .with(Offence::getOffenceCode, is(offenceUpdates.getFirstOffenceFromUpdatedOffences().getOffenceCode()))
                                                .with(Offence::getWording, is(offenceUpdates.getFirstOffenceFromUpdatedOffences().getWording()))
                                                .with(Offence::getStartDate, is(offenceUpdates.getFirstOffenceFromUpdatedOffences().getStartDate()))
                                                .with(Offence::getEndDate, is(offenceUpdates.getFirstOffenceFromUpdatedOffences().getEndDate()))
                                                .with(Offence::getCount, is(offenceUpdates.getFirstOffenceFromUpdatedOffences().getCount()))
                                                .with(Offence::getIndictmentParticular, is(offenceUpdates.getFirstOffenceFromUpdatedOffences().getIndictmentParticular()))
                                                .with(Offence::getConvictionDate, is(offenceUpdates.getFirstOffenceFromUpdatedOffences().getConvictionDate()))
                                                .with(Offence::getLaaApplnReference, is(offenceUpdates.getFirstOffenceFromUpdatedOffences().getLaaApplnReference()))
                                                .with(Offence::getIsDiscontinued, is(offenceUpdates.getFirstOffenceFromUpdatedOffences().getIsDiscontinued()))
                                                .with(Offence::getIntroducedAfterInitialProceedings, is(offenceUpdates.getFirstOffenceFromUpdatedOffences().getIntroducedAfterInitialProceedings()))
                                                .with(Offence::getProceedingsConcluded, is(offenceUpdates.getFirstOffenceFromUpdatedOffences().getProceedingsConcluded()))

                                        ))
                                ))
                        ))
                )
        );

        h(updateOffences(
                deleteOffencesForDefendantTemplate(
                        updateOffencesForDefendantArguments(
                                hearingOne.getFirstCase().getId(),
                                hearingOne.getFirstDefendantForFirstCase().getId()
                        )
                                .setOffenceToDelete(singletonList(hearingOne.getFirstOffenceIdForFirstDefendant()))
                )
        ));

        getHearingPollForMatch(hearingOne.getHearingId(), isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingOne.getHearingId()))
                        .with(Hearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                .with(ProsecutionCase::getId, is(hearingOne.getFirstCase().getId()))
                                .with(ProsecutionCase::getDefendants, first(isBean(Defendant.class)
                                        .with(Defendant::getId, is(hearingOne.getFirstDefendantForFirstCase().getId()))
                                        .with(Defendant::getOffences, is(emptyList()))
                                ))
                        ))
                )
        );


        final UpdateOffencesForDefendantCommandHelper offenceAdded = h(updateOffences(
                addOffencesForDefendantTemplate(
                        updateOffencesForDefendantArguments(
                                hearingOne.getFirstCase().getId(),
                                hearingOne.getFirstDefendantForFirstCase().getId()
                        )
                                .setOffencesToAdd(singletonList(randomUUID()))
                )
        ));

        getHearingPollForMatch(hearingOne.getHearingId(), isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingOne.getHearingId()))
                        .with(Hearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                .with(ProsecutionCase::getId, is(hearingOne.getFirstCase().getId()))
                                .with(ProsecutionCase::getDefendants, first(isBean(Defendant.class)
                                        .with(Defendant::getId, is(hearingOne.getFirstDefendantForFirstCase().getId()))
                                        .with(Defendant::getOffences, hasItem(isBean(Offence.class)
                                                .with(Offence::getId, is(offenceAdded.getFirstOffenceFromAddedOffences().getId()))
                                                .with(Offence::getOffenceCode, is(offenceAdded.getFirstOffenceFromAddedOffences().getOffenceCode()))
                                                .with(Offence::getWording, is(offenceAdded.getFirstOffenceFromAddedOffences().getWording()))
                                                .with(Offence::getStartDate, is(offenceAdded.getFirstOffenceFromAddedOffences().getStartDate()))
                                                .with(Offence::getEndDate, is(offenceAdded.getFirstOffenceFromAddedOffences().getEndDate()))
                                                .with(Offence::getCount, is(offenceAdded.getFirstOffenceFromAddedOffences().getCount()))
                                                .with(Offence::getIndictmentParticular, is(offenceAdded.getFirstOffenceFromAddedOffences().getIndictmentParticular()))
                                                .with(Offence::getConvictionDate, is(offenceAdded.getFirstOffenceFromAddedOffences().getConvictionDate()))
                                        ))
                                ))
                        ))
                )
        );

    }

    @Test
    void caseDefendantOffencesChanged_updateExistingOffenceByRemovingReportingRestrictions() throws Exception {

        final InitiateHearingCommandHelper hearingOne = h(initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));
        getHearingPollForMatch(hearingOne.getHearingId(), isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingOne.getHearingId()))
                        .with(Hearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                .with(ProsecutionCase::getId, is(hearingOne.getFirstCase().getId()))
                                .with(ProsecutionCase::getDefendants, first(isBean(Defendant.class)
                                        .with(Defendant::getId, is(hearingOne.getFirstDefendantForFirstCase().getId()))
                                        .with(Defendant::getOffences, hasItem(isBean(Offence.class)
                                                .with(Offence::getId, is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId()))
                                                .with(Offence::getOffenceCode, is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getOffenceCode()))
                                                .with(Offence::getWording, is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getWording()))
                                                .with(Offence::getStartDate, is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getStartDate()))
                                                .with(Offence::getEndDate, is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getEndDate()))
                                                .with(Offence::getCount, is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getCount()))
                                                .with(Offence::getConvictionDate, is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getConvictionDate()))
                                                .with(Offence::getLaaApplnReference, is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getLaaApplnReference()))
                                                .with(Offence::getIsDiscontinued, is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getIsDiscontinued()))
                                                .with(Offence::getIntroducedAfterInitialProceedings, is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getIntroducedAfterInitialProceedings()))
                                                .with(Offence::getProceedingsConcluded, is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getProceedingsConcluded()))
                                                .with(Offence::getReportingRestrictions, first(isBean(ReportingRestriction.class)
                                                        .with(ReportingRestriction::getId, is(hearingOne.getFirstReportingRestrictionForFirstOffenceForFirstDefendantForFirstCase().getId()))
                                                        .with(ReportingRestriction::getJudicialResultId, is(hearingOne.getFirstReportingRestrictionForFirstOffenceForFirstDefendantForFirstCase().getJudicialResultId()))
                                                        .with(ReportingRestriction::getLabel, is(hearingOne.getFirstReportingRestrictionForFirstOffenceForFirstDefendantForFirstCase().getLabel()))
                                                        .with(ReportingRestriction::getOrderedDate, is(hearingOne.getFirstReportingRestrictionForFirstOffenceForFirstDefendantForFirstCase().getOrderedDate()))
                                                ))
                                        ))
                                ))
                        ))
                )
        );

        final UpdateOffencesForDefendantCommand updateOffencesForDefendantCommand = updateOffencesForDefendantTemplate(
                updateOffencesForDefendantArguments(
                        hearingOne.getFirstCase().getId(),
                        hearingOne.getFirstDefendantForFirstCase().getId()
                )
                        .setOffencesToUpdate(singletonList(hearingOne.getFirstOffenceIdForFirstDefendant()))
        );
        updateOffencesForDefendantCommand.getUpdatedOffences().get(0).getOffences().get(0).setReportingRestrictions(null);

        final UpdateOffencesForDefendantCommandHelper offenceUpdates = h(updateOffences(updateOffencesForDefendantCommand));

        getHearingPollForMatch(hearingOne.getHearingId(), isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingOne.getHearingId()))
                        .with(Hearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                .with(ProsecutionCase::getId, is(hearingOne.getFirstCase().getId()))
                                .with(ProsecutionCase::getDefendants, first(isBean(Defendant.class)
                                        .with(Defendant::getId, is(hearingOne.getFirstDefendantForFirstCase().getId()))
                                        .with(Defendant::getOffences, hasItem(isBean(Offence.class)
                                                .with(Offence::getId, is(offenceUpdates.getFirstOffenceFromUpdatedOffences().getId()))
                                                .with(Offence::getOffenceCode, is(offenceUpdates.getFirstOffenceFromUpdatedOffences().getOffenceCode()))
                                                .with(Offence::getWording, is(offenceUpdates.getFirstOffenceFromUpdatedOffences().getWording()))
                                                .with(Offence::getStartDate, is(offenceUpdates.getFirstOffenceFromUpdatedOffences().getStartDate()))
                                                .with(Offence::getEndDate, is(offenceUpdates.getFirstOffenceFromUpdatedOffences().getEndDate()))
                                                .with(Offence::getCount, is(offenceUpdates.getFirstOffenceFromUpdatedOffences().getCount()))
                                                .with(Offence::getConvictionDate, is(offenceUpdates.getFirstOffenceFromUpdatedOffences().getConvictionDate()))
                                                .with(Offence::getLaaApplnReference, is(offenceUpdates.getFirstOffenceFromUpdatedOffences().getLaaApplnReference()))
                                                .with(Offence::getIsDiscontinued, is(offenceUpdates.getFirstOffenceFromUpdatedOffences().getIsDiscontinued()))
                                                .with(Offence::getIntroducedAfterInitialProceedings, is(offenceUpdates.getFirstOffenceFromUpdatedOffences().getIntroducedAfterInitialProceedings()))
                                                .with(Offence::getProceedingsConcluded, is(offenceUpdates.getFirstOffenceFromUpdatedOffences().getProceedingsConcluded()))
                                                .with(Offence::getReportingRestrictions, empty())
                                        ))
                                ))
                        ))
                )
        );
    }

    /**
     * Regression test for the Grounds for Sending / mode of trial data-loss bug: an offence
     * re-sync (as would happen when upstream case/offence detail changes) must not wipe out
     * an allocation decision already recorded against the offence by the hearing service's own
     * plea/allocation flow, even though the re-sync payload itself carries no allocation data.
     */
    @Test
    void updateOffencesForDefendant_shouldRetainExistingAllocationDecision_whenSyncPayloadHasNone() throws Exception {

        final InitiateHearingCommandHelper hearingOne = h(initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));

        final UUID hearingId = hearingOne.getHearingId();
        final UUID offenceId = hearingOne.getFirstOffenceIdForFirstDefendant();
        final UUID defendantId = hearingOne.getFirstDefendantForFirstCase().getId();
        final UUID caseId = hearingOne.getFirstCase().getId();


        // record a mode-of-trial / Grounds for Sending decision via the plea flow
        final UpdatePleaCommand allocationDecisionCommand =
                updatePleaTemplate(hearingId, offenceId, defendantId, caseId, null, null, true, null);

        updatePleaNoAdditionalCheck(getRequestSpec(), hearingId, allocationDecisionCommand);
        final UUID motReasonId = allocationDecisionCommand.getPleas().get(0).getAllocationDecision().getMotReasonId();

        getHearingPollForMatch(hearingId, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                .with(ProsecutionCase::getDefendants, first(isBean(Defendant.class)
                                        .with(Defendant::getOffences, hasItem(isBean(Offence.class)
                                                .with(Offence::getId, is(offenceId))
                                                .with(Offence::getAllocationDecision, isBean(AllocationDecision.class)
                                                        .with(AllocationDecision::getMotReasonId, is(motReasonId)))
                                        ))
                                ))
                        ))
                )
        );

        // simulate an unrelated case/offence-detail re-sync from upstream that carries no
        // allocation decision at all -- it must not clear the one recorded above
        final UpdateOffencesForDefendantCommand resyncCommand = updateOffencesForDefendantTemplate(
                updateOffencesForDefendantArguments(caseId, defendantId)
                        .setOffencesToUpdate(singletonList(offenceId))
        );
        final Offence resyncOffence = resyncCommand.getUpdatedOffences().get(0).getOffences().get(0);
        resyncOffence.setAllocationDecision(null);
        final String newWording = "resynced-wording-" + randomUUID();
        resyncOffence.setWording(newWording);

        h(updateOffences(resyncCommand));

        getHearingPollForMatch(hearingId, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                .with(ProsecutionCase::getDefendants, first(isBean(Defendant.class)
                                        .with(Defendant::getOffences, hasItem(isBean(Offence.class)
                                                .with(Offence::getId, is(offenceId))
                                                // the re-synced field did change, proving the update was applied
                                                .with(Offence::getWording, is(newWording))
                                                // but the allocation decision from before the re-sync must survive
                                                .with(Offence::getAllocationDecision, isBean(AllocationDecision.class)
                                                        .with(AllocationDecision::getMotReasonId, is(motReasonId)))
                                        ))
                                ))
                        ))
                )
        );
    }

}
