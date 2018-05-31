package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.Optional.ofNullable;
import static java.util.UUID.fromString;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.initiate.Interpreter;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateAdded;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateRemoved;
import uk.gov.moj.cpp.hearing.domain.event.InheritedPlea;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Address;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.DefendantCase;
import uk.gov.moj.cpp.hearing.persist.entity.ha.DefendantCaseKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDate;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Judge;
import uk.gov.moj.cpp.hearing.persist.entity.ha.LegalCase;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Offence;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Witness;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;
import uk.gov.moj.cpp.hearing.repository.LegalCaseRepository;
import uk.gov.moj.cpp.hearing.repository.OffenceRepository;
import uk.gov.moj.cpp.hearing.repository.WitnessRepository;

@SuppressWarnings({"squid:S2201"})
@ServiceComponent(EVENT_LISTENER)
public class InitiateHearingEventListener {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(InitiateHearingEventListener.class.getName());
    @Inject
    private HearingRepository hearingRepository;

    @Inject
    private LegalCaseRepository legalCaseRepository;

    @Inject
    private OffenceRepository offenceRepository;

    @Inject
    private WitnessRepository witnessRepository;


    private static final String FIELD_GENERIC_ID = "id";
    private static final String FIELD_HEARING_ID = "hearingId";
    public static final String FIELD_CLASSIFICATION = "classification";
    public static final String FIELD_TYPE = "type";
    public static final String FIELD_TITLE = "title";
    public static final String FIELD_FIRST_NAME = "firstName";
    public static final String FIELD_LAST_NAME = "lastName";

    @Inject
    JsonObjectToObjectConverter jsonObjectToObjectConverter;

    private Offence.Builder translateOffence(final UUID hearingId, final uk.gov.moj.cpp.hearing.command.initiate.Offence offenceIn, final Map<UUID, LegalCase> id2Case) {
        return Offence.builder()
                .withId(new HearingSnapshotKey(offenceIn.getId(), hearingId))
                .withConvictionDate(offenceIn.getConvictionDate())
                .withCase(id2Case.get(offenceIn.getCaseId()))
                .withCode(offenceIn.getOffenceCode())
                .withCount(offenceIn.getCount())
                .withWording(offenceIn.getWording())
                .withTitle(offenceIn.getTitle())
                .withLegislation(offenceIn.getLegislation())
                .withStartDate(offenceIn.getStartDate())
                .withEndDate(offenceIn.getEndDate());
    }

    private Defendant.Builder translateDefendant(final UUID hearingId, final uk.gov.moj.cpp.hearing.command.initiate.Defendant defendantIn) {
        return Defendant.builder()
                .withAddress(ofNullable(defendantIn.getAddress())
                        .map(a -> Address.builder()
                                .withAddress1(a.getAddress1())
                                .withAddress2(a.getAddress2())
                                .withAddress3(a.getAddress3())
                                .withAddress4(a.getAddress4())
                                .withPostCode(a.getPostCode())
                                .build()
                        )
                        .orElse(null)
                )
                .withDateOfBirth(defendantIn.getDateOfBirth())
                .withNationality(defendantIn.getNationality())
                .withFirstName(defendantIn.getFirstName())
                .withLastName(defendantIn.getLastName())
                .withGender(defendantIn.getGender())
                .withInterpreterLanguage(ofNullable(defendantIn.getInterpreter()).map(Interpreter::getLanguage).orElse(null))
                .withDefenceSolicitorFirm(defendantIn.getDefenceOrganisation())
                .withId(new HearingSnapshotKey(defendantIn.getId(), hearingId))
                .withDefendantCases(defendantIn.getDefendantCases().stream()
                        .map(dc -> DefendantCase.builder()
                                .withId(new DefendantCaseKey(hearingId, dc.getCaseId(), defendantIn.getId()))
                                .withBailStatus(dc.getBailStatus())
                                .withCustodyTimeLimitDate(dc.getCustodyTimeLimitDate())
                                .build()
                        )
                        .collect(Collectors.toList())
                );
    }

