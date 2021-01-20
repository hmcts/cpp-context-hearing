package uk.gov.moj.cpp.hearing.it;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import uk.gov.justice.core.courts.AllocationDecision;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.IndicatedPlea;
import uk.gov.justice.core.courts.IndicatedPleaValue;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.Plea;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.Verdict;
import uk.gov.justice.core.courts.VerdictType;
import uk.gov.moj.cpp.hearing.domain.updatepleas.UpdatePleaCommand;
import uk.gov.moj.cpp.hearing.it.Utilities.EventListener;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.UpdatePleaCommandHelper;

import java.time.LocalDate;
import java.util.UUID;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.core.courts.IndicatedPleaValue.INDICATED_GUILTY;
import static uk.gov.justice.core.courts.IndicatedPleaValue.INDICATED_NOT_GUILTY;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.it.Queries.getHearingPollForMatch;
import static uk.gov.moj.cpp.hearing.it.UseCases.initiateHearing;
import static uk.gov.moj.cpp.hearing.it.UseCases.updatePlea;
import static uk.gov.moj.cpp.hearing.it.Utilities.listenFor;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.InitiateHearingCommandHelper;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplateForIndicatedPlea;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.UpdatePleaCommandTemplates.updatePleaTemplate;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.with;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;


public class PleaIT extends AbstractIT {
    private static final String GUILTY = "GUILTY";
    private static final String NOT_GUILTY = "NOT_GUILTY";
    private static final String CONSENTS = "CONSENTS";

    public static final String PUBLIC_EVENT_OFFENCE_CONVICTION_DATE_REMOVED = "public.hearing.offence-conviction-date-removed";
    public static final String PUBLIC_EVENT_OFFENCE_CONVICTION_DATE_CHANGED = "public.hearing.offence-conviction-date-changed";

    @Test
    public void updatePlea_toGuilty_shouldHaveConvictionDate() {



        final InitiateHearingCommandHelper hearingOne = h(initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));

        getHearingPollForMatch(hearingOne.getHearingId(), isBean(HearingDetailsResponse.class)
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

        final EventListener convictionDateListener = listenFor(PUBLIC_EVENT_OFFENCE_CONVICTION_DATE_CHANGED)
                .withFilter(isJson(allOf(
                        withJsonPath("$.offenceId", is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId().toString())),
                        withJsonPath("$.caseId", is(hearingOne.getFirstCase().getId().toString()))
                        ))
                );

        final UpdatePleaCommandHelper pleaOne = getUpdatePleaCommandHelper(hearingOne, null, false, GUILTY);

        convictionDateListener.waitFor();

