package uk.gov.moj.cpp.hearing.domain.transformation.mot.util;

import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.ALLOCATION_DECISION;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.ALLOCATION_DECISION_DATE;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.AQUITTAL_DATE;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.ARREST_DATE;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.CATEGORY;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.CHARGE_DATE;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.CONVICTION_DATE;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.COUNT;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.DATE_OF_INFORMATION;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.DESCRIPTION;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.END_DATE;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.INDICATED_PLEA;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.INDICATED_PLEA_DATE;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.INDICATED_PLEA_VALUE;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.INDICTABLE_ONLY_OFFENCE;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.JUDICIAL_RESULTS;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.MODE_OF_TRIAL;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.MOT_REASON_CODE;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.MOT_REASON_DESCRIPTION;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.MOT_REASON_ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.MOT_REASON_ID_1;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.MOT_REASON_ID_2;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.MOT_REASON_ID_3;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.NOTIFIED_PLEA;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.NO_MODE_OF_TRIAL_EITHER_WAY_OFFENCE;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.OFFENCE_CODE;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.OFFENCE_DEFINITION_ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.OFFENCE_FACTS;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.OFFENCE_ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.OFFENCE_LEGISLATION;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.OFFENCE_LEGISLATION_WELSH;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.OFFENCE_TITLE;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.OFFENCE_TITLE_WELSH;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.ORDER_INDEX;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.ORIGINATING_HEARING_ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.PLEA;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.REASON;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.SECTION;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.SEQUENCE_NUMBER;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.SOURCE;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.START_DATE;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.SUMMARY_ONLY_OFFENCE;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.VERDICT;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.VICTIMS;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.WORDING;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.WORDING_WELSH;

import java.util.HashMap;
import java.util.Map;

import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

@SuppressWarnings({"squid:MethodCyclomaticComplexity", "squid:S3776"})
public class OffenceHelper {

    private OffenceHelper() {
    }

    public static JsonArray transformOffences(final JsonArray offenceJsonObjects, final String hearingId) {

        final JsonArrayBuilder offenceList = createArrayBuilder();
        offenceJsonObjects.forEach(o -> {
            final JsonObject offence = (JsonObject) o;
            final JsonObjectBuilder offenceBuilder = transformOffence(offence, hearingId);
            offenceList.add(offenceBuilder.build());
        });
        return offenceList.build();

    }

