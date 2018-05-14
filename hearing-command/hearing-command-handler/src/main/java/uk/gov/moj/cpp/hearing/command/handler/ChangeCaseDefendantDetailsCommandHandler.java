package uk.gov.moj.cpp.hearing.command.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.progression.events.CaseDefendantDetails;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.defendant.Address;
import uk.gov.moj.cpp.hearing.command.defendant.CaseDefendantDetailsCommand;
import uk.gov.moj.cpp.hearing.command.defendant.CaseDefendantDetailsWithHearingCommand;
import uk.gov.moj.cpp.hearing.command.defendant.Defendant;
import uk.gov.moj.cpp.hearing.command.defendant.Interpreter;
import uk.gov.moj.cpp.hearing.domain.aggregate.DefendantAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.NewModelHearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.CaseDefendantDetailsWithHearings;

import javax.inject.Inject;
import javax.json.JsonObject;
import java.util.Collections;
import java.util.UUID;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

@ServiceComponent(COMMAND_HANDLER)
public class ChangeCaseDefendantDetailsCommandHandler extends AbstractCommandHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(ChangeCaseDefendantDetailsCommandHandler.class.getName());


    @Inject
    public ChangeCaseDefendantDetailsCommandHandler(final EventSource eventSource, final Enveloper enveloper,
                                                    final AggregateService aggregateService, final JsonObjectToObjectConverter jsonObjectToObjectConverter) {
        super(eventSource, enveloper, aggregateService, jsonObjectToObjectConverter);
    }

    @Handles("hearing.update-case-defendant-details")
    public void initiateCaseDefendantDetailsChange(final JsonEnvelope envelope) throws EventStreamException {
        LOGGER.debug("hearing.update-case-defendant-details event received {}", envelope.payloadAsJsonObject());

        final CaseDefendantDetails caseDefendantDetails = jsonObjectToObjectConverter.convert(envelope.payloadAsJsonObject(), CaseDefendantDetails.class);

        for (final Defendant defendant : caseDefendantDetails.getDefendants()) {

            final Address address = defendant.getAddress();

            final Interpreter interpreter = defendant.getInterpreter();

            final CaseDefendantDetailsCommand caseDefendantDetailsCommand = CaseDefendantDetailsCommand.builder()
                    .withCaseId(caseDefendantDetails.getCaseId())
                    .withCaseUrn(caseDefendantDetails.getCaseUrn())
                    .withDefendant(Defendant.builder()
                            .withId(defendant.getId())
                            .withPersonId(defendant.getPersonId())
                            .withFirstName(defendant.getFirstName())
                            .withLastName(defendant.getLastName())
                            .withNationality(defendant.getNationality())
                            .withGender(defendant.getGender())
                            .withAddress(Address.address()
                                    .withAddress1(address.getAddress1())
                                    .withAddress2(address.getAddress2())
                                    .withAddress3(address.getAddress3())
                                    .withAddress4(address.getAddress4())
                                    .withPostcode(address.getPostCode()))
                            .withDateOfBirth(defendant.getDateOfBirth())
                            .withBailStatus(defendant.getBailStatus())
                            .withCustodyTimeLimitDate(defendant.getCustodyTimeLimitDate())
                            .withDefenceOrganisation(defendant.getDefenceOrganisation())
                            .withInterpreter(Interpreter.interpreter()
                                    .withLanguage(interpreter.getLanguage())
                                    .withNeeded(interpreter.getNeeded())))
                    .build();

            aggregate(DefendantAggregate.class,
                    defendant.getId(),
                    envelope,
                    defendantAggregate -> defendantAggregate.enrichCaseDefendantDetailsWithHearingIds(caseDefendantDetailsCommand));
        }
    }

    @Handles("hearing.update-case-defendant-details-against-hearing-aggregate")
    public void updateCaseDefendantDetails(final JsonEnvelope envelope) throws EventStreamException {
        LOGGER.debug("hearing.update-case-defendant-details-against-hearing-aggregate event received {}", envelope.payloadAsJsonObject());

        final JsonObject payload = envelope.payloadAsJsonObject();

        final CaseDefendantDetailsWithHearings caseDefendantDetailsWithHearings = jsonObjectToObjectConverter.convert(payload, CaseDefendantDetailsWithHearings.class);

        final Defendant defendant = caseDefendantDetailsWithHearings.getDefendant();

        final Address address = defendant.getAddress();

        final Interpreter interpreter = defendant.getInterpreter();

        for (final UUID hearingId : caseDefendantDetailsWithHearings.getHearingIds()) {

            final CaseDefendantDetailsWithHearingCommand defendantWithHearingCommand = CaseDefendantDetailsWithHearingCommand.builder()
                    .withCaseId(caseDefendantDetailsWithHearings.getCaseId())
                    .withCaseUrn(caseDefendantDetailsWithHearings.getCaseUrn())
                    .withDefendants(Defendant.builder()
                            .withId(defendant.getId())
                            .withPersonId(defendant.getPersonId())
                            .withFirstName(defendant.getFirstName())
                            .withLastName(defendant.getLastName())
                            .withNationality(defendant.getNationality())
                            .withGender(defendant.getGender())
                            .withAddress(Address.address()
                                    .withAddress1(address.getAddress1())
                                    .withAddress2(address.getAddress2())
                                    .withAddress3(address.getAddress3())
                                    .withAddress4(address.getAddress4())
                                    .withPostcode(address.getPostCode()))
                            .withDateOfBirth(defendant.getDateOfBirth())
                            .withBailStatus(defendant.getBailStatus())
                            .withCustodyTimeLimitDate(defendant.getCustodyTimeLimitDate())
                            .withDefenceOrganisation(defendant.getDefenceOrganisation())
                            .withInterpreter(Interpreter.interpreter()
                                    .withLanguage(interpreter.getLanguage())
                                    .withNeeded(interpreter.getNeeded())))
                    .withHearingIds(Collections.singletonList(hearingId))
                    .build();

            aggregate(NewModelHearingAggregate.class, hearingId, envelope, hearingAggregate -> hearingAggregate.updateDefendantDetails(defendantWithHearingCommand));

        }
    }
}
