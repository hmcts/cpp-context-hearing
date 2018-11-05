package uk.gov.moj.cpp.hearing.persist.entity;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.beans.SamePropertyValuesAs.samePropertyValuesAs;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsSame.sameInstance;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.BOOLEAN;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.INTEGER;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;

import org.junit.Test;
import uk.gov.moj.cpp.hearing.persist.entity.heda.HearingEventDefinition;

public class HearingEventDefinitionTest {

    @Test
    public void shouldHaveANoArgsConstructor() {
        assertThat(HearingEventDefinition.class, hasValidBeanConstructor());
    }

    @Test
    public void shouldCreateNewObjectWithSameValuesIfBuilderDoesNotOverwriteAnyFields() {
        final HearingEventDefinition hearingEventDefinition = prepareHearingEventDefinition();

        final HearingEventDefinition actualHearingEventDefinition = hearingEventDefinition.builder().build();

        assertThat(actualHearingEventDefinition, is(not(sameInstance(hearingEventDefinition))));

        assertThat(actualHearingEventDefinition, is(samePropertyValuesAs(hearingEventDefinition)));
    }

    @Test
    public void shouldBeAbleToOverwriteFieldsFromBuilder() {
        final HearingEventDefinition hearingEventDefinition = prepareHearingEventDefinition();

        final HearingEventDefinition updatedHearingEventDefinition = hearingEventDefinition.builder().delete().build();

        assertThat(updatedHearingEventDefinition, is(not(sameInstance(hearingEventDefinition))));

        assertThat(updatedHearingEventDefinition.getId(), is(hearingEventDefinition.getId()));
        assertThat(updatedHearingEventDefinition.getActionLabel(), is(hearingEventDefinition.getActionLabel()));
        assertThat(updatedHearingEventDefinition.getActionLabelExtension(), is(hearingEventDefinition.getActionLabelExtension()));
        assertThat(updatedHearingEventDefinition.getRecordedLabel(), is(hearingEventDefinition.getRecordedLabel()));
        assertThat(updatedHearingEventDefinition.getSequenceNumber(), is(hearingEventDefinition.getSequenceNumber()));
        assertThat(updatedHearingEventDefinition.getSequenceType(), is(hearingEventDefinition.getSequenceType()));
        assertThat(updatedHearingEventDefinition.getCaseAttribute(), is(hearingEventDefinition.getCaseAttribute()));
        assertThat(updatedHearingEventDefinition.getGroupLabel(), is(hearingEventDefinition.getGroupLabel()));
        assertThat(updatedHearingEventDefinition.isAlterable(), is(hearingEventDefinition.isAlterable()));
        assertThat(updatedHearingEventDefinition.isDeleted(), is(not(hearingEventDefinition.isDeleted())));
    }

    private HearingEventDefinition prepareHearingEventDefinition() {
        return new HearingEventDefinition(randomUUID(), STRING.next(), STRING.next(), INTEGER.next(), STRING.next(), STRING.next(), STRING.next(), STRING.next(), BOOLEAN.next());
    }

}