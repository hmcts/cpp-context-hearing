package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.Objects.nonNull;
import static java.util.UUID.randomUUID;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;
import static uk.gov.moj.cpp.hearing.Utilities.with;

import uk.gov.justice.core.courts.ContactNumber;
import uk.gov.justice.core.courts.LegalEntityDefendant;
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

import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.transaction.Transactional;

@ServiceComponent(EVENT_LISTENER)
public class CaseDefendantDetailsUpdatedEventListener {

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
            with(defendant.getLegalEntityOrganisation(), getLegalEntityDefendantOrganisation(defendantIn.getLegalEntityDefendant()), this::setOrganisation);
            with(defendant.getPersonDefendant(), defendantIn.getPersonDefendant(),
                    (personDefendantJpa, personDefendantPojo) -> {
                        if (nonNull(personDefendantJpa) && nonNull(personDefendantPojo)) {
                            personDefendantJpa.setArrestSummonsNumber(personDefendantPojo.getArrestSummonsNumber());
                            personDefendantJpa.setBailStatus(nonNull(personDefendantPojo.getBailStatus()) ? personDefendantPojo.getBailStatus().name() : null);
                            personDefendantJpa.setCustodyTimeLimit(personDefendantPojo.getCustodyTimeLimit());
                            personDefendantJpa.setDriverNumber(personDefendantPojo.getDriverNumber());
                            personDefendantJpa.setEmployerPayrollReference(personDefendantPojo.getEmployerPayrollReference());
                            personDefendantJpa.setPerceivedBirthYear(personDefendantPojo.getPerceivedBirthYear());
                            with(personDefendantJpa.getEmployerOrganisation(), personDefendantPojo.getEmployerOrganisation(), this::setOrganisation);
                            with(personDefendantJpa.getPersonDetails(), personDefendantPojo.getPersonDetails(), this::setPerson);
                        }
                    });

            if (defendantIn.getAssociatedPersons() != null) {
                defendant.getAssociatedPersons().clear();
                defendant.getAssociatedPersons().addAll(
                        defendantIn.getAssociatedPersons().stream()
                                .map(associatedPerson -> setAssociatedPerson(hearingId, defendant, associatedPerson))
                                .collect(Collectors.toList()));
            }

            defendantRepository.save(defendant);
        }
    }

    private uk.gov.justice.core.courts.Organisation getLegalEntityDefendantOrganisation(LegalEntityDefendant legalEntityDefendant) {
        return nonNull(legalEntityDefendant) ? legalEntityDefendant.getOrganisation() : null;
    }

    private AssociatedPerson setAssociatedPerson(final UUID hearingId, final Defendant defendant, final uk.gov.justice.core.courts.AssociatedPerson person) {
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

    private void setPerson(final Person personDetailsJpa, final uk.gov.justice.core.courts.Person personDetailsPojo) {
        personDetailsJpa.setAdditionalNationalityCode(personDetailsPojo.getAdditionalNationalityCode());
        personDetailsJpa.setAdditionalNationalityId(personDetailsPojo.getAdditionalNationalityId());
        personDetailsJpa.setDateOfBirth(personDetailsPojo.getDateOfBirth());
        personDetailsJpa.setDisabilityStatus(personDetailsPojo.getDisabilityStatus());
        personDetailsJpa.setDocumentationLanguageNeeds(personDetailsPojo.getDocumentationLanguageNeeds());
        personDetailsJpa.setFirstName(personDetailsPojo.getFirstName());
        personDetailsJpa.setGender(personDetailsPojo.getGender());
        personDetailsJpa.setInterpreterLanguageNeeds(personDetailsPojo.getInterpreterLanguageNeeds());
        personDetailsJpa.setLastName(personDetailsPojo.getLastName());
        personDetailsJpa.setMiddleName(personDetailsPojo.getMiddleName());
        personDetailsJpa.setNationalInsuranceNumber(personDetailsPojo.getNationalInsuranceNumber());
        personDetailsJpa.setNationalityCode(personDetailsPojo.getNationalityCode());
        personDetailsJpa.setNationalityId(personDetailsPojo.getNationalityId());
        personDetailsJpa.setOccupation(personDetailsPojo.getOccupation());
        personDetailsJpa.setOccupationCode(personDetailsPojo.getOccupationCode());
        personDetailsJpa.setSpecificRequirements(personDetailsPojo.getSpecificRequirements());
        personDetailsJpa.setTitle(personDetailsPojo.getTitle());
        with(personDetailsJpa.getAddress(), personDetailsPojo.getAddress(), this::setAddress);
        with(personDetailsJpa.getContact(), personDetailsPojo.getContact(), this::setContact);
    }

    private void setContact(final Contact contactJpa, final ContactNumber contactPojo) {
        if (nonNull(contactJpa) && nonNull(contactPojo)) {
            contactJpa.setFax(contactPojo.getFax());
            contactJpa.setHome(contactPojo.getHome());
            contactJpa.setMobile(contactPojo.getMobile());
            contactJpa.setPrimaryEmail(contactPojo.getPrimaryEmail());
            contactJpa.setSecondaryEmail(contactPojo.getSecondaryEmail());
            contactJpa.setWork(contactPojo.getWork());
        }
    }

    private void setOrganisation(final Organisation organisationJpa, uk.gov.justice.core.courts.Organisation organisationPojo) {
        if (nonNull(organisationJpa) && nonNull(organisationPojo)) {
            organisationJpa.setId(randomUUID());
            organisationJpa.setIncorporationNumber(organisationPojo.getIncorporationNumber());
            organisationJpa.setName(organisationPojo.getName());
            organisationJpa.setRegisteredCharityNumber(organisationPojo.getRegisteredCharityNumber());
            with(organisationJpa.getContact(), organisationPojo.getContact(), this::setContact);
            with(organisationJpa.getAddress(), organisationPojo.getAddress(), this::setAddress);
        }
    }

    private void setAddress(final Address addressJpa, final uk.gov.justice.core.courts.Address addressPojo) {
        if (nonNull(addressJpa) && nonNull(addressPojo)) {
            addressJpa.setAddress1(addressPojo.getAddress1());
            addressJpa.setAddress2(addressPojo.getAddress2());
            addressJpa.setAddress3(addressPojo.getAddress3());
            addressJpa.setAddress4(addressPojo.getAddress4());
            addressJpa.setAddress5(addressPojo.getAddress5());
            addressJpa.setPostCode(addressPojo.getPostcode());
        }
    }
}