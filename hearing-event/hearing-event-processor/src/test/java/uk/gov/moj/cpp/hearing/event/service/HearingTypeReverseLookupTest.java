package uk.gov.moj.cpp.hearing.event.service;

import static java.util.Arrays.asList;
import static uk.gov.moj.cpp.hearing.event.service.HearingTypeReverseLookup.GET_HEARING_TYPES_ID;

import uk.gov.justice.core.courts.HearingType;
import uk.gov.justice.hearing.courts.referencedata.HearingTypes;
import uk.gov.justice.hearing.courts.referencedata.HearingTypesResult;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HearingTypeReverseLookupTest extends ReferenceDataClientTestBase {

    @InjectMocks
    private HearingTypeReverseLookup target;

    @Test
    public void hearingTypeByName() {
        final HearingTypesResult hearingTypesResult = HearingTypesResult.hearingTypesResult()
                .withHearingTypes(asList(
                        HearingTypes.hearingTypes()
                                .withId(UUID.randomUUID().toString())
                                .withHearingDescription("abCdEf")
                                .build()
                ))
                .build();
        mockQuery(GET_HEARING_TYPES_ID, hearingTypesResult, true);

        HearingType hearingType = target.getHearingTypeByName(context, "abcdeF");

        Assert.assertEquals(hearingTypesResult.getHearingTypes().get(0).getId(), hearingType.getId().toString());

    }


}
