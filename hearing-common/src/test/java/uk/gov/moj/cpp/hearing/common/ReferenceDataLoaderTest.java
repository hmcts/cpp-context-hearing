package uk.gov.moj.cpp.hearing.common;


import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import uk.gov.justice.core.courts.LjaDetails;
import uk.gov.justice.hearing.courts.referencedata.EnforcementArea;
import uk.gov.justice.hearing.courts.referencedata.LocalJusticeArea;
import uk.gov.justice.hearing.courts.referencedata.OrganisationalUnit;
import uk.gov.justice.hearing.courts.referencedata.OrganisationunitsResult;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.moj.cpp.hearing.common.exception.ReferenceDataNotFoundException;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ReferenceDataLoaderTest {

    @InjectMocks
    private ReferenceDataLoader referenceDataLoader;

    @Mock(answer = RETURNS_DEEP_STUBS)
    private Requester requester;

    private final UUID courtCentreId = randomUUID();

    @Test
    public void shouldGetOrganisationUnitList() {

        final OrganisationunitsResult expectedOrganisationUnitsResult = OrganisationunitsResult.organisationunitsResult()
                .withOrganisationunits(Arrays.asList(getOrganisationUnit()))
                .build();

        when(requester.requestAsAdmin(any(), eq(OrganisationunitsResult.class)).payload()).thenReturn(expectedOrganisationUnitsResult);

        final Optional<OrganisationunitsResult> actualOrganisationUnitsResultOptional = referenceDataLoader.getOrganisationUnitList();

        assertTrue(actualOrganisationUnitsResultOptional.isPresent());

        final OrganisationunitsResult actualOrganisationUnitsResult = actualOrganisationUnitsResultOptional.get();

        assertThat(actualOrganisationUnitsResult.getOrganisationunits().size(), is(expectedOrganisationUnitsResult.getOrganisationunits().size()));

        IntStream.range(0, actualOrganisationUnitsResult.getOrganisationunits().size()).forEach(index -> {
            final OrganisationalUnit actualOrganisationUnit = actualOrganisationUnitsResult.getOrganisationunits().get(index);

            final OrganisationalUnit expectedOrganisationUnit = expectedOrganisationUnitsResult.getOrganisationunits().get(index);

            assertThat(actualOrganisationUnit.getId(), is(expectedOrganisationUnit.getId()));
            assertThat(actualOrganisationUnit.getLja(), is(expectedOrganisationUnit.getLja()));
        });
    }

    @Test(expected = ReferenceDataNotFoundException.class)
    public void shouldThrowInvalidReferenceDataExceptionIfOrganisationUnitIsNotPresent() {
        when(requester.requestAsAdmin(any(), eq(OrganisationunitsResult.class)).payload()).thenReturn(null);

        referenceDataLoader.getOrganisationUnitList();
    }

    @Test
    public void shouldGetOrganisationUnitById() {
        final OrganisationalUnit organisationUnit = getOrganisationUnit();
        when(requester.requestAsAdmin(any(), eq(OrganisationalUnit.class)).payload()).thenReturn(organisationUnit);

        final OrganisationalUnit organisationalUnit = referenceDataLoader.getOrganisationUnitById(courtCentreId);

        assertThat(organisationalUnit.getId(), is(organisationUnit.getId()));
        assertThat(organisationalUnit.getLja(), is(organisationUnit.getLja()));
    }

    @Test(expected = ReferenceDataNotFoundException.class)
    public void shouldThrowReferenceDataNotFoundExceptionIfResponseEnvelopeIsNull() {
        when(requester.requestAsAdmin(any(), eq(OrganisationalUnit.class)).payload()).thenReturn(null);

        referenceDataLoader.getOrganisationUnitById(courtCentreId);
    }

    @Test
    public void shouldGetLjaDetails() {
        final OrganisationalUnit organisationUnit = getOrganisationUnit();
        final UUID courtCentreId = randomUUID();
        final String ljaName = "Lavender Hill Magistrates' Court";
        final String ljaWelshName = "Llys Ynadon Lavender Hill";

        final EnforcementArea expectedEnforcementArea = EnforcementArea.enforcementArea()
                .withLocalJusticeArea(LocalJusticeArea.localJusticeArea()
                        .withNationalCourtCode(organisationUnit.getLja())
                        .withName(ljaName)
                        .withWelshName(ljaWelshName)
                        .build())
                .build();

        when(requester.requestAsAdmin(any(), eq(OrganisationalUnit.class)).payload()).thenReturn(organisationUnit);

        when(requester.requestAsAdmin(any(), eq(EnforcementArea.class)).payload()).thenReturn(expectedEnforcementArea);

        final LjaDetails ljaDetails = referenceDataLoader.getLjaDetails(organisationUnit);

        assertNotNull(ljaDetails);
        assertThat(ljaDetails.getLjaCode(), is(expectedEnforcementArea.getLocalJusticeArea().getNationalCourtCode()));
        assertThat(ljaDetails.getLjaName(), is(expectedEnforcementArea.getLocalJusticeArea().getName()));
        assertThat(ljaDetails.getWelshLjaName(), is(expectedEnforcementArea.getLocalJusticeArea().getWelshName()));
    }

    @Test(expected = ReferenceDataNotFoundException.class)
    public void shouldThrowEnforcementAreaNotFoundExceptionIfEnforcementAreaIsNullWhilstGettingLjaDetails() {
        final OrganisationalUnit organisationUnit = getOrganisationUnit();

        when(requester.requestAsAdmin(any(), eq(OrganisationalUnit.class)).payload()).thenReturn(organisationUnit);
        when(requester.requestAsAdmin(any(), eq(EnforcementArea.class)).payload()).thenReturn(null);

        referenceDataLoader.getLjaDetails(organisationUnit);
    }

    private OrganisationalUnit getOrganisationUnit() {
        return OrganisationalUnit.organisationalUnit()
                .withId(courtCentreId.toString())
                .withLja("2577")
                .build();
    }

}
