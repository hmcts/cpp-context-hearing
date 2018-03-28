package uk.gov.moj.cpp.hearing.event.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.initiate.Interpreter;
import uk.gov.moj.cpp.hearing.domain.event.NewDefenceCounselAdded;
import uk.gov.moj.cpp.hearing.domain.event.NewProsecutionCounselAdded;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Address;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Ahearing;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Attendee;
import uk.gov.moj.cpp.hearing.persist.entity.ex.DefenceAdvocate;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ex.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ex.LegalCase;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Offence;
import uk.gov.moj.cpp.hearing.persist.entity.ex.ProsecutionAdvocate;
import uk.gov.moj.cpp.hearing.repository.AhearingRepository;
import uk.gov.moj.cpp.hearing.repository.LegalCaseRepository;

import javax.inject.Inject;
import javax.json.JsonObject;
import javax.transaction.Transactional;
import java.time.LocalDate;
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
                .withId(new HearingSnapshotKey(defendantIn.getId(), hearingId));

        if (!defendantIn.getDefendantCases().isEmpty()) {
            builder.withBailStatus(defendantIn.getDefendantCases().get(0).getBailStatus()) //TODO - the FE needs to handle multiple cases
                    .withCustodyTimeLimitDate(defendantIn.getDefendantCases().get(0).getCustodyTimeLimitDate());
        }

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
                        id2Case.put(legalCase.getId(), legalCase);
                        legalCaseRepository.save(legalCase);
                    }
                }
        );


        this.ahearingRepository.save(Ahearing.builder()
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
                                    offenceIn -> defendant.getOffences().add(translateOffence(hearing.getId(), offenceIn, id2Case).withDefendant(defendant).build())
                            );
                            return defendant;
                        })
                        .collect(Collectors.toList()))
                .withJudge((uk.gov.moj.cpp.hearing.persist.entity.ex.Judge.Builder) uk.gov.moj.cpp.hearing.persist.entity.ex.Judge.builder()
                        .withId(new HearingSnapshotKey(hearing.getJudge().getId(), hearing.getId()))
                        .withFirstName(hearing.getJudge().getFirstName())
                        .withLastName(hearing.getJudge().getLastName())
                        .withTitle(hearing.getJudge().getTitle()))
                .build());
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
                        //TODO should throw an exception ?
                        throw new RuntimeException(message);
                    } else {
                        //TODO should check whether its already been added ?
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
