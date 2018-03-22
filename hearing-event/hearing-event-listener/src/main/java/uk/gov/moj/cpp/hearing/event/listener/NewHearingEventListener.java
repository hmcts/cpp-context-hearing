package uk.gov.moj.cpp.hearing.event.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjects;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.initiate.Judge;
import uk.gov.moj.cpp.hearing.domain.event.CaseCreated;
import uk.gov.moj.cpp.hearing.domain.event.NewDefenceCounselAdded;
import uk.gov.moj.cpp.hearing.domain.event.NewProsecutionCounselAdded;
import uk.gov.moj.cpp.hearing.domain.event.ProsecutionCounselAdded;
import uk.gov.moj.cpp.hearing.persist.entity.DefenceCounsel;
import uk.gov.moj.cpp.hearing.persist.entity.DefenceCounselDefendant;
import uk.gov.moj.cpp.hearing.persist.entity.ProsecutionCounsel;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import static java.util.UUID.fromString;
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
        final HearingSnapshotKey offenceId = new HearingSnapshotKey(offenceIn.getId(), hearingId);
        if (!id2Case.containsKey(offenceIn.getCaseId())) {
            //should not happen
            final String strMessage = String.format("hearing %s offence %s unknown case %s", hearingId, offenceIn.getId(), offenceIn.getCaseId() );
            LOGGER.error(strMessage);
            throw new RuntimeException(strMessage);
        }
        return Offence.builder().withId(offenceId)
                //TODO review this loss of information
                .withConvictionDate(offenceIn.getConvictionDate()==null?null:offenceIn.getConvictionDate().atStartOfDay())
                //TODO review where legislation is coming from
                //.withLegislation(offenceIn.g)
                .withCase(id2Case.get(offenceIn.getCaseId()))
                .withCode(offenceIn.getOffenceCode()).withCount(offenceIn.getCount());
    }

    private Defendant.Builder translateDefendant(UUID hearingId, uk.gov.moj.cpp.hearing.command.initiate.Defendant defendantIn) {


        Address address = null;

        uk.gov.moj.cpp.hearing.command.initiate.Address addressIn = defendantIn.getAddress();
        if (addressIn != null) {
            address = Address.builder()
                    .withAddress1(addressIn.getAddress1())
                    .withAddress2(addressIn.getAddress2())
                    .withAddress3(addressIn.getAddress3())
                    .withAddress4(addressIn.getAddress4())
                    .withPostCode(addressIn.getPostCode()).build();
        }
        return Defendant.builder().withAddress(address)
                .withDateOfBirth(defendantIn.getDateOfBirth())
                //TODO where should email + fax + homeTelephone come from ?
                //.withEmail(defendantIn.getEmail)
                //.withFax(defendantIn.getFax)
                //.withHomeTelephone(defendantIn.get..)
                //TODO remove this - see comment above
                //.withDefenceAdvocates(Arrays.asList(defenceAdvocateBuilder.build()))
                .withNationality(defendantIn.getNationality())
                .withFirstName(defendantIn.getFirstName())
                .withLastName(defendantIn.getLastName())
                .withGender(defendantIn.getGender())
                .withId(new HearingSnapshotKey(defendantIn.getId(), hearingId));
    }


    @Transactional
    @Handles("hearing.initiated")
    public void newHearingInitiated(final JsonEnvelope event) {
        final JsonObject payload = event.payloadAsJsonObject();
        final InitiateHearingCommand initiated = jsonObjectToObjectConverter.convert(payload, InitiateHearingCommand.class);
        final uk.gov.moj.cpp.hearing.command.initiate.Hearing hearing = initiated.getHearing();
        final List<Defendant> defendants = new ArrayList<>();
        final Map<UUID, LegalCase> id2Case = new HashMap<>();
        initiated.getCases().forEach(
                (caseIn) -> {
                    LegalCase legalCase = legalCaseRepository.findById(caseIn.getCaseId());
                    if (null==legalCase) {
                        legalCase = LegalCase.builder().withId(caseIn.getCaseId()).withCaseurn(caseIn.getUrn()).build();
                        id2Case.put(legalCase.getId(), legalCase);
                        legalCaseRepository.save(legalCase);
                    }
                }
        );


        Function<Judge, uk.gov.moj.cpp.hearing.persist.entity.ex.Judge.Builder> translateJudge = (judgeIn) -> {
            return (uk.gov.moj.cpp.hearing.persist.entity.ex.Judge.Builder) uk.gov.moj.cpp.hearing.persist.entity.ex.Judge.builder()
                    .withFirstName(judgeIn.getFirstName())
                    .withLastName(judgeIn.getLastName())
                    .withTitle(judgeIn.getTitle());
        };


        hearing.getDefendants().forEach(
                defendantIn -> {
                    Defendant defendant = translateDefendant(hearing.getId(), defendantIn).build();
                    defendants.add(defendant);
                    defendant.setOffences(new ArrayList<>());
                    defendantIn.getOffences().forEach(
                            offenceIn -> defendant.getOffences().add(translateOffence(hearing.getId(), offenceIn, id2Case).withDefendant(defendant).build())
                    );
                }
        );

        uk.gov.moj.cpp.hearing.persist.entity.ex.Judge.Builder judgeBuilder = translateJudge.apply(hearing.getJudge());
        judgeBuilder.withId(new HearingSnapshotKey(hearing.getJudge().getId(), hearing.getId()));
        Ahearing hearingex = Ahearing.builder().withId(hearing.getId())
                .withHearingType(hearing.getType())
                .withCourtCentreId(hearing.getCourtCentreId())
                .withCourtCentreName(hearing.getCourtCentreName())
                .withRoomId(hearing.getCourtRoomId())
                .withRoomName(hearing.getCourtRoomName())
                .withDefendants(defendants)
                .withJudge(judgeBuilder)
                .build();

        this.ahearingRepository.save(hearingex);
        //TODO resnapshot
    }


    //TODO remove this
    @Transactional
    @Handles("hearing.initiate-hearing-offence-plead")
    public void hearingInitiatedPleaData(final JsonEnvelope event) {
        LOGGER.error("HERE HERE HERE - we have received plea info");
    }



    @Transactional
    @Handles("hearing.case-created")
    public void caseCreated(final JsonEnvelope event) {
    }

    private DefenceAdvocate findDefenceAdvocateByAttendeeId(Ahearing hearing, UUID attendeeId) {
        Optional<Attendee> oAttendee = hearing.getAttendees().stream().filter(a -> a instanceof DefenceAdvocate && a.getId().getId().equals(attendeeId)).findFirst();
        DefenceAdvocate defenceAdvocate = null;
        if (oAttendee.isPresent()) {
            defenceAdvocate = (DefenceAdvocate) oAttendee.get();
        } else {
            //TODO extend command to include these
            defenceAdvocate = DefenceAdvocate.builder().withId(new HearingSnapshotKey(attendeeId, hearing.getId())).withTitle("QC").withLastName("unknown").withFirstName("firstName").build();
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
        final DefenceAdvocate defenceAdvocate = findDefenceAdvocateByAttendeeId(hearing, newDefenceCounselAdded.getAttendeeId());
        defenceAdvocate.setStatus(newDefenceCounselAdded.getStatus());

        newDefenceCounselAdded.getDefendantIds().forEach(
                did->{
                    Optional<Defendant> defendant = hearing.getDefendants().stream().filter(d->d.getId().getId().equals(did)).findFirst();
                    if (!defendant.isPresent()) {
                         String message = String.format("hearing %s defence counsel %s added for unkown defendant %s ", hearing.getId(), newDefenceCounselAdded.getAttendeeId(), did  );
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
        LOGGER.error(context + prosecutionCounselAdded);

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

        //TODO link counsel to case ?
        ahearingRepository.save(hearing);
    }

}
