package uk.gov.moj.cpp.hearing.it;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.givenAUserHasLoggedInAsACourtClerk;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.CaseDefendantOffencesChangedCommandTemplates.addOffencesForDefendantTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.CaseDefendantOffencesChangedCommandTemplates.deleteOffencesForDefendantTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.CaseDefendantOffencesChangedCommandTemplates.updateOffencesForDefendantArguments;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.CaseDefendantOffencesChangedCommandTemplates.updateOffencesForDefendantTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.asList;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_POLL_TIMEOUT_IN_SEC;
import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.stubUsersAndGroupsUserRoles;

import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.ReportingRestriction;
import uk.gov.moj.cpp.hearing.command.offence.UpdateOffencesForDefendantCommand;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;

import java.util.UUID;

import org.junit.Test;

public class UpdateOffencesForDefendantIT extends AbstractIT {

    @Test
    public void caseDefendantOffencesChanged_addOffenceToExistingHearing() throws Exception {



        final CommandHelpers.InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));

        final CommandHelpers.UpdateOffencesForDefendantCommandHelper offenceAdded = h(UseCases.updateOffences(
                addOffencesForDefendantTemplate(
                        updateOffencesForDefendantArguments(
                                hearingOne.getFirstCase().getId(),
                                hearingOne.getFirstDefendantForFirstCase().getId()
                        )
                                .setOffencesToAdd(singletonList(randomUUID()))
                )
        ));

        Queries.getHearingPollForMatch(hearingOne.getHearingId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingOne.getHearingId()))
                        .with(Hearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                .with(ProsecutionCase::getId, is(hearingOne.getFirstCase().getId()))
                                .with(ProsecutionCase::getDefendants, first(isBean(Defendant.class)
                                        .with(Defendant::getId, is(hearingOne.getFirstDefendantForFirstCase().getId()))
                                        .with(Defendant::getOffences, hasItems(isBean(Offence.class)
                                                .with(Offence::getId, is(hearingOne.getFirstOffenceIdForFirstDefendant()))
                                        ))
                                        .with(Defendant::getOffences, hasItem(isBean(Offence.class)
                                                .with(Offence::getId, is(offenceAdded.getFirstOffenceFromAddedOffences().getId()))
                                                .with(Offence::getOffenceCode, is(offenceAdded.getFirstOffenceFromAddedOffences().getOffenceCode()))
                                                .with(Offence::getWording, is(offenceAdded.getFirstOffenceFromAddedOffences().getWording()))
                                                .with(Offence::getStartDate, is(offenceAdded.getFirstOffenceFromAddedOffences().getStartDate()))
                                                .with(Offence::getEndDate, is(offenceAdded.getFirstOffenceFromAddedOffences().getEndDate()))
                                                .with(Offence::getCount, is(offenceAdded.getFirstOffenceFromAddedOffences().getCount()))
                                                .with(Offence::getConvictionDate, is(offenceAdded.getFirstOffenceFromAddedOffences().getConvictionDate()))
                                        ))
                                ))
                        ))
                )
        );
    }

    @Test
    public void caseDefendantOffencesChanged_updateExistingOffence() throws Exception {



        final CommandHelpers.InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));
        Queries.getHearingPollForMatch(hearingOne.getHearingId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
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

        final CommandHelpers.UpdateOffencesForDefendantCommandHelper offenceUpdates = h(UseCases.updateOffences(updateOffencesForDefendantCommand));

        Queries.getHearingPollForMatch(hearingOne.getHearingId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
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

                                        ))
                                ))
                        ))
                )
        );
    }

    @Test
    public void caseDefendantOffencesChanged_deleteExistingOffence() throws Exception {



        final CommandHelpers.InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));

        h(UseCases.updateOffences(
                deleteOffencesForDefendantTemplate(
                        updateOffencesForDefendantArguments(
                                hearingOne.getFirstCase().getId(),
                                hearingOne.getFirstDefendantForFirstCase().getId()
                        )
                                .setOffenceToDelete(singletonList(hearingOne.getFirstOffenceIdForFirstDefendant()))
                )
        ));

        Queries.getHearingPollForMatch(hearingOne.getHearingId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
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
    }

    @Test
    public void caseDefendantOffencesChanged_updateExistingOffenceByRemovingReportingRestrictions() throws Exception {


        final CommandHelpers.InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));
        Queries.getHearingPollForMatch(hearingOne.getHearingId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
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

        final CommandHelpers.UpdateOffencesForDefendantCommandHelper offenceUpdates = h(UseCases.updateOffences(updateOffencesForDefendantCommand));

        Queries.getHearingPollForMatch(hearingOne.getHearingId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
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

    @Test
    public void caseDefendantOffencesChanged_addNewOffenceToExistingHearing_updateExistingOffenceByRemovingReportingRestrictions() throws Exception {



        final UUID newOffenceId = UUID.randomUUID();

        final CommandHelpers.InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));

        Queries.getHearingPollForMatch(hearingOne.getHearingId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingOne.getHearingId()))
                        .with(Hearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                .with(ProsecutionCase::getId, is(hearingOne.getFirstCase().getId()))
                                .with(ProsecutionCase::getDefendants, first(isBean(Defendant.class)
                                        .with(Defendant::getId, is(hearingOne.getFirstDefendantForFirstCase().getId()))
                                        .with(Defendant::getOffences, hasSize(1))
                                        .with(Defendant::getOffences, hasItem(isBean(Offence.class)
                                                .with(Offence::getId, is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId()))
                                                .with(Offence::getOffenceCode, is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getOffenceCode()))
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

        final CommandHelpers.UpdateOffencesForDefendantCommandHelper offenceAdded = h(UseCases.updateOffences(
                addOffencesForDefendantTemplate(
                        updateOffencesForDefendantArguments(
                                hearingOne.getFirstCase().getId(),
                                hearingOne.getFirstDefendantForFirstCase().getId()
                        )
                                .setOffencesToAdd(singletonList(newOffenceId))
                )
        ));


        Queries.getHearingPollForMatch(hearingOne.getHearingId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingOne.getHearingId()))
                        .with(Hearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                .with(ProsecutionCase::getId, is(hearingOne.getFirstCase().getId()))
                                .with(ProsecutionCase::getDefendants, first(isBean(Defendant.class)
                                        .with(Defendant::getId, is(hearingOne.getFirstDefendantForFirstCase().getId()))
                                        .with(Defendant::getOffences, hasSize(2))
                                        .with(Defendant::getOffences, hasItem(isBean(Offence.class)
                                                .with(Offence::getId, is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId()))
                                        ))
                                        .with(Defendant::getOffences, hasItem(isBean(Offence.class)
                                                .with(Offence::getId, is(offenceAdded.getFirstOffenceFromAddedOffences().getId()))
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
                        .setOffencesToUpdate(asList(hearingOne.getFirstOffenceIdForFirstDefendant(), offenceAdded.getFirstOffenceFromAddedOffences().getId()))
        );
        updateOffencesForDefendantCommand.getUpdatedOffences().get(0).getOffences().get(0).setReportingRestrictions(null);
        updateOffencesForDefendantCommand.getUpdatedOffences().get(0).getOffences().get(1).setReportingRestrictions(null);

        final CommandHelpers.UpdateOffencesForDefendantCommandHelper offenceUpdates = h(UseCases.updateOffences(updateOffencesForDefendantCommand));

        Queries.getHearingPollForMatch(hearingOne.getHearingId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingOne.getHearingId()))
                        .with(Hearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                .with(ProsecutionCase::getId, is(hearingOne.getFirstCase().getId()))
                                .with(ProsecutionCase::getDefendants, first(isBean(Defendant.class)
                                        .with(Defendant::getId, is(hearingOne.getFirstDefendantForFirstCase().getId()))
                                        .with(Defendant::getOffences, hasItem(isBean(Offence.class)
                                                        .with(Offence::getId, is(offenceUpdates.getFirstOffenceFromUpdatedOffences().getId()))
                                                        .with(Offence::getReportingRestrictions, empty())
                                                )
                                        ).with(Defendant::getOffences, hasItem(isBean(Offence.class)
                                                        .with(Offence::getId, is(offenceUpdates.getSecondOffenceFromUpdatedOffences().getId()))
                                                        .with(Offence::getReportingRestrictions, empty())
                                                )
                                        )
                                ))
                        ))
                )
        );
    }
}
