package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.Objects.nonNull;
import static java.util.UUID.randomUUID;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;
import static uk.gov.moj.cpp.hearing.Utilities.with;

import uk.gov.justice.json.schemas.core.ContactNumber;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.DefendantDetailsUpdated;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Address;
import uk.gov.moj.cpp.hearing.persist.entity.ha.AssociatedPerson;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Contact;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Organisation;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Person;
import uk.gov.moj.cpp.hearing.repository.DefendantRepository;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.UUID;
import java.util.stream.Collectors;

@ServiceComponent(EVENT_LISTENER)
public class CaseDefendantDetailsChangedEventListener {

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Inject
    private DefendantRepository defendantRepository;

    @Transactional
    @Handles("hearing.defendant-details-updated")
    public void defendantDetailsUpdated(final JsonEnvelope envelope) {

        final DefendantDetailsUpdated defendantDetailsToBeUpdated = jsonObjectToObjectConverter.convert(envelope.payloadAsJsonObject(), DefendantDetailsUpdated.class);

        final uk.gov.moj.cpp.hearing.command.defendant.Defendant defendantIn = defendantDetailsToBeUpdated.getDefendant();

        final UUID hearingId = defendantDetailsToBeUpdated.getHearingId();

        final Defendant defendant = defendantRepository.findBy(new HearingSnapshotKey(defendantIn.getId(), hearingId));

        if (defendant.getProsecutionCase().getId().getId().equals(defendantIn.getProsecutionCaseId())) {
            defendant.setNumberOfPreviousConvictionsCited(defendantIn.getNumberOfPreviousConvictionsCited());
            defendant.setProsecutionAuthorityReference(defendantIn.getProsecutionAuthorityReference());
            defendant.setWitnessStatement(defendantIn.getWitnessStatement());
            defendant.setWitnessStatementWelsh(defendantIn.getWitnessStatementWelsh());
            defendant.setMitigation(defendantIn.getMitigation());
            defendant.setMitigationWelsh(defendantIn.getMitigationWelsh());
            with(defendant.getDefenceOrganisation(), defendantIn.getDefenceOrganisation(), this::setOrganisation);
            with(defendant.getLegalEntityOrganisation(), defendantIn.getLegalEntityDefendant().getOrganisation(), this::setOrganisation);
            with(defendant.getPersonDefendant(), defendantIn.getPersonDefendant(),
                    (personDefendant, personDefendant2) -> {
                        if (nonNull(personDefendant)) {
                            personDefendant.setAliases(String.join(", ", personDefendant2.getAliases()));
                            personDefendant.setArrestSummonsNumber(personDefendant2.getArrestSummonsNumber());
                            personDefendant.setBailStatus(personDefendant2.getBailStatus());
                            personDefendant.setCustodyTimeLimit(personDefendant2.getCustodyTimeLimit());
                            personDefendant.setDriverNumber(personDefendant2.getDriverNumber());
                            personDefendant.setEmployerPayrollReference(personDefendant2.getEmployerPayrollReference());
                            personDefendant.setObservedEthnicityCode(personDefendant2.getObservedEthnicityCode());
                            personDefendant.setObservedEthnicityId(personDefendant2.getObservedEthnicityId());
                            personDefendant.setPerceivedBirthYear(personDefendant2.getPerceivedBirthYear());
                            personDefendant.setPncId(personDefendant2.getPncId());
                            personDefendant.setSelfDefinedEthnicityCode(personDefendant2.getSelfDefinedEthnicityCode());
                            personDefendant.setSelfDefinedEthnicityId(personDefendant2.getSelfDefinedEthnicityId());
                            with(personDefendant.getEmployerOrganisation(), personDefendant2.getEmployerOrganisation(), this::setOrganisation);
                            with(personDefendant.getPersonDetails(), personDefendant2.getPersonDetails(), this::setPerson);
                        }
                    });

            defendant.getAssociatedPersons().clear();

            defendant.getAssociatedPersons().addAll(
                    defendantIn.getAssociatedPersons().stream()
                            .map(associatedPerson -> setAssociatedPerson(hearingId, defendant, associatedPerson))
                            .collect(Collectors.toList()));

            defendantRepository.save(defendant);
        }
    }

