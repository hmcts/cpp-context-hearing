package uk.gov.moj.cpp.hearing.event.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.moj.cpp.hearing.event.service.LjaReferenceDataLoader.ENFORCEMENT_AREA_QUERY_NAME;
import static uk.gov.moj.cpp.hearing.event.service.LjaReferenceDataLoader.GET_ORGANISATION_UNIT_BY_ID_ID;

import uk.gov.justice.core.courts.LjaDetails;
import uk.gov.justice.hearing.courts.referencedata.EnforcementArea;
import uk.gov.justice.hearing.courts.referencedata.EnforcementAreaBacs;
import uk.gov.justice.hearing.courts.referencedata.LocalJusticeArea;
import uk.gov.justice.hearing.courts.referencedata.OrganisationalUnit;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LjaReferenceDataLoaderTest extends ReferenceDataClientTestBase {

    @InjectMocks
    private LjaReferenceDataLoader target;

    @Test
    public void testLoad() {

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

        final UUID courtCentreId = UUID.randomUUID();

        mockQuery(GET_ORGANISATION_UNIT_BY_ID_ID, organisationalUnit, false);
        mockQuery(ENFORCEMENT_AREA_QUERY_NAME, enforcementArea, true);

        final LjaDetails result = target.getLjaDetails(context, courtCentreId, null);

        assertThat(result.getAccountDivisionCode(), is(enforcementArea.getAccountDivisionCode().toString()));
        assertThat(result.getEnforcementEmail(), is(enforcementArea.getEmail()));
        assertThat(result.getEnforcementAddress().getAddress1(), is(enforcementArea.getAddress1()));
        assertThat(result.getEnforcementAddress().getAddress2(), is(enforcementArea.getAddress1()));
        assertThat(result.getEnforcementAddress().getAddress3(), is(enforcementArea.getAddress1()));

    }

}
