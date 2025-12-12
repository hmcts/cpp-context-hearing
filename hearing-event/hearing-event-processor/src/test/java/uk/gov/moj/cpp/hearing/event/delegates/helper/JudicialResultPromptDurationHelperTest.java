package uk.gov.moj.cpp.hearing.event.delegates.helper;

import static java.time.LocalDate.of;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.core.courts.JudicialResultPrompt.judicialResultPrompt;

import uk.gov.justice.core.courts.JudicialResultPrompt;
import uk.gov.justice.core.courts.JudicialResultPromptDurationElement;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class JudicialResultPromptDurationHelperTest {


    public static Stream<Arguments> testData() {
        return Stream.of(
                Arguments.of("5 Years 32 months", 1, false, "D", 2800, null, null, false, false, null, null, "INTM", of(2024, 3, 15)),
                Arguments.of("5 Years", 1, false, "Y", 5, null, null, false, false, null, null, "INT", of(2024, 3, 15)),
                Arguments.of("32 months", 1, false, "M", 32, null, null, false, false, null, null, "INT", of(2024, 3, 15)),
                Arguments.of("5 Years", 4, false, null, null, null, null, false, false, null, null, "INT", of(2024, 3, 15)),
                Arguments.of("5 Years", null, false, null, null, null, null, false, false, null, null, "INT", of(2024, 3, 15)),
                Arguments.of("5    Years", 2, false, null, null, "Y", 5, false, false, null, null, "INT", of(2024, 3, 15)),
                Arguments.of("32         MonThs", 2, false, null, null, "M", 32, false, false, null, null, "INT", of(2024, 3, 15)),
                Arguments.of("MonThs", 2, false, null, null, null, null, false, false, null, null, "INT", of(2024, 3, 15)),
                Arguments.of("MonThs", 1, true, "L", 1, null, null, false, false, null, null, "INT", of(2024, 3, 15)),
                Arguments.of("MonThs", 1, false, null, null, null, null, false, false, null, null, "INT", of(2024, 3, 15)),
                Arguments.of("MonThs", 1000, false, null, null, null, null, false, false, null, null, "INT", of(2024, 3, 15)),
                Arguments.of("10/12/2021", 1000, false, null, null, null, null, true, false, "10/12/2021", null, "INT", of(2024, 3, 15)),
                Arguments.of("11/12/2021", 1000, false, null, null, null, null, false, true, null, "11/12/2021", "INT", of(2024, 3, 15)),
                Arguments.of("12/12/2021", 1000, false, null, null, null, null, true, true, "12/12/2021", "12/12/2021", "INT", of(2024, 3, 15)),
                Arguments.of("5 Years", 1, false, "Y", 5, null, null, true, false, "5 Years", null, "INT", of(2024, 3, 15)),
                Arguments.of("5 Years", 2, false, null, null, "Y", 5, false, true, null, "5 Years", "INT", of(2024, 3, 15))
        );
    }

    @BeforeEach
    public void init() {
    }


    @ParameterizedTest
    @MethodSource("testData")
    public void shouldTestLifeAndPrimaryAndSecondaryUnitWithValue(final String value, final Integer durationSequence, final Boolean lifeDuration, final String expectedPrimaryUnit, final Integer expectedPrimaryValue, final String expectedSecondaryUnit, final Integer expectedSecondaryValue, final Boolean isDurationStartDate, final Boolean isDurationEndDate, final String durationStartDate, final String durationEndDate, final String promptType, final LocalDate orderedDate) {
        final ResultDefinition resultDefinition = ResultDefinition.resultDefinition().setId(UUID.randomUUID()).setLifeDuration(lifeDuration);
        final List<JudicialResultPrompt> judicialResultPromptList = ImmutableList.of(judicialResultPrompt().withValue(value).withIsDurationStartDate(isDurationStartDate).withIsDurationEndDate(isDurationEndDate).withDurationSequence(durationSequence).withType(promptType).build());

        final Optional<JudicialResultPromptDurationElement> resultPromptDurationElement = new JudicialResultPromptDurationHelper().populate(judicialResultPromptList, resultDefinition, orderedDate);
        assertThat(resultPromptDurationElement.map(JudicialResultPromptDurationElement::getPrimaryDurationUnit).orElse(null), is(expectedPrimaryUnit));
        assertThat(resultPromptDurationElement.map(JudicialResultPromptDurationElement::getPrimaryDurationValue).orElse(null), is(expectedPrimaryValue));
        assertThat(resultPromptDurationElement.map(JudicialResultPromptDurationElement::getSecondaryDurationUnit).orElse(null), is(expectedSecondaryUnit));
        assertThat(resultPromptDurationElement.map(JudicialResultPromptDurationElement::getSecondaryDurationValue).orElse(null), is(expectedSecondaryValue));
    }

    @ParameterizedTest
    @MethodSource("testData")
    public void shouldTestDurationStartAndEndDate(final String value, final Integer durationSequence, final Boolean lifeDuration, final String expectedPrimaryUnit, final Integer expectedPrimaryValue, final String expectedSecondaryUnit, final Integer expectedSecondaryValue, final Boolean isDurationStartDate, final Boolean isDurationEndDate, final String durationStartDate, final String durationEndDate, final String promptType, final LocalDate orderedDate) {
        final ResultDefinition resultDefinition = ResultDefinition.resultDefinition().setId(UUID.randomUUID()).setLifeDuration(lifeDuration);
        final List<JudicialResultPrompt> judicialResultPromptList = ImmutableList.of(judicialResultPrompt().withValue(value).withIsDurationStartDate(isDurationStartDate).withIsDurationEndDate(isDurationEndDate).withDurationSequence(durationSequence).withType(promptType).build());

        final Optional<JudicialResultPromptDurationElement> resultPromptDurationElement = new JudicialResultPromptDurationHelper().populate(judicialResultPromptList, resultDefinition, orderedDate);
        assertThat(resultPromptDurationElement.map(JudicialResultPromptDurationElement::getDurationStartDate).orElse(null), is(durationStartDate));
        assertThat(resultPromptDurationElement.map(JudicialResultPromptDurationElement::getDurationEndDate).orElse(null), is(durationEndDate));
    }
}