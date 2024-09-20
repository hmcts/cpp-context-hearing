package uk.gov.moj.cpp.hearing.event.delegates.helper;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.core.courts.JudicialResultPromptDurationElement;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class DurationDateHelperTest {

    private final Hearing hearing = new Hearing.Builder()
            .withHearingDays(asList(
                    new HearingDay.Builder()
                            .withSittingDay(ZonedDateTime.parse("2019-09-13T15:22:53.778+01:00[Europe/London]"))
                            .build()
            ))
            .build();

    private JudicialResultPromptDurationElement.Builder builder;

    public static Stream<Arguments> testData() {
        return Stream.of(
                Arguments.of("H", 1200, "2019-09-13", "2019-11-01"),
                Arguments.of("W", 1000, "2019-09-13", "2038-11-11"),
                Arguments.of("M", 1000, "2019-09-13", "2103-01-12"),
                Arguments.of("Y", 1000, "2019-09-13", "3019-09-12"),
                Arguments.of("T", 1000, "2019-09-13", "2019-09-13"),
                Arguments.of("D", 1000, "2019-09-13", "2022-06-08")
        );
    }

    @BeforeEach
    public void init() {

        this.builder = new JudicialResultPromptDurationElement.Builder();
    }

    @ParameterizedTest
    @MethodSource("testData")
    public void test(final String unit, final Integer value, final Object expectedStartDate,final  Object expectedEndDate) {
        Pair<String, Integer> values = Pair.of(unit, value);
        DurationDateHelper.populateStartAndEndDates(builder, hearing, values);

        final JudicialResultPromptDurationElement actualJudicialResultPromptDurationElement = builder.build();

        assertThat(Objects.toString(actualJudicialResultPromptDurationElement.getDurationStartDate()), is(Objects.toString(expectedStartDate)));
        assertThat(Objects.toString(actualJudicialResultPromptDurationElement.getDurationEndDate()), is(Objects.toString(expectedEndDate)));
    }
}