    public static JsonObjectBuilder transformOffence(final JsonObject offence, final String hearingId) {
        //add required fields
        final JsonObjectBuilder offenceBuilder = createObjectBuilder()
                .add(ID, offence.getString(ID))
                .add(OFFENCE_DEFINITION_ID, offence.getString(OFFENCE_DEFINITION_ID))
                .add(OFFENCE_CODE, offence.getString(OFFENCE_CODE))
                .add(OFFENCE_TITLE, offence.getString(OFFENCE_TITLE))
                .add(WORDING, offence.getString(WORDING))
                .add(START_DATE, offence.getString(START_DATE));

        // add optional fields
        if (offence.containsKey(OFFENCE_TITLE_WELSH)) {
            offenceBuilder.add(OFFENCE_TITLE_WELSH, offence.getString(OFFENCE_TITLE_WELSH));
        }

        if (offence.containsKey(OFFENCE_LEGISLATION)) {
            offenceBuilder.add(OFFENCE_LEGISLATION, offence.getString(OFFENCE_LEGISLATION));
        }

        if (offence.containsKey(OFFENCE_LEGISLATION_WELSH)) {
            offenceBuilder.add(OFFENCE_LEGISLATION_WELSH, offence.getString(OFFENCE_LEGISLATION_WELSH));
        }

        if (offence.containsKey(MODE_OF_TRIAL)) {
            offenceBuilder.add(MODE_OF_TRIAL, derivedModeOfTrial(offence.getString(MODE_OF_TRIAL)));
        }

        if (offence.containsKey(WORDING_WELSH)) {
            offenceBuilder.add(WORDING_WELSH, offence.getString(WORDING_WELSH));
        }

        if (offence.containsKey(END_DATE)) {
            offenceBuilder.add(END_DATE, offence.getString(END_DATE));
        }

        if (offence.containsKey(ARREST_DATE)) {
            offenceBuilder.add(ARREST_DATE, offence.getString(ARREST_DATE));
        }

        if (offence.containsKey(CHARGE_DATE)) {
            offenceBuilder.add(CHARGE_DATE, offence.getString(CHARGE_DATE));
        }

        if (offence.containsKey(ORDER_INDEX)) {
            offenceBuilder.add(ORDER_INDEX, offence.getInt(ORDER_INDEX));
        }

        if (offence.containsKey(DATE_OF_INFORMATION)) {
            offenceBuilder.add(DATE_OF_INFORMATION, offence.getString(DATE_OF_INFORMATION));
        }

        if (offence.containsKey(COUNT)) {
            offenceBuilder.add(COUNT, offence.getInt(COUNT));
        }

        if (offence.containsKey(CONVICTION_DATE)) {
            offenceBuilder.add(CONVICTION_DATE, offence.getString(CONVICTION_DATE));
        }

        if (offence.containsKey(NOTIFIED_PLEA)) {
            offenceBuilder.add(NOTIFIED_PLEA, offence.getJsonObject(NOTIFIED_PLEA));
        }

        if (offence.containsKey(VERDICT)) {
            offenceBuilder.add(VERDICT, offence.getJsonObject(VERDICT));
        }

        if (offence.containsKey(OFFENCE_FACTS)) {
            offenceBuilder.add(OFFENCE_FACTS, offence.getJsonObject(OFFENCE_FACTS));
        }

        if (offence.containsKey(PLEA)) {
            offenceBuilder.add(PLEA, offence.getJsonObject(PLEA));
            if (offence.containsKey(MODE_OF_TRIAL)) {
                if ("IND".equalsIgnoreCase(offence.getString(MODE_OF_TRIAL))) {
                    offenceBuilder.add(ALLOCATION_DECISION, transformAllocationDecision(offence.getJsonObject(PLEA), hearingId, "4ba29b9f-9e57-32ed-b376-1840f4ba6c53", 20, "2", "Indictable-only offence"));
                } else if ("EWAY".equalsIgnoreCase(offence.getString(MODE_OF_TRIAL))) {
                    offenceBuilder.add(ALLOCATION_DECISION, transformAllocationDecision(offence.getJsonObject(PLEA), hearingId, "78efce20-8a52-3272-9d22-2e7e6e3e565e", 70, "7", "No mode of Trial - Either way offence"));
                } else {
                    offenceBuilder.add(ALLOCATION_DECISION, transformAllocationDecision(offence.getJsonObject(PLEA), hearingId, "b8c37e33-defd-351c-b91e-1e03e51657da", 10, "1", "Summary-only offence"));
                }
            } else {
                offenceBuilder.add(ALLOCATION_DECISION, transformAllocationDecision(offence.getJsonObject(PLEA), hearingId, "b8c37e33-defd-351c-b91e-1e03e51657da", 10, "1", "Summary-only offence"));
            }
        }

        if (offence.containsKey(INDICATED_PLEA)) {
            offenceBuilder.add(INDICATED_PLEA, transformIndicatedPlea(offence.getJsonObject(INDICATED_PLEA)));
            if (offence.getJsonObject(INDICATED_PLEA).containsKey(ALLOCATION_DECISION)) {
                offenceBuilder.add(ALLOCATION_DECISION, transformAllocationDecision(offence.getJsonObject(INDICATED_PLEA), hearingId));
            }
        }

        if (offence.containsKey(AQUITTAL_DATE)) {
            offenceBuilder.add(AQUITTAL_DATE, offence.getString(AQUITTAL_DATE));
        }

        if (offence.containsKey(JUDICIAL_RESULTS)) {
            offenceBuilder.add(JUDICIAL_RESULTS, offence.getJsonArray(JUDICIAL_RESULTS));
        }

        if (offence.containsKey(VICTIMS)) {
            offenceBuilder.add(VICTIMS, offence.getJsonArray(VICTIMS));
        }

        return offenceBuilder;

    }