    private Witness.Builder translateWitness(final UUID hearingId, final uk.gov.moj.cpp.hearing.command.initiate.Witness witnessIn, final LegalCase id2Case) {
        return Witness.builder()
                .withLegalCase(id2Case)
                .withType(witnessIn.getType())
                .withClassification(witnessIn.getClassification())
                .withPersonId(witnessIn.getPersonId())
                .withTitle(witnessIn.getTitle())
                .withFirstName(witnessIn.getFirstName())
                .withLastName(witnessIn.getLastName())
                .withGender(witnessIn.getGender())
                .withDateOfBirth(witnessIn.getDateOfBirth())
                .withNationality(witnessIn.getNationality())
                .withHomeTelephone(witnessIn.getHomeTelephone())
                .withWorkTelephone(witnessIn.getWorkTelephone())
                .withEmail(witnessIn.getEmail())
                .withFax(witnessIn.getFax())
                .withMobileTelephone(witnessIn.getMobile())
                .withId(new HearingSnapshotKey(witnessIn.getId(), hearingId));
    }

    @Transactional
    @Handles("hearing.events.initiated")
    public void newHearingInitiated(final JsonEnvelope event) {
        final JsonObject payload = event.payloadAsJsonObject();
        LOGGER.debug("hearing.initiated event received {}", payload);

        final InitiateHearingCommand initiated = jsonObjectToObjectConverter.convert(payload, InitiateHearingCommand.class);
        final uk.gov.moj.cpp.hearing.command.initiate.Hearing hearing = initiated.getHearing();


        final Map<UUID, LegalCase> id2Case = new HashMap<>();
        initiated.getCases().forEach(
                (caseIn) -> {
                    LegalCase legalCase = legalCaseRepository.findById(caseIn.getCaseId());
                    if (null == legalCase) {
                        legalCase = LegalCase.builder().withId(caseIn.getCaseId()).withCaseurn(caseIn.getUrn()).build();
                        legalCaseRepository.saveAndFlush(legalCase);
                    }
                    id2Case.put(legalCase.getId(), legalCase);
                }
        );
        final Hearing hearingEntity = Hearing.builder()
                .withId(hearing.getId())
                .withHearingType(hearing.getType())
                .withCourtCentreId(hearing.getCourtCentreId())
                .withCourtCentreName(hearing.getCourtCentreName())
                .withRoomId(hearing.getCourtRoomId())
                .withRoomName(hearing.getCourtRoomName())
                .withHearingDays(hearing.getHearingDays().stream()
                        .map(zdt -> HearingDate.builder()
                                .withDateTime(zdt)
                                .withDate(zdt.toLocalDate())
                                .withId(new HearingSnapshotKey(UUID.randomUUID(), hearing.getId()))
                                .build())
                        .collect(Collectors.toList()))
                .withDefendants(hearing.getDefendants().stream()
                        .map(defendantIn -> {
                            final Defendant defendant = translateDefendant(hearing.getId(), defendantIn).build();
                            defendant.setOffences(new ArrayList<>());
                            defendantIn.getOffences().forEach(
                                    offenceIn -> {
                                        final Offence offence = translateOffence(hearing.getId(), offenceIn, id2Case).withDefendant(defendant).build();
                                        defendant.getOffences().add(offence);
                                    }
                            );
                            return defendant;
                        })
                        .collect(Collectors.toList()))
                .withWitnesses((hearing.getWitnesses() == null ? new ArrayList<>() : hearing.getWitnesses().stream()
                        .map(witnessIn -> translateWitness(hearing.getId(), witnessIn, id2Case.get(witnessIn.getCaseId())).build()
                        ).collect((Collectors.toList()))))
                .withJudge(Judge.builder()
                        .withId(new HearingSnapshotKey(hearing.getJudge().getId(), hearing.getId()))
                        .withFirstName(hearing.getJudge().getFirstName())
                        .withLastName(hearing.getJudge().getLastName())
                        .withTitle(hearing.getJudge().getTitle()))
                .build();
        this.hearingRepository.save(hearingEntity);
    }

    @Transactional
    @Handles("hearing.conviction-date-added")
    public void convictionDateUpdated(final JsonEnvelope event) {
        LOGGER.debug("hearing.conviction-date-added event received {}", event.payloadAsJsonObject());
        final ConvictionDateAdded convictionDateAdded = jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), ConvictionDateAdded.class);
        save(convictionDateAdded.getOffenceId(), convictionDateAdded.getHearingId(), (o) -> o.setConvictionDate(convictionDateAdded.getConvictionDate()));
    }

    @Transactional
    @Handles("hearing.conviction-date-removed")
    public void convictionDateRemoved(final JsonEnvelope event) {
        LOGGER.debug("hearing.conviction-date-removed event received {}", event.payloadAsJsonObject());
        final ConvictionDateRemoved convictionDateRemoved = jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), ConvictionDateRemoved.class);
        save(convictionDateRemoved.getOffenceId(), convictionDateRemoved.getHearingId(), (o) -> o.setConvictionDate(null));
    }

    @Transactional
    @Handles("hearing.events.inherited-plea")
    public void hearingInitiatedPleaData(final JsonEnvelope envelop) {
        LOGGER.debug("hearing.events.inherited-plea event received {}", envelop.payloadAsJsonObject());
        final InheritedPlea event = jsonObjectToObjectConverter.convert(envelop.payloadAsJsonObject(), InheritedPlea.class);
        save(event.getOffenceId(), event.getHearingId(), (o) -> {
            o.setOriginHearingId(event.getOriginHearingId());
            o.setPleaDate(event.getPleaDate());
            o.setPleaValue(event.getValue());
            o.setConvictionDate(isGuilty(event.getValue()) ? event.getPleaDate() : null);
        });
    }

    //TODO - this event is being emitted from defendantAggregate - this is incorrect
    // There should be an event processor method that translates this into another event that arrives on the hearingAggregate.
    @Transactional
    @Handles("hearing.events.found-witnesses-for-hearing-to-inherit")
    public void initiateHearingWitnessEnriched(final JsonEnvelope event) {
        final JsonObject payload = event.payloadAsJsonObject();
        LOGGER.debug("hearing.initiate-hearing-defence-witness-enriched listener payload {} ",
                payload);
        final UUID id = fromString(payload.getString(FIELD_GENERIC_ID));
        final UUID hearingId = fromString(payload.getString(FIELD_HEARING_ID));
        final String type = payload.getString(FIELD_TYPE);
        final String classification = payload.getString(FIELD_CLASSIFICATION);
        final String title =
                payload.containsKey(FIELD_TITLE) ? payload.getString(FIELD_TITLE) : null;
        final String firstName = payload.getString(FIELD_FIRST_NAME);
        final String lastName = payload.getString(FIELD_LAST_NAME);
        final UUID defendantId = fromString(payload.getString("defendantId"));

        final Hearing hearing = hearingRepository.findById(hearingId);
        if (hearing != null) {
            final HearingSnapshotKey witnessKey = new HearingSnapshotKey(id, hearingId);

            final Witness witness = ofNullable(witnessRepository.findBy(witnessKey))
                    .orElseGet(() -> {
                        LOGGER.info("Witness {} not found for hearing id  {}  , creating new witness", id,
                                hearingId);
                        return Witness.builder()
                                .withId(witnessKey)
                                .withHearing(hearing)
                                .withType(type)
                                .withTitle(title)
                                .withFirstName(firstName)
                                .withLastName(lastName)
                                .withClassification(classification)
                                .build();
                    });

            final Defendant defendant = hearing.getDefendants().stream()
                    .filter(d -> d.getId().getId().equals((defendantId))).findFirst()
                    .orElseThrow(() -> new RuntimeException(String.format(
                            "hearing %s witness added for unkown defendant %s ",
                            hearing.getId(), defendantId)));


            witness.getDefendants().add(defendant);
            defendant.getDefendantWitnesses().removeIf(w -> w.getId().getId().equals(witness.getId().getId()));
            defendant.getDefendantWitnesses().add(witness);

            hearingRepository.saveAndFlush(hearing);
        }
    }

    private boolean isGuilty(final String value) {
        return "GUILTY".equalsIgnoreCase(value);
    }

    private void save(final UUID offenceId, final UUID hearingId,
                      final Consumer<Offence> consumer) {
        Optional.ofNullable(offenceRepository
                .findBySnapshotKey(new HearingSnapshotKey(offenceId, hearingId))).map(o -> {
            consumer.accept(o);
            offenceRepository.saveAndFlush(o);
            return o;
        }).orElseThrow(() -> new RuntimeException(
                "Offence id is not found on hearing id: " + hearingId));
    }

    //TODO - this event handler needs to be moved into its own event listener class - this handler does not form part of the initiate hearing process.
    @Transactional
    @Handles("hearing.events.witness-added")
    public void witnessAdded(final JsonEnvelope event) {
        final JsonObject payload = event.payloadAsJsonObject();
        LOGGER.debug("hearing.events.witness-added listener payload {} ", payload);
        final UUID id = fromString(payload.getString(FIELD_GENERIC_ID));
        final UUID hearingId = fromString(payload.getString(FIELD_HEARING_ID));
        final Hearing hearing = hearingRepository.findById(hearingId);
        if (hearing != null) {
            final Witness hearingWitness = hearing.getDefendants().stream()
                            .map(d -> d.getDefendantWitnesses()).flatMap(w -> w.stream())
                            .filter(w -> w.getId().equals(new HearingSnapshotKey(id, hearingId)))
                            .findFirst().orElse(null);
            if (hearingWitness == null) {
                addWitness(event, hearing);
            } else {
                updateWitness(event, hearing, hearingWitness);
            }
        }

    }

    private void updateWitness(final JsonEnvelope event, final Hearing hearing,
                    final Witness witness) {
        final JsonObject payload = event.payloadAsJsonObject();
        final String type = payload.getString(FIELD_TYPE);
        final String classification = payload.getString(FIELD_CLASSIFICATION);
        final String title =
                        payload.containsKey(FIELD_TITLE) ? payload.getString(FIELD_TITLE) : null;
        final String firstName = payload.getString(FIELD_FIRST_NAME);
        final String lastName = payload.getString(FIELD_LAST_NAME);
        final Set<String> commandWitnessDefendantIds = payload.getJsonArray("defendantIds").stream()
                        .map(d -> ((JsonString) d).getString()).collect(Collectors.toSet());
        final Set<String> hearingWitnessDefendantId = witness.getDefendants().stream()
                        .map(d -> d.getId().getId().toString()).collect(Collectors.toSet());
        final Set<String> addedDefendants =
                        Sets.difference(commandWitnessDefendantIds, hearingWitnessDefendantId);
        final Set<String> removedDefendants =
                        Sets.difference(hearingWitnessDefendantId, commandWitnessDefendantIds);


        witness.setType(type);
        witness.setClassification(classification);
        if (title != null) {
            witness.setTitle(title);
        }
        witness.setFirstName(firstName);
        witness.setLastName(lastName);

        addedDefendants.forEach(defendantId -> {
            final Defendant defendant = hearing.getDefendants().stream()
                            .filter(d -> d.getId().getId().toString()
                                            .equals(defendantId))
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException(String.format(
                                            "hearingId %s witness added for unkown defendantId %s ",
                                            hearing.getId(), defendantId)));
            witness.getDefendants().add(defendant);
            defendant.getDefendantWitnesses().add(witness);
        });
        removedDefendants.forEach(defendantId -> {
            final Defendant defendant = hearing.getDefendants().stream()
                            .filter(d -> d.getId().getId().toString().equals(defendantId))
                            .findFirst()
                            .orElse(null);
            if (defendant != null) {
                witness.getDefendants().remove(defendant);
                defendant.getDefendantWitnesses().remove(witness);
            }
        });
        hearingRepository.save(hearing);

    }

    private void addWitness(final JsonEnvelope event, final Hearing hearing) {

        final JsonObject payload = event.payloadAsJsonObject();
        final UUID id = fromString(payload.getString(FIELD_GENERIC_ID));
        final UUID hearingId = fromString(payload.getString(FIELD_HEARING_ID));
        final String type = payload.getString(FIELD_TYPE);
        final String classification = payload.getString(FIELD_CLASSIFICATION);
        final String title =
                payload.containsKey(FIELD_TITLE) ? payload.getString(FIELD_TITLE) : null;
        final String firstName = payload.getString(FIELD_FIRST_NAME);
        final String lastName = payload.getString(FIELD_LAST_NAME);
        final JsonArray defendantIds = payload.getJsonArray("defendantIds");

       if (hearing != null) {
            final Witness witness = Witness.builder()
                    .withId(new HearingSnapshotKey(id, hearingId))
                    .withHearing(hearing)
                    .withType(type)
                    .withTitle(title)
                    .withFirstName(firstName)
                    .withLastName(lastName)
                    .withClassification(classification)
                    .build();

            defendantIds.forEach(defendantId -> {
                final Defendant defendant = hearing.getDefendants().stream()
                        .filter(d -> d.getId().getId().toString()
                                .equals(((JsonString) defendantId).getString()))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException(String.format(
                                "hearingId is %s witness added for unkown defendant with Id %s ",
                                hearing.getId(), defendantId)));

                witness.getDefendants().add(defendant);
                defendant.getDefendantWitnesses().add(witness);
            });
            
            hearingRepository.save(hearing);
        }

    }
}
