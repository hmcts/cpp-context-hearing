package uk.gov.moj.cpp.hearing.event.delegates.helper;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.core.courts.Prompt.prompt;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.justice.core.courts.Prompt;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

@ExtendWith(MockitoExtension.class)
public class UUIDComparatorTest {


    private Prompt firstPrompt, secondPrompt, promptMissingFromReferenceData;
    private List<UUID> referenceList;

    @Mock
    private Logger mockLogger;

//    @InjectMocks
    private UUIDComparator uuidComparator;

    @BeforeEach
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        final UUID uuid1 = randomUUID();
        final UUID uuid2 = randomUUID();
        final UUID uuid3 = randomUUID();

        referenceList = new ArrayList<>();
        referenceList.add(uuid1);
        referenceList.add(uuid2);

        firstPrompt = prompt().withId(uuid1).build();
        secondPrompt = prompt().withId(uuid2).build();

        promptMissingFromReferenceData = prompt().withId(uuid3).build();

        uuidComparator = new UUIDComparator(referenceList);
        setField(this.uuidComparator, "LOGGER", mockLogger);

    }


    @Test
    public void shouldSortPrompts() {
        final int result = uuidComparator.compare(firstPrompt, secondPrompt);
        assertThat(result, is(-1));
    }

    @Test
    public void shouldSortPrompts2() {
        final int result = uuidComparator.compare(secondPrompt, firstPrompt);
        assertThat(result, is(1));
    }

    @Test
    public void shouldSortPrompts3() {
        final int result = uuidComparator.compare(firstPrompt, firstPrompt);
        assertThat(result, is(0));
    }

    @Test
    public void shouldLogErrorWhenPromptMissingFromReferenceData() {

        final int result = uuidComparator.compare(firstPrompt, promptMissingFromReferenceData);

        verify(mockLogger, times(1)).error(UUIDComparator.PROMPT_MISSING_FROM_REFERENCE_DATA);

        assertThat(result, is(0));
    }
}