        getHearingPollForMatch(hearingOne.getHearingId(), isBean(HearingDetailsResponse.class)
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



        final InitiateHearingCommandHelper hearingOne = h(initiateHearing(getRequestSpec(),
                standardInitiateHearingTemplateForIndicatedPlea(INDICATED_GUILTY, false)));

        getHearingPollForMatch(hearingOne.getHearingId(), isBean(HearingDetailsResponse.class)
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

        final EventListener convictionDateListener = listenFor(PUBLIC_EVENT_OFFENCE_CONVICTION_DATE_CHANGED)
                .withFilter(isJson(allOf(
                        withJsonPath("$.offenceId", is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId().toString())),
                        withJsonPath("$.caseId", is(hearingOne.getFirstCase().getId().toString()))
                        ))
                );

        final UpdatePleaCommandHelper pleaOne = getUpdatePleaCommandHelper(hearingOne, INDICATED_GUILTY, false, null);

        convictionDateListener.waitFor();

        getHearingPollForMatch(hearingOne.getHearingId(), isBean(HearingDetailsResponse.class)
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


        final InitiateHearingCommandHelper hearingOne = h(initiateHearing(getRequestSpec(),
                standardInitiateHearingTemplateForIndicatedPlea(INDICATED_NOT_GUILTY, true)));

        getHearingPollForMatch(hearingOne.getHearingId(), isBean(HearingDetailsResponse.class)
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

        final EventListener convictionDateListener = listenFor(PUBLIC_EVENT_OFFENCE_CONVICTION_DATE_REMOVED)
                .withFilter(isJson(allOf(
                        withJsonPath("$.offenceId", is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId().toString())),
                        withJsonPath("$.caseId", is(hearingOne.getFirstCase().getId().toString())))));


        final UpdatePleaCommandHelper pleaOne = getUpdatePleaCommandHelper(hearingOne, INDICATED_NOT_GUILTY, true, null);

        convictionDateListener.waitFor();

        getHearingPollForMatch(hearingOne.getHearingId(), isBean(HearingDetailsResponse.class)
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



        final InitiateHearingCommandHelper hearingOne = h(initiateHearing(getRequestSpec(),
                standardInitiateHearingTemplateForIndicatedPlea(INDICATED_NOT_GUILTY, true)));

        getHearingPollForMatch(hearingOne.getHearingId(), isBean(HearingDetailsResponse.class)
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

        final EventListener convictionDateAddedListener = listenFor(PUBLIC_EVENT_OFFENCE_CONVICTION_DATE_CHANGED)
                .withFilter(isJson(allOf(
                        withJsonPath("$.offenceId", is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId().toString())),
                        withJsonPath("$.caseId", is(hearingOne.getFirstCase().getId().toString())))));

        final UpdatePleaCommandHelper guiltyPlea = getUpdatePleaCommandHelper(hearingOne, null, false, GUILTY);

        convictionDateAddedListener.waitFor();

        final EventListener convictionDateListener = listenFor(PUBLIC_EVENT_OFFENCE_CONVICTION_DATE_REMOVED)
                .withFilter(isJson(allOf(
                        withJsonPath("$.offenceId", is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId().toString())),
                        withJsonPath("$.caseId", is(hearingOne.getFirstCase().getId().toString())))));


        final UpdatePleaCommandHelper notGuiltyIndiatedPlea = getUpdatePleaCommandHelper(hearingOne, INDICATED_NOT_GUILTY, true, NOT_GUILTY);

        convictionDateListener.waitFor();

        getHearingPollForMatch(hearingOne.getHearingId(), isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingOne.getHearingId()))
                        .with(Hearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                .with(ProsecutionCase::getDefendants, first(isBean(Defendant.class)
                                        .with(Defendant::getOffences, first(isBean(Offence.class)
                                                .with(Offence::getId, is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId()))
                                                .with(Offence::getPlea, isBean(Plea.class)
                                                        .with(Plea::getPleaDate, is(notGuiltyIndiatedPlea.getFirstPleaDate()))
                                                        .with(Plea::getPleaValue, is(notGuiltyIndiatedPlea.getFirstPleaValue())))
                                                .with(Offence::getIndicatedPlea, isBean(IndicatedPlea.class)
                                                        .with(IndicatedPlea::getIndicatedPleaValue, is(notGuiltyIndiatedPlea.getFirstIndicatedPleaValue()))
                                                        .with(IndicatedPlea::getIndicatedPleaDate, is(notGuiltyIndiatedPlea.getFirstIndicatedPleaDate())))
                                                .with(Offence::getAllocationDecision, isBean(AllocationDecision.class)
                                                        .with(AllocationDecision::getMotReasonId, is(notGuiltyIndiatedPlea.getFirstAllocationDecisionMotReasonId())))
                                        ))
                                ))
                        ))

                )
        );
    }


    @Test
    public void updatePlea_toNotGuilty_shouldNotHaveConvictionDate() {


        final InitiateHearingCommandHelper hearingOne = h(initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));

        getHearingPollForMatch(hearingOne.getHearingId(), isBean(HearingDetailsResponse.class)
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

        final EventListener convictionDateAddedListener = listenFor(PUBLIC_EVENT_OFFENCE_CONVICTION_DATE_CHANGED)
                .withFilter(isJson(allOf(
                        withJsonPath("$.offenceId", is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId().toString())),
                        withJsonPath("$.caseId", is(hearingOne.getFirstCase().getId().toString())))));

        final UpdatePleaCommandHelper guiltyPlea = getUpdatePleaCommandHelper(hearingOne, null, false, GUILTY);

        convictionDateAddedListener.waitFor();

        getHearingPollForMatch(hearingOne.getHearingId(), isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingOne.getHearingId()))
                        .with(Hearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                .with(ProsecutionCase::getDefendants, first(isBean(Defendant.class)
                                        .with(Defendant::getOffences, first(isBean(Offence.class)
                                                .with(Offence::getId, is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId()))
                                                .with(Offence::getPlea, isBean(Plea.class)
                                                        .with(Plea::getPleaDate, is(guiltyPlea.getFirstPleaDate()))
                                                        .with(Plea::getPleaValue, is(guiltyPlea.getFirstPleaValue()))
                                                )
                                                .with(Offence::getConvictionDate, is(guiltyPlea.getFirstPleaDate()))
                                        ))
                                ))
                        ))
                )
        );

        final EventListener convictionDateRemovedListener = listenFor(PUBLIC_EVENT_OFFENCE_CONVICTION_DATE_REMOVED)
                .withFilter(isJson(allOf(
                        withJsonPath("$.offenceId", is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId().toString())),
                        withJsonPath("$.caseId", is(hearingOne.getFirstCase().getId().toString())))));

        final UpdatePleaCommandHelper notGuiltyPlea = getUpdatePleaCommandHelper(hearingOne, null, false, NOT_GUILTY);

        convictionDateRemovedListener.waitFor();

        getHearingPollForMatch(hearingOne.getHearingId(), isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingOne.getHearingId()))
                        .with(Hearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                .with(ProsecutionCase::getDefendants, first(isBean(Defendant.class)
                                        .with(Defendant::getOffences, first(isBean(Offence.class)
                                                .with(Offence::getId, is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId()))
                                                .with(Offence::getPlea, isBean(Plea.class)
                                                        .with(Plea::getPleaDate, is(notGuiltyPlea.getFirstPleaDate()))
                                                        .with(Plea::getPleaValue, is(notGuiltyPlea.getFirstPleaValue()))
                                                )
                                                .with(Offence::getConvictionDate, is(nullValue()))
                                        ))
                                ))
                        ))
                )
        );
    }

    @Test
    public void initiateAdjournedHearing_shouldUpdateGuiltyPleaWhenVerdictWasSetToGuiltyInEarlierHearing_andConvictionDateIsHearing1Date() {

        final LocalDate convictionDate = LocalDate.now();

        final CommandHelpers.InitiateHearingCommandHelper adjournedHearing = h(
                UseCases.initiateHearing(getRequestSpec(),
                        with(standardInitiateHearingTemplate(), i -> {
                            final Offence offence = h(i).getFirstOffenceForFirstDefendantForFirstCase();
                            offence.setConvictionDate(convictionDate);
                            offence.setVerdict(getVerdict(convictionDate, offence.getId()));
                            offence.setPlea(null);
                            offence.setIndicatedPlea(null);
                        })));

        final UpdatePleaCommandHelper pleaOne = getUpdatePleaCommandHelper(adjournedHearing, null, false, GUILTY);

        getHearingPollForMatch(adjournedHearing.getHearingId(), isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(adjournedHearing.getHearingId()))
                        .with(Hearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                .with(ProsecutionCase::getDefendants, first(isBean(Defendant.class)
                                        .with(Defendant::getOffences, first(isBean(Offence.class)
                                                .with(Offence::getId, is(adjournedHearing.getFirstOffenceForFirstDefendantForFirstCase().getId()))
                                                .with(Offence::getPlea, isBean(Plea.class)
                                                        .with(Plea::getPleaDate, is(pleaOne.getFirstPleaDate()))
                                                        .with(Plea::getPleaValue, is(pleaOne.getFirstPleaValue()))
                                                )
                                                .with(Offence::getConvictionDate, is(convictionDate))
                                        ))
                                ))
                        ))

                )
        );

    }

    private Verdict getVerdict(final LocalDate convictionDate, final UUID offenceId) {
        return Verdict.verdict().withVerdictDate(convictionDate)
                .withOffenceId(offenceId)
                .withOriginatingHearingId(randomUUID())
                .withVerdictType(VerdictType.verdictType()
                        .withCategoryType("Guilty")
                        .withCategory("Guilty")
                        .withId(randomUUID())
                        .withDescription(STRING.next())
                        .build())
                .build();
    }

    @Test
    public void initiateHearing_shouldInheritNotGuiltyPlea_andNullConvictionDate() {

        final InitiateHearingCommandHelper hearingOne = h(initiateHearing(getRequestSpec(), with(standardInitiateHearingTemplate(),
                hearing -> h(hearing).getFirstOffenceForFirstDefendantForFirstCase().setConvictionDate(PAST_LOCAL_DATE.next()))));

        final UpdatePleaCommandHelper pleaOne = getUpdatePleaCommandHelper(hearingOne, null, false, NOT_GUILTY);

        getHearingPollForMatch(hearingOne.getHearingId(), isBean(HearingDetailsResponse.class)
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
    public void updatePlea_toConsent_shouldHaveConvictionDate() {

        final InitiateHearingCommandHelper hearingOne = h(initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));

        getHearingPollForMatch(hearingOne.getHearingId(), isBean(HearingDetailsResponse.class)
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

        final EventListener convictionDateListener = listenFor(PUBLIC_EVENT_OFFENCE_CONVICTION_DATE_CHANGED)
                .withFilter(isJson(allOf(
                        withJsonPath("$.offenceId", is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId().toString())),
                        withJsonPath("$.caseId", is(hearingOne.getFirstCase().getId().toString()))
                        ))
                );

        final UpdatePleaCommandHelper pleaOne = getUpdatePleaCommandHelper(hearingOne, null, false, CONSENTS);

        convictionDateListener.waitFor();

        getHearingPollForMatch(hearingOne.getHearingId(), isBean(HearingDetailsResponse.class)
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

    private UpdatePleaCommandHelper getUpdatePleaCommandHelper(final InitiateHearingCommandHelper hearing,
                                                               final IndicatedPleaValue indicatedPleaValue,
                                                               final boolean isAllocationDecision,
                                                               final String pleaValue) {
        final UUID firstOffenceId = hearing.getFirstOffenceForFirstDefendantForFirstCase().getId();
        final UUID hearingId = hearing.getHearingId();
        final UUID defendantId = hearing.getFirstDefendantForFirstCase().getId();
        final UUID caseId = hearing.getFirstCase().getId();
        final UpdatePleaCommand hearingUpdatePleaCommand = updatePleaTemplate(hearingId, firstOffenceId, defendantId, caseId, indicatedPleaValue, pleaValue, isAllocationDecision);
        return new UpdatePleaCommandHelper(
                updatePlea(getRequestSpec(), hearingId, firstOffenceId,
                        hearingUpdatePleaCommand
                ));
    }

}