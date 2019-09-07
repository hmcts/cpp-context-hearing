package uk.gov.moj.cpp.hearing.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.core.courts.IndicatedPleaValue.INDICATED_GUILTY;
import static uk.gov.justice.core.courts.IndicatedPleaValue.INDICATED_NOT_GUILTY;
import static uk.gov.justice.core.courts.PleaValue.GUILTY;
import static uk.gov.justice.core.courts.PleaValue.NOT_GUILTY;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.moj.cpp.hearing.it.Queries.getHearingPollForMatch;
import static uk.gov.moj.cpp.hearing.it.UseCases.initiateHearing;
import static uk.gov.moj.cpp.hearing.it.UseCases.updatePlea;
import static uk.gov.moj.cpp.hearing.it.Utilities.listenFor;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.InitiateHearingCommandHelper;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.CoreTemplateArguments.toMap;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.customStructureInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplateForIndicatedPlea;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.UpdatePleaCommandTemplates.updatePleaTemplate;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.asList;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.with;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;

import uk.gov.justice.core.courts.AllocationDecision;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.IndicatedPlea;
import uk.gov.justice.core.courts.IndicatedPleaValue;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.Plea;
import uk.gov.justice.core.courts.PleaValue;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.moj.cpp.hearing.it.Utilities.EventListener;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.UpdatePleaCommandHelper;

import org.hamcrest.CoreMatchers;
import org.junit.Test;


public class PleaIT extends AbstractIT {

