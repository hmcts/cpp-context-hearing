package uk.gov.moj.cpp.hearing.domain.aggregate.util;

import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static uk.gov.justice.core.courts.JurisdictionType.CROWN;

import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Offence;
import uk.gov.moj.cpp.hearing.command.result.SharedResultsCommandPrompt;
import uk.gov.moj.cpp.hearing.command.result.SharedResultsCommandResultLine;
import uk.gov.moj.cpp.hearing.command.result.SharedResultsCommandResultLineV2;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.HearingAggregateMomento;
import uk.gov.moj.cpp.hearing.domain.event.CustodyTimeLimitClockStopped;
import uk.gov.moj.cpp.hearing.eventlog.HearingEvent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class CustodyTimeLimitUtil implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String[] onBailStatusCodes = new String[]{ "B", "U"};
    private static final String GUILTY = "GUILTY";
    private static final String JURIES_SWORN_IN_CROWN_ONLY = "Jury sworn-in";
    private static final UUID DEFENDANT_FOUND_UNDER_A_DISABILITY = UUID.fromString("d3d94468-02a4-3259-b55d-38e6d163e820");
    private static final UUID WITHDRAWN_RESULT_ID = UUID.fromString("eb2e4c4f-b738-4a4d-9cce-0572cecb7cb8");
    private static final UUID REMAND_STATUS_PROMPT_ID = UUID.fromString("9403f0d7-90b5-4377-84b4-f06a77811362");
    private static final UUID NEWTON_HEARING = UUID.fromString("b4352aac-5c07-30c5-a7f5-7d123c80775a");
    private static final String[] onBailStatusValues = new String[]{ "Conditional Bail", "Unconditional Bail"};

    /**
     * This method is to stop the clock for custodyTimeLimit. Stop the clock means delete
     * custodyTimeLimit object from offence and update CTLClockStopped flag as true.
     * <p>
     * If any of offence is Guilty or at least one final result or remand status is on bail , it will raise a event for per
     * offence and if the defendant is on Bail or  any of defendant's offence's verdict is
     * "Defendant found under a disability", it will raise event for all defendants offences
     */
    @SuppressWarnings("squid:S1612")
    public static Stream<Object> stopCTLExpiry(final HearingAggregateMomento momento, final List<SharedResultsCommandResultLine> resultLines) {

        if (isNotEmpty(momento.getHearing().getProsecutionCases())) {
            final Set<UUID> offenceIds = momento.getHearing().getProsecutionCases().stream()
                    .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream())
                    .flatMap(defendant -> defendant.getOffences().stream())
                    .filter(offence -> isGuiltyAndHasCTLExpiry(offence) || hasFinalResultAndHasCTLExpiry(offence, resultLines) || isOnBailAndHasCTLExpiry(offence, resultLines))
                    .map(Offence::getId)
                    .collect(Collectors.toSet());

            momento.getHearing().getProsecutionCases().stream()
                    .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream())
                    .filter(defendant -> isDefendantOnBail(defendant) || isAnyDefendantsOffencesVerdictIsDefendantFoundUnderADisability(defendant))
                    .flatMap(defendant -> defendant.getOffences().stream())
                    .forEach(offence -> {
                        if (isCTLExpiryExists(offence)) {
                            offenceIds.add(offence.getId());
                        }
                    });

            if (!offenceIds.isEmpty()) {
                return Stream.of(new CustodyTimeLimitClockStopped(momento.getHearing().getId(), new ArrayList<>(offenceIds)));
            }

        }

        return Stream.empty();
    }

    @SuppressWarnings("squid:S1612")
    public static Stream<Object> stopCTLExpiryForV2(final HearingAggregateMomento momento, final List<SharedResultsCommandResultLineV2> resultLines) {

        if (isNotEmpty(momento.getHearing().getProsecutionCases())) {
            final Set<UUID> offenceIds = momento.getHearing().getProsecutionCases().stream()
                    .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream())
                    .flatMap(defendant -> defendant.getOffences().stream())
                    .filter(offence -> isGuiltyAndHasCTLExpiry(offence) || hasFinalResultAndHasCTLExpiryForV2(offence, resultLines) || isOnBailAndHasCTLExpiryForV2(offence, resultLines))
                    .map(Offence::getId)
                    .collect(Collectors.toSet());

            momento.getHearing().getProsecutionCases().stream()
                    .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream())
                    .filter(defendant -> isDefendantOnBail(defendant) || isAnyDefendantsOffencesVerdictIsDefendantFoundUnderADisability(defendant))
                    .flatMap(defendant -> defendant.getOffences().stream())
                    .forEach(offence -> {
                        if (isCTLExpiryExists(offence)) {
                            offenceIds.add(offence.getId());
                        }
                    });

            if (!offenceIds.isEmpty()) {
                return Stream.of(new CustodyTimeLimitClockStopped(momento.getHearing().getId(), new ArrayList<>(offenceIds)));
            }

        }

        return Stream.empty();
    }

    /**
     * This method for event log flow.
     *
     * If the hearing type is Newton hearing, the clock will not be stopped. For any other type of trial hearing type the below check will be performed.
     *
     * If the hearing is CROWN and recorded event is Juries Sworn, it will raise the stop the clock event.
     *
     */
    @SuppressWarnings("squid:S1612")
    public static Stream<Object> stopCTLExpiryForTrialHearingUser(final HearingAggregateMomento momento, final HearingEvent hearingEvent, final List<UUID> hearingTypeIds) {

        final List<UUID> hearingTypeIdsWithoutNewtonHearing = hearingTypeIds.stream()
                .filter(uuid -> !NEWTON_HEARING.equals(uuid))
                .collect(Collectors.toList());

        if (nonNull(momento.getHearing()) && !hearingTypeIdsWithoutNewtonHearing.contains(momento.getHearing().getType().getId())) {
            return Stream.empty();
        }

        if (isHearingCrownAndIsEventJuriesSworn(momento, hearingEvent) && isNotEmpty(momento.getHearing().getProsecutionCases())) {
            final List<UUID> offenceIds = momento.getHearing().getProsecutionCases().stream()
                    .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream())
                    .flatMap(defendant -> defendant.getOffences().stream())
                    .filter(offence -> isCTLExpiryExists(offence))
                    .map(Offence::getId)
                    .collect(Collectors.toList());

            if (!offenceIds.isEmpty()) {
                return Stream.of(new CustodyTimeLimitClockStopped(momento.getHearing().getId(), offenceIds));
            }
        }

        return Stream.empty();
    }

    private static boolean isHearingCrownAndIsEventJuriesSworn(final HearingAggregateMomento momento, final HearingEvent hearingEvent) {
        return nonNull(momento.getHearing()) && CROWN.equals(momento.getHearing().getJurisdictionType()) && JURIES_SWORN_IN_CROWN_ONLY.equalsIgnoreCase(hearingEvent.getRecordedLabel());
    }

    private static boolean isDefendantOnBail(final Defendant defendant) {
        return nonNull(defendant.getPersonDefendant()) && nonNull(defendant.getPersonDefendant().getBailStatus())
                && Arrays.stream(onBailStatusCodes).anyMatch(defendant.getPersonDefendant().getBailStatus().getCode()::equals);
    }

    private static boolean isAnyDefendantsOffencesVerdictIsDefendantFoundUnderADisability(final Defendant defendant) {
        return defendant.getOffences().stream()
                .anyMatch(offence -> nonNull(offence.getVerdict()) && nonNull(offence.getVerdict().getVerdictType()) &&
                        offence.getVerdict().getVerdictType().getId().equals(DEFENDANT_FOUND_UNDER_A_DISABILITY));
    }

    private static boolean isOnBailAndHasCTLExpiryForV2(final Offence offence, final List<SharedResultsCommandResultLineV2> resultLines) {
        return isCTLExpiryExists(offence) && nonNull(resultLines) &&
                resultLines.stream().filter(Objects::nonNull)
                        .filter(result -> isResultNotDeletedAndAssociatedWithOffenceForV2(offence, result))
                        .anyMatch(result -> ofNullable(result.getPrompts()).map(Collection::stream).orElseGet(Stream::empty)
                                .anyMatch(prompt -> isPromptOnBail(prompt))
                        );
    }

    private static boolean isOnBailAndHasCTLExpiry(final Offence offence, final List<SharedResultsCommandResultLine> resultLines) {
        return isCTLExpiryExists(offence) && nonNull(resultLines) &&
                resultLines.stream().filter(Objects::nonNull)
                        .filter(result -> !result.getIsDeleted() &&  offence.getId().equals(result.getOffenceId()))
                        .anyMatch(result -> ofNullable(result.getPrompts()).map(Collection::stream).orElseGet(Stream::empty)
                                .anyMatch(prompt -> isPromptOnBail(prompt))
                        );
    }

    private static boolean isPromptOnBail(final SharedResultsCommandPrompt prompt) {
        return REMAND_STATUS_PROMPT_ID.equals(prompt.getId()) &&
                Arrays.stream(onBailStatusValues).anyMatch(prompt.getValue()::equalsIgnoreCase);
    }

    private static boolean isGuiltyAndHasCTLExpiry(final Offence offence) {
        return isGuilty(offence) &&
                isCTLExpiryExists(offence);
    }

    private static boolean hasFinalResultAndHasCTLExpiry(final Offence offence, final List<SharedResultsCommandResultLine> resultLines) {
        return isCTLExpiryExists(offence) && nonNull(resultLines) && resultLines.stream().filter(Objects::nonNull)
                .filter(result -> isResultNotDeletedAndAssociatedWithOffence(offence, result))
                .anyMatch(result -> WITHDRAWN_RESULT_ID.equals(result.getResultDefinitionId()));
    }

    private static boolean hasFinalResultAndHasCTLExpiryForV2(final Offence offence, final List<SharedResultsCommandResultLineV2> resultLines) {
        return isCTLExpiryExists(offence) && nonNull(resultLines) && resultLines.stream().filter(Objects::nonNull)
                .filter(result -> isResultNotDeletedAndAssociatedWithOffenceForV2(offence, result))
                .anyMatch(result -> WITHDRAWN_RESULT_ID.equals(result.getResultDefinitionId()));
    }

    private static boolean isResultNotDeletedAndAssociatedWithOffence(final Offence offence, final SharedResultsCommandResultLine result) {
        return !result.getIsDeleted() && offence.getId().equals(result.getOffenceId());
    }

    private static boolean isResultNotDeletedAndAssociatedWithOffenceForV2(final Offence offence, final SharedResultsCommandResultLineV2 result) {
        return !result.getIsDeleted() && offence.getId().equals(result.getOffenceId());
    }


    private static boolean isCTLExpiryExists(final Offence offence) {
        return nonNull(offence.getCustodyTimeLimit()) && nonNull(offence.getCustodyTimeLimit().getTimeLimit());
    }

    private static boolean isGuilty(final Offence offence) {
        return nonNull(offence.getPlea()) && GUILTY.equalsIgnoreCase(offence.getPlea().getPleaValue());
    }
}