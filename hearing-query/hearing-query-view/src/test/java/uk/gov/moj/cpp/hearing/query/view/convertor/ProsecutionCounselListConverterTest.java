package uk.gov.moj.cpp.hearing.query.view.convertor;

import static java.lang.String.valueOf;
import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isOneOf;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;

import uk.gov.moj.cpp.hearing.persist.entity.ProsecutionCounsel;

import java.util.List;
import java.util.UUID;

import javax.json.JsonArray;
import javax.json.JsonObject;

import org.junit.Test;

public class ProsecutionCounselListConverterTest {

    private final ProsecutionCounselListConverter prosecutionCounselListConverter =
            new ProsecutionCounselListConverter();

    @Test
    public void shouldConvertListOfProsecutionCounselsToJsonObjects() {
        final UUID id1 = randomUUID();
        final UUID hearingId1 = randomUUID();
        final UUID personId1 = randomUUID();
        final String status1 = STRING.next();
        final ProsecutionCounsel prosecutionCounsel1 = new ProsecutionCounsel(id1, hearingId1, personId1, status1);

        final UUID id2 = randomUUID();
        final UUID hearingId2 = randomUUID();
        final UUID personId2 = randomUUID();
        final String status2 = STRING.next();
        final ProsecutionCounsel prosecutionCounsel2 = new ProsecutionCounsel(id2, hearingId2, personId2, status2);

        final List<ProsecutionCounsel> prosecutionCounsels = asList(prosecutionCounsel1, prosecutionCounsel2);

        final JsonObject jsonObject = new ProsecutionCounselListConverter().convert(prosecutionCounsels);

        final JsonArray prosecutionCounselJsonArray = jsonObject.getJsonArray("prosecution-counsels");
        assertThat(prosecutionCounselJsonArray, hasSize(2));

        final JsonObject prosecutionCounselsJsonObject1 = prosecutionCounselJsonArray.getJsonObject(0);
        assertThat(prosecutionCounselsJsonObject1.getJsonString("id").getString(), isOneOf(valueOf(id1), valueOf(id2)));
        assertThat(prosecutionCounselsJsonObject1.getJsonString("personId").getString(), isOneOf(valueOf(personId1), valueOf(personId2)));
        assertThat(prosecutionCounselsJsonObject1.getJsonString("status").getString(), isOneOf(valueOf(status1), valueOf(status2)));

        final JsonObject prosecutionCounselsJsonObject2 = prosecutionCounselJsonArray.getJsonObject(1);
        assertThat(prosecutionCounselsJsonObject2.getJsonString("id").getString(), isOneOf(valueOf(id1), valueOf(id2)));
        assertThat(prosecutionCounselsJsonObject2.getJsonString("personId").getString(), isOneOf(valueOf(personId1), valueOf(personId2)));
        assertThat(prosecutionCounselsJsonObject2.getJsonString("status").getString(), isOneOf(valueOf(status1), valueOf(status2)));

    }
}