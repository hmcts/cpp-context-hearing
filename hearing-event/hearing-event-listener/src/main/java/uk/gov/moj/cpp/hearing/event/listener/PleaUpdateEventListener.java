package uk.gov.moj.cpp.hearing.event.listener;

import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.PleaUpsert;
import uk.gov.moj.cpp.hearing.persist.entity.ha.DelegatedPowers;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Offence;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Plea;
import uk.gov.moj.cpp.hearing.repository.OffenceRepository;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;

@SuppressWarnings({"squid:CommentedOutCodeLine"})
@ServiceComponent(EVENT_LISTENER)
public class PleaUpdateEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(PleaUpdateEventListener.class);

    private final OffenceRepository offenceRepository;
    private final JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Inject
    public PleaUpdateEventListener(final OffenceRepository offenceRepository,
                                   final JsonObjectToObjectConverter jsonObjectToObjectConverter) {
        this.offenceRepository = offenceRepository;
        this.jsonObjectToObjectConverter = jsonObjectToObjectConverter;
    }

    @Transactional
    @Handles("hearing.hearing-offence-plea-updated")
    public void offencePleaUpdated(final JsonEnvelope envelope) {
        LOGGER.debug("hearing.hearing-offence-plea-updated event received {}", envelope.payloadAsJsonObject());

        final PleaUpsert event = convertToObject(envelope);

        final Offence offence = offenceRepository.findBy(new HearingSnapshotKey(event.getOffenceId(), event.getHearingId()));

        if (offence != null) {
            transferPleaData(event, offence);
            offenceRepository.save(offence);

            final List<Offence> inheritedOffences = offenceRepository.findByOffenceIdAndOriginatingHearingId(event.getOffenceId(), event.getHearingId());
            for (Offence inheritedOffence : inheritedOffences) {
                transferPleaData(event, inheritedOffence);
                offenceRepository.save(inheritedOffence);
            }
        }
    }

    private void transferPleaData(final PleaUpsert event, final Offence offence) {
        offence.setPlea(buildPlea(event));
        if (event.getDelegatedPowers() != null) {
            offence.getPlea().setDelegatedPowers(buildDelegatedPowers(event.getDelegatedPowers()));
        } else {
/*            offence.setDelegatedPowersUserId(null);
            offence.setDelegatedPowersFirstName(null);
            offence.setDelegatedPowersLastName(null);*/
        }
    }

    private DelegatedPowers buildDelegatedPowers(uk.gov.justice.json.schemas.core.DelegatedPowers delegatedPowers) {
        final DelegatedPowers delegatedPowersDomain = new DelegatedPowers();
        delegatedPowersDomain.setDelegatedPowersFirstName(delegatedPowers.getFirstName());
        delegatedPowersDomain.setDelegatedPowersLastName(delegatedPowers.getLastName());
        delegatedPowersDomain.setDelegatedPowersUserId(delegatedPowers.getUserId());
        return delegatedPowersDomain;
    }

    private Plea buildPlea(PleaUpsert event) {
        final Plea plea = new Plea();
        plea.setPleaDate(event.getPleaDate());
        plea.setPleaValue(event.getValue());
        return plea;
    }

    private PleaUpsert convertToObject(final JsonEnvelope envelop) {
        return this.jsonObjectToObjectConverter.convert(envelop.payloadAsJsonObject(), PleaUpsert.class);
    }
}
