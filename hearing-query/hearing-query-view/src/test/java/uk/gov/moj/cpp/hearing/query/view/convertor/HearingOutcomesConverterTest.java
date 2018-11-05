package uk.gov.moj.cpp.hearing.query.view.convertor;

import static java.lang.String.valueOf;
import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isOneOf;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;

import uk.gov.moj.cpp.hearing.persist.entity.ui.HearingOutcome;

import java.util.List;
import java.util.UUID;

import javax.json.JsonArray;
import javax.json.JsonObject;

import org.junit.Test;


public class HearingOutcomesConverterTest {
    @Test
    public void convert() throws Exception {
        //Given
        final UUID targetIdOne = randomUUID();
        final UUID hearingIdOne = randomUUID();
        final UUID offenceIdOne = randomUUID();
        final UUID defendantIdOne = randomUUID();
        final String draftResultOne = STRING.next();
        HearingOutcome arbitraryHearingOutcomeOne = new HearingOutcome(offenceIdOne,hearingIdOne,defendantIdOne,targetIdOne,draftResultOne);

        final UUID targetIdTwo = randomUUID();
        final UUID hearingIdTwo = randomUUID();
        final UUID offenceIdTwo = randomUUID();
        final UUID defendantIdTwo = randomUUID();
        final String draftResultTwo = STRING.next();
        HearingOutcome arbitraryHearingOutcomeTwo = new HearingOutcome(offenceIdTwo,hearingIdTwo,defendantIdTwo,targetIdTwo,draftResultTwo);

        List<HearingOutcome> outcomes = asList(arbitraryHearingOutcomeOne,arbitraryHearingOutcomeTwo);

        //when
        HearingOutcomesConverter testObj = new HearingOutcomesConverter();
        final JsonObject jsonObject = testObj.convert(outcomes);

        //then
        final JsonArray targets = jsonObject.getJsonArray("targets");
        assertThat(targets, hasSize(2));

        final JsonObject targetOne = targets.getJsonObject(0);
        assertThat(targetOne.getJsonString("targetId").getString(), isOneOf(valueOf(targetIdOne), valueOf(targetIdTwo)));
        assertThat(targetOne.getJsonString("defendantId").getString(), isOneOf(valueOf(defendantIdOne), valueOf(defendantIdTwo)));
        assertThat(targetOne.getJsonString("offenceId").getString(), isOneOf(valueOf(offenceIdOne), valueOf(offenceIdTwo)));
        assertThat(targetOne.getJsonString("draftResult").getString(), isOneOf(valueOf(draftResultOne), valueOf(draftResultTwo)));

        final JsonObject targetTwo = targets.getJsonObject(1);
        assertThat(targetTwo.getJsonString("targetId").getString(), isOneOf(valueOf(targetIdOne), valueOf(targetIdTwo)));
        assertThat(targetTwo.getJsonString("defendantId").getString(), isOneOf(valueOf(defendantIdOne), valueOf(defendantIdTwo)));
        assertThat(targetTwo.getJsonString("offenceId").getString(), isOneOf(valueOf(offenceIdOne), valueOf(offenceIdTwo)));
        assertThat(targetTwo.getJsonString("draftResult").getString(), isOneOf(valueOf(draftResultOne), valueOf(draftResultTwo)));


    }

}