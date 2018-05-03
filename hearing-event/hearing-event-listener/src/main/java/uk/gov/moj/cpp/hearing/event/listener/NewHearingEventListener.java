package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.Optional.ofNullable;
import static java.util.UUID.fromString;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.initiate.Interpreter;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateAdded;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateRemoved;
import uk.gov.moj.cpp.hearing.domain.event.InitiateHearingOffencePlead;
import uk.gov.moj.cpp.hearing.persist.WitnessRepository;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Address;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Ahearing;
import uk.gov.moj.cpp.hearing.persist.entity.ex.AhearingDate;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ex.DefendantCase;
import uk.gov.moj.cpp.hearing.persist.entity.ex.DefendantCaseKey;
import uk.gov.moj.cpp.hearing.persist.entity.ex.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ex.LegalCase;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Offence;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Witness;
import uk.gov.moj.cpp.hearing.repository.AhearingRepository;
import uk.gov.moj.cpp.hearing.repository.LegalCaseRepository;
import uk.gov.moj.cpp.hearing.repository.OffenceRepository;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
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

@SuppressWarnings({"squid:S2201"})
@ServiceComponent(EVENT_LISTENER)
public class NewHearingEventListener {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(NewHearingEventListener.class.getName());
    @Inject
    private AhearingRepository ahearingRepository;

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
                                .withCustodyTimeLimitDate(ofNullable(dc.getCustodyTimeLimitDate()).map(d -> d.atStartOfDay().atZone(ZoneOffset.UTC)).orElse(null))
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
    @Handles("hearing.initiated")
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
        final Ahearing aHearing = Ahearing.builder()
                .withId(hearing.getId())
                .withHearingType(hearing.getType())
                .withCourtCentreId(hearing.getCourtCentreId())
                .withCourtCentreName(hearing.getCourtCentreName())
                .withRoomId(hearing.getCourtRoomId())
                .withRoomName(hearing.getCourtRoomName())
                .withStartDateTime(hearing.getStartDateTime())
                .withHearingDays(hearing.getHearingDays().stream()
                        .map(zdt -> AhearingDate.builder()
                                .withDate(zdt)
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
                .withWitnesses((hearing.getWitnesses() == null ? new ArrayList<Witness>() : hearing.getWitnesses().stream()
                        .map(witnessIn -> {
                            return translateWitness(hearing.getId(), witnessIn, id2Case.get(witnessIn.getCaseId())).build();
                        }).collect((Collectors.toList()))))
                .withJudge((uk.gov.moj.cpp.hearing.persist.entity.ex.Judge.Builder) uk.gov.moj.cpp.hearing.persist.entity.ex.Judge.builder()
                        .withId(new HearingSnapshotKey(hearing.getJudge().getId(), hearing.getId()))
                        .withFirstName(hearing.getJudge().getFirstName())
                        .withLastName(hearing.getJudge().getLastName())
                        .withTitle(hearing.getJudge().getTitle()))
                .build();
        this.ahearingRepository.save(aHearing);
    }

    @Transactional
    @Handles("hearing.conviction-date-added")
    public void convictionDateUpdated(final JsonEnvelope event) {
        final ConvictionDateAdded convictionDateAdded = jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), ConvictionDateAdded.class);
        save(convictionDateAdded.getOffenceId(), convictionDateAdded.getHearingId(), (o) -> {
            o.setConvictionDate(convictionDateAdded.getConvictionDate());
        });
    }

    @Transactional
    @Handles("hearing.conviction-date-removed")
    public void convictionDateRemoved(final JsonEnvelope event) {
        final ConvictionDateRemoved convictionDateRemoved = jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), ConvictionDateRemoved.class);
        save(convictionDateRemoved.getOffenceId(), convictionDateRemoved.getHearingId(), (o) -> {
            o.setConvictionDate(null);
        });
    }

    @Transactional
    @Handles("hearing.initiate-hearing-offence-plead")
    public void hearingInitiatedPleaData(final JsonEnvelope envelop) {
        final InitiateHearingOffencePlead event = jsonObjectToObjectConverter.convert(envelop.payloadAsJsonObject(), InitiateHearingOffencePlead.class);
        save(event.getOffenceId(), event.getHearingId(), (o) -> {
            o.setOriginHearingId(event.getOriginHearingId());
            o.setPleaDate(event.getPleaDate());
            o.setPleaValue(event.getValue());
            o.setConvictionDate(isGuilty(event.getValue()) ? event.getPleaDate() : null);
        });
    }

    @Transactional
    @Handles("hearing.initiate-hearing-defence-witness-enriched")
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

        final Ahearing hearing = ahearingRepository.findById(hearingId);
        if (hearing != null) {
            final HearingSnapshotKey witnessKey = new HearingSnapshotKey(id, hearingId);
            Witness witness = witnessRepository.findBy(witnessKey);

            if (witness == null) {
                LOGGER.info("Witness {} not found for hearing id  {}  , creating new witness", id,
                        hearingId);
                witness = Witness.builder().withId(witnessKey).withHearing(hearing).withType(type)
                        .withTitle(title).withFirstName(firstName).withLastName(lastName)
                        .withClassification(classification).build();
            }

            final Defendant defendant = hearing.getDefendants().stream()
                    .filter(d -> d.getId().getId().equals((defendantId))).findFirst()
                    .orElseThrow(() -> new RuntimeException(String.format(
                            "hearing %s witness added for unkown defendant %s ",
                            hearing.getId(), defendantId)));


            witness.getDefendants().add(defendant);
            defendant.getDefendantWitnesses().add(witness);

            ahearingRepository.saveAndFlush(hearing);
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

    @Transactional
    @Handles("hearing.events.witness-added")
    public void witnessAdded(final JsonEnvelope event) {

        addWitness(event);
    }

    private void addWitness(final JsonEnvelope event) {

        final JsonObject payload = event.payloadAsJsonObject();
        LOGGER.debug("hearing.events.witness-added listener payload {} ", payload);
        final UUID id = fromString(payload.getString(FIELD_GENERIC_ID));
        final UUID hearingId = fromString(payload.getString(FIELD_HEARING_ID));
        final String type = payload.getString(FIELD_TYPE);
        final String classification = payload.getString(FIELD_CLASSIFICATION);
        final String title =
                payload.containsKey(FIELD_TITLE) ? payload.getString(FIELD_TITLE) : null;
        final String firstName = payload.getString(FIELD_FIRST_NAME);
        final String lastName = payload.getString(FIELD_LAST_NAME);
        final JsonArray defendantIds = payload.getJsonArray("defendantIds");

        final Ahearing hearing = ahearingRepository.findById(hearingId);
        if (hearing != null) {
            final Witness witness = Witness.builder().withId(new HearingSnapshotKey(id, hearingId))
                    .withHearing(hearing).withType(type).withTitle(title)
                    .withFirstName(firstName).withLastName(lastName)
                    .withClassification(classification).build();

            defendantIds.forEach(defendantId -> {
                final Defendant defendant = hearing.getDefendants().stream()
                        .filter(d -> d.getId().getId().toString()
                                .equals(((JsonString) defendantId).getString()))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException(String.format(
                                "hearing %s witness added for unkown defendant %s ",
                                hearing.getId(), defendantId)));
                witness.getDefendants().add(defendant);
                defendant.getDefendantWitnesses().add(witness);
            });
            ahearingRepository.save(hearing);
        }
    }

}
