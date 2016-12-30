package uk.gov.moj.cpp.hearing.query.view.convertor;

import static java.lang.String.valueOf;
import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isOneOf;
import static org.junit.Assert.assertThat;

import uk.gov.justice.services.test.utils.core.random.RandomGenerator;
import uk.gov.moj.cpp.hearing.persist.entity.DefenceCounsel;
import uk.gov.moj.cpp.hearing.persist.entity.DefenceCounselDefendant;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.json.JsonArray;
import javax.json.JsonObject;

import org.junit.Test;

public class DefenceCounselToDefendantMapConverterTest {

    private final DefenceCounselToDefendantMapConverter defenceCounselToDefendantMapConverter =
            new DefenceCounselToDefendantMapConverter();

    @Test
    public void shouldConvertDefenceCounselDefendantMapToJsonObject() {
        final Map<DefenceCounsel, List<DefenceCounselDefendant>> defenceCounselListToDefendantMap =
                new HashMap<>();
        final UUID defenceCounselAttendeeId = randomUUID();
        final UUID hearingId = randomUUID();
        final UUID personId = randomUUID();
        final String status = RandomGenerator.STRING.next();

        final DefenceCounsel defenceCounsel = new DefenceCounsel(defenceCounselAttendeeId,
                hearingId, personId, status);

        final UUID defendantId1 = randomUUID();
        final UUID defendantId2 = randomUUID();

        final DefenceCounselDefendant defenceCounselDefendant1 =
                new DefenceCounselDefendant(defenceCounselAttendeeId, defendantId1);
        final DefenceCounselDefendant defenceCounselDefendant2 =
                new DefenceCounselDefendant(defenceCounselAttendeeId, defendantId2);

        defenceCounselListToDefendantMap.put(defenceCounsel, asList(defenceCounselDefendant1, defenceCounselDefendant2));

        final JsonObject jsonObject = defenceCounselToDefendantMapConverter.convert(
                defenceCounselListToDefendantMap);

        final JsonArray defenceCounsels = jsonObject.getJsonArray("defence-counsels");
        assertThat(defenceCounsels, hasSize(1));

        final JsonObject defenceCounselsJsonObject = defenceCounsels.getJsonObject(0);
        assertThat(defenceCounselsJsonObject.getJsonString("id").getString(), is(valueOf(defenceCounselAttendeeId)));
        assertThat(defenceCounselsJsonObject.getJsonString("personId").getString(), is(valueOf(personId)));
        assertThat(defenceCounselsJsonObject.getJsonString("status").getString(), is(status));

        final JsonArray defendantIds = defenceCounselsJsonObject.getJsonArray("defendantIds");
        assertThat(defendantIds, hasSize(2));
        assertThat(defendantIds.getJsonObject(0).getJsonString("defendantId").getString(),
                isOneOf(valueOf(defendantId1), valueOf(defendantId2)));
        assertThat(defendantIds.getJsonObject(1).getJsonString("defendantId").getString(),
                isOneOf(valueOf(defendantId1), valueOf(defendantId2)));
    }

}