    @SuppressWarnings({"squid:S1066","squid:S134"})
    public static JsonObject transformOffence(final JsonObject offence) {

            final JsonObjectBuilder offenceBuilder = createObjectBuilder()
                    .add(ID, offence.getString(ID))
                    .add(OFFENCE_DEFINITION_ID, offence.getString(OFFENCE_DEFINITION_ID))
                    .add(OFFENCE_TITLE, offence.getString(OFFENCE_TITLE))
                    .add(WORDING, offence.getString(WORDING))
                    .add(START_DATE, offence.getString(START_DATE));

            if (offence.containsKey(OFFENCE_CODE)) {
                offenceBuilder.add(OFFENCE_CODE, offence.getString(OFFENCE_CODE));
            }
            // add optional fields
            if (offence.containsKey(OFFENCE_TITLE_WELSH)) {
                offenceBuilder.add(OFFENCE_TITLE_WELSH, offence.getString(OFFENCE_TITLE_WELSH));
            }

            if (offence.containsKey(OFFENCE_LEGISLATION)) {
                offenceBuilder.add(OFFENCE_LEGISLATION, offence.getString(OFFENCE_LEGISLATION));
            }

            if (offence.containsKey(OFFENCE_LEGISLATION_WELSH)) {
                offenceBuilder.add(OFFENCE_LEGISLATION_WELSH, offence.getString(OFFENCE_LEGISLATION_WELSH));
            }

            if (offence.containsKey(MODE_OF_TRIAL)) {
                offenceBuilder.add(MODE_OF_TRIAL, derivedModeOfTrial(offence.getString(MODE_OF_TRIAL)));
            }

            if (offence.containsKey(WORDING_WELSH)) {
                offenceBuilder.add(WORDING_WELSH, offence.getString(WORDING_WELSH));
            }

            if (offence.containsKey(END_DATE)) {
                offenceBuilder.add(END_DATE, offence.getString(END_DATE));
            }

            if (offence.containsKey(ARREST_DATE)) {
                offenceBuilder.add(ARREST_DATE, offence.getString(ARREST_DATE));
            }

            if (offence.containsKey(CHARGE_DATE)) {
                offenceBuilder.add(CHARGE_DATE, offence.getString(CHARGE_DATE));
            }

            if (offence.containsKey(ORDER_INDEX)) {
                offenceBuilder.add(ORDER_INDEX, offence.getInt(ORDER_INDEX));
            }

            if (offence.containsKey(DATE_OF_INFORMATION)) {
                offenceBuilder.add(DATE_OF_INFORMATION, offence.getString(DATE_OF_INFORMATION));
            }

            if (offence.containsKey(COUNT)) {
                offenceBuilder.add(COUNT, offence.getInt(COUNT));
            }

            if (offence.containsKey(CONVICTION_DATE)) {
                offenceBuilder.add(CONVICTION_DATE, offence.getString(CONVICTION_DATE));
            }

            if (offence.containsKey(NOTIFIED_PLEA)) {
                offenceBuilder.add(NOTIFIED_PLEA, offence.getJsonObject(NOTIFIED_PLEA));
            }

            if (offence.containsKey(VERDICT)) {
                offenceBuilder.add(VERDICT, offence.getJsonObject(VERDICT));
            }

            if (offence.containsKey(OFFENCE_FACTS)) {
                offenceBuilder.add(OFFENCE_FACTS, offence.getJsonObject(OFFENCE_FACTS));
            }

            if (offence.containsKey(AQUITTAL_DATE)) {
                offenceBuilder.add(AQUITTAL_DATE, offence.getString(AQUITTAL_DATE));
            }

            if (offence.containsKey(JUDICIAL_RESULTS)) {
                offenceBuilder.add(JUDICIAL_RESULTS, offence.getJsonArray(JUDICIAL_RESULTS));
            }

            if (offence.containsKey(VICTIMS)) {
                offenceBuilder.add(VICTIMS, offence.getJsonArray(VICTIMS));
            }

            if (offence.containsKey(PLEA)) {
                offenceBuilder.add(PLEA, offence.getJsonObject(PLEA));
                if(offence.getJsonObject(PLEA).containsKey(ORIGINATING_HEARING_ID)){
                    if (offence.containsKey(MODE_OF_TRIAL)) {
                        if ("IND".equalsIgnoreCase(offence.getString(MODE_OF_TRIAL))) {
                            offenceBuilder.add(ALLOCATION_DECISION, transformAllocationDecision(offence.getJsonObject(PLEA), offence.getJsonObject(PLEA).getString(ORIGINATING_HEARING_ID), MOT_REASON_ID_1, 20, "2", INDICTABLE_ONLY_OFFENCE));
                        } else if ("EWAY".equalsIgnoreCase(offence.getString(MODE_OF_TRIAL))) {
                            offenceBuilder.add(ALLOCATION_DECISION, transformAllocationDecision(offence.getJsonObject(PLEA), offence.getJsonObject(PLEA).getString(ORIGINATING_HEARING_ID), MOT_REASON_ID_2, 70, "7", NO_MODE_OF_TRIAL_EITHER_WAY_OFFENCE));
                        }else {
                            offenceBuilder.add(ALLOCATION_DECISION, transformAllocationDecision(offence.getJsonObject(PLEA), offence.getJsonObject(PLEA).getString(ORIGINATING_HEARING_ID), MOT_REASON_ID_3, 10, "1", SUMMARY_ONLY_OFFENCE));
                        }
                    }
                }
            }


            if (offence.containsKey(INDICATED_PLEA)) {
                offenceBuilder.add(INDICATED_PLEA, transformIndicatedPlea(offence.getJsonObject(INDICATED_PLEA)));
                if (offence.getJsonObject(INDICATED_PLEA).containsKey(ALLOCATION_DECISION) && offence.getJsonObject(INDICATED_PLEA).containsKey(ORIGINATING_HEARING_ID)) {
                    offenceBuilder.add(ALLOCATION_DECISION, transformAllocationDecision(offence.getJsonObject(INDICATED_PLEA), offence.getJsonObject(INDICATED_PLEA).getString(ORIGINATING_HEARING_ID)));
                }
            }


        return offenceBuilder.build();

    }

