package uk.gov.moj.cpp.hearing.query.view.referencedata;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import uk.gov.moj.cpp.external.domain.referencedata.XhibitEventMapping;
import uk.gov.moj.cpp.external.domain.referencedata.XhibitEventMappingsList;
import uk.gov.moj.cpp.hearing.query.view.service.ReferenceDataService;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class XhibitEventMapperCacheTest {

    @Mock
    private ReferenceDataService referenceDataService;

    @InjectMocks
    private XhibitEventMapperCache xhibitEventMapperCache;

    @Test
    public void shouldPopulateCache() {
        final String firstXhibitEventCode = "exhibitEventCode_1";
        final String secondXhibitEventCode = "exhibitEventCode_2";
        final UUID cpHearingEventId_1 = randomUUID();
        final UUID cpHearingEventId_2 = randomUUID();
        final Set<UUID> hearingEventIds = new HashSet();
        hearingEventIds.add(cpHearingEventId_1);
        hearingEventIds.add(cpHearingEventId_2);

        final XhibitEventMapping xhibitEventMapping1 = new XhibitEventMapping(cpHearingEventId_1, firstXhibitEventCode, EMPTY, EMPTY, EMPTY);
        final XhibitEventMapping xhibitEventMapping2 = new XhibitEventMapping(cpHearingEventId_2, secondXhibitEventCode, EMPTY, EMPTY, EMPTY);
        final XhibitEventMappingsList mappings = new XhibitEventMappingsList(asList(xhibitEventMapping1, xhibitEventMapping2));

        when(referenceDataService.listAllEventMappings()).thenReturn(mappings);

        xhibitEventMapperCache.init();

        assertThat(xhibitEventMapperCache.getCppHearingEventIds(), is(hearingEventIds));
    }
}