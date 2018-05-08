package uk.gov.moj.cpp.hearing.event.listener;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.DefendantDetailsUpdated;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Address;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.DefendantCase;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.repository.DefendantRepository;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.function.Predicate;

import static java.util.Optional.ofNullable;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

@ServiceComponent(EVENT_LISTENER)
public class CaseDefendantChangeEventListener {

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

        final uk.gov.moj.cpp.hearing.command.defendant.Address addressIn = defendantIn.getAddress();

        defendant.setFirstName(defendantIn.getFirstName());

        defendant.setLastName(defendantIn.getLastName());

        defendant.setDateOfBirth(defendantIn.getDateOfBirth());

        defendant.setGender(defendantIn.getGender());

        defendant.setNationality(defendantIn.getNationality());

        defendant.setDefenceSolicitorFirm(defendantIn.getDefenceOrganisation());

        final Address address = ofNullable(addressIn).map(a -> Address.builder()
                .withAddress1(addressIn.getAddress1())
                .withAddress2(addressIn.getAddress2())
                .withAddress3(addressIn.getAddress3())
                .withAddress4(addressIn.getAddress4())
                .withPostCode(addressIn.getPostCode())
                .build())
                .orElse(null);

        defendant.setAddress(address);

        defendant.getDefendantCases().stream().filter(dc -> getDefendantCasePredicate(defendantDetailsToBeUpdated).test(dc)).forEach(dc -> {
            dc.setBailStatus(defendantDetailsToBeUpdated.getDefendant().getBailStatus());
            dc.setCustodyTimeLimitDate(defendantDetailsToBeUpdated.getDefendant().getCustodyTimeLimitDate().toLocalDate());
        });

        defendant.setInterpreterLanguage(defendantDetailsToBeUpdated.getDefendant().getInterpreter().getLanguage());

        defendantRepository.saveAndFlush(defendant);
    }

    private Predicate<DefendantCase> getDefendantCasePredicate(DefendantDetailsUpdated defendantDetailsToBeUpdated) {

        final Predicate<DefendantCase> caseIdPredicate = p -> p.getId().getCaseId().equals(defendantDetailsToBeUpdated.getCaseId());

        final Predicate<DefendantCase> hearingIdPredicate = p -> p.getId().getHearingId().equals(defendantDetailsToBeUpdated.getHearingId());

        final Predicate<DefendantCase> defendantPredicate = p -> p.getId().getDefendantId().equals(defendantDetailsToBeUpdated.getDefendant().getId());

        return caseIdPredicate.and(hearingIdPredicate).and(defendantPredicate);
    }
}
