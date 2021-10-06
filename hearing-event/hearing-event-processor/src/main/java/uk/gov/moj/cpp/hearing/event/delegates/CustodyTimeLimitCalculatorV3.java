package uk.gov.moj.cpp.hearing.event.delegates;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

import uk.gov.justice.core.courts.BailStatus;
import uk.gov.justice.core.courts.CustodyTimeLimit;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.JudicialResult;
import uk.gov.justice.core.courts.JudicialResultPrompt;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.Target2;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsSharedV3;
import uk.gov.moj.cpp.hearing.event.model.ExtendedCustodyTimeLimit;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings({"squid:S1612", "squid:S00112", "squid:S00108", "squid:S1166"})
public class CustodyTimeLimitCalculatorV3 {


    private static final String CUSTODY_OR_REMANDED_INTO_CUSTODY = "C";
    private static final String REMANDED_INTO_CARE_OF_LOCAL_AUTHORITY = "L";
    private static final String SECURE_ACCOMMODATION = "S";
    private static final String REMANDED_IN_CUSTODY_PENDING_CONDITIONS = "P";
    private static final String[] onBailStatusCodes = new String[]{"B", "U"};
    private static final String[] inCustodyCodes = new String[]{"C", "S", "L", "P"};

    private static final String CTL_TIME_LIMIT_PROMPT_REF = "CTLDATE";
    private static final String DATE_FORMAT = "dd/MM/yyyy";
    private static final UUID CTLE_RESULT_DEFINITION_ID = UUID.fromString("68737dc2-8d10-45e0-8bc1-21a523100fa2");
    private static final UUID CTLE_PROMPT_ID = UUID.fromString("3c915c3b-57d8-45f4-972e-a5d2b5f91bfa");

    public void calculate(final Hearing hearing) {
        //go through the offences
        ofNullable(hearing.getProsecutionCases()).map(Collection::stream).orElseGet(Stream::empty)
                .flatMap(pc -> pc.getDefendants().stream())
                .filter(CustodyTimeLimitCalculatorV3::valid)
                .forEach(this::calculate);

    }

    public void updateExtendedCustodyTimeLimit(final ResultsSharedV3 resultsSharedV2) {

        final List<ExtendedCustodyTimeLimit> extendedCustodyTimeLimits = resultsSharedV2.getTargets().stream()
                .filter(target -> target.getResultLines().stream()
                        .anyMatch(resultLine -> CTLE_RESULT_DEFINITION_ID.equals(resultLine.getResultDefinitionId())))
                .map(target -> new ExtendedCustodyTimeLimit(target.getHearingId(), target.getOffenceId(), getExtendedCustodyTimeLimit(target)))
                .collect(toList());

        extendedCustodyTimeLimits.forEach(extendedCustodyTimeLimit ->
                ofNullable(resultsSharedV2.getHearing().getProsecutionCases())
                        .map(Collection::stream).orElseGet(Stream::empty)
                        .flatMap(pc -> pc.getDefendants().stream())
                        .flatMap(defendant -> defendant.getOffences().stream())
                        .forEach(offence -> {
                            if (offence.getId().equals(extendedCustodyTimeLimit.getOffenceId())) {
                                if (nonNull(offence.getCustodyTimeLimit())) {
                                    offence.getCustodyTimeLimit().setTimeLimit(extendedCustodyTimeLimit.getExtendedTimeLimit());
                                    offence.getCustodyTimeLimit().setIsCtlExtended(true);
                                } else {
                                    final CustodyTimeLimit custodyTimeLimit = CustodyTimeLimit.custodyTimeLimit()
                                            .withTimeLimit(extendedCustodyTimeLimit.getExtendedTimeLimit())
                                            .withIsCtlExtended(true)
                                            .build();
                                    offence.setCustodyTimeLimit(custodyTimeLimit);
                                }
                            }
                        })

        );

    }

    /**
     * This method is to calculate previousDaysHeldInCustody and dateHeldInCustodySince fields for
     * per offence.
     * <p>
     * If the defendant is on Bail, dateHeldInCustodySince will be null and will add diff days
     * between orderDate and dateHeldInCustodySince to previousDaysHeldInCustody.
     * <p>
     * If the defendant was on Bail but now in Custody, dateHeldInCustodySince will be orderDate but
     * if the defendant is already in Custody, dateHeldInCustodySince and previousDaysHeldInCustody
     * will not be changed
     *
     * @param hearing
     */
    public void calculateDateHeldInCustody(final Hearing hearing) {
        calculateDateHeldInCustody(hearing, hearing.getHearingDays().get(0).getSittingDay().toLocalDate());
    }

