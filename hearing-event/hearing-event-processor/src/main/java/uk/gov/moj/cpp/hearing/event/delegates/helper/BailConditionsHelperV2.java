package uk.gov.moj.cpp.hearing.event.delegates.helper;

import static java.util.Arrays.stream;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static uk.gov.moj.cpp.hearing.event.helper.HearingHelper.getOffencesFromApplication;

import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.JudicialResult;
import uk.gov.justice.core.courts.MasterDefendant;
import uk.gov.justice.core.courts.Offence;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.commons.lang3.StringUtils.split;

public class BailConditionsHelperV2 {

    public static final String RESULT_TEXT_SPLIT_REGEX = "\n";
    public static final String DELIMITER = ",";
    private static final Logger LOGGER = LoggerFactory.getLogger(BailConditionsHelperV2.class);
    private static final String BAIL_CONDITIONS = "Bail conditions";
    private static final String PRE_BAIL_CONDITIONS = "Pre-release bail conditions";
    private static final String CONDITIONS_IMPOSED_ON_DEFENDANT = "ConditionsImposedOnDefendant";
    private static final String[] applicableBailStatusCodes = new String[]{"L", "P", "B"};
    private static final String LINE_SEPERATOR = ";";
    private static final String SPACE = " ";

    public void setBailConditions(final Hearing hearing) {
        ofNullable(hearing.getProsecutionCases()).map(Collection::stream).orElseGet(Stream::empty)
                .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream())
                .forEach(this::setBailConditions);

        ofNullable(hearing.getCourtApplications()).map(Collection::stream).orElseGet(Stream::empty)
                .filter(ca -> nonNull(ca.getSubject().getMasterDefendant()))
                .filter(ca -> nonNull(ca.getSubject().getMasterDefendant().getPersonDefendant()))
                .forEach(ca -> {
                    final List<Offence> offences = getOffencesFromApplication(ca);
                    setBailConditions(ca.getSubject().getMasterDefendant(), offences);
                });
    }

    public void setBailConditions(final ResultsShared resultsShared) {
        ofNullable(resultsShared.getHearing().getProsecutionCases()).map(Collection::stream).orElseGet(Stream::empty)
                .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream())
                .forEach(this::setBailConditions);

        ofNullable(resultsShared.getHearing().getCourtApplications()).map(Collection::stream).orElseGet(Stream::empty)
                .filter(ca -> nonNull(ca.getSubject().getMasterDefendant()))
                .filter(ca -> nonNull(ca.getSubject().getMasterDefendant().getPersonDefendant()))
                .forEach(ca -> {
                    final List<Offence> offences = getOffencesFromApplication(ca);
                    setBailConditions(ca.getSubject().getMasterDefendant(), offences);
                });
    }

    private void setBailConditions(final Defendant defendant) {
        if (nonNull((defendant.getPersonDefendant()))) {
            if (nonNull(defendant.getPersonDefendant().getBailStatus()) && Arrays.stream(applicableBailStatusCodes).anyMatch(defendant.getPersonDefendant().getBailStatus().getCode()::equals)) {
                final String bailConditions = getBailConditions(defendant.getOffences());
                defendant.getPersonDefendant().setBailConditions(bailConditions);
            } else {
                //Set Bail Conditions if Bail Status = ‘B’, ‘P’ or ‘L’. Otherwise field must be blank.
                defendant.getPersonDefendant().setBailConditions("");
            }
        }
    }

    private void setBailConditions(final MasterDefendant defendant, final List<Offence> offences) {
        if (nonNull((defendant.getPersonDefendant()))) {
            if (nonNull(defendant.getPersonDefendant().getBailStatus()) && Arrays.stream(applicableBailStatusCodes).anyMatch(defendant.getPersonDefendant().getBailStatus().getCode()::equals)) {
                final String bailConditions = getBailConditions(offences);
                if (nonNull(bailConditions)) {
                    defendant.getPersonDefendant().setBailConditions(bailConditions);
                }
            } else {
                //Set Bail Conditions if Bail Status = ‘B’, ‘P’ or ‘L’. Otherwise field must be blank.
                defendant.getPersonDefendant().setBailConditions("");
            }
        }
    }

    private String getBailConditions(final List<Offence> offences) {
        final List<String> resultTexts = getJudicialResultsGroupDefintionByOrder(offences);
        final StringBuilder bailConditionsBuilder = new StringBuilder();
        LOGGER.info("resultTexts size :: {}", resultTexts.size());

        for (final String resultText : resultTexts) {
            if (resultText == null) {
                return null;
            }
            bailConditionsBuilder.append(resultText.replace(RESULT_TEXT_SPLIT_REGEX, SPACE) + LINE_SEPERATOR);
        }
        return bailConditionsBuilder.toString();
    }

    private List<String> getJudicialResultsGroupDefintionByOrder(final List<Offence> offences) {

        final List<String> resultTextsByDefintions = new ArrayList<>();
        getEachResultDefitionGroup(offences, PRE_BAIL_CONDITIONS, resultTextsByDefintions);
        getEachResultDefitionGroup(offences, BAIL_CONDITIONS, resultTextsByDefintions);
        getEachResultDefitionGroup(offences, CONDITIONS_IMPOSED_ON_DEFENDANT, resultTextsByDefintions);

        return resultTextsByDefintions;
    }

    private void getEachResultDefitionGroup(final List<Offence> offences, final String resultGroupDefintion, final List<String> resultTextsByDefintions) {

        resultTextsByDefintions.addAll(offences
                .stream()
                .filter(o -> nonNull(o.getJudicialResults()))
                .flatMap(o -> o.getJudicialResults().stream())
                .filter(jr -> nonNull(jr.getResultDefinitionGroup()) && nonNull(jr.getResultText()))
                .filter(jr -> stream(split(jr.getResultDefinitionGroup(), DELIMITER)).map(StringUtils::strip).anyMatch(resultGroupDefintion::equalsIgnoreCase))
                .map(JudicialResult::getResultText)
                .distinct()
                .collect(toList()));
    }

}