    private AssociatedPerson setAssociatedPerson(final UUID hearingId, final Defendant defendant, final uk.gov.justice.json.schemas.core.AssociatedPerson person) {
        if (nonNull(person)) {
            final AssociatedPerson associatedPerson = new AssociatedPerson();
            associatedPerson.setPerson(with(getDefaultPerson(), person.getPerson(), this::setPerson));
            associatedPerson.setId(new HearingSnapshotKey(randomUUID(), hearingId));
            associatedPerson.setRole(person.getRole());
            associatedPerson.setDefendant(defendant);
            associatedPerson.setDefendantId(defendant.getId().getId());
            return associatedPerson;
        }
        return null;
    }

    private Person getDefaultPerson() {
        final Person person = new Person();
        person.setContact(new Contact());
        person.setAddress(new Address());
        return person;
    }

    private void setPerson(final Person personDetails, final uk.gov.justice.json.schemas.core.Person personDetails2) {
        personDetails.setAdditionalNationalityCode(personDetails2.getAdditionalNationalityCode());
        personDetails.setAdditionalNationalityId(personDetails2.getAdditionalNationalityId());
        personDetails.setDateOfBirth(personDetails2.getDateOfBirth());
        personDetails.setDisabilityStatus(personDetails2.getDisabilityStatus());
        personDetails.setDocumentationLanguageNeeds(personDetails2.getDocumentationLanguageNeeds());
        personDetails.setEthnicity(personDetails2.getEthnicity());
        personDetails.setEthnicityId(personDetails2.getEthnicityId());
        personDetails.setFirstName(personDetails2.getFirstName());
        personDetails.setGender(personDetails2.getGender());
        personDetails.setInterpreterLanguageNeeds(personDetails2.getInterpreterLanguageNeeds());
        personDetails.setLastName(personDetails2.getLastName());
        personDetails.setMiddleName(personDetails2.getMiddleName());
        personDetails.setNationalInsuranceNumber(personDetails2.getNationalInsuranceNumber());
        personDetails.setNationalityCode(personDetails2.getNationalityCode());
        personDetails.setNationalityId(personDetails2.getNationalityId());
        personDetails.setOccupation(personDetails2.getOccupation());
        personDetails.setOccupationCode(personDetails2.getOccupationCode());
        personDetails.setSpecificRequirements(personDetails2.getSpecificRequirements());
        personDetails.setTitle(personDetails2.getTitle());
        with(personDetails.getAddress(), personDetails2.getAddress(), this::setAddress);
        with(personDetails.getContact(), personDetails2.getContact(), this::setContact);
    }

    private void setContact(final Contact contact, final ContactNumber contact1) {
        if (nonNull(contact)) {
            contact.setFax(contact1.getFax());
            contact.setHome(contact1.getHome());
            contact.setMobile(contact1.getMobile());
            contact.setPrimaryEmail(contact1.getPrimaryEmail());
            contact.setSecondaryEmail(contact1.getSecondaryEmail());
            contact.setWork(contact1.getWork());
        }
    }

    private void setOrganisation(final Organisation employerOrganisation, final uk.gov.justice.json.schemas.core.Organisation organisation) {
        if (nonNull(employerOrganisation)) {
            employerOrganisation.setId(organisation.getId());
            employerOrganisation.setIncorporationNumber(organisation.getIncorporationNumber());
            employerOrganisation.setName(organisation.getName());
            employerOrganisation.setRegisteredCharityNumber(organisation.getRegisteredCharityNumber());
            with(employerOrganisation.getAddress(), organisation.getAddress(), this::setAddress);
        }
    }

    private void setAddress(final Address address, final uk.gov.justice.json.schemas.core.Address address2) {
        if (nonNull(address)) {
            address.setAddress1(address2.getAddress1());
            address.setAddress2(address2.getAddress2());
            address.setAddress3(address2.getAddress3());
            address.setAddress4(address2.getAddress4());
            address.setAddress5(address2.getAddress5());
            address.setPostCode(address2.getPostcode());
        }
    }
}