package uk.gov.moj.cpp.hearing.event.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.initiate.Interpreter;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateAdded;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateRemoved;
import uk.gov.moj.cpp.hearing.domain.event.NewDefenceCounselAdded;
import uk.gov.moj.cpp.hearing.domain.event.NewProsecutionCounselAdded;
import uk.gov.moj.cpp.hearing.domain.event.OffenceVerdictUpdated;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Address;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Ahearing;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Attendee;
import uk.gov.moj.cpp.hearing.persist.entity.ex.DefenceAdvocate;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ex.DefendantCase;
import uk.gov.moj.cpp.hearing.persist.entity.ex.DefendantCaseKey;
import uk.gov.moj.cpp.hearing.persist.entity.ex.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ex.LegalCase;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Offence;
import uk.gov.moj.cpp.hearing.persist.entity.ex.ProsecutionAdvocate;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Witness;
import uk.gov.moj.cpp.hearing.repository.AhearingRepository;
import uk.gov.moj.cpp.hearing.repository.LegalCaseRepository;
import uk.gov.moj.cpp.hearing.repository.OffenceRepository;

import javax.inject.Inject;
import javax.json.JsonObject;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

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

    private Defendant.Builder translateDefendant(UUID hearingId, uk.gov.moj.cpp.hearing.command.initiate.Defendant defendantIn) {
        Defendant.Builder builder = Defendant.builder()
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

        return builder;
    }

    private Witness.Builder translateWitness(UUID hearingId, uk.gov.moj.cpp.hearing.command.initiate.Witness witnessIn, LegalCase id2Case) {
        Witness.Builder builder = Witness.builder()
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

        return builder;
    }


    @Transactional
    @Handles("hearing.initiated")
    public void newHearingInitiated(final JsonEnvelope event) {
        final JsonObject payload = event.payloadAsJsonObject();
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
        Ahearing aHearing = Ahearing.builder()
                .withId(hearing.getId())
                .withHearingType(hearing.getType())
                .withCourtCentreId(hearing.getCourtCentreId())
                .withCourtCentreName(hearing.getCourtCentreName())
                .withRoomId(hearing.getCourtRoomId())
                .withRoomName(hearing.getCourtRoomName())
                .withStartDateTime(hearing.getStartDateTime())
                .withDefendants(hearing.getDefendants().stream()
                        .map(defendantIn -> {
                            Defendant defendant = translateDefendant(hearing.getId(), defendantIn).build();
                            defendant.setOffences(new ArrayList<>());
                            defendantIn.getOffences().forEach(
                                    offenceIn -> {
                                        Offence offence = translateOffence(hearing.getId(), offenceIn, id2Case).withDefendant(defendant).build();
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
        final HearingSnapshotKey snapshotKey = new HearingSnapshotKey(convictionDateAdded.getOffenceId(), convictionDateAdded.getHearingId());
        final Offence offence = offenceRepository.findBySnapshotKey(snapshotKey);
        Optional.ofNullable(offence).map(o -> {
            o.setConvictionDate(convictionDateAdded.getConvictionDate());
            offenceRepository.save(o);
            return o;
        }).orElseThrow(() -> new RuntimeException("Invalid offence id.  Offence id is not found on hearing: " + snapshotKey));
    }

    @Transactional
    @Handles("hearing.conviction-date-removed")
    public void convictionDateRemoved(final JsonEnvelope event) {
        final ConvictionDateRemoved convictionDateRemoved = jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), ConvictionDateRemoved.class);
        final HearingSnapshotKey snapshotKey = new HearingSnapshotKey(convictionDateRemoved.getOffenceId(), convictionDateRemoved.getHearingId());
        final Offence offence = offenceRepository.findBySnapshotKey(snapshotKey);
        Optional.ofNullable(offence).map(o -> {
            o.setConvictionDate(null);
            offenceRepository.save(o);
            return o;
        }).orElseThrow(() -> new RuntimeException("Invalid offence id.  Offence id is not found on hearing: " + snapshotKey));
    }

    @Transactional
    @Handles("hearing.initiate-hearing-offence-plead")
    public void hearingInitiatedPleaData(final JsonEnvelope event) {
        LOGGER.error("HERE HERE HERE - we have received plea info");
    }

    private DefenceAdvocate findOrCreateDefenceAdvocateByAttendeeId(Ahearing hearing, NewDefenceCounselAdded newDefenceCounselAdded) {
        UUID attendeeId = newDefenceCounselAdded.getAttendeeId();
        Optional<Attendee> oAttendee = hearing.getAttendees().stream().filter(a -> a instanceof DefenceAdvocate && a.getId().getId().equals(attendeeId)).findFirst();
        DefenceAdvocate defenceAdvocate = null;
        if (oAttendee.isPresent()) {
            defenceAdvocate = (DefenceAdvocate) oAttendee.get();
        } else {
            defenceAdvocate = DefenceAdvocate.builder()
                    .withId(new HearingSnapshotKey(attendeeId, hearing.getId()))
                    .withTitle(newDefenceCounselAdded.getTitle())
                    .withPersonId(newDefenceCounselAdded.getPersonId())
                    .withStatus(newDefenceCounselAdded.getStatus())
                    .withLastName(newDefenceCounselAdded.getLastName())
                    .withFirstName(newDefenceCounselAdded.getFirstName()).build();
            hearing.getAttendees().add(defenceAdvocate);
        }
        return defenceAdvocate;
    }

    @Transactional
    @Handles("hearing.newdefence-counsel-added")
    public void newDefenceCounselAdded(final JsonEnvelope event) {
        String strContext = getClass().getSimpleName() + "::" + "hearing.newdefence-counsel-added";
        final JsonObject payload = event.payloadAsJsonObject();
        NewDefenceCounselAdded newDefenceCounselAdded = jsonObjectToObjectConverter.convert(payload, NewDefenceCounselAdded.class);
        Ahearing hearing = ahearingRepository.findBy(newDefenceCounselAdded.getHearingId());
        if (null == hearing) {
            throw new RuntimeException(strContext + "  cant find hearing " + newDefenceCounselAdded.getHearingId());
        }
        final DefenceAdvocate defenceAdvocate = findOrCreateDefenceAdvocateByAttendeeId(hearing, newDefenceCounselAdded);
        defenceAdvocate.setStatus(newDefenceCounselAdded.getStatus());

        newDefenceCounselAdded.getDefendantIds().forEach(
                did -> {
                    Optional<Defendant> defendant = hearing.getDefendants().stream().filter(d -> d.getId().getId().equals(did)).findFirst();
                    if (!defendant.isPresent()) {
                        String message = String.format("hearing %s defence counsel %s added for unkown defendant %s ", hearing.getId(), newDefenceCounselAdded.getAttendeeId(), did);
                        LOGGER.error(message);
                        throw new RuntimeException(message);
                    } else {
                        defenceAdvocate.getDefendants().add(defendant.get());
                    }
                }
        );
        ahearingRepository.save(hearing);
    }

    @Transactional
    @Handles("hearing.newprosecution-counsel-added")
    public void newProsecutionCounselAdded(final JsonEnvelope event) {
        String context = getClass().getSimpleName() + "::newProsecutionCounselAdded: ";
        NewProsecutionCounselAdded prosecutionCounselAdded = (NewProsecutionCounselAdded) jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), NewProsecutionCounselAdded.class);

        if (ahearingRepository == null) {
            throw new RuntimeException(context + " ahearingRepository not available ");
        }

        Ahearing hearing = ahearingRepository.findBy(prosecutionCounselAdded.getHearingId());

        // assume there is 1 case only
        if (hearing == null) {
            throw new RuntimeException(context + " hearing not found " + prosecutionCounselAdded.getHearingId());
        }

        Optional<ProsecutionAdvocate> existing = hearing.getAttendees().stream().filter(a -> a instanceof ProsecutionAdvocate)
                .map(a -> (ProsecutionAdvocate) a).filter(a -> a.getId().getId().equals(prosecutionCounselAdded.getAttendeeId())).findFirst();
        existing.ifPresent(
                p -> p.setStatus(prosecutionCounselAdded.getStatus())
        );
        if (!existing.isPresent()) {
            HearingSnapshotKey id = new HearingSnapshotKey(prosecutionCounselAdded.getAttendeeId(), prosecutionCounselAdded.getHearingId());
            ProsecutionAdvocate prosecutionAdvocate = ProsecutionAdvocate.builder()
                    .withStatus(prosecutionCounselAdded.getStatus())
                    .withFirstName(prosecutionCounselAdded.getFirstName())
                    .withLastName(prosecutionCounselAdded.getLastName())
                    .withTitle(prosecutionCounselAdded.getTitle())
                    .withPersonId(prosecutionCounselAdded.getPersonId()).withId(id).build();
            if (hearing.getAttendees() == null) {
                hearing.setAttendees(new ArrayList<>());
            }
            hearing.getAttendees().add(prosecutionAdvocate);

        }
        ahearingRepository.save(hearing);
    }

}
