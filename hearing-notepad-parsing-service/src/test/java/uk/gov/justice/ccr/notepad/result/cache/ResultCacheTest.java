package uk.gov.justice.ccr.notepad.result.cache;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newTreeSet;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.BDDMockito.given;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUIDAndName;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.BOOLEAN;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.INTEGER;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.randomEnum;

import uk.gov.justice.ccr.notepad.result.cache.model.ResultPrompt;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultType;
import uk.gov.justice.ccr.notepad.result.loader.ResultLoader;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.google.common.cache.CacheLoader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ResultCacheTest {

    private static final UUID ID = randomUUID();

    private static final UUID PROMPT_ID = randomUUID();
    private static final UUID PROMPT_ID_2 = randomUUID();
    private static final String RESULT_DEFINITION_LABEL = STRING.next();
    private static final String LABEL = STRING.next();
    private static final ResultType TYPE = randomEnum(ResultType.class).next();
    private static final boolean MANDATORY = BOOLEAN.next();
    private static final String DURATION = STRING.next();
    private static final Set<String> KEYWORDS = newTreeSet(newArrayList(STRING.next(), STRING.next()));
    private static final Set<String> FIXED_LIST = newTreeSet(newArrayList(STRING.next(), STRING.next()));
    private static final int SEQUENCE = INTEGER.next();
    private static final String REFERENCE = STRING.next();

    @Mock
    private ResultLoader resultLoader;

    @InjectMocks
    private ResultCache underTest = new ResultCache();

    @Test(expected = CacheLoader.InvalidCacheLoadException.class)
    public void getResultLoaderWithKeyNotFound() throws Exception {
        underTest.cache.get("UNKNOWN");
    }

    @Test
    public void shouldGetResultPromptByResultDefinitionIdIfPresent() throws Exception {
        given(resultLoader.loadResultPrompt()).willReturn(prepareResultPrompts());

        underTest.lazyLoad(envelopeFrom(metadataWithRandomUUIDAndName(), createObjectBuilder().build()));

        final List<ResultPrompt> prompts = underTest.getResultPromptByResultDefinitionId(ID.toString());

        assertThat(prompts, is(notNullValue()));
        assertThat(prompts, hasSize(2));
        assertThat(prompts.get(0).getId(), is(PROMPT_ID.toString()));
        assertThat(prompts.get(1).getId(), is(PROMPT_ID_2.toString()));
    }

    private List<ResultPrompt> prepareResultPrompts() {
        return newArrayList(
                new ResultPrompt(PROMPT_ID.toString(), ID, RESULT_DEFINITION_LABEL, LABEL, TYPE,
                        MANDATORY, DURATION, KEYWORDS, FIXED_LIST, SEQUENCE, REFERENCE),
                new ResultPrompt(PROMPT_ID_2.toString(), ID, RESULT_DEFINITION_LABEL, LABEL, TYPE,
                        MANDATORY, DURATION, KEYWORDS, FIXED_LIST, SEQUENCE, REFERENCE)
        );
    }


}