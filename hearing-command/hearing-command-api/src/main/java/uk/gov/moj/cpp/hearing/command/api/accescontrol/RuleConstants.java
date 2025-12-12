package uk.gov.moj.cpp.hearing.command.api.accescontrol;

import static uk.gov.moj.cpp.hearing.common.util.ObjectTypes.CASE;
import static uk.gov.moj.cpp.hearing.common.util.ActionTypes.CREATE;

import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.moj.cpp.accesscontrol.drools.ExpectedPermission;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


@SuppressWarnings("WeakerAccess")
public class RuleConstants {

    private static final ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();

    private RuleConstants() {
    }

    public static String[] expectedPermissionsForCase() throws JsonProcessingException {
        final ExpectedPermission expectedPermissionsForCase = ExpectedPermission.builder()
                .withAction(CREATE.toString())
                .withObject(CASE.toString())
                .build();
        return new String[]{objectMapper.writeValueAsString(expectedPermissionsForCase)};
    }

    public static String[] getUsersForInitiateHearing() {
        return new String[]{"Probation Admin", "Listing Officers", "Court Clerks", "Legal Advisers", "Court Administrators", "Crown Court Admin", "System Users","Court Associate", "Magistrates"};
    }
}
