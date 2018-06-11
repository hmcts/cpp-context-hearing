package uk.gov.moj.cpp.hearing.steps.data.factory;

import static java.util.UUID.randomUUID;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.FUTURE_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.INTEGER;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_ZONED_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;

import uk.gov.justice.services.common.converter.LocalDates;
import uk.gov.justice.services.common.converter.ZonedDateTimes;

import java.util.UUID;

import javax.json.JsonObject;

import org.fluttercode.datafactory.impl.DataFactory;

public class ProgressionDataFactory {

    public static JsonObject hearingConfirmedFor(final UUID hearingId) {
        return hearingConfirmedFor(hearingId, randomUUID());
    }

    public static JsonObject hearingConfirmedFor(final UUID hearingId, final UUID caseId) {
        final DataFactory dataFactory = new DataFactory();

        return createObjectBuilder()
                .add("caseId", caseId.toString())
                .add("urn", STRING.next())
                .add("hearing", createObjectBuilder()
                        .add("id", hearingId.toString())
                        .add("type", "TRIAL")
                        .add("courtCentreId", randomUUID().toString())
                        .add("courtCentreName", "Liverpool Crown Court")
                        .add("courtRoomId", randomUUID().toString())
                        .add("courtRoomName", INTEGER.next().toString())
                        .add("judge", createObjectBuilder()
                                .add("id", randomUUID().toString())
                                .add("title", "HHJ")
                                .add("firstName", dataFactory.getFirstName())
                                .add("lastName", dataFactory.getLastName())
                        )
                        .add("defendants", createArrayBuilder()
                                .add(
                                        createObjectBuilder()
                                                .add("id", randomUUID().toString())
                                                .add("personId", randomUUID().toString())
                                                .add("firstName", dataFactory.getFirstName())
                                                .add("lastName", dataFactory.getLastName())
                                                .add("dateOfBirth", LocalDates.to(PAST_LOCAL_DATE.next()))
                                                .add("bailStatus", "inCustody")
                                                .add("custodyTimeLimit", LocalDates.to(FUTURE_LOCAL_DATE.next()))
                                                .add("defenceOrganisation", dataFactory.getBusinessName())
                                                .add("offences", createArrayBuilder()
                                                        .add(
                                                                createObjectBuilder()
                                                                        .add("id", randomUUID().toString())
                                                                        .add("offenceCode", "OF61131")
                                                                        .add("startDate", LocalDates.to(PAST_LOCAL_DATE.next()))
                                                                        .add("endDate", LocalDates.to(FUTURE_LOCAL_DATE.next()))
                                                                        .add("statementOfOffence", createObjectBuilder()
                                                                                .add("title", "Wounding with intent")
                                                                                .add("legislation", STRING.next())
                                                                        )
                                                        )
                                                )
                                )
                        )
                )
                .build();

    }
}
