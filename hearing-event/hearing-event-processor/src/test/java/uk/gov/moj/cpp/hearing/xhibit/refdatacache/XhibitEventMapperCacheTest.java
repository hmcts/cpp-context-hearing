package uk.gov.moj.cpp.hearing.xhibit.refdatacache;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import uk.gov.moj.cpp.hearing.event.service.EventMapping;
import uk.gov.moj.cpp.hearing.event.service.ReferenceDataService;
import uk.gov.moj.cpp.hearing.xhibit.ReferenceDataXhibitDataLoader;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class XhibitEventMapperCacheTest {

    @Mock
    private ReferenceDataXhibitDataLoader referenceDataXhibitDataLoader;

    @InjectMocks
    private XhibitEventMapperCache xhibitEventMapperCache;

    @Test
    public void shouldPopulateCache() {
        final String key1 = "testKey1";
        final String value1 = "testValue1";
        final String key2 = "testKey2";
        final String value2 = "testValue2";

        final List<EventMapping> mappings = Arrays.asList(new EventMapping(key1, value1), new EventMapping(key2, value2));

        when(referenceDataXhibitDataLoader.getEventMapping()).thenReturn(mappings);

        xhibitEventMapperCache.init();

        assertThat(xhibitEventMapperCache.getXhibitEventCodeBy(key1), is(value1));
        assertThat(xhibitEventMapperCache.getXhibitEventCodeBy(key2), is(value2));
    }
}