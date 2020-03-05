package uk.gov.moj.cpp.hearing.it;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.CaseDefendantOffencesChangedCommandTemplates.addOffencesForDefendantTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.CaseDefendantOffencesChangedCommandTemplates.deleteOffencesForDefendantTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.CaseDefendantOffencesChangedCommandTemplates.updateOffencesForDefendantArguments;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.CaseDefendantOffencesChangedCommandTemplates.updateOffencesForDefendantTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_POLL_TIMEOUT_IN_SEC;

import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;

import org.junit.Ignore;
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
                                                .with(Offence::getIsIntroduceAfterInitialProceedings, is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getIsIntroduceAfterInitialProceedings()))
                                                .with(Offence::getProceedingsConcluded, is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getProceedingsConcluded()))

                                        ))
                                ))
                        ))
                )
        );

        final CommandHelpers.UpdateOffencesForDefendantCommandHelper offenceUpdates = h(UseCases.updateOffences(
                updateOffencesForDefendantTemplate(
                        updateOffencesForDefendantArguments(
                                hearingOne.getFirstCase().getId(),
                                hearingOne.getFirstDefendantForFirstCase().getId()
                        )
                                .setOffencesToUpdate(singletonList(hearingOne.getFirstOffenceIdForFirstDefendant()))
                )
        ));

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
                                                .with(Offence::getIsIntroduceAfterInitialProceedings, is(offenceUpdates.getFirstOffenceFromUpdatedOffences().getIsIntroduceAfterInitialProceedings()))
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
}
