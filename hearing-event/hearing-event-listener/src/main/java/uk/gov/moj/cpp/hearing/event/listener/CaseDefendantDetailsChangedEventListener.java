package uk.gov.moj.cpp.hearing.event.listener;

import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;
import static uk.gov.moj.cpp.hearing.Utilities.with;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.DefendantDetailsUpdated;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Address;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.repository.DefendantRepository;

import javax.inject.Inject;
import javax.transaction.Transactional;

@SuppressWarnings({"squid:S1188", "squid:CommentedOutCodeLine"})
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

        final Defendant defendant = defendantRepository.findBy(new HearingSnapshotKey(defendantDetailsToBeUpdated.getDefendant().getId(), defendantDetailsToBeUpdated.getHearingId()));

        final uk.gov.moj.cpp.hearing.command.defendant.Defendant defendantIn = defendantDetailsToBeUpdated.getDefendant();

        final uk.gov.moj.cpp.hearing.command.defendant.Person person = defendantIn.getPerson();

        with(defendant.getPersonDefendant().getPersonDetails(), defendantDetailsToBeUpdated.getDefendant().getPerson(), (personDetails, inputPerson) -> {
            personDetails.setFirstName(inputPerson.getFirstName());
            personDetails.setLastName(inputPerson.getLastName());
            personDetails.setDateOfBirth(inputPerson.getDateOfBirth());
            personDetails.setGender(inputPerson.getGender());
            personDetails.setNationalityCode(inputPerson.getNationality());

            with(personDetails.getContact(), contact -> {
                contact.setFax(inputPerson.getFax());
                contact.setHome(inputPerson.getHomeTelephone());
                contact.setMobile(inputPerson.getMobile());
                contact.setPrimaryEmail(inputPerson.getEmail());
                contact.setWork(inputPerson.getWorkTelephone());
            });

            if (person.getAddress() != null) {
                personDetails.setAddress(with(new Address(), person.getAddress(), (output, input) -> {
                    output.setAddress1(input.getAddress1());
                    output.setAddress2(input.getAddress2());
                    output.setAddress3(input.getAddress3());
                    output.setAddress4(input.getAddress4());
                    //output.setAddress5(input.getAddress5());
                    output.setPostCode(input.getPostCode());
                }));
            }
        });

        with(defendant.getPersonDefendant(), defendantDetailsToBeUpdated.getDefendant(), (output, input) ->{
            output.setBailStatus(input.getBailStatus());
            output.setCustodyTimeLimit(input.getCustodyTimeLimitDate());
        });

        if (defendantIn.getInterpreter() != null){
            defendant.getPersonDefendant().getPersonDetails().setInterpreterLanguageNeeds(defendantIn.getInterpreter().getLanguage());
        }

        defendantRepository.save(defendant);
    }
}