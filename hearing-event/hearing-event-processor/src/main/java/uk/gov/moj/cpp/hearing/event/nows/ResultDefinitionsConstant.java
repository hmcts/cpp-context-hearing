package uk.gov.moj.cpp.hearing.event.nows;

import static java.util.Arrays.asList;
import static java.util.UUID.fromString;

import java.util.List;
import java.util.UUID;

@SuppressWarnings({"squid:S1213", "squid:S2386"})
public final class ResultDefinitionsConstant {

    //List of Payment Terms
    public static final UUID RD_PDATE = fromString("bcb5a496-f7cf-11e8-8eb2-f2801f1b9fd1");
    public static final UUID RD_LUMSI = fromString("272d1ec2-634b-11e8-adc0-fa7ae01bbebc");
    public static final UUID RD_INSTL = fromString("6d76b10c-64c4-11e8-adc0-fa7ae01bbebc");

    public static final UUID RD_FINE = fromString("969f150c-cd05-46b0-9dd9-30891efcc766");
    public static final UUID RD_COSTSTOCROWNPROSECUTIONSERVICE = fromString("f5d492b8-a09b-4f70-9ceb-aa06c306a7dc");
    public static final UUID RD_SURCHARGE = fromString("e866cd11-6073-4fdf-a229-51c9d694e1d0");
    public static final UUID RD_VEHICLEEXCISEBACKDUTY = fromString("5edd3a3a-8dc7-43e4-96c4-10fed16278ac");
    public static final UUID RD_COSTS = fromString("76d43772-0660-4a33-b5c6-8f8ccaf6b4e3");
    public static final UUID RD_COMPENSATION = fromString("ae89b99c-e0e3-47b5-b218-24d4fca3ca53");
    public static final UUID RD_RLSUM = fromString("a09bbfa0-5dd5-11e8-9c2d-fa7ae01bbebc");
    public static final UUID RD_RLSUMI = fromString("d6e93aae-5dd7-11e8-9c2d-fa7ae01bbebc");
    public static final UUID RD_RINSTL = fromString("9ba8f03a-5dda-11e8-9c2d-fa7ae01bbebc");
    public static final UUID RD_COLLECTIONORDER = fromString("9ea0d845-5096-44f6-9ce0-8ae801141eac");
    public static final UUID RD_ABCD = fromString("f7dfefd2-64c6-11e8-adc0-fa7ae01bbebc");
    public static final UUID RD_AEOC = fromString("bdb32555-8d55-4dc1-b4b6-580db5132496");

    public static final List<UUID> PAYMENT_TERMS_RESULT_DEFINITIONS = asList(
            RD_PDATE, RD_LUMSI, RD_LUMSI, RD_INSTL,
            RD_FINE, RD_COSTSTOCROWNPROSECUTIONSERVICE, RD_SURCHARGE, RD_VEHICLEEXCISEBACKDUTY,
            RD_COSTS, RD_COMPENSATION, RD_ABCD, RD_AEOC);

    public static final UUID BENEFIT_DEDUCTIONS_RESULT_DEFINITION_ID = RD_ABCD;
    public static final UUID ATTACHMENT_OF_EARNINGS_RESULT_DEFINITION_ID = RD_AEOC;
    public static final UUID NOTICE_OF_FINANCIAL_PENALTY_NOW_DEFINITION_ID = fromString("66cd749a-1d51-11e8-accf-0ed5f89f718b");
    public static final UUID ATTACHMENT_OF_EARNINGS_NOW_DEFINITION_ID = fromString("10115268-8efc-49fe-b8e8-feee216a03da");

    public static final UUID NEXT_HEARING_IN_MAGISTRATES_COURT_RESULT_DEFINITION_ID = fromString("70c98fa6-804d-11e8-adc0-fa7ae01bbebc");

    private ResultDefinitionsConstant() {
    }
}