    public void calculateDateHeldInCustody(final Hearing hearing, final LocalDate hearingDay) {
        Optional.ofNullable(hearing.getProsecutionCases()).map(Collection::stream).orElseGet(Stream::empty)
                .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream())
                .forEach(defendant -> {
                    if (nonNull(defendant.getPersonDefendant())
                            && nonNull(defendant.getPersonDefendant().getBailStatus())
                            && nonNull(defendant.getPersonDefendant().getBailStatus().getCode())) {

                        final String bailStatusCode = defendant.getPersonDefendant().getBailStatus().getCode();

                        for (final Offence offence : defendant.getOffences()) {
                            if (Arrays.stream(onBailStatusCodes).anyMatch(bailStatusCode::equals)) {
                                calculateDateHeldInCustodyForBailCodes(offence, hearingDay);
                            } else if (Arrays.stream(inCustodyCodes).anyMatch(bailStatusCode::equals)) {
                                calculateDateHeldInCustodyForCustodyCodes(offence, hearingDay);
                            }
                        }
                    }

                });

    }

    private void calculateDateHeldInCustodyForCustodyCodes(final Offence offence, final LocalDate hearingDay) {
        if (isNull(offence.getDateHeldInCustodySince())) {
            offence.setDateHeldInCustodySince(hearingDay);
        }
    }

    private void calculateDateHeldInCustodyForBailCodes(final Offence offence, final LocalDate hearingDay) {
        if (nonNull(offence.getDateHeldInCustodySince())) {
            final int previousDaysHeldInCustody = nonNull(offence.getPreviousDaysHeldInCustody()) ? offence.getPreviousDaysHeldInCustody() : 0;
            final int timeSpent = (int) DAYS.between(offence.getDateHeldInCustodySince(), hearingDay) + previousDaysHeldInCustody;
            offence.setPreviousDaysHeldInCustody(timeSpent);
        }
        offence.setDateHeldInCustodySince(null);
    }

    @SuppressWarnings("squid:S3655")
    private LocalDate getExtendedCustodyTimeLimit(final Target2 target) {

        final String extendedTimeLimit = target.getResultLines().stream()
                .flatMap(resultLine -> resultLine.getPrompts().stream())
                .filter(prompt -> CTLE_PROMPT_ID.equals(prompt.getId()))
                .findFirst().get().getValue();

        return LocalDate.parse(extendedTimeLimit);

    }

    private void calculate(final Defendant defendant) {

        if (Objects.isNull(defendant.getOffences())) {
            return;
        }
        final Map<Offence, CustodyTimeLimit> offencesToCustodyTimeLimit =
                defendant.getOffences().stream()
                        .map(o -> new AbstractMap.SimpleEntry<>(o, getCustodyTimeLimit(o)))
                        .filter(p -> nonNull(p.getValue()))
                        .collect(Collectors.toMap(
                                AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue
                        ));
        if (offencesToCustodyTimeLimit.isEmpty()) {
            return;
        }
        for (final Offence offence : defendant.getOffences()) {
            if (offencesToCustodyTimeLimit.containsKey(offence)) {
                offence.setCustodyTimeLimit(offencesToCustodyTimeLimit.get(offence));
            }
        }
    }

    private CustodyTimeLimit getCustodyTimeLimit(final Offence offence) {
        Optional<CustodyTimeLimit> custodyTimeLimit = Optional.empty();
        if (nonNull(offence.getJudicialResults()) && Objects.isNull(offence.getConvictionDate())) {
            custodyTimeLimit = offence.getJudicialResults().stream()
                    .filter(Objects::nonNull)
                    .filter(jr -> nonNull(jr.getJudicialResultPrompts()))
                    .map(this::extractCustodyTimeLimit)
                    .filter(Objects::nonNull)
                    .findFirst();
        }

        return custodyTimeLimit.orElseGet(offence::getCustodyTimeLimit);
    }

    private CustodyTimeLimit extractCustodyTimeLimit(final JudicialResult judicialResult) {
        final Optional<JudicialResultPrompt> timeLimitPrompt = judicialResult.getJudicialResultPrompts().stream()
                .filter(p -> CTL_TIME_LIMIT_PROMPT_REF.equalsIgnoreCase(p.getPromptReference()))
                .findFirst();

        if (!timeLimitPrompt.isPresent()) {
            return null;
        }

        final LocalDate timeLimit = LocalDate.parse(timeLimitPrompt.get().getValue(), DateTimeFormatter.ofPattern(DATE_FORMAT));
        return new CustodyTimeLimit(null, false, timeLimit);

    }

    private static boolean valid(final Defendant defendant) {
        Optional<BailStatus> bailStatus = Optional.empty();
        if (nonNull(defendant.getPersonDefendant())) {
            bailStatus = Optional.ofNullable(defendant.getPersonDefendant().getBailStatus());
        }
        return isCustodyOrRemandInLocalAuthority(bailStatus) || isSecureOrCustodyPendingConditions(bailStatus);
    }

    private static boolean isSecureOrCustodyPendingConditions(final Optional<BailStatus> bailStatus) {
        return bailStatus.isPresent() && (SECURE_ACCOMMODATION.equals(bailStatus.get().getCode())
                || REMANDED_IN_CUSTODY_PENDING_CONDITIONS.equals(bailStatus.get().getCode()));
    }

    private static boolean isCustodyOrRemandInLocalAuthority(final Optional<BailStatus> bailStatus) {
        return bailStatus.isPresent() && (CUSTODY_OR_REMANDED_INTO_CUSTODY.equals(bailStatus.get().getCode())
                || REMANDED_INTO_CARE_OF_LOCAL_AUTHORITY.equals(bailStatus.get().getCode()));
    }

}
