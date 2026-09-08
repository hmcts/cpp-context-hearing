package uk.gov.moj.cpp.hearing.listing.common.xhibit;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.moj.cpp.hearing.listing.common.xhibit.model.CourtCentreRoomKey;
import uk.gov.moj.cpp.hearing.listing.domain.referencedata.CourtRoomMapping;

import java.util.UUID;

import org.junit.jupiter.api.Test;

public class XhibitValueObjectsTest {

    @Test
    public void shouldExposeInjectedConnectionParameters() throws Exception {
        final XhibitSessionConnectionParameters parameters = new XhibitSessionConnectionParameters();
        setField(parameters, "outboundUrl", "http://xhibit/send");
        setField(parameters, "user", "listing-user");
        setField(parameters, "password", "secret");

        assertThat(parameters.getOutboundUrl(), is("http://xhibit/send"));
        assertThat(parameters.getUser(), is("listing-user"));
        assertThat(parameters.getPassword(), is("secret"));
    }

    @Test
    public void shouldImplementEqualityForCourtCentreRoomKey() {
        final UUID courtCentreId = randomUUID();
        final UUID courtRoomId = randomUUID();

        final CourtCentreRoomKey key = new CourtCentreRoomKey(courtCentreId, courtRoomId);
        final CourtCentreRoomKey same = new CourtCentreRoomKey(courtCentreId, courtRoomId);
        final CourtCentreRoomKey different = new CourtCentreRoomKey(randomUUID(), courtRoomId);

        assertThat(key.equals(key), is(true));
        assertThat(key.equals(same), is(true));
        assertThat(key.hashCode(), is(same.hashCode()));
        assertThat(key.equals(different), is(false));
        assertThat(key.equals(null), is(false));
        assertThat(key.equals("not-a-key"), is(false));
        assertThat(key.hashCode(), is(not(different.hashCode())));
    }

    @Test
    public void shouldBuildCourtRoomMappingViaAllArgsConstructor() {
        final UUID id = randomUUID();
        final UUID courtRoomUUID = randomUUID();
        final UUID crestCourtSiteUUID = randomUUID();

        final CourtRoomMapping mapping = new CourtRoomMapping(id, courtRoomUUID, "Crest Site", "OU01", 7,
                "crestCourtId", "crestSiteId", "crestSiteCode", "Court Room 1", crestCourtSiteUUID);

        assertThat(mapping.getId(), is(id));
        assertThat(mapping.getCourtRoomUUID(), is(courtRoomUUID));
        assertThat(mapping.getCrestCourtSiteName(), is("Crest Site"));
        assertThat(mapping.getOucode(), is("OU01"));
        assertThat(mapping.getCourtRoomId(), is(7));
        assertThat(mapping.getCrestCourtId(), is("crestCourtId"));
        assertThat(mapping.getCrestCourtSiteId(), is("crestSiteId"));
        assertThat(mapping.getCrestCourtSiteCode(), is("crestSiteCode"));
        assertThat(mapping.getCrestCourtRoomName(), is("Court Room 1"));
        assertThat(mapping.getCrestCourtSiteUUID(), is(crestCourtSiteUUID));
    }

    @Test
    public void shouldBuildCourtRoomMappingViaBuilderAndNameConstructor() {
        final UUID id = randomUUID();

        final CourtRoomMapping built = new CourtRoomMapping.Builder()
                .withId(id)
                .withCrestCourtSiteCode("siteCode")
                .withCrestCourtRoomName("Court Room 2")
                .build();

        assertThat(built.getId(), is(id));
        assertThat(built.getCrestCourtSiteCode(), is("siteCode"));
        assertThat(built.getCrestCourtRoomName(), is("Court Room 2"));

        final CourtRoomMapping fromName = new CourtRoomMapping("Court Room 3");
        assertThat(fromName.getCrestCourtRoomName(), is("Court Room 3"));
    }
}