    @SuppressWarnings({"squid:S1066","squid:S134", "squid:S1188"})
    public static JsonArray transformOffences(final JsonArray offenceJsonObjects) {
        final JsonArrayBuilder offenceList = createArrayBuilder();
        offenceJsonObjects.forEach(o -> {
            final JsonObject offence = (JsonObject) o;

            //add required fields,
            final JsonObjectBuilder offenceBuilder = createObjectBuilder()
                    .add(ID, offence.getString(ID))
                    .add(OFFENCE_DEFINITION_ID, offence.getString(OFFENCE_DEFINITION_ID))
                    .add(OFFENCE_TITLE, offence.getString(OFFENCE_TITLE))
                    .add(WORDING, offence.getString(WORDING))
                    .add(START_DATE, offence.getString(START_DATE));

            if (offence.containsKey(OFFENCE_CODE)) {
                offenceBuilder.add(OFFENCE_CODE, offence.getString(OFFENCE_CODE));
            }
            // add optional fields
            if (offence.containsKey(OFFENCE_TITLE_WELSH)) {
                offenceBuilder.add(OFFENCE_TITLE_WELSH, offence.getString(OFFENCE_TITLE_WELSH));
            }

            if (offence.containsKey(OFFENCE_LEGISLATION)) {
                offenceBuilder.add(OFFENCE_LEGISLATION, offence.getString(OFFENCE_LEGISLATION));
            }

            if (offence.containsKey(OFFENCE_LEGISLATION_WELSH)) {
                offenceBuilder.add(OFFENCE_LEGISLATION_WELSH, offence.getString(OFFENCE_LEGISLATION_WELSH));
            }

            if (offence.containsKey(MODE_OF_TRIAL)) {
                offenceBuilder.add(MODE_OF_TRIAL, derivedModeOfTrial(offence.getString(MODE_OF_TRIAL)));
            }

            if (offence.containsKey(WORDING_WELSH)) {
                offenceBuilder.add(WORDING_WELSH, offence.getString(WORDING_WELSH));
            }

            if (offence.containsKey(END_DATE)) {
                offenceBuilder.add(END_DATE, offence.getString(END_DATE));
            }

            if (offence.containsKey(ARREST_DATE)) {
                offenceBuilder.add(ARREST_DATE, offence.getString(ARREST_DATE));
            }

            if (offence.containsKey(CHARGE_DATE)) {
                offenceBuilder.add(CHARGE_DATE, offence.getString(CHARGE_DATE));
            }

            if (offence.containsKey(ORDER_INDEX)) {
                offenceBuilder.add(ORDER_INDEX, offence.getInt(ORDER_INDEX));
            }

            if (offence.containsKey(DATE_OF_INFORMATION)) {
                offenceBuilder.add(DATE_OF_INFORMATION, offence.getString(DATE_OF_INFORMATION));
            }

            if (offence.containsKey(COUNT)) {
                offenceBuilder.add(COUNT, offence.getInt(COUNT));
            }

            if (offence.containsKey(CONVICTION_DATE)) {
                offenceBuilder.add(CONVICTION_DATE, offence.getString(CONVICTION_DATE));
            }

            if (offence.containsKey(NOTIFIED_PLEA)) {
                offenceBuilder.add(NOTIFIED_PLEA, offence.getJsonObject(NOTIFIED_PLEA));
            }

            if (offence.containsKey(VERDICT)) {
                offenceBuilder.add(VERDICT, offence.getJsonObject(VERDICT));
            }

            if (offence.containsKey(OFFENCE_FACTS)) {
                offenceBuilder.add(OFFENCE_FACTS, offence.getJsonObject(OFFENCE_FACTS));
            }

            if (offence.containsKey(AQUITTAL_DATE)) {
                offenceBuilder.add(AQUITTAL_DATE, offence.getString(AQUITTAL_DATE));
            }

            if (offence.containsKey(JUDICIAL_RESULTS)) {
                offenceBuilder.add(JUDICIAL_RESULTS, offence.getJsonArray(JUDICIAL_RESULTS));
            }

            if (offence.containsKey(VICTIMS)) {
                offenceBuilder.add(VICTIMS, offence.getJsonArray(VICTIMS));
            }

            if (offence.containsKey(PLEA)) {
                offenceBuilder.add(PLEA, offence.getJsonObject(PLEA));
                if(offence.getJsonObject(PLEA).containsKey(ORIGINATING_HEARING_ID)){
                    if (offence.containsKey(MODE_OF_TRIAL)) {
                        if ("IND".equalsIgnoreCase(offence.getString(MODE_OF_TRIAL))) {
                            offenceBuilder.add(ALLOCATION_DECISION, transformAllocationDecision(offence.getJsonObject(PLEA), offence.getJsonObject(PLEA).getString(ORIGINATING_HEARING_ID), MOT_REASON_ID_1, 20, "2", INDICTABLE_ONLY_OFFENCE));
                        } else if ("EWAY".equalsIgnoreCase(offence.getString(MODE_OF_TRIAL))) {
                            offenceBuilder.add(ALLOCATION_DECISION, transformAllocationDecision(offence.getJsonObject(PLEA), offence.getJsonObject(PLEA).getString(ORIGINATING_HEARING_ID), MOT_REASON_ID_2, 70, "7", NO_MODE_OF_TRIAL_EITHER_WAY_OFFENCE));
                        }else {
                            offenceBuilder.add(ALLOCATION_DECISION, transformAllocationDecision(offence.getJsonObject(PLEA), offence.getJsonObject(PLEA).getString(ORIGINATING_HEARING_ID), MOT_REASON_ID_3, 10, "1", SUMMARY_ONLY_OFFENCE));
                        }
                    }
                }
            }


            if (offence.containsKey(INDICATED_PLEA)) {
                offenceBuilder.add(INDICATED_PLEA, transformIndicatedPlea(offence.getJsonObject(INDICATED_PLEA)));
                if (offence.getJsonObject(INDICATED_PLEA).containsKey(ALLOCATION_DECISION) && offence.getJsonObject(INDICATED_PLEA).containsKey(ORIGINATING_HEARING_ID)) {
                    offenceBuilder.add(ALLOCATION_DECISION, transformAllocationDecision(offence.getJsonObject(INDICATED_PLEA), offence.getJsonObject(INDICATED_PLEA).getString(ORIGINATING_HEARING_ID)));
                }
            }

            offenceList.add(offenceBuilder.build());
        });
        return offenceList.build();

    }

