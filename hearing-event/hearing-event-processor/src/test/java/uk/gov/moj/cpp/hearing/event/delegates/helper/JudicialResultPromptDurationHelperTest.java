package uk.gov.moj.cpp.hearing.event.delegates.helper;

import static com.google.common.collect.ImmutableList.of;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.core.courts.JudicialResultPrompt.judicialResultPrompt;

import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.core.courts.JudicialResultPrompt;
import uk.gov.justice.core.courts.JudicialResultPromptDurationElement;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class JudicialResultPromptDurationHelperTest {

    @Parameterized.Parameter(0)
    public String value;
    @Parameterized.Parameter(1)
    public Integer durationSequence;
    @Parameterized.Parameter(2)
    public Boolean lifeDuration;
    @Parameterized.Parameter(3)
    public String expectedPrimaryUnit;
    @Parameterized.Parameter(4)
    public Integer expectedPrimaryValue;
    @Parameterized.Parameter(5)
    public String expectedSecondaryUnit;
    @Parameterized.Parameter(6)
    public Integer expectedSecondaryValue;
    @Parameterized.Parameter(7)
    public Boolean isDurationStartDate;
    @Parameterized.Parameter(8)
    public Boolean isDurationEndDate;
    @Parameterized.Parameter(9)
    public String durationStartDate;
    @Parameterized.Parameter(10)
    public String durationEndDate;

    private ResultDefinition resultDefinition;
    private List<JudicialResultPrompt> judicialResultPromptList;

    @Parameterized.Parameters(name = "Duration Value: {0} Duration Sequence: {1} lifeDuration: {2} expectedPrimaryUnit: {3} expectedPrimaryValue: {4} expectedSecondaryUnit: {5} expectedSecondaryValue: {6} isDurationStartDate: {7} isDurationEndDate: {8} durationStartDate: {9} durationEndDate: {10}")
    public static Collection<Object[]> testData() {
        return asList(new Object[][]{
                {"5 Years", 1, false, "Y", 5, null, null, false, false, null, null},
                {"32 months", 1, false, "M", 32, null, null, false, false, null, null},
                {"5 Years", 4, false, null, null, null, null, false, false, null, null},
                {"5 Years", null, false, null, null, null, null, false, false, null, null},
                {"5    Years", 2, false, null, null, "Y", 5, false, false, null, null},
                {"32         MonThs", 2, false, null, null, "M", 32, false, false, null, null},
                {"MonThs", 2, false, null, null, null, null, false, false, null, null},
                {"MonThs", 1, true, "L", 1, null, null, false, false, null, null},
                {"MonThs", 1, false, null, null, null, null, false, false, null, null},
                {"MonThs", 1000, false, null, null, null, null, false, false, null, null},
                {"10/12/2021", 1000, false, null, null, null, null, true, false, "10/12/2021", null},
                {"11/12/2021", 1000, false, null, null, null, null, false, true, null, "11/12/2021"},
                {"12/12/2021", 1000, false, null, null, null, null, true, true, "12/12/2021", "12/12/2021"},
                {"5 Years", 1, false, "Y", 5, null, null, true, false, "5 Years", null},
                {"5 Years", 2, false, null, null, "Y", 5, false, true, null, "5 Years"}
        });
    }

    @Before
    public void init() {
        resultDefinition = ResultDefinition.resultDefinition().setId(UUID.randomUUID()).setLifeDuration(lifeDuration);
        judicialResultPromptList = of(judicialResultPrompt().withValue(value).withDurationSequence(durationSequence).build());
    }


    @Test
    public void shouldTestLifeAndPrimaryAndSecondaryUnitWithValue() {
        final Optional<JudicialResultPromptDurationElement> resultPromptDurationElement = new JudicialResultPromptDurationHelper().populate(judicialResultPromptList, resultDefinition);
        assertThat(resultPromptDurationElement.map(JudicialResultPromptDurationElement::getPrimaryDurationUnit).orElse(null), is(expectedPrimaryUnit));
        assertThat(resultPromptDurationElement.map(JudicialResultPromptDurationElement::getPrimaryDurationValue).orElse(null), is(expectedPrimaryValue));
        assertThat(resultPromptDurationElement.map(JudicialResultPromptDurationElement::getSecondaryDurationUnit).orElse(null), is(expectedSecondaryUnit));
        assertThat(resultPromptDurationElement.map(JudicialResultPromptDurationElement::getSecondaryDurationValue).orElse(null), is(expectedSecondaryValue));
    }

    @Test
    public void shouldTestDurationStartAndEndDate() {
        judicialResultPromptList = of(judicialResultPrompt().withValue(value).withDurationSequence(durationSequence)
                .withIsDurationStartDate(isDurationStartDate).withIsDurationEndDate(isDurationEndDate).build());

        final Optional<JudicialResultPromptDurationElement> resultPromptDurationElement = new JudicialResultPromptDurationHelper().populate(judicialResultPromptList, resultDefinition);
        assertThat(resultPromptDurationElement.map(JudicialResultPromptDurationElement::getDurationStartDate).orElse(null), is(durationStartDate));
        assertThat(resultPromptDurationElement.map(JudicialResultPromptDurationElement::getDurationEndDate).orElse(null), is(durationEndDate));
    }
}