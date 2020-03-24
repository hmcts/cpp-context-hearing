package uk.gov.moj.cpp.hearing.query.view.referencedata;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import uk.gov.moj.cpp.external.domain.referencedata.HearingTypeMapping;
import uk.gov.moj.cpp.external.domain.referencedata.HearingTypeMappingList;
import uk.gov.moj.cpp.hearing.query.view.service.ReferenceDataService;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class XhibitHearingTypesCacheTest {

    @Mock
    private ReferenceDataService referenceDataService;

    @InjectMocks
    private XhibitHearingTypesCache xhibitHearingTypesCache;

    @Test
    public void shouldPopulateCache(){
        final UUID hearingTypeId1 = UUID.randomUUID();
        final UUID hearingTypeId2 = UUID.randomUUID();
        final String exhibitHearingTypeDescription1 = "Plea and Trial Preparation";
        final String exhibitHearingTypeDescription2= "Committal for Sentence";

        final HearingTypeMapping hearingTypeMapping1 = new HearingTypeMapping(hearingTypeId1, 0, EMPTY, EMPTY, EMPTY, 0, EMPTY, EMPTY, EMPTY, exhibitHearingTypeDescription1);
        final HearingTypeMapping hearingTypeMapping2 = new HearingTypeMapping(hearingTypeId2, 0, EMPTY, EMPTY, EMPTY, 0, EMPTY, EMPTY, EMPTY, exhibitHearingTypeDescription2);

        final HearingTypeMappingList hearingTypeMappingList = new HearingTypeMappingList(asList(hearingTypeMapping1, hearingTypeMapping2));

        when(referenceDataService.getXhibitHearingType()).thenReturn(hearingTypeMappingList);

        xhibitHearingTypesCache.init();

        assertThat(xhibitHearingTypesCache.getHearingTypeDescription(hearingTypeId1), is(exhibitHearingTypeDescription1));
        assertThat(xhibitHearingTypesCache.getHearingTypeDescription(hearingTypeId2), is(exhibitHearingTypeDescription2));
    }

}