    private static String derivedModeOfTrial(final String modeOfTrial) {
        return getDerivedModOfTrial().getOrDefault(modeOfTrial.toUpperCase(), "Summary");
    }

    private static Map<String, String> getDerivedModOfTrial() {
        final Map<String, String> derivedModeOfTrial = new HashMap<>();
        derivedModeOfTrial.put("EWAY", "Either Way");
        derivedModeOfTrial.put("IND", "Indictable");
        derivedModeOfTrial.put("SIMP", "Summary");
        derivedModeOfTrial.put("STRAFF", "Summary");
        derivedModeOfTrial.put("SNONIMP", "Summary");
        derivedModeOfTrial.put("CIVIL", "Summary");
        return derivedModeOfTrial;
    }


    private static JsonObject transformAllocationDecision(final JsonObject jsonObject, final String hearingId) {
        final JsonObjectBuilder jsonObjectBuilder = createObjectBuilder();
        if (jsonObject.containsKey(ORIGINATING_HEARING_ID)) {
            jsonObjectBuilder.add(ORIGINATING_HEARING_ID, jsonObject.getString(ORIGINATING_HEARING_ID));
        } else {
            jsonObjectBuilder.add(ORIGINATING_HEARING_ID, hearingId);
        }
        //required
        jsonObjectBuilder
                .add(ALLOCATION_DECISION_DATE, jsonObject.getString(INDICATED_PLEA_DATE))
                .add(MOT_REASON_ID, "f8eb278a-8bce-373e-b365-b45e939da38a")
                .add(MOT_REASON_DESCRIPTION, "Defendant chooses trial by jury")
                .add(MOT_REASON_CODE, "4")
                .add(SEQUENCE_NUMBER, 40);
        // add optional attributen
        if (jsonObject.getJsonObject(ALLOCATION_DECISION).containsKey("indicationOfSentence")) {
            jsonObjectBuilder.add("courtIndicatedSentence", createObjectBuilder()
                    .add("courtIndicatedSentenceTypeId", "d3d94468-02a4-3259-b55d-38e6d163e820")
                    .add("courtIndicatedSentenceDescription", jsonObject.getJsonObject(ALLOCATION_DECISION).getString("indicationOfSentence"))
                    .build());
        }
        return jsonObjectBuilder.build();
    }


