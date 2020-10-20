package uk.gov.moj.cpp.hearing.event.listener;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.*;
import uk.gov.moj.cpp.hearing.mapping.HearingJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.PleaJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.ProsecutionCaseJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.VerdictJPAMapper;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Offence;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;
import uk.gov.moj.cpp.hearing.repository.OffenceRepository;
import uk.gov.moj.cpp.hearing.repository.ProsecutionCaseRepository;

import javax.inject.Inject;
import javax.json.JsonObject;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

@SuppressWarnings({"squid:S2201"})
@ServiceComponent(EVENT_LISTENER)
public class InitiateHearingEventListener {
    private static final String GUILTY = "GUILTY";
    private static final Logger LOGGER = LoggerFactory.getLogger(InitiateHearingEventListener.class.getName());

    @Inject
    private HearingRepository hearingRepository;

    @Inject
    private ProsecutionCaseRepository prosecutionCaseRepository;

    @Inject
    private OffenceRepository offenceRepository;

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Inject
    private HearingJPAMapper hearingJPAMapper;

    @Inject
    private ProsecutionCaseJPAMapper prosecutionCaseJPAMapper;

    @Inject
    private PleaJPAMapper pleaJPAMapper;

    @Inject
    private VerdictJPAMapper verdictJPAMapper;

    @Transactional
    @Handles("hearing.events.initiated")
    public void newHearingInitiated(final JsonEnvelope event) {

        final JsonObject payload = event.payloadAsJsonObject();

        LOGGER.debug("hearing.initiated event received {}", payload);

        final HearingInitiated initiated = jsonObjectToObjectConverter.convert(payload, HearingInitiated.class);

        final Hearing hearingEntity = hearingJPAMapper.toJPA(initiated.getHearing());
        getOffencesForHearing(hearingEntity)
                .forEach(x -> updateOffenceForShadowListedStatus(initiated.getHearing().getShadowListedOffences(), x));

        hearingRepository.save(hearingEntity);
    }

    private List<Offence> getOffencesForHearing(final Hearing hearingEntity) {
        return ofNullable(hearingEntity.getProsecutionCases()).orElse(new HashSet<>())
                .stream()
                .flatMap(x -> ofNullable(x.getDefendants()).orElse(new HashSet<>()).stream())
                .flatMap(def -> ofNullable(def.getOffences()).orElse(new HashSet<>()).stream())
                .collect(Collectors.toList());
    }

    private void updateOffenceForShadowListedStatus(final List<UUID> shadowListedOffences, final Offence offence) {
        ofNullable(shadowListedOffences).orElseGet(() -> new ArrayList<>())
                .stream()
                .filter(x -> offence.getId().getId().equals(x))
                .findFirst()
                .ifPresent(x -> offence.setShadowListed(true));


    }

    @Transactional
    @Handles("hearing.events.hearing-extended")
    public void hearingExtended(final JsonEnvelope event) {

        final JsonObject payload = event.payloadAsJsonObject();

        LOGGER.debug("hearing.hearingExtended event received {}", payload);

        final HearingExtended hearingExtended = jsonObjectToObjectConverter.convert(payload, HearingExtended.class);

        final Hearing hearingEntity = hearingRepository.findBy(hearingExtended.getHearingId());

        if (nonNull(hearingExtended.getCourtApplication())) {
            final String courtApplicationsJson = hearingJPAMapper.addOrUpdateCourtApplication(hearingEntity.getCourtApplicationsJson(), hearingExtended.getCourtApplication());
            hearingEntity.setCourtApplicationsJson(courtApplicationsJson);
            hearingRepository.save(hearingEntity);
        }
        if (CollectionUtils.isNotEmpty(hearingExtended.getProsecutionCases())) {
            hearingExtended.getProsecutionCases()
                    .forEach(p -> {
                                final ProsecutionCase prosecutionCase = prosecutionCaseJPAMapper.toJPA(hearingEntity, p);
                                getOffencesForProsecutionCase(prosecutionCase).forEach(x -> updateOffenceForShadowListedStatus(hearingExtended.getShadowListedOffences(), x));
                                prosecutionCaseRepository.save(prosecutionCase);
                            }
                    );
        }

    }

    private List<Offence> getOffencesForProsecutionCase(final ProsecutionCase prosecutionCase) {
        return prosecutionCase.getDefendants()
                .stream()
                .flatMap(def -> ofNullable(def.getOffences()).orElse(new HashSet<>()).stream())
                .collect(Collectors.toList());
    }

