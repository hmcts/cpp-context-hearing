package uk.gov.moj.cpp.hearing.xhibit.refdatacache;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import uk.gov.moj.cpp.external.domain.referencedata.XhibitEventMapping;
import uk.gov.moj.cpp.external.domain.referencedata.XhibitEventMappingsList;
import uk.gov.moj.cpp.hearing.xhibit.ReferenceDataXhibitDataLoader;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class XhibitEventMapperCacheTest {

    @Mock
    private ReferenceDataXhibitDataLoader referenceDataXhibitDataLoader;

    @InjectMocks
    private XhibitEventMapperCache xhibitEventMapperCache;

    @Test
    public void shouldPopulateCache() {
        final String firstXhibitEventCode = "exhibitEventCode_1";
        final String secondXhibitEventCode = "exhibitEventCode_2";
        final UUID cpHearingEventId_1 = randomUUID();
        final UUID cpHearingEventId_2 = randomUUID();

        final XhibitEventMappingsList mappings = new XhibitEventMappingsList(asList(new XhibitEventMapping(cpHearingEventId_1, firstXhibitEventCode, "", "", ""), new XhibitEventMapping(cpHearingEventId_2, secondXhibitEventCode, "", "", "")));

        when(referenceDataXhibitDataLoader.getEventMapping()).thenReturn(mappings);

        xhibitEventMapperCache.init();

        assertThat(xhibitEventMapperCache.getXhibitEventCodeBy(cpHearingEventId_1.toString()), is(firstXhibitEventCode));
        assertThat(xhibitEventMapperCache.getXhibitEventCodeBy(cpHearingEventId_2.toString()), is(secondXhibitEventCode));
    }
}