    private static JsonObject transformAllocationDecision(final JsonObject jsonObject, final String hearingId, final String motReasonId, final int sequenceNumber, final String motReasonCode, final String motReasonDescription) {
        final JsonObjectBuilder jsonObjectBuilder = createObjectBuilder();
        if (jsonObject.containsKey(ORIGINATING_HEARING_ID)) {
            jsonObjectBuilder.add(ORIGINATING_HEARING_ID, jsonObject.getString(ORIGINATING_HEARING_ID));
        } else {
            jsonObjectBuilder.add(ORIGINATING_HEARING_ID, hearingId);
        }
        //required
        jsonObjectBuilder
                .add(ALLOCATION_DECISION_DATE, jsonObject.getString("pleaDate"))
                .add(MOT_REASON_ID, motReasonId)
                .add(MOT_REASON_DESCRIPTION, motReasonDescription)
                .add(MOT_REASON_CODE, motReasonCode)
                .add(SEQUENCE_NUMBER, sequenceNumber);

        return jsonObjectBuilder.build();
    }

    public static JsonObject transformIndicatedPlea(final JsonObject jsonObject) {
        final JsonObjectBuilder jsonObjectBuilder = createObjectBuilder();
        //required
        jsonObjectBuilder
                .add(OFFENCE_ID, jsonObject.getString(OFFENCE_ID))
                .add(INDICATED_PLEA_DATE, jsonObject.getString(INDICATED_PLEA_DATE))
                .add(INDICATED_PLEA_VALUE, jsonObject.getString(INDICATED_PLEA_VALUE))
                .add(SOURCE, jsonObject.getString(SOURCE));
        // add optional attributen
        if (jsonObject.containsKey(ORIGINATING_HEARING_ID)) {
            jsonObjectBuilder.add(ORIGINATING_HEARING_ID, jsonObject.getString(ORIGINATING_HEARING_ID));
        }
        return jsonObjectBuilder.build();
    }

