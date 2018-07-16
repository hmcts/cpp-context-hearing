package uk.gov.moj.cpp.hearing.it;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.Matchers.is;
import static uk.gov.moj.cpp.hearing.it.Queries.getHearingPollForMatch;
import static uk.gov.moj.cpp.hearing.it.UseCases.addOffence;
import static uk.gov.moj.cpp.hearing.it.UseCases.deleteOffence;
import static uk.gov.moj.cpp.hearing.it.UseCases.updateOffence;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.offenceTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.with;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;
import org.junit.Test;
import uk.gov.moj.cpp.hearing.command.offence.BaseDefendantOffence;
import uk.gov.moj.cpp.hearing.command.offence.CaseDefendantOffencesChangedCommand;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.Case;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.Defendant;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.Offence;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.InitiateHearingCommandHelper;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class CaseDefendantOffencesChangedIT extends AbstractIT {

    @Test
    public void caseDefendantOffencesChanged_addOffenceToExistingHearing() throws Exception {

        final InitiateHearingCommandHelper hearingOne = new InitiateHearingCommandHelper(
                UseCases.initiateHearing(requestSpec, standardInitiateHearingTemplate())
        );

        final CaseDefendantOffencesChangedCommand caseDefendantOffencesChanged = addOffence(hearingOne.getHearingId(), command -> {
            command.getAddedOffences().forEach(addedOffence -> {
                addedOffence
                        .setCaseId(hearingOne.getFirstCaseId())
                        .setDefendantId(hearingOne.getFirstDefendantId());
            });
        });

        Queries.getHearingPollForMatch(hearingOne.getHearingId(), 30,
                isBean(HearingDetailsResponse.class)
                        .with(HearingDetailsResponse::getHearingId, is(hearingOne.getHearingId().toString()))
                        .with(HearingDetailsResponse::getCases, first(isBean(Case.class)
                                        .with(Case::getCaseId, is(hearingOne.getFirstCaseId().toString()))
                                        .with(Case::getCaseUrn, is(hearingOne.getFirstCaseUrn()))
                                        .with(Case::getDefendants, first(isBean(Defendant.class)
                                                        .with(Defendant::getDefendantId, is(hearingOne.getFirstDefendantId().toString()))
                                                        .with(d -> d.getOffences().size(), is(2))
                                                        .with(d -> d.getOffences().stream().map(Offence::getId).collect(Collectors.toSet()),
                                                                hasItems(
                                                                        caseDefendantOffencesChanged.getAddedOffences().get(0).getOffences().get(0).getId().toString(),
                                                                        hearingOne.getFirstOffenceIdForFirstDefendant().toString())
                                                        )

                                                )
                                        )
                                )
                        )
        );

    }

    @Test
    public void caseDefendantOffencesChanged_updateExistingOffence() throws Exception {

        final InitiateHearingCommandHelper hearingOne = new InitiateHearingCommandHelper(
                UseCases.initiateHearing(requestSpec, standardInitiateHearingTemplate())
        );

        final CaseDefendantOffencesChangedCommand caseDefendantOffencesChanged = updateOffence(hearingOne.getHearingId(), command -> {
            command.getUpdatedOffences().forEach(updatedOffence -> {
                updatedOffence.getOffences()
                        .forEach(bdo -> bdo.setId(hearingOne.getFirstOffenceIdForFirstDefendant()));
            });
        });

        final BaseDefendantOffence updatedOffence0 = caseDefendantOffencesChanged.getUpdatedOffences().get(0).getOffences().get(0);

        getHearingPollForMatch(hearingOne.getHearingId(), 30,
                isBean(HearingDetailsResponse.class)
                        .with(HearingDetailsResponse::getHearingId, is(hearingOne.getHearingId().toString()))
                        .with(HearingDetailsResponse::getCases, first(isBean(Case.class)
                                        .with(Case::getCaseId, is(hearingOne.getFirstCaseId().toString()))
                                        .with(Case::getCaseUrn, is(hearingOne.getFirstCaseUrn()))
                                        .with(Case::getDefendants, first(isBean(Defendant.class)
                                                .with(Defendant::getDefendantId, is(hearingOne.getFirstDefendantId().toString()))
                                                .with(Defendant::getOffences, first(isBean(Offence.class)
                                                        .with(Offence::getId, is(updatedOffence0.getId().toString()))
                                                        .with(Offence::getConvictionDate, is(updatedOffence0.getConvictionDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))))
                                                        .with(Offence::getCount, is(updatedOffence0.getCount()))
                                                        .with(Offence::getWording, is(updatedOffence0.getWording()))
                                                )))
                                        )
                                )
                        )
        );

    }

    @Test
    public void caseDefendantOffencesChanged_deleteExistingOffence() throws Exception {

        final InitiateHearingCommandHelper hearingOne = new InitiateHearingCommandHelper(
                UseCases.initiateHearing(requestSpec, with(standardInitiateHearingTemplate(), i -> {
                    i.getHearing().getDefendants().get(0).getOffences().add(
                            offenceTemplate(i.getCases().get(0).getCaseId())
                    );
                }))
        );

        deleteOffence(hearingOne.getHearingId(), command -> {
            command.getDeletedOffences().forEach(deletedOffence -> {
                deletedOffence.setOffences(Arrays.asList(hearingOne.getFirstOffenceIdForFirstDefendant()));
            });
        });

        getHearingPollForMatch(hearingOne.getHearingId(), 30,
                isBean(HearingDetailsResponse.class)
                        .with(HearingDetailsResponse::getHearingId, is(hearingOne.getHearingId().toString()))
                        .with(HearingDetailsResponse::getCases, first(isBean(Case.class)
                                        .with(Case::getCaseId, is(hearingOne.getFirstCaseId().toString()))
                                        .with(Case::getCaseUrn, is(hearingOne.getFirstCaseUrn()))
                                        .with(Case::getDefendants, first(isBean(Defendant.class)
                                                .with(Defendant::getDefendantId, is(hearingOne.getFirstDefendantId().toString()))
                                                .with(Defendant::getOffences, first(isBean(Offence.class)
                                                        .with(Offence::getId, is(hearingOne.getSecondOffenceIdForFirstDefendant().toString()))
                                                )))
                                        )
                                )
                        )
        );
    }
}