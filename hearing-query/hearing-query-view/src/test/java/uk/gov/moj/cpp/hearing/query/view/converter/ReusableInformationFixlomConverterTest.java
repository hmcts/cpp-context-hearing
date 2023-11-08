package uk.gov.moj.cpp.hearing.query.view.converter;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.common.ReusableInformation.IdType.DEFENDANT;

import uk.gov.moj.cpp.hearing.common.ReusableInformation;
import uk.gov.moj.cpp.hearing.query.view.convertor.ReusableInformationFixlomConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.json.JsonObject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ReusableInformationFixlomConverterTest {

    @InjectMocks
    private ReusableInformationFixlomConverter cacheableInformationFixlomConverter;

    @Test
    public void shouldConvertToFixlom() {
        final List<String> listValues = new ArrayList<>();
        listValues.add("an unprovoked attack of a serious nature");
        listValues.add("the defendant has flagrant disregard for people and their property");
        final UUID defendantId = UUID.randomUUID();
        final String promptRef = STRING.next();
        final Integer cacheable = 2;
        final String cacheDataPath = STRING.next();

        final ReusableInformation reusableInformation = new ReusableInformation.Builder<List<String>>()
                .withValue(listValues)
                .withIdType(DEFENDANT)
                .withId(defendantId)
                .withPromptRef(promptRef)
                .withCacheable(cacheable)
                .withCacheDataPath(cacheDataPath)
                .build();

        final JsonObject jsonObject = cacheableInformationFixlomConverter.toJsonObject(reusableInformation);
        final String fixlmValue = listValues.stream().collect(Collectors.joining("###"));

        assertNotNull(jsonObject);
        assertThat(jsonObject.getString("promptRef"), is(reusableInformation.getPromptRef()));
        assertThat(jsonObject.getString("masterDefendantId"), is(reusableInformation.getMasterDefendantId().toString()));
        assertThat(jsonObject.getString("value"), is(fixlmValue));
        assertThat(jsonObject.getString("type"), is("FIXLOM"));

    }

    @Test
    public void shouldReturnNullObjectWhenInputIsNull() {
        final JsonObject jsonObject = cacheableInformationFixlomConverter.toJsonObject(null);
        assertNull(jsonObject);

    }
}