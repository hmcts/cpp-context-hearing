package uk.gov.moj.cpp.hearing.event.service;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonObject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
public class CrownCourtCentresCacheTest {

    @Mock
    ReferenceDataLoader referenceDataLoader;
    @InjectMocks
    CrownCourtCentresCache target;

    @Test
    public void shouldInit() {
        final UUID courtCentreId = randomUUID();
        final List<UUID> expectedCourtCentreIds = Arrays.asList(courtCentreId);
         Set<UUID> resultCourCentreIds = new HashSet<>();
         resultCourCentreIds.add(courtCentreId);
        final JsonObject courCentre = Json.createObjectBuilder().add("id",expectedCourtCentreIds.get(0).toString()).build();
        when( referenceDataLoader.getAllCrownCourtCentres()).thenReturn(Collections.singletonList(courCentre));
        target.init();
        Set<UUID> result = target.getAllCrownCourtCentres();
        assertThat(resultCourCentreIds,is(result));
    }

}