    @Test
    public void updatePlea_toGuilty_shouldHaveConvictionDate() {

        final InitiateHearingCommandHelper hearingOne = h(initiateHearing(requestSpec, standardInitiateHearingTemplate()));

        getHearingPollForMatch(hearingOne.getHearingId(), 30, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingOne.getHearingId()))
                        .with(Hearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                .with(ProsecutionCase::getDefendants, first(isBean(Defendant.class)
                                        .with(Defendant::getOffences, first(isBean(Offence.class)
                                                .with(Offence::getId, is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId()))
                                                .with(Offence::getPlea, is(nullValue()))
                                                .with(Offence::getConvictionDate, is(nullValue()))
                                        ))
                                ))
                        ))

                )
        );

        final EventListener convictionDateListener = listenFor("public.hearing.offence-conviction-date-changed")
                .withFilter(isJson(allOf(
                        withJsonPath("$.offenceId", is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId().toString())),
                        withJsonPath("$.caseId", is(hearingOne.getFirstCase().getId().toString()))
                        ))
                );

        final UpdatePleaCommandHelper pleaOne = getUpdatePleaCommandHelper(hearingOne, null, false, GUILTY);

        convictionDateListener.waitFor();

        getHearingPollForMatch(hearingOne.getHearingId(), 30, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingOne.getHearingId()))
                        .with(Hearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                .with(ProsecutionCase::getDefendants, first(isBean(Defendant.class)
                                        .with(Defendant::getOffences, first(isBean(Offence.class)
                                                .with(Offence::getId, is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId()))
                                                .with(Offence::getPlea, isBean(Plea.class)
                                                        .with(Plea::getPleaDate, is(pleaOne.getFirstPleaDate()))
                                                        .with(Plea::getPleaValue, is(pleaOne.getFirstPleaValue()))
                                                )
                                                .with(Offence::getConvictionDate, is(pleaOne.getFirstPleaDate()))
                                        ))
                                ))
                        ))

                )
        );
    }

    @Test
    public void updateIndicatedPlea_toGuilty() {

        final InitiateHearingCommandHelper hearingOne = h(initiateHearing(requestSpec,
                standardInitiateHearingTemplateForIndicatedPlea(INDICATED_GUILTY, false)));

        getHearingPollForMatch(hearingOne.getHearingId(), 30, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingOne.getHearingId()))
                        .with(Hearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                .with(ProsecutionCase::getDefendants, first(isBean(Defendant.class)
                                        .with(Defendant::getOffences, first(isBean(Offence.class)
                                                .with(Offence::getId, is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId()))
                                                .with(Offence::getPlea, is(nullValue()))
                                                .with(Offence::getConvictionDate, is(nullValue()))
                                        ))
                                ))
                        ))

                )
        );

        final EventListener convictionDateListener = listenFor("public.hearing.offence-conviction-date-changed")
                .withFilter(isJson(allOf(
                        withJsonPath("$.offenceId", is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId().toString())),
                        withJsonPath("$.caseId", is(hearingOne.getFirstCase().getId().toString()))
                        ))
                );

        final UpdatePleaCommandHelper pleaOne = getUpdatePleaCommandHelper(hearingOne, INDICATED_GUILTY, false, null);

        convictionDateListener.waitFor();

        getHearingPollForMatch(hearingOne.getHearingId(), 30, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingOne.getHearingId()))
                        .with(Hearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                .with(ProsecutionCase::getDefendants, first(isBean(Defendant.class)
                                        .with(Defendant::getOffences, first(isBean(Offence.class)
                                                .with(Offence::getId, is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId()))
                                                .with(Offence::getPlea, is(nullValue()))
                                                .with(Offence::getIndicatedPlea, isBean(IndicatedPlea.class)
                                                    .with(IndicatedPlea::getIndicatedPleaValue, is(pleaOne.getFirstIndicatedPleaValue()))
                                                    .with(IndicatedPlea::getIndicatedPleaDate, is(pleaOne.getFirstIndicatedPleaDate()))
                                                    .with(IndicatedPlea::getOffenceId, is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId())))
                                        ))
                                ))
                        ))

                )
        );

        //Once Plea value set then Indicated value will be null as only one is possible at one time in real scenario.
        final UpdatePleaCommandHelper updatePlea = getUpdatePleaCommandHelper(hearingOne, null, true, GUILTY);

        assertThat(updatePlea.getPlea(), CoreMatchers.is(notNullValue()));
        assertThat(updatePlea.getIndicatedPlea(), CoreMatchers.is(nullValue()));

    }

    @Test
    public void updateIndicatedPlea_toNotGuilty_WithAllocationDecision() {

        final InitiateHearingCommandHelper hearingOne = h(initiateHearing(requestSpec,
                standardInitiateHearingTemplateForIndicatedPlea(INDICATED_NOT_GUILTY, true)));

        getHearingPollForMatch(hearingOne.getHearingId(), 30, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingOne.getHearingId()))
                        .with(Hearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                .with(ProsecutionCase::getDefendants, first(isBean(Defendant.class)
                                        .with(Defendant::getOffences, first(isBean(Offence.class)
                                                .with(Offence::getId, is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId()))
                                                .with(Offence::getPlea, is(nullValue()))
                                                .with(Offence::getConvictionDate, is(nullValue()))
                                        ))
                                ))
                        ))

                )
        );

        final EventListener convictionDateListener = listenFor("public.hearing.offence-conviction-date-removed")
                .withFilter(isJson(allOf(
                        withJsonPath("$.offenceId", is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId().toString())),
                        withJsonPath("$.caseId", is(hearingOne.getFirstCase().getId().toString())))));


        final UpdatePleaCommandHelper pleaOne = getUpdatePleaCommandHelper(hearingOne, INDICATED_NOT_GUILTY, true, null);

        convictionDateListener.waitFor();

        getHearingPollForMatch(hearingOne.getHearingId(), 30, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingOne.getHearingId()))
                        .with(Hearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                .with(ProsecutionCase::getDefendants, first(isBean(Defendant.class)
                                        .with(Defendant::getOffences, first(isBean(Offence.class)
                                                .with(Offence::getId, is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId()))
                                                .with(Offence::getPlea, is(nullValue()))
                                                .with(Offence::getIndicatedPlea, isBean(IndicatedPlea.class)
                                                        .with(IndicatedPlea::getIndicatedPleaValue, is(pleaOne.getFirstIndicatedPleaValue()))
                                                        .with(IndicatedPlea::getIndicatedPleaDate, is(pleaOne.getFirstIndicatedPleaDate())))
                                                .with(Offence::getAllocationDecision, isBean(AllocationDecision.class)
                                                        .with(AllocationDecision::getMotReasonId, is(pleaOne.getFirstAllocationDecisionMotReasonId())))
                                        ))
                                ))
                        ))

                )
        );
    }

    @Test
    public void updateIndicatedPlea_toNotGuilty_WithAllocationDecision_AndPlea() {

        final InitiateHearingCommandHelper hearingOne = h(initiateHearing(requestSpec,
                standardInitiateHearingTemplateForIndicatedPlea(INDICATED_NOT_GUILTY, true)));

        getHearingPollForMatch(hearingOne.getHearingId(), 30, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingOne.getHearingId()))
                        .with(Hearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                .with(ProsecutionCase::getDefendants, first(isBean(Defendant.class)
                                        .with(Defendant::getOffences, first(isBean(Offence.class)
                                                .with(Offence::getId, is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId()))
                                                .with(Offence::getPlea, is(nullValue()))
                                                .with(Offence::getConvictionDate, is(nullValue()))
                                        ))
                                ))
                        ))

                )
        );

        final EventListener convictionDateListener = listenFor("public.hearing.offence-conviction-date-removed")
                .withFilter(isJson(allOf(
                        withJsonPath("$.offenceId", is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId().toString())),
                        withJsonPath("$.caseId", is(hearingOne.getFirstCase().getId().toString())))));


        final UpdatePleaCommandHelper pleaOne = getUpdatePleaCommandHelper(hearingOne, INDICATED_NOT_GUILTY, true, NOT_GUILTY);

        convictionDateListener.waitFor();

        getHearingPollForMatch(hearingOne.getHearingId(), 30, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingOne.getHearingId()))
                        .with(Hearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                .with(ProsecutionCase::getDefendants, first(isBean(Defendant.class)
                                        .with(Defendant::getOffences, first(isBean(Offence.class)
                                                .with(Offence::getId, is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId()))
                                                .with(Offence::getPlea, isBean(Plea.class)
                                                        .with(Plea::getPleaDate, is(pleaOne.getFirstPleaDate()))
                                                        .with(Plea::getPleaValue, is(pleaOne.getFirstPleaValue())))
                                                .with(Offence::getIndicatedPlea, isBean(IndicatedPlea.class)
                                                        .with(IndicatedPlea::getIndicatedPleaValue, is(pleaOne.getFirstIndicatedPleaValue()))
                                                        .with(IndicatedPlea::getIndicatedPleaDate, is(pleaOne.getFirstIndicatedPleaDate())))
                                                .with(Offence::getAllocationDecision, isBean(AllocationDecision.class)
                                                        .with(AllocationDecision::getMotReasonId, is(pleaOne.getFirstAllocationDecisionMotReasonId())))
                                        ))
                                ))
                        ))

                )
        );
    }


    @Test
    public void updatePlea_toNotGuilty_shouldNotHaveConvictionDate() {

        final InitiateHearingCommandHelper hearingOne = h(initiateHearing(requestSpec, standardInitiateHearingTemplate()));

        getHearingPollForMatch(hearingOne.getHearingId(), 30, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingOne.getHearingId()))
                        .with(Hearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                .with(ProsecutionCase::getDefendants, first(isBean(Defendant.class)
                                        .with(Defendant::getOffences, first(isBean(Offence.class)
                                                .with(Offence::getId, is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId()))
                                                .with(Offence::getPlea, is(nullValue()))
                                                .with(Offence::getConvictionDate, is(nullValue()))
                                        ))
                                ))
                        ))

                )
        );

        final EventListener convictionDateListener = listenFor("public.hearing.offence-conviction-date-removed")
                .withFilter(isJson(allOf(
                        withJsonPath("$.offenceId", is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId().toString())),
                        withJsonPath("$.caseId", is(hearingOne.getFirstCase().getId().toString())))));

        final UpdatePleaCommandHelper pleaOne = getUpdatePleaCommandHelper(hearingOne, null, false, NOT_GUILTY);

        convictionDateListener.waitFor();

        getHearingPollForMatch(hearingOne.getHearingId(), 30, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingOne.getHearingId()))
                        .with(Hearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                .with(ProsecutionCase::getDefendants, first(isBean(Defendant.class)
                                        .with(Defendant::getOffences, first(isBean(Offence.class)
                                                .with(Offence::getId, is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId()))
                                                .with(Offence::getPlea, isBean(Plea.class)
                                                        .with(Plea::getPleaDate, is(pleaOne.getFirstPleaDate()))
                                                        .with(Plea::getPleaValue, is(pleaOne.getFirstPleaValue()))
                                                )
                                                .with(Offence::getConvictionDate, is(nullValue()))
                                        ))
                                ))
                        ))

                )
        );
    }

    @Test
    public void initiateHearing_shouldInheritGuiltyPlea_andConvictionDate() {
        final InitiateHearingCommandHelper hearingOne = h(initiateHearing(requestSpec, standardInitiateHearingTemplate()));

        final UpdatePleaCommandHelper pleaOne = getUpdatePleaCommandHelper(hearingOne, null, false, GUILTY);

        getHearingPollForMatch(hearingOne.getHearingId(), 30, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingOne.getHearingId()))
                        .with(Hearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                .with(ProsecutionCase::getDefendants, first(isBean(Defendant.class)
                                        .with(Defendant::getOffences, first(isBean(Offence.class)
                                                .with(Offence::getId, is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId()))
                                                .with(Offence::getPlea, isBean(Plea.class)
                                                        .with(Plea::getPleaDate, is(pleaOne.getFirstPleaDate()))
                                                        .with(Plea::getPleaValue, is(pleaOne.getFirstPleaValue()))
                                                )
                                                .with(Offence::getConvictionDate, is(pleaOne.getFirstPleaDate()))
                                        ))
                                ))
                        ))

                )
        );

        final InitiateHearingCommandHelper hearingTwo = h(initiateHearing(requestSpec, customStructureInitiateHearingTemplate(
                toMap(hearingOne.getFirstCase().getId(), toMap(hearingOne.getFirstDefendantForFirstCase().getId(), asList(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId())))
        )));

        getHearingPollForMatch(hearingTwo.getHearingId(), 30, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingTwo.getHearingId()))
                        .with(Hearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                .with(ProsecutionCase::getDefendants, first(isBean(Defendant.class)
                                        .with(Defendant::getOffences, first(isBean(Offence.class)
                                                .with(Offence::getId, is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId()))
                                                .with(Offence::getPlea, isBean(Plea.class)
                                                        .with(Plea::getPleaDate, is(pleaOne.getFirstPleaDate()))
                                                        .with(Plea::getPleaValue, is(pleaOne.getFirstPleaValue()))
                                                )
                                                .with(Offence::getConvictionDate, is(pleaOne.getFirstPleaDate()))
                                        ))
                                ))
                        ))

                )
        );

    }

    @Test
    public void initiateHearing_shouldInheritNotGuiltyPlea_andNullConvictionDate() {
        final InitiateHearingCommandHelper hearingOne = h(initiateHearing(requestSpec, with(standardInitiateHearingTemplate(), hearing -> {
            h(hearing).getFirstOffenceForFirstDefendantForFirstCase().setConvictionDate(PAST_LOCAL_DATE.next());
        })));

        final UpdatePleaCommandHelper pleaOne = getUpdatePleaCommandHelper(hearingOne, null, false, NOT_GUILTY);

        getHearingPollForMatch(hearingOne.getHearingId(), 30, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingOne.getHearingId()))
                        .with(Hearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                .with(ProsecutionCase::getDefendants, first(isBean(Defendant.class)
                                        .with(Defendant::getOffences, first(isBean(Offence.class)
                                                .with(Offence::getId, is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId()))
                                                .with(Offence::getPlea, isBean(Plea.class)
                                                        .with(Plea::getPleaDate, is(pleaOne.getFirstPleaDate()))
                                                        .with(Plea::getPleaValue, is(pleaOne.getFirstPleaValue()))
                                                )
                                                .with(Offence::getConvictionDate, is(nullValue()))
                                        ))
                                ))
                        ))

                )
        );

        final InitiateHearingCommandHelper hearingTwo = h(initiateHearing(requestSpec, customStructureInitiateHearingTemplate(
                toMap(hearingOne.getFirstCase().getId(), toMap(hearingOne.getFirstDefendantForFirstCase().getId(), asList(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId())))
        )));

        getHearingPollForMatch(hearingTwo.getHearingId(), 30, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingTwo.getHearingId()))
                        .with(Hearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                .with(ProsecutionCase::getDefendants, first(isBean(Defendant.class)
                                        .with(Defendant::getOffences, first(isBean(Offence.class)
                                                .with(Offence::getId, is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId()))
                                                .with(Offence::getPlea, isBean(Plea.class)
                                                        .with(Plea::getPleaDate, is(pleaOne.getFirstPleaDate()))
                                                        .with(Plea::getPleaValue, is(pleaOne.getFirstPleaValue()))
                                                )
                                                .with(Offence::getConvictionDate, is(nullValue()))
                                        ))
                                ))
                        ))

                )
        );

    }


    private UpdatePleaCommandHelper getUpdatePleaCommandHelper(final InitiateHearingCommandHelper hearingOne,
                                                               final IndicatedPleaValue indicatedPleaValue,
                                                               final boolean isAllocationDecision,
                                                               final PleaValue pleaValue) {
        return new UpdatePleaCommandHelper(
                updatePlea(requestSpec, hearingOne.getHearingId(), hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId(),
                        updatePleaTemplate(hearingOne.getHearingId(), hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId(),
                                hearingOne.getFirstDefendantForFirstCase().getId(), hearingOne.getFirstCase().getId(),
                                indicatedPleaValue, pleaValue, isAllocationDecision)
                ));
    }

}