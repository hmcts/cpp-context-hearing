package uk.gov.moj.cpp.hearing.common;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SessionTimeUUIDServiceTest {

    @InjectMocks
    private SessionTimeUUIDService uuidService;

    private UUID courtHouseId;
    private UUID courtRoomId;
    private LocalDate courtSessionDate;

    @Before
    public void setUp() {
        courtHouseId = UUID.randomUUID();
        courtRoomId = UUID.randomUUID();
        courtSessionDate = LocalDate.now();
    }

    @Test
    public void shouldReturnSameUuidForSameInput() {
        final UUID courtListId = uuidService.getCourtSessionId(courtHouseId, courtRoomId, courtSessionDate);
        assertThat(courtListId, is(notNullValue()));

        final UUID courtListId2 = uuidService.getCourtSessionId(courtHouseId, courtRoomId, courtSessionDate);

        assertThat(courtListId2, is(courtListId));
    }

    @Test
    public void shouldReturnDifferentUuidForDifferentInputs() {
        final UUID courtListId = uuidService.getCourtSessionId(courtHouseId, courtRoomId, courtSessionDate);
        assertThat(courtListId, is(notNullValue()));

        final UUID courtListId2 = uuidService.getCourtSessionId(courtHouseId, UUID.randomUUID(), courtSessionDate);
        assertThat(courtListId2, is(not(courtListId)));
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerExceptionIfAnyInputIsNull() {
        uuidService.getCourtSessionId(null, courtRoomId, courtSessionDate);
    }
}
