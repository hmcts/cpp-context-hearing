package uk.gov.moj.cpp.hearing.event.nows;

import static java.util.UUID.fromString;
import java.util.UUID;

@SuppressWarnings({"squid:S1213"})
public class PromptTypesConstant {

    public static final String P_AMOUNT_OF_SURCHARGE =  "AOS";
    public static final String P_AMOUNT_OF_FINE = "AOF";
    public static final String P_AMOUNT_OF_COSTS = "AOC";
    public static final String P_AMOUNT_OF_BACK_DUTY = "AOBD";
    public static final String P_AMOUNT_OF_COMPENSATION = "AOCOM";
    public static final String P_CREDITOR_NAME = "CREDNAME";
    public static final String P_LUMP_SUM_AMOUNT = "LSA";
    public static final String P_LUMP_SUM_PAY_WITHIN = "LSPW";
    public static final String P_PAYMENT_FREQUENCY = "PF";
    public static final String P_INSTALMENT_START_DATE = "ISTD";
    public static final String P_INSTALMENT_AMOUNT = "IAMT";
    public static final String P_PAY_BY_DATE = "PBD";
    public static final String P_NON_STANDARD_REASON = "NSR";


    public static final String P_PAYMENT_CARD_REQUIRED_PROMPT_REFERENCE = "PAYMENT_CARD_REQUIRED";
    public static final String P_PARENT_GUARDIAN_TOPAY_PROMPT_REFERENCE = "PARENT_GAURDIAN_TO_PAY";
    //the label is "Number of days in default"
    public static final String P_DEFAULT_DAYS_IN_JAIL_PROMPT_REFERENCE = "DID";

    // Fixed List for Payment Frequency
    public static final String P_WEEKLY = "weekly";
    public static final String P_FORTNIGHTLY = "fortnightly";
    public static final String P_MONTHLY = "monthly";

    // Fixed List for LUMP SUM PAY WITHIN
    public static final String P_FOURTEEN = "14 days";
    public static final String P_TWOEIGHT = "28 days";

    public static final String EMPLOYER_ORGANISATION_NAME_PROMPT_REFERENCE = "employerName";
    public static final String EMPLOYER_ORGANISATION_ADDRESS1_PROMPT_REFERENCE = "employerAddress1";
    public static final String EMPLOYER_ORGANISATION_ADDRESS2_PROMPT_REFERENCE = "employerAddress2";
    public static final String EMPLOYER_ORGANISATION_ADDRESS3_PROMPT_REFERENCE = "employerAddress3";
    public static final String EMPLOYER_ORGANISATION_ADDRESS4_PROMPT_REFERENCE = "employerAddress4";
    public static final String EMPLOYER_ORGANISATION_ADDRESS5_PROMPT_REFERENCE = "employerAddress5";
    public static final String EMPLOYER_ORGANISATION_POST_CODE_PROMPT_REFERENCE = "employerPostCode";
    public static final String EMPLOYER_ORGANISATION_REFERENCE_NUMBER_PROMPT_REFERENCE = "employerReferenceNumber";

    public static final String VICTIM_SURCHARGE_AMOUNT_PROMPT_REFERENCE = "AOS";
    public static final String FINE_AMOUNT_PROMPT_REFERENCE = "AOF";
    public static final String COMPENSATION_AMOUNT_PROMPT_REFERENCE = "AOCOM";

    public static final String VEHICLE_EXCISE_BACK_DUTY_AMOUNT_PROMPT_REFERENCE = "AOBD";
    public static final String COSTS_TO_CROWN_PROSECUTION_SERVICE_AMOUNT_PROMPT_REFERENCE = "FCPC";

    //this needs to get added to the reference data sheet
    public static final String TOTAL_AMOUNT_ENFORCED_PROMPT_REFERENCE = "TOTENF";

    public static final String AMOUNT_OF_COSTS_PROMPT_REFERENCE = "AOC";

    //For free text capture in ABDC (Benefit deduction)
    public static final UUID P_DWP_AP_NUMBER = fromString("91687bb9-d0ca-44a7-ada0-b89b71b89b8e");


    private PromptTypesConstant() {
    }
}
