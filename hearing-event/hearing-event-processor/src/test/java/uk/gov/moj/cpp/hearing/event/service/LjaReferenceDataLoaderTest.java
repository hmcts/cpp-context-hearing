package uk.gov.moj.cpp.hearing.event.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import uk.gov.justice.core.courts.LjaDetails;
import uk.gov.justice.hearing.courts.referencedata.EnforcementArea;
import uk.gov.justice.hearing.courts.referencedata.EnforcementAreaBacs;
import uk.gov.justice.hearing.courts.referencedata.LocalJusticeArea;
import uk.gov.justice.hearing.courts.referencedata.OrganisationalUnit;

import java.util.UUID;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.metadata;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payload;
import static uk.gov.moj.cpp.hearing.event.service.LjaReferenceDataLoader.ENFORCEMENT_AREA_QUERY_NAME;
import static uk.gov.moj.cpp.hearing.event.service.LjaReferenceDataLoader.GET_ORGANISATION_UNIT_BY_ID_ID;

@RunWith(MockitoJUnitRunner.class)
public class LjaReferenceDataLoaderTest extends ReferenceDataClientTestBase {

    @InjectMocks
    private LjaReferenceDataLoader target;

    @Test
    public void shouldQueryAndReturnLjaDetailsCorrectly() {

        final String ouCode = "1234abcd";
        final OrganisationalUnit organisationalUnit = OrganisationalUnit.organisationalUnit()
                .withOucode(ouCode)
                .withLja("1234")
                .withEnforcementArea(EnforcementAreaBacs.enforcementAreaBacs()
                        .withBankAccntName("NetWest")
                        .withBankAccntNum(123456789)
                        .withBankAccntSortCode("12-34-56")
                        .build())
                .build();

        final EnforcementArea enforcementArea = EnforcementArea.enforcementArea()
                .withAccountDivisionCode(98765)
                .withLocalJusticeArea(LocalJusticeArea.localJusticeArea().build())
                .build();

        final UUID courtCentreId = randomUUID();

        mockQuery(orgUnitQueryEnvelopeCaptor, organisationalUnit);
        mockAdminQuery(enforcementAreaQueryEnvelopeCaptor, enforcementArea);

        target.getLjaDetails(context, courtCentreId);

        verify(requester).request(orgUnitQueryEnvelopeCaptor.capture());
        verify(requester).requestAsAdmin(enforcementAreaQueryEnvelopeCaptor.capture());

        assertThat(enforcementAreaQueryEnvelopeCaptor.getValue(), jsonEnvelope()
                .withMetadataOf(metadata().withName(ENFORCEMENT_AREA_QUERY_NAME)));

        assertThat(orgUnitQueryEnvelopeCaptor.getValue().metadata(), metadata().withName(GET_ORGANISATION_UNIT_BY_ID_ID));
        assertThat(orgUnitQueryEnvelopeCaptor.getValue().payload(), payload().isJson(withJsonPath("$.id", is(courtCentreId.toString()))));
    }

}
