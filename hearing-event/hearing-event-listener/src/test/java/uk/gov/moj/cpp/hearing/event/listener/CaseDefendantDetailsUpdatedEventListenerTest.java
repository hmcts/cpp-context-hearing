package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.defendantTemplate;

import uk.gov.justice.core.courts.FundingType;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.moj.cpp.hearing.domain.event.DefendantDetailsUpdated;
import uk.gov.moj.cpp.hearing.mapping.AssociatedDefenceOrganisationJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.CourtApplicationsSerializer;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Address;
import uk.gov.moj.cpp.hearing.persist.entity.ha.AssociatedDefenceOrganisation;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Contact;
import uk.gov.moj.cpp.hearing.persist.entity.ha.DefenceOrganisation;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase;
import uk.gov.moj.cpp.hearing.repository.DefendantRepository;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;

import java.time.LocalDate;
import java.util.UUID;

import javax.json.JsonObject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CaseDefendantDetailsUpdatedEventListenerTest {

    @Mock
    HearingRepository hearingRepository;
    @Spy
    CourtApplicationsSerializer courtApplicationsSerializer;
    @InjectMocks
    private CaseDefendantDetailsUpdatedEventListener caseDefendantDetailsUpdatedEventListener;
    @Mock
    private DefendantRepository defendantRepository;
    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Mock
    private AssociatedDefenceOrganisationJPAMapper associatedDefenceOrganisationJPAMapper;

    @Before
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        setField(this.courtApplicationsSerializer, "jsonObjectToObjectConverter", jsonObjectToObjectConverter);
        setField(this.courtApplicationsSerializer, "objectToJsonObjectConverter", objectToJsonObjectConverter);
    }

    @Test
    public void shouldUpdateDefendant() {

        final UUID hearingId = randomUUID();

        final DefendantDetailsUpdated defendantDetailsUpdated = DefendantDetailsUpdated.defendantDetailsUpdated()
                .setHearingId(hearingId)
                .setDefendant(defendantTemplate());

        final Hearing hearing = new Hearing();
        hearing.setId(hearingId);

        final JsonEnvelope envelope = createJsonEnvelope(defendantDetailsUpdated);

        final Defendant defendant = getDefendant(hearingId, defendantDetailsUpdated);

        when(defendantRepository.findBy(defendant.getId())).thenReturn(defendant);

        when(hearingRepository.findBy(hearingId)).thenReturn(hearing);

        when(associatedDefenceOrganisationJPAMapper.toJPA(any(uk.gov.justice.core.courts.AssociatedDefenceOrganisation.class)))
                .thenReturn(getAssociatedDefenceOrganisation());

        caseDefendantDetailsUpdatedEventListener.defendantDetailsUpdated(envelope);

        final ArgumentCaptor<Defendant> defendantexArgumentCaptor = ArgumentCaptor.forClass(Defendant.class);

        verify(defendantRepository).save(defendantexArgumentCaptor.capture());

        assertAssociatedDefenceOrganisation(defendant, defendantexArgumentCaptor);
    }

    private AssociatedDefenceOrganisation getAssociatedDefenceOrganisation() {
        AssociatedDefenceOrganisation associatedDefenceOrganisation = new AssociatedDefenceOrganisation();
        associatedDefenceOrganisation.setApplicationReference("application-reference");
        associatedDefenceOrganisation.setFundingType(FundingType.REPRESENTATION_ORDER);
        associatedDefenceOrganisation.setAssociationStartDate(LocalDate.of(2019, 12, 13));
        associatedDefenceOrganisation.setAssociationEndDate(LocalDate.of(2020, 12, 12));
        associatedDefenceOrganisation.setAssociatedByLAA(true);
        final DefenceOrganisation defenceOrganisation = new DefenceOrganisation();
        defenceOrganisation.setLaaContractNumber("LAA44569");
        defenceOrganisation.setName("Test");
        defenceOrganisation.setRegisteredCharityNumber("Reg-001");
        defenceOrganisation.setIncorporationNumber("Inc-0001");
        final Address address = new Address();
        address.setAddress1("address1");
        address.setAddress2("address2");
        address.setAddress3("address3");
        address.setAddress4("address4");
        address.setAddress5("address5");
        address.setPostCode("postCode");
        defenceOrganisation.setAddress(address);

        final Contact contact = new Contact();
        contact.setPrimaryEmail("test.test@test.com");
        contact.setSecondaryEmail("test-sec@test.com");
        contact.setMobile("07411201778");
        contact.setHome("02088888888");
        contact.setWork("02077777777");
        contact.setFax("020555555555");

        defenceOrganisation.setContact(contact);
        associatedDefenceOrganisation.setDefenceOrganisation(defenceOrganisation);
        return associatedDefenceOrganisation;
    }

    private Defendant getDefendant(final UUID hearingId, final DefendantDetailsUpdated defendantDetailsUpdated) {
        final ProsecutionCase prosecutionCase = new ProsecutionCase();
        prosecutionCase.setId(new HearingSnapshotKey(defendantDetailsUpdated.getDefendant().getProsecutionCaseId(), hearingId));

        final Defendant defendant = new Defendant();
        defendant.setProsecutionCase(prosecutionCase);
        defendant.setId(new HearingSnapshotKey(defendantDetailsUpdated.getDefendant().getId(), hearingId));
        return defendant;
    }

    private void assertAssociatedDefenceOrganisation(final Defendant defendant, final ArgumentCaptor<Defendant> defendantArgumentCaptor) {
        final Defendant defendantOut = defendantArgumentCaptor.getValue();
        assertThat(defendant.getId(), is(defendantOut.getId()));
        assertThat(defendantOut.getAssociatedDefenceOrganisation().getAssociatedByLAA(), is(true));
        assertThat(defendantOut.getAssociatedDefenceOrganisation().getApplicationReference(), is("application-reference"));

        final DefenceOrganisation defenceOrganisationResulted = defendantOut.getAssociatedDefenceOrganisation().getDefenceOrganisation();
        assertThat(defenceOrganisationResulted, notNullValue());
        assertThat(defenceOrganisationResulted.getLaaContractNumber(), is("LAA44569"));
        assertThat(defenceOrganisationResulted.getName(), is("Test"));
        assertThat(defendantOut.getAssociatedDefenceOrganisation().getAssociatedByLAA(), is(true));
        assertThat(defenceOrganisationResulted.getRegisteredCharityNumber(), is("Reg-001"));
        assertThat(defenceOrganisationResulted.getIncorporationNumber(), is("Inc-0001"));

        assertThat(defenceOrganisationResulted.getAddress().getAddress1(), is("address1"));
        assertThat(defenceOrganisationResulted.getAddress().getAddress2(), is("address2"));
        assertThat(defenceOrganisationResulted.getAddress().getAddress3(), is("address3"));
        assertThat(defenceOrganisationResulted.getAddress().getAddress4(), is("address4"));
        assertThat(defenceOrganisationResulted.getAddress().getAddress5(), is("address5"));
        assertThat(defenceOrganisationResulted.getAddress().getPostCode(), is("postCode"));

        assertThat(defenceOrganisationResulted.getContact().getPrimaryEmail(), is("test.test@test.com"));
        assertThat(defenceOrganisationResulted.getContact().getSecondaryEmail(), is("test-sec@test.com"));
        assertThat(defenceOrganisationResulted.getContact().getWork(), is("02077777777"));
        assertThat(defenceOrganisationResulted.getContact().getMobile(), is("07411201778"));
        assertThat(defenceOrganisationResulted.getContact().getFax(), is("020555555555"));
    }

    private JsonEnvelope createJsonEnvelope(final DefendantDetailsUpdated defendantDetailsUpdated) {

        final JsonObject jsonObject = objectToJsonObjectConverter.convert(defendantDetailsUpdated);

        return envelopeFrom((Metadata) null, jsonObject);
    }

}
