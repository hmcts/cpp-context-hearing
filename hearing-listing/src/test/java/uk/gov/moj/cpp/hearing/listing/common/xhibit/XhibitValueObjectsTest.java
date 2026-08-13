package uk.gov.moj.cpp.hearing.listing.common.xhibit;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.moj.cpp.hearing.listing.common.xhibit.model.CourtCentreRoomKey;
import uk.gov.moj.cpp.hearing.listing.domain.referencedata.CourtMapping;
import uk.gov.moj.cpp.hearing.listing.domain.referencedata.CourtMappingsList;
import uk.gov.moj.cpp.hearing.listing.domain.referencedata.CourtRoomMapping;
import uk.gov.moj.cpp.hearing.listing.domain.referencedata.CourtRoomMappingsList;
import uk.gov.moj.cpp.hearing.listing.domain.referencedata.HearingType;
import uk.gov.moj.cpp.hearing.listing.domain.referencedata.HearingTypesList;
import uk.gov.moj.cpp.hearing.listing.domain.referencedata.JudiciariesList;
import uk.gov.moj.cpp.hearing.listing.domain.referencedata.Judiciary;
import uk.gov.moj.cpp.hearing.listing.domain.referencedata.OrganisationUnit;
import uk.gov.moj.cpp.hearing.listing.domain.referencedata.OrganisationUnitList;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;

public class XhibitValueObjectsTest {

    private static final String OUCODE = "OU01";
    private static final String COURT_TYPE = "CROWN_COURT";
    private static final LocalDate VALID_FROM = LocalDate.of(2026, 1, 1);
    private static final LocalDate VALID_TO = LocalDate.of(2026, 12, 31);

    private static final String CREST_COURT_ID = "crestCourtId";
    private static final String CREST_SITE_ID = "crestSiteId";
    private static final String SITE_CODE = "siteCode";
    private static final String CREST_SITE_NAME = "Crest Site";
    private static final String WELSH_CREST_SITE_NAME = "Welsh Crest Site";
    private static final String CREST_COURT_NAME = "Crest Court";
    private static final String WELSH_CREST_COURT_NAME = "Welsh Crest Court";
    private static final String SHORT_NAME = "Short";
    private static final String WELSH_SHORT_NAME = "Welsh Short";
    private static final String FULL_NAME = "Full Name";
    private static final String WELSH_FULL_NAME = "Welsh Full Name";

    private static final String HEARING_CODE = "HC";
    private static final String HEARING_DESCRIPTION = "Hearing Description";
    private static final String WELSH_HEARING_DESCRIPTION = "Welsh Hearing Description";
    private static final String XHIBIT_HEARING_CODE = "XHC";
    private static final String XHIBIT_DESCRIPTION = "Xhibit Description";

    private static final String TITLE_SUFFIX = "QC";
    private static final String TITLE_PREFIX = "Mr";
    private static final String TITLE_JUDICIAL_PREFIX = "Judge";
    private static final String SURNAME = "Surname";
    private static final String FORENAMES = "Forenames";
    private static final String REQUESTED_NAME = "Requested Name";
    private static final String JUDICIARY_TYPE = "JUDGE";

    private static final String COURT_ROOM_1 = "Court Room 1";

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

        final CourtRoomMapping mapping = new CourtRoomMapping(id, courtRoomUUID, CREST_SITE_NAME, OUCODE, 7,
                CREST_COURT_ID, CREST_SITE_ID, SITE_CODE, COURT_ROOM_1, crestCourtSiteUUID);