    @Transactional
    @Handles("hearing.events.application-detail-changed")
    public void hearingApplicationDetailChanged(final JsonEnvelope event) {

        final JsonObject payload = event.payloadAsJsonObject();

        LOGGER.debug("hearing.events.application-detail-changed event received {}", payload);

        final ApplicationDetailChanged applicationDetailChanged = jsonObjectToObjectConverter.convert(payload, ApplicationDetailChanged.class);

        final Hearing hearingEntity = hearingRepository.findBy(applicationDetailChanged.getHearingId());

        final String courtApplicationsJson = hearingJPAMapper.addOrUpdateCourtApplication(hearingEntity.getCourtApplicationsJson(), applicationDetailChanged.getCourtApplication());

        hearingEntity.setCourtApplicationsJson(courtApplicationsJson);

        hearingRepository.save(hearingEntity);
    }

    @Transactional
    @Handles("hearing.conviction-date-added")
    public void convictionDateUpdated(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.conviction-date-added event received {}", event.toObfuscatedDebugString());
        }
        final ConvictionDateAdded convictionDateAdded = jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), ConvictionDateAdded.class);
        save(convictionDateAdded.getOffenceId(), convictionDateAdded.getHearingId(), (o) -> o.setConvictionDate(convictionDateAdded.getConvictionDate()));
    }

    @Transactional
    @Handles("hearing.conviction-date-removed")
    public void convictionDateRemoved(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.conviction-date-removed event received {}", event.toObfuscatedDebugString());
        }
        final ConvictionDateRemoved convictionDateRemoved = jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), ConvictionDateRemoved.class);
        save(convictionDateRemoved.getOffenceId(), convictionDateRemoved.getHearingId(), (o) -> o.setConvictionDate(null));
    }

    @Transactional
    @Handles("hearing.events.inherited-plea")
    public void hearingInitiatedPleaData(final JsonEnvelope envelop) {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.events.inherited-plea event received {}", envelop.toObfuscatedDebugString());
        }

        final InheritedPlea event = jsonObjectToObjectConverter.convert(envelop.payloadAsJsonObject(), InheritedPlea.class);

        final Offence offence = offenceRepository.findBy(new HearingSnapshotKey(event.getPlea().getOffenceId(), event.getHearingId()));

        if (nonNull(offence)) {

            final boolean shouldSetPlea = isNull(offence.getPlea()) || isPleaInherited(event, offence);

            if (shouldSetPlea) {
                offence.setPlea(pleaJPAMapper.toJPA(event.getPlea()));
                offence.setConvictionDate(GUILTY.equals(event.getPlea().getPleaValue()) ? event.getPlea().getPleaDate() : null);
                offenceRepository.save(offence);
            }
        }
    }

    @Transactional
    @Handles("hearing.events.inherited-verdict-added")
    public void hearingInitiatedVerdictData(final JsonEnvelope envelop) {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.events.inherited-plea event received {}", envelop.toObfuscatedDebugString());
        }

        final InheritedVerdictAdded event = jsonObjectToObjectConverter.convert(envelop.payloadAsJsonObject(), InheritedVerdictAdded.class);

        final Offence offence = offenceRepository.findBy(new HearingSnapshotKey(event.getVerdict().getOffenceId(), event.getHearingId()));

        if (nonNull(offence)) {

            final boolean shouldSetVerdict = isNull(offence.getVerdict()) || isVerdictInherited(event, offence);

            if (shouldSetVerdict) {
                offence.setVerdict(verdictJPAMapper.toJPA(event.getVerdict()));
                offenceRepository.save(offence);
            }
        }
    }

    private boolean isPleaInherited(InheritedPlea event, Offence offence) {
        return !event.getHearingId().equals(offence.getPlea().getOriginatingHearingId());
    }

    private boolean isVerdictInherited(InheritedVerdictAdded event, Offence offence) {
        return !event.getHearingId().equals(offence.getVerdict().getOriginatingHearingId());
    }

    private void save(final UUID offenceId, final UUID hearingId, final Consumer<Offence> consumer) {
        ofNullable(offenceRepository.findBy(new HearingSnapshotKey(offenceId, hearingId))).map(o -> {
            consumer.accept(o);
            offenceRepository.saveAndFlush(o);
            return o;
        }).orElseThrow(() -> new RuntimeException("Offence id is not found on hearing id: " + hearingId));
    }
}
