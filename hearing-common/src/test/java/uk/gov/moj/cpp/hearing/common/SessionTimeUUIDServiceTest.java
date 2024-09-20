package uk.gov.moj.cpp.hearing.common;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import uk.gov.moj.cpp.hearing.common.exception.ReferenceDataNotFoundException;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SessionTimeUUIDServiceTest {

    @InjectMocks
    private SessionTimeUUIDService uuidService;

    private UUID courtHouseId;
    private UUID courtRoomId;
    private LocalDate courtSessionDate;

    @BeforeEach
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

    @Test
    public void shouldThrowNullPointerExceptionIfAnyInputIsNull() {
        assertThrows(NullPointerException.class, () -> uuidService.getCourtSessionId(null, courtRoomId, courtSessionDate));
    }
}