        assertThat(mapping.getId(), is(id));
        assertThat(mapping.getCourtRoomUUID(), is(courtRoomUUID));
        assertThat(mapping.getCrestCourtSiteName(), is(CREST_SITE_NAME));
        assertThat(mapping.getOucode(), is(OUCODE));
        assertThat(mapping.getCourtRoomId(), is(7));
        assertThat(mapping.getCrestCourtId(), is(CREST_COURT_ID));
        assertThat(mapping.getCrestCourtSiteId(), is(CREST_SITE_ID));
        assertThat(mapping.getCrestCourtSiteCode(), is(SITE_CODE));
        assertThat(mapping.getCrestCourtRoomName(), is(COURT_ROOM_1));
        assertThat(mapping.getCrestCourtSiteUUID(), is(crestCourtSiteUUID));
    }

    @Test
    public void shouldBuildCourtRoomMappingViaBuilderAndNameConstructor() {
        final UUID id = randomUUID();

        final CourtRoomMapping built = new CourtRoomMapping.Builder()
                .withId(id)
                .withCrestCourtSiteCode(SITE_CODE)
                .withCrestCourtRoomName("Court Room 2")
                .build();

        assertThat(built.getId(), is(id));
        assertThat(built.getCrestCourtSiteCode(), is(SITE_CODE));
        assertThat(built.getCrestCourtRoomName(), is("Court Room 2"));

        final CourtRoomMapping fromName = new CourtRoomMapping("Court Room 3");
        assertThat(fromName.getCrestCourtRoomName(), is("Court Room 3"));
    }

    @Test
    public void shouldExposeEveryCourtMappingFieldFromTheAllArgsConstructor() {
        final UUID id = randomUUID();

        final CourtMapping mapping = new CourtMapping(id, OUCODE, CREST_COURT_ID, CREST_SITE_ID,
                CREST_SITE_NAME, WELSH_CREST_SITE_NAME, VALID_FROM, VALID_TO, CREST_COURT_NAME,
                WELSH_CREST_COURT_NAME, SHORT_NAME, WELSH_SHORT_NAME, FULL_NAME, WELSH_FULL_NAME,
                SITE_CODE, COURT_TYPE);

        assertCourtMappingFields(mapping, id);
    }

    @Test
    public void shouldSetEveryCourtMappingFieldThroughTheBuilder() {
        final UUID id = randomUUID();

        final CourtMapping mapping = new CourtMapping.Builder()
                .withId(id)
                .withOucode(OUCODE)
                .withCrestCourtId(CREST_COURT_ID)
                .withCrestCourtSiteId(CREST_SITE_ID)
                .withCrestCourtSiteName(CREST_SITE_NAME)
                .withWelshCrestCourtSiteName(WELSH_CREST_SITE_NAME)
                .withValidFrom(VALID_FROM)
                .withValidTo(VALID_TO)
                .withCrestCourtName(CREST_COURT_NAME)
                .withWelshCrestCourtName(WELSH_CREST_COURT_NAME)
                .withCrestCourtShortName(SHORT_NAME)
                .withWelshCrestCourtShortName(WELSH_SHORT_NAME)
                .withCrestCourtFullName(FULL_NAME)
                .withWelshCrestCourtFullName(WELSH_FULL_NAME)
                .withCrestCourtSiteCode(SITE_CODE)
                .withCourtType(COURT_TYPE)
                .build();

        assertCourtMappingFields(mapping, id);
    }

    private void assertCourtMappingFields(final CourtMapping mapping, final UUID id) {
        assertThat(mapping.getId(), is(id));
        assertThat(mapping.getOucode(), is(OUCODE));
        assertThat(mapping.getCrestCourtId(), is(CREST_COURT_ID));
        assertThat(mapping.getCrestCourtSiteId(), is(CREST_SITE_ID));
        assertThat(mapping.getCrestCourtSiteName(), is(CREST_SITE_NAME));
        assertThat(mapping.getWelshCrestCourtSiteName(), is(WELSH_CREST_SITE_NAME));
        assertThat(mapping.getValidFrom(), is(VALID_FROM));
        assertThat(mapping.getValidTo(), is(VALID_TO));
        assertThat(mapping.getCrestCourtName(), is(CREST_COURT_NAME));
        assertThat(mapping.getWelshCrestCourtName(), is(WELSH_CREST_COURT_NAME));
        assertThat(mapping.getCrestCourtShortName(), is(SHORT_NAME));
        assertThat(mapping.getWelshCrestCourtShortName(), is(WELSH_SHORT_NAME));
        assertThat(mapping.getCrestCourtFullName(), is(FULL_NAME));
        assertThat(mapping.getWelshCrestCourtFullName(), is(WELSH_FULL_NAME));
        assertThat(mapping.getCrestCourtSiteCode(), is(SITE_CODE));
        assertThat(mapping.getCourtType(), is(COURT_TYPE));
    }

    @Test
    public void shouldExposeEveryHearingTypeFieldFromTheAllArgsConstructor() {
        final UUID id = randomUUID();

        final HearingType hearingType = new HearingType(id, 3, HEARING_CODE, HEARING_DESCRIPTION,
                WELSH_HEARING_DESCRIPTION, 30, VALID_FROM, VALID_TO, XHIBIT_HEARING_CODE, XHIBIT_DESCRIPTION);

        assertThat(hearingType.getId(), is(id));
        assertThat(hearingType.getSeqId(), is(3));
        assertThat(hearingType.getHearingCode(), is(HEARING_CODE));
        assertThat(hearingType.getHearingDescription(), is(HEARING_DESCRIPTION));
        assertThat(hearingType.getWelshHearingDescription(), is(WELSH_HEARING_DESCRIPTION));
        assertThat(hearingType.getDefaultDurationMin(), is(30));
        assertThat(hearingType.getValidFrom(), is(VALID_FROM));
        assertThat(hearingType.getValidTo(), is(VALID_TO));
        assertThat(hearingType.getExhibitHearingCode(), is(XHIBIT_HEARING_CODE));
        assertThat(hearingType.getExhibitHearingDescription(), is(XHIBIT_DESCRIPTION));
    }

    @Test
    public void shouldSetHearingTypeFieldsThroughTheBuilder() {
        final UUID id = randomUUID();

        final HearingType hearingType = new HearingType.Builder()
                .withId(id)
                .withHearingCode(HEARING_CODE)
                .withHearingDescription(HEARING_DESCRIPTION)
                .withExhibitHearingCode(XHIBIT_HEARING_CODE)
                .withExhibitHearingDescription(XHIBIT_DESCRIPTION)
                .build();

        assertThat(hearingType.getId(), is(id));
        assertThat(hearingType.getHearingCode(), is(HEARING_CODE));
        assertThat(hearingType.getHearingDescription(), is(HEARING_DESCRIPTION));
        assertThat(hearingType.getExhibitHearingCode(), is(XHIBIT_HEARING_CODE));
        assertThat(hearingType.getExhibitHearingDescription(), is(XHIBIT_DESCRIPTION));
    }

    @Test
    public void shouldExposeEveryJudiciaryFieldFromTheAllArgsConstructor() {
        final UUID id = randomUUID();

        final Judiciary judiciary = new Judiciary(id, TITLE_SUFFIX, TITLE_PREFIX, TITLE_JUDICIAL_PREFIX,
                SURNAME, FORENAMES, REQUESTED_NAME, JUDICIARY_TYPE);

        assertJudiciaryFields(judiciary, id);
    }

    @Test
    public void shouldSetEveryJudiciaryFieldThroughTheBuilder() {
        final UUID id = randomUUID();

        final Judiciary judiciary = new Judiciary.Builder()
                .withId(id)
                .withTitleSuffix(TITLE_SUFFIX)
                .withTitlePrefix(TITLE_PREFIX)
                .withTitleJudicialPrefix(TITLE_JUDICIAL_PREFIX)
                .withSurname(SURNAME)
                .withForenames(FORENAMES)
                .withRequestedName(REQUESTED_NAME)
                .withJudiciaryType(JUDICIARY_TYPE)
                .build();

        assertJudiciaryFields(judiciary, id);
    }

    private void assertJudiciaryFields(final Judiciary judiciary, final UUID id) {
        assertThat(judiciary.getId(), is(id));
        assertThat(judiciary.getTitleSuffix(), is(TITLE_SUFFIX));
        assertThat(judiciary.getTitlePrefix(), is(TITLE_PREFIX));
        assertThat(judiciary.getTitleJudicialPrefix(), is(TITLE_JUDICIAL_PREFIX));
        assertThat(judiciary.getSurname(), is(SURNAME));
        assertThat(judiciary.getForenames(), is(FORENAMES));
        assertThat(judiciary.getRequestedName(), is(REQUESTED_NAME));
        assertThat(judiciary.getJudiciaryType(), is(JUDICIARY_TYPE));
    }

    @Test
    public void shouldExposeOrganisationUnitFieldsFromConstructorAndBuilder() {
        final UUID id = randomUUID();

        final OrganisationUnit fromConstructor = new OrganisationUnit(id, OUCODE);
        assertThat(fromConstructor.getId(), is(id));
        assertThat(fromConstructor.getOucode(), is(OUCODE));

        final OrganisationUnit fromBuilder = new OrganisationUnit.Builder()
                .withId(id)
                .withOucode(OUCODE)
                .build();
        assertThat(fromBuilder.getId(), is(id));
        assertThat(fromBuilder.getOucode(), is(OUCODE));
    }

    @Test
    public void shouldReadBackTheListWrappersThroughBothConstructorAndSetter() {
        final CourtRoomMapping courtRoomMapping = new CourtRoomMapping(COURT_ROOM_1);
        final HearingType hearingType = new HearingType.Builder().withHearingCode(HEARING_CODE).build();
        final Judiciary judiciary = new Judiciary.Builder().withSurname(SURNAME).build();
        final OrganisationUnit organisationUnit = new OrganisationUnit(randomUUID(), OUCODE);
        final CourtMapping courtMapping = new CourtMapping.Builder().withOucode(OUCODE).build();

        assertThat(new CourtMappingsList(singletonList(courtMapping)).getCpXhibitCourtMappings(),
                is(singletonList(courtMapping)));

        final CourtRoomMappingsList courtRoomMappings = new CourtRoomMappingsList(singletonList(courtRoomMapping));
        assertThat(courtRoomMappings.getCpXhibitCourtRoomMappings(), is(singletonList(courtRoomMapping)));
        courtRoomMappings.setCpXhibitCourtRoomMappings(emptyList());
        assertThat(courtRoomMappings.getCpXhibitCourtRoomMappings(), is(emptyList()));

        final HearingTypesList hearingTypes = new HearingTypesList(singletonList(hearingType));
        assertThat(hearingTypes.getHearingTypes(), is(singletonList(hearingType)));
        hearingTypes.setHearingTypes(emptyList());
        assertThat(hearingTypes.getHearingTypes(), is(emptyList()));

        final JudiciariesList judiciaries = new JudiciariesList(singletonList(judiciary));
        assertThat(judiciaries.getJudiciaries(), is(singletonList(judiciary)));
        judiciaries.setJudiciaries(emptyList());
        assertThat(judiciaries.getJudiciaries(), is(emptyList()));

        final OrganisationUnitList organisationUnits = new OrganisationUnitList(singletonList(organisationUnit));
        assertThat(organisationUnits.getOrganisationunits(), is(singletonList(organisationUnit)));
        organisationUnits.setOrganisationunits(emptyList());
        assertThat(organisationUnits.getOrganisationunits(), is(emptyList()));
    }
}
