package uk.gov.moj.cpp.hearing.query.view.converter;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.common.ReusableInformation.IdType.DEFENDANT;

import uk.gov.moj.cpp.hearing.common.ReusableInformation;
import uk.gov.moj.cpp.hearing.query.view.convertor.ReusableInformationTxtConverter;

import java.util.UUID;

import javax.json.JsonObject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ReusableInformationTxtConverterTest {

    @InjectMocks
    private ReusableInformationTxtConverter cacheableInformationTxtConverter;

    @Test
    public void shouldConvertToTxt() {
        final String value = "ED9 12X";
        final UUID defendantId = UUID.randomUUID();
        final String promptRef = STRING.next();
        final Integer cacheable = 2;
        final String cacheDataPath = STRING.next();

        final ReusableInformation reusableInformation = new ReusableInformation.Builder<String>()
                .withValue(value)
                .withIdType(DEFENDANT)
                .withId(defendantId)
                .withPromptRef(promptRef)
                .withCacheable(cacheable)
                .withCacheDataPath(cacheDataPath)
                .build();

        final JsonObject jsonObject = cacheableInformationTxtConverter.toJsonObject(reusableInformation);

        assertNotNull(jsonObject);
        assertThat(jsonObject.getString("promptRef"), is(reusableInformation.getPromptRef()));
        assertThat(jsonObject.getString("masterDefendantId"), is(reusableInformation.getMasterDefendantId().toString()));
        assertThat(jsonObject.getString("value"), is(reusableInformation.getValue()));
        assertThat(jsonObject.getString("type"), is("TXT"));

    }

    @Test
    public void shouldReturnNullObjectWhenInputIsNull() {
        final JsonObject jsonObject = cacheableInformationTxtConverter.toJsonObject(null);
        assertNull(jsonObject);

    }
}