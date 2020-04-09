package uk.gov.moj.cpp.hearing.event.delegates.helper;

import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.justice.core.courts.Prompt.prompt;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.UUIDComparator.PROMPT_MISSING_FROM_REFERENCE_DATA;
import static uk.org.lidalia.slf4jtest.LoggingEvent.error;

import uk.gov.justice.core.courts.Prompt;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import uk.org.lidalia.slf4jtest.TestLogger;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

public class UUIDComparatorTest {

    private UUIDComparator uuidComparator;
    private Prompt firstPrompt, secondPrompt, promptMissingFromReferenceData;
    private List<UUID> referenceList;

    @Before
    public void setUp() {
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
        final TestLogger logger = TestLoggerFactory.getTestLogger(UUIDComparator.class);

        final int result = uuidComparator.compare(firstPrompt, promptMissingFromReferenceData);

        assertThat(logger.getLoggingEvents(), is(singletonList(error(PROMPT_MISSING_FROM_REFERENCE_DATA))));
        TestLoggerFactory.clear();

        assertThat(result, is(0));
    }
}