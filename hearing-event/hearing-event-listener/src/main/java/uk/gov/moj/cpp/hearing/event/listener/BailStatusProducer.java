package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.UUID.fromString;

import uk.gov.justice.core.courts.BailStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class BailStatusProducer {
    private static final UUID RI_C_RESULT_DEFINITON_ID = fromString("d0a369c9-5a28-40ec-99cb-da7943550b18");

    private static final UUID RILA_L_RESULT_DEFINITON_ID = fromString("903b3e90-f185-40d3-92dd-6f81b73c4bb2");
    private static final UUID RILAB_L_RESULT_DEFINITON_ID = fromString("f666fd58-36c5-493f-aa11-89714faee6e6");
    private static final UUID RIB_P_RESULT_DEFINITON_ID = fromString("e26940b7-2534-42f2-9c44-c70072bf6ad2");
    private static final UUID CCSIB_P_RESULT_DEFINITON_ID = fromString("35430208-3705-44ce-b5d5-153c0337f6ab");

    private static final UUID CCIIYDA_S_RESULT_DEFINITON_ID = fromString("d271def7-14a1-4a92-a40b-b6ee5d4654ff");
    private static final UUID CCSIYDA_S_RESULT_DEFINITON_ID = fromString("404de620-d5ce-4eb4-87c4-f7e96271d240");

    private static final UUID REMCBY_B_RESULT_DEFINITON_ID = fromString("0536dbd2-b922-4899-9bc9-cad08429a889");
    private static final UUID RC_B_RESULT_DEFINITON_ID = fromString("3a529001-2f43-45ba-a0a8-d3ced7e9e7ad");
    private static final UUID RICD_B_RESULT_DEFINITON_ID = fromString("55639b76-055a-4557-97bb-f99f38fd5b2b");
    private static final UUID RCBV_B_RESULT_DEFINITON_ID = fromString("90d8268d-cc6a-4a09-bdb3-ddf8ea8ef2f9");
    private static final UUID CCSC_B_RESULT_DEFINITON_ID = fromString("b0076de5-5769-472f-b97d-31f3b5688cbf");
    private static final UUID CCIC_B_RESULT_DEFINITON_ID = fromString("b318ca35-8b6a-41e5-a674-879ac9a05cc2");
    private static final UUID REMCB_B_RESULT_DEFINITON_ID = fromString("f917ba0c-1faf-4945-83a8-50be9049f9b4");

    private static final UUID CCIU_U_RESULT_DEFINITON_ID = fromString("705140dc-833a-4aa0-a872-839009fc4494");
    private static final UUID REMUBY_U_RESULT_DEFINITON_ID = fromString("b0303006-5edf-402a-955f-94ce9d3916aa");
    private static final UUID REMUCB_U_RESULT_DEFINITON_ID = fromString("d1d31ca4-c9d6-43ac-ae6a-c591ab2d5d53");
    private static final UUID REMUB_U_RESULT_DEFINITON_ID = fromString("d076bd4a-17d5-4720-899a-1c6f96e3b35f");

    private static final UUID BAIL_STATUS_C_REF_DATA_ID = fromString("12e69486-4d01-3403-a50a-7419ca040635");
    private static final String BAIL_STATUS_C_REF_DATA_CODE = "C";
    private static final String BAIL_STATUS_C_REF_DATA_DESC = "Custody";

    private static final UUID BAIL_STATUS_P_REF_DATA_ID = fromString("34443c87-fa6f-34c0-897f-0cce45773df5");
    private static final String BAIL_STATUS_P_REF_DATA_CODE = "P";
    private static final String BAIL_STATUS_P_REF_DATA_DESC = "Conditional Bail with Pre-Release conditions";

    private static final UUID BAIL_STATUS_L_REF_DATA_ID = fromString("4dc146db-9d89-30bf-93b3-b22bc072d666");
    private static final String BAIL_STATUS_L_REF_DATA_CODE = "L";
    private static final String BAIL_STATUS_L_REF_DATA_DESC = "Remanded into care of Local Authority";

    private static final UUID BAIL_STATUS_S_REF_DATA_ID = fromString("549336f9-2a07-3767-960f-107da761a698");
    private static final String BAIL_STATUS_S_REF_DATA_CODE = "S";
    private static final String BAIL_STATUS_S_REF_DATA_DESC = "Remanded to youth detention accommodation";

    private static final UUID BAIL_STATUS_B_REF_DATA_ID = fromString("a5e5df07-c729-3f95-bf12-957c018eb526");
    private static final String BAIL_STATUS_B_REF_DATA_CODE = "B";
    private static final String BAIL_STATUS_B_REF_DATA_DESC = "Conditional Bail";

    private static final UUID BAIL_STATUS_U_REF_DATA_ID = fromString("4cfa861d-2931-30a6-a505-ddb91c95ab74");
    private static final String BAIL_STATUS_U_REF_DATA_CODE = "U";
    private static final String BAIL_STATUS_U_REF_DATA_DESC = "Unconditional Bail";


    private static final Map<UUID, BailStatus> bailStatusModelMap = new HashMap<>();

    static {
        bailStatusModelMap.put(RI_C_RESULT_DEFINITON_ID, getCustodyWithCodeC());

        bailStatusModelMap.put(RILA_L_RESULT_DEFINITON_ID, getCustodyWithCodeL());
        bailStatusModelMap.put(RILAB_L_RESULT_DEFINITON_ID, getCustodyWithCodeL());

        bailStatusModelMap.put(RIB_P_RESULT_DEFINITON_ID, getCustodyWithCodeP());
        bailStatusModelMap.put(CCSIB_P_RESULT_DEFINITON_ID, getCustodyWithCodeP());

        bailStatusModelMap.put(CCIIYDA_S_RESULT_DEFINITON_ID, getCustodyWithCodeS());
        bailStatusModelMap.put(CCSIYDA_S_RESULT_DEFINITON_ID, getCustodyWithCodeS());

        bailStatusModelMap.put(REMCBY_B_RESULT_DEFINITON_ID, getCustodyWithCodeB());
        bailStatusModelMap.put(RC_B_RESULT_DEFINITON_ID, getCustodyWithCodeB());
        bailStatusModelMap.put(RICD_B_RESULT_DEFINITON_ID, getCustodyWithCodeB());
        bailStatusModelMap.put(RCBV_B_RESULT_DEFINITON_ID, getCustodyWithCodeB());
        bailStatusModelMap.put(CCSC_B_RESULT_DEFINITON_ID, getCustodyWithCodeB());
        bailStatusModelMap.put(CCIC_B_RESULT_DEFINITON_ID, getCustodyWithCodeB());
        bailStatusModelMap.put(REMCB_B_RESULT_DEFINITON_ID, getCustodyWithCodeB());

        bailStatusModelMap.put(CCIU_U_RESULT_DEFINITON_ID, getCustodyWithCodeU());
        bailStatusModelMap.put(REMUBY_U_RESULT_DEFINITON_ID, getCustodyWithCodeU());
        bailStatusModelMap.put(REMUCB_U_RESULT_DEFINITON_ID, getCustodyWithCodeU());
        bailStatusModelMap.put(REMUB_U_RESULT_DEFINITON_ID, getCustodyWithCodeU());

    }

    public Optional<BailStatus> getBailStatus(final UUID resultDefinitionId) {
        final BailStatus bailStatus = bailStatusModelMap.get(resultDefinitionId);
        if (null != bailStatus) {
            return Optional.of(bailStatus);
        }
        return Optional.empty();
    }

    private static BailStatus getCustodyWithCodeC() {
        return new BailStatus.Builder().withId(BAIL_STATUS_C_REF_DATA_ID).withCode(BAIL_STATUS_C_REF_DATA_CODE).withDescription(BAIL_STATUS_C_REF_DATA_DESC).build();
    }

    private static BailStatus getCustodyWithCodeP() {
        return new BailStatus.Builder().withId(BAIL_STATUS_P_REF_DATA_ID).withCode(BAIL_STATUS_P_REF_DATA_CODE).withDescription(BAIL_STATUS_P_REF_DATA_DESC).build();
    }

    private static BailStatus getCustodyWithCodeL() {
        return new BailStatus.Builder().withId(BAIL_STATUS_L_REF_DATA_ID).withCode(BAIL_STATUS_L_REF_DATA_CODE).withDescription(BAIL_STATUS_L_REF_DATA_DESC).build();
    }

    private static BailStatus getCustodyWithCodeS() {
        return new BailStatus.Builder().withId(BAIL_STATUS_S_REF_DATA_ID).withCode(BAIL_STATUS_S_REF_DATA_CODE).withDescription(BAIL_STATUS_S_REF_DATA_DESC).build();
    }

    private static BailStatus getCustodyWithCodeB() {
        return new BailStatus.Builder().withId(BAIL_STATUS_B_REF_DATA_ID).withCode(BAIL_STATUS_B_REF_DATA_CODE).withDescription(BAIL_STATUS_B_REF_DATA_DESC).build();
    }

    private static BailStatus getCustodyWithCodeU() {
        return new BailStatus.Builder().withId(BAIL_STATUS_U_REF_DATA_ID).withCode(BAIL_STATUS_U_REF_DATA_CODE).withDescription(BAIL_STATUS_U_REF_DATA_DESC).build();
    }
}
