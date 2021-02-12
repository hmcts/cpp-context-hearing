package uk.gov.moj.cpp.hearing.query.view.converter;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;

import uk.gov.moj.cpp.hearing.common.ReusableInformation;
import uk.gov.moj.cpp.hearing.common.ReusableInformationConverterType;
import uk.gov.moj.cpp.hearing.query.view.convertor.ReusableInformationFixlConverter;

import java.util.UUID;

import javax.json.JsonObject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ReusableInformationFixlConverterTest {

    @InjectMocks
    private ReusableInformationFixlConverter cacheableInformationFixlConverter;

    @Test
    public void shouldConvertToFixl() {
        final String value = "ED9 12X";
        final UUID defendantId = UUID.randomUUID();
        final String promptRef = STRING.next();
        final Integer cacheable = 2;
        final String cacheDataPath = STRING.next();

        final ReusableInformation reusableInformation = new ReusableInformation.Builder<String>()
                .withValue(value)
                .withMasterDefendantId(defendantId)
                .withPromptRef(promptRef)
                .withCacheable(cacheable)
                .withCacheDataPath(cacheDataPath)
                .build();

        final JsonObject jsonObject = cacheableInformationFixlConverter.toJsonObject(reusableInformation);

        assertNotNull(jsonObject);
        assertThat(jsonObject.getString("promptRef"), is(reusableInformation.getPromptRef()));
        assertThat(jsonObject.getString("masterDefendantId"), is(reusableInformation.getMasterDefendantId().toString()));
        assertThat(jsonObject.getString("value"), is(reusableInformation.getValue()));
        assertThat(jsonObject.getString("type"), is(ReusableInformationConverterType.FIXL.name()));

    }
}