    public static JsonArray transformOffencesForSendingSheet(final JsonArray offenceJsonObjects) {

        final JsonArrayBuilder offenceList = createArrayBuilder();
        offenceJsonObjects.forEach(o -> {
            final JsonObject offence = (JsonObject) o;
            final JsonObjectBuilder offenceBuilder = transformOffenceForSendingSheet(offence);
            offenceList.add(offenceBuilder.build());
        });
        return offenceList.build();

    }

    public static JsonObjectBuilder transformOffenceForSendingSheet(final JsonObject offence) {
        //add required fields
        final JsonObjectBuilder offenceBuilder = createObjectBuilder()
                .add(ID, offence.getString(ID))
                .add(OFFENCE_CODE, offence.getString(OFFENCE_CODE))
                .add(SECTION, offence.getString(SECTION))
                .add(WORDING, offence.getString(WORDING))
                .add(START_DATE, offence.getString(START_DATE));


        // add optional fields
        if (offence.containsKey(CONVICTION_DATE)) {
            offenceBuilder.add(CONVICTION_DATE, offence.getString(CONVICTION_DATE));
        }

        if (offence.containsKey(PLEA)) {
            offenceBuilder.add(PLEA, offence.getJsonObject(PLEA));
        }

        if (offence.containsKey(INDICATED_PLEA)) {
            offenceBuilder.add(INDICATED_PLEA, transformIndicatedPleaForSendingSheet(offence.getJsonObject(INDICATED_PLEA)));
        }

        if (offence.containsKey(REASON)) {
            offenceBuilder.add(REASON, offence.getString(REASON));
        }

        if (offence.containsKey(DESCRIPTION)) {
            offenceBuilder.add(DESCRIPTION, offence.getString(DESCRIPTION));
        }

        if (offence.containsKey(CATEGORY)) {
            offenceBuilder.add(CATEGORY, offence.getString(CATEGORY));
        }


        if (offence.containsKey(END_DATE)) {
            offenceBuilder.add(END_DATE, offence.getString(END_DATE));
        }

        return offenceBuilder;

    }

    public static JsonObject transformIndicatedPleaForSendingSheet(final JsonObject jsonObject) {
        final JsonObjectBuilder objectBuilder =  createObjectBuilder()
                .add(OFFENCE_ID, jsonObject.getString(ID));
                if(jsonObject.containsKey(INDICATED_PLEA_VALUE)) {
                    objectBuilder.add(INDICATED_PLEA_VALUE, jsonObject.getString(INDICATED_PLEA_VALUE)).build();
                }
        return objectBuilder.build();
    }


}