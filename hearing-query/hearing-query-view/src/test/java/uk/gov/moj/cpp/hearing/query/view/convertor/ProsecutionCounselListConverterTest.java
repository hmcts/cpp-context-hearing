package uk.gov.moj.cpp.hearing.query.view.convertor;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.junit.Assert.*;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;

import uk.gov.justice.services.test.utils.core.random.RandomGenerator;
import uk.gov.moj.cpp.hearing.persist.entity.ProsecutionCounsel;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProsecutionCounselListConverterTest {

    @InjectMocks
    ProsecutionCounselListConverter prosecutionCounselListConverter;

    @Test
    public void shouldConvertListOfProsecutionCounselsToJsonObjects() {
        final UUID id1 = randomUUID();
        final UUID hearingId1 = randomUUID();
        final UUID personId1 = randomUUID();
        final String status1 = STRING.next();
        final ProsecutionCounsel prosecutionCounsel1 = new ProsecutionCounsel(id1, hearingId1, personId1, status1);

        final UUID id2 = randomUUID();
        final UUID hearingId2 = randomUUID();
        final UUID personId2 = randomUUID();
        final String status2 = STRING.next();
        final ProsecutionCounsel prosecutionCounsel2 = new ProsecutionCounsel(id2, hearingId2, personId2, status2);

        final List<ProsecutionCounsel> prosecutionCounsels = asList(prosecutionCounsel1, prosecutionCounsel2);

        prosecutionCounselListConverter.convert(prosecutionCounsels);


    }
}