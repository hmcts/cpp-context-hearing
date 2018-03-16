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
import uk.gov.moj.cpp.hearing.persist.entity.DefenceCounsel;
import uk.gov.moj.cpp.hearing.persist.entity.DefenceCounselDefendant;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Address;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Ahearing;
import uk.gov.moj.cpp.hearing.persist.entity.ex.DefenceAdvocate;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ex.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ex.LegalCase;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Offence;
import uk.gov.moj.cpp.hearing.repository.AhearingRepository;
import uk.gov.moj.cpp.hearing.repository.LegalCaseRepository;

import javax.inject.Inject;
import javax.json.JsonObject;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

    @Transactional
    @Handles("hearing.initiated")
    public void newHearingInitiated(final JsonEnvelope event) {
        final JsonObject payload = event.payloadAsJsonObject();
        LOGGER.error("TODO remove me 2 *************************hearing.initiated payload:" + payload);
        final InitiateHearingCommand initiated = jsonObjectToObjectConverter.convert(payload, InitiateHearingCommand.class);
        final uk.gov.moj.cpp.hearing.command.initiate.Hearing hearing = initiated.getHearing();
        final List<Defendant> defendants = new ArrayList<>();

        final UUID hearingId = initiated.getHearing().getId();

///        final String caseurn = initiated.getCases().get(0).getUrn();
        //TODO move this to offence level
   ///     final UUID caseId = initiated.getCases().get(0).getCaseId();

        //TODO attach legalcase to offence

        //this.legalCaseRepository.save(legalCase);

        Function<uk.gov.moj.cpp.hearing.command.initiate.Offence, Offence.Builder> translateOffence = (offenceIn) -> {
            final HearingSnapshotKey offenceId = new HearingSnapshotKey(offenceIn.getId(), hearing.getId());
            LegalCase legalCase = this.legalCaseRepository.findBy(offenceIn.getCaseId());
            if (legalCase==null) {
                //TODO determine if this is an error conditons
                LOGGER.error("got offence before case is known caseId: " + offenceIn.getCaseId());
                legalCase = new LegalCase.Builder().withId(offenceIn.getCaseId()).build();
            }
            return new Offence.Builder().withId(offenceId).withCase(legalCase).withCode(offenceIn.getOffenceCode()).withCount(offenceIn.getCount());
        };

        Function<uk.gov.moj.cpp.hearing.command.initiate.Defendant, Defendant.Builder> translateDefendant = (defendantIn) -> {
            //TODO - ugly null checks !
            //TODO get this form somewhere real !
            DefenceAdvocate.Builder defenceAdvocateBuilder = (DefenceAdvocate.Builder)
                    new DefenceAdvocate.Builder().withLastName("Bowie").withFirstName("David").withId(
                            new HearingSnapshotKey( UUID.randomUUID(), hearing.getId())
                    ).withPersonId(UUID.randomUUID());

            Address address = null;

            uk.gov.moj.cpp.hearing.command.initiate.Address addressIn = defendantIn.getAddress();
            if (addressIn!=null) {
                address = new Address.Builder()
                        .withAddress1(addressIn.getAddress1())
                        .withAddress2(addressIn.getAddress2())
                        .withAddress3(addressIn.getAddress3())
                        .withAddress4(addressIn.getAddress4())
                        .withPostCode(addressIn.getPostCode()).build();
            }
            return new Defendant.Builder().withAddress(address)
                    .withDefenceAdvocates(Arrays.asList(defenceAdvocateBuilder.build()))
                    .withId(new HearingSnapshotKey(defendantIn.getId(), hearing.getId()));
        };

        Function<Judge, uk.gov.moj.cpp.hearing.persist.entity.ex.Judge.Builder> translateJudge = (judgeIn) -> {
            return (uk.gov.moj.cpp.hearing.persist.entity.ex.Judge.Builder) new uk.gov.moj.cpp.hearing.persist.entity.ex.Judge.Builder()
                    .withFirstName(judgeIn.getFirstName())
                    .withLastName(judgeIn.getLastName())
                    .withTitle(judgeIn.getTitle());
        } ;


        hearing.getDefendants().forEach(
                defendantIn -> {
                    Defendant defendant = translateDefendant.apply(defendantIn).build();
                    defendants.add(defendant);
                    defendant.setOffences(new ArrayList<>());
                    defendantIn.getOffences().forEach(
                            offenceIn -> defendant.getOffences().add(translateOffence.apply(offenceIn).withDefendant(defendant).build())
                    );
                }
        );

        //TODO resolve whether this can be an update !
        uk.gov.moj.cpp.hearing.persist.entity.ex.Judge.Builder judgeBuilder = translateJudge.apply(hearing.getJudge());
        judgeBuilder.withId(new HearingSnapshotKey(hearing.getJudge().getId(), hearing.getId()));
        Ahearing hearingex = (new Ahearing.Builder()).withId(hearing.getId())
                .withHearingType(hearing.getType())
                .withCourtCentreId(hearing.getCourtCentreId())
                .withCourtCentreName(hearing.getCourtCentreName())
                .withRoomId(hearing.getCourtRoomId())
                .withRoomName(hearing.getCourtRoomName())
                .withDefendants(defendants)
                .withJudge(judgeBuilder)
                .build();
        //make sure the defence advocates are included
        defendants.forEach(
                d->hearingex.getAttendees().addAll(d.getDefenceAdvocates())
        );
        this.ahearingRepository.save(hearingex);
        //TODO resnapshot
    }


    @Transactional
    @Handles("hearing.case-created")
    public void caseCreated(final JsonEnvelope event) {
        final JsonObject payload = event.payloadAsJsonObject();
        LOGGER.error("TODO remove me START *************************hearing.case-created payload:" + payload);
        final CaseCreated caseCreated = jsonObjectToObjectConverter.convert(payload, CaseCreated.class);
        LegalCase legalCase =  legalCaseRepository.findBy(caseCreated.getCaseId());
        if (legalCase==null) {
            LOGGER.error("TODO remove me CREATing CASE *************************hearing.case-created creating case " + caseCreated.getCaseId());
            legalCase = new LegalCase.Builder().withId(caseCreated.getCaseId()).withCaseurn(caseCreated.getUrn()).build();
            LOGGER.error("TODO remove me CREATED CASE *************************hearing.case-created created case "  + caseCreated.getCaseId());

        } else {
            //TODO acept a new caseurn or does a different caseurn indicate an error ?
            LOGGER.error("TODO remove me 2 updateing CASE *************************hearing.case-created updating case " + caseCreated.getCaseId());
            legalCase.setCaseurn(caseCreated.getUrn());
            LOGGER.error("TODO remove me 2 UPDATED CASE *************************hearing.case-created updated case " + caseCreated.getCaseId());

        }
        LOGGER.error("TODO remove me 2 SAVING CASE *************************hearing.case-created saving case " + caseCreated.getCaseId());
        this.legalCaseRepository.save(legalCase);
        LOGGER.error("TODO remove me 2 SAVED CASE *************************hearing.case-created saved case " + caseCreated.getCaseId());
    }

    @Transactional
    //@Handles("hearing.defence-counsel-addedNEW")
    public void defenceCounselAdded(final JsonEnvelope event) {
        final JsonObject payload = event.payloadAsJsonObject();

        LOGGER.error("TODO remove me 2 *************************hearing.defence-counsel-added payload:" + payload);

        /*final JsonObject payload = event.payloadAsJsonObject();
        final UUID hearingId = fromString(payload.getString(FIELD_HEARING_ID));
        final UUID personId = fromString(payload.getString(FIELD_PERSON_ID));
        final UUID attendeeId = fromString(payload.getString(FIELD_ATTENDEE_ID));
        final String status = payload.getString(FIELD_STATUS);

        final List<UUID> defendantIds = JsonObjects.getUUIDs(payload, FIELD_DEFENDANT_IDS);

        this.defenceCounselRepository.save(new DefenceCounsel(attendeeId, hearingId, personId, status));

        final List<DefenceCounselDefendant> existingDefendants =
                this.defenceCounselDefendantRepository.findByDefenceCounselAttendeeId(attendeeId);

        existingDefendants.stream()
                .filter(defendant -> !defendantIds.contains(defendant.getDefendantId()))
                .forEach(this.defenceCounselDefendantRepository::remove);

        defendantIds.forEach(defendantId ->
                this.defenceCounselDefendantRepository.save(new DefenceCounselDefendant(attendeeId, defendantId)));
                */
    }




}
