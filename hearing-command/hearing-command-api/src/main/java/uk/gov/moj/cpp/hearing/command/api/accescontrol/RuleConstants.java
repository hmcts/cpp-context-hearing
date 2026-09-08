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

    /**
     * The groups that may write a hearing's tier and list type — save, finalise or delete.
     *
     * <p>Deliberately the same five groups that may record a plea or a draft result
     * ({@code hearing.update-plea}, {@code hearing.save-draft-result}): tier and list type are
     * part of the formal court record, so court staff write them even though the judge decides
     * their content. The judicial groups are therefore absent here, though they can still read
     * the values — see the query side's equivalent.
     */
    public static String[] getUsersForPtphDetail() {
        return new String[]{"Listing Officers", "Court Clerks", "Legal Advisers", "Court Associate", "Court Administrators"};
    }
}
