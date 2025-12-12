package uk.gov.moj.cpp.hearing.event.delegates.helper;

import static java.util.Comparator.comparing;
import static org.slf4j.LoggerFactory.getLogger;

import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.core.courts.JudicialResultPromptDurationElement;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;

public class DurationDateHelper {

    private static final Logger LOGGER = getLogger(DurationDateHelper.class);

    private static final String YEAR = "Y";
    private static final String MONTH = "M";
    private static final String DAY = "D";
    private static final String WEEK = "W";
    private static final String HOUR = "H";

    private DurationDateHelper() {

    }

    public static void populateStartAndEndDates(final JudicialResultPromptDurationElement.Builder builder, final Hearing hearing, final Pair<String, Integer> primaryValue) {
        final LocalDateTime startDate = calculateStartDate(hearing);
        final LocalDateTime endDate = calculateEndDate(startDate, primaryValue);
        builder.withDurationStartDate(startDate.toLocalDate().toString()).withDurationEndDate(endDate.toLocalDate().toString());
    }

    private static LocalDateTime calculateStartDate(final Hearing hearing) {

        return hearing.getHearingDays().stream()
                .map(HearingDay::getSittingDay)
                .map(ZonedDateTime::toLocalDateTime)
                .min(comparing(localDateTime -> localDateTime.toEpochSecond(ZoneOffset.UTC)))
                .orElse(LocalDateTime.now());
    }

    private static LocalDateTime calculateEndDate(final LocalDateTime hearingDateTime, final Pair<String, Integer> primaryValue) {

        final String unit = primaryValue.getKey();
        final Integer value = primaryValue.getValue();

        switch (unit) {
            case YEAR:
                return hearingDateTime.plusYears(value).minusDays(1);
            case MONTH:
                return hearingDateTime.plusMonths(value).minusDays(1);
            case DAY:
                return hearingDateTime.plusDays(value).minusDays(1);
            case WEEK:
                return hearingDateTime.plusWeeks(value).minusDays(1);
            case HOUR:
                return hearingDateTime.plusHours(value).minusDays(1);
            default:
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(String.format("Invalid or Unknown primary unit %s.Can not calculate end Date.End date will be same as start date.", unit));
                }
                return hearingDateTime;
        }
    }
}
