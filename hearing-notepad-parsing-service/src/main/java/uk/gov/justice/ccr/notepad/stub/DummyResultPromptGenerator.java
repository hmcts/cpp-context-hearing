package uk.gov.justice.ccr.notepad.stub;


import uk.gov.justice.ccr.notepad.model.PromptChoice;
import uk.gov.justice.ccr.notepad.model.ResultPrompt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DummyResultPromptGenerator {

    public static ResultPrompt getPrompt(String resultCode) {

        ResultPrompt resultPrompt = new ResultPrompt();
        resultPrompt.setResultCode(resultCode);
        resultPrompt.setPromptChoices(store.get(resultCode));
        return resultPrompt;
    }

    private static Map<String, List<PromptChoice>> store = new HashMap<>();

    static {
        Map<String, String> dummyEnumForYearUnit = new HashMap<>();
        dummyEnumForYearUnit.put("y", "years");
        dummyEnumForYearUnit.put("y", "years");
        dummyEnumForYearUnit.put("yrs", "years");
        dummyEnumForYearUnit.put("y", "years");
        dummyEnumForYearUnit.put("yr", "years");
        dummyEnumForYearUnit.put("yrs", "years");
        dummyEnumForYearUnit.put("m", "months");
        dummyEnumForYearUnit.put("ms", "months");
        dummyEnumForYearUnit.put("mo", "months");
        dummyEnumForYearUnit.put("w", "weeks");
        dummyEnumForYearUnit.put("wk", "weeks");
        dummyEnumForYearUnit.put("d", "days");
        dummyEnumForYearUnit.put("dy", "days");


        PromptChoice promptChoiceImp1_1 = new PromptChoice();
        promptChoiceImp1_1.setCode("1.1.1");
        promptChoiceImp1_1.setLabel("Imprisonment Duration Length");
        promptChoiceImp1_1.setType("INT");
        promptChoiceImp1_1.setRequired("true");
        promptChoiceImp1_1.setSynonyms(null);

        PromptChoice promptChoiceImp1_2 = new PromptChoice();
        promptChoiceImp1_2.setCode("1.1.0");
        promptChoiceImp1_2.setRequired("true");
        promptChoiceImp1_2.setLabel("Imprisonment Duration Unit");
        promptChoiceImp1_2.setSynonyms(dummyEnumForYearUnit);
        promptChoiceImp1_2.setType("ENUM");


        List<PromptChoice> impChoices = new ArrayList<>();
        impChoices.add(promptChoiceImp1_1);
        impChoices.add(promptChoiceImp1_2);



        PromptChoice promptChoiceImpSus121 = new PromptChoice();
        promptChoiceImpSus121.setCode("1.2.1");
        promptChoiceImpSus121.setRequired("true");
        promptChoiceImpSus121.setLabel("Imprisonment Duration Length");
        promptChoiceImpSus121.setType("INT");
        promptChoiceImpSus121.setSynonyms(null);

        PromptChoice promptChoiceImpSus122 = new PromptChoice();
        promptChoiceImpSus122.setCode("1.2.2");
        promptChoiceImpSus122.setRequired("true");
        promptChoiceImpSus122.setLabel("Imprisonment Duration Unit");
        promptChoiceImpSus122.setSynonyms(dummyEnumForYearUnit);
        promptChoiceImpSus122.setType("ENUM");
        promptChoiceImpSus122.setSynonyms(dummyEnumForYearUnit);



        PromptChoice promptChoiceImpSus111 = new PromptChoice();
        promptChoiceImpSus111.setCode("1.2.3");
        promptChoiceImpSus111.setLabel("Suspended Duration Length");
        promptChoiceImpSus111.setRequired("true");
        promptChoiceImpSus111.setType("INT");
        promptChoiceImpSus111.setSynonyms(null);

        PromptChoice promptChoiceImpSus112 = new PromptChoice();
        promptChoiceImpSus112.setCode("1.2.4");
        promptChoiceImpSus112.setRequired("true");
        promptChoiceImpSus112.setLabel("Suspended Duration Unit");
        promptChoiceImpSus112.setSynonyms(dummyEnumForYearUnit);
        promptChoiceImpSus112.setType("ENUM");
        promptChoiceImpSus112.setSynonyms(dummyEnumForYearUnit);

        List<PromptChoice> impSusConcChoices = new ArrayList<>();
        impSusConcChoices.add(promptChoiceImpSus111);
        impSusConcChoices.add(promptChoiceImpSus112);
        impSusConcChoices.add(promptChoiceImpSus121);
        impSusConcChoices.add(promptChoiceImpSus122);


        PromptChoice promptChoiceVS111 = new PromptChoice();
        promptChoiceVS111.setCode("1.3.1");
        promptChoiceVS111.setRequired("true");
        promptChoiceVS111.setLabel("Victim Surcharge Amount");
        //promptChoiceImpSus111.setSynonyms(dummyEnumForYearUnit);
        promptChoiceVS111.setType("CUR");
        promptChoiceVS111.setSynonyms(null);

        List<PromptChoice> impVSConcChoices = new ArrayList<>();
        impVSConcChoices.add(promptChoiceVS111);

        PromptChoice promptChoiceAMR1 = new PromptChoice();
        promptChoiceAMR1.setCode("1.5.1");
        promptChoiceAMR1.setLabel("Abstinance Days");
        promptChoiceAMR1.setRequired("true");
        promptChoiceAMR1.setType("INT");
        promptChoiceAMR1.setSynonyms(null);

        PromptChoice promptChoiceAMR2 = new PromptChoice();
        promptChoiceAMR2.setCode("1.5.2");
        promptChoiceAMR2.setLabel("Alcohol Limit");
        promptChoiceAMR2.setRequired("true");
        promptChoiceAMR2.setType("INT");
        promptChoiceAMR2.setSynonyms(null);

        List<PromptChoice> impAAMRConcChoices = new ArrayList<>();
        impAAMRConcChoices.add(promptChoiceAMR1);
        impAAMRConcChoices.add(promptChoiceAMR2);

        PromptChoice promptChoiceAMT1 = new PromptChoice();
        promptChoiceAMT1.setCode("1.6.1");
        promptChoiceAMT1.setLabel("Doctor/Psychiatrist");
        promptChoiceAMT1.setRequired("true");
        promptChoiceAMT1.setType("TXT");
        promptChoiceAMT1.setSynonyms(null);

        PromptChoice promptChoiceAMT2 = new PromptChoice();
        promptChoiceAMT2.setCode("1.6.2");
        promptChoiceAMT2.setLabel("Hospital/Clinic");
        promptChoiceAMT2.setRequired("true");
        promptChoiceAMT2.setType("TXT");
        promptChoiceAMT2.setSynonyms(null);

        PromptChoice promptChoiceAMT3 = new PromptChoice();
        promptChoiceAMT3.setCode("1.6.3");
        promptChoiceAMT3.setLabel("Treatment Period Value");
        promptChoiceAMT3.setRequired("true");
        promptChoiceAMT3.setType("INT");
        promptChoiceAMT3.setSynonyms(null);

        PromptChoice promptChoiceAMT4 = new PromptChoice();
        promptChoiceAMT4.setCode("1.6.4");
        promptChoiceAMT4.setLabel("Additional Info N");
        promptChoiceAMT4.setRequired("true");
        promptChoiceAMT4.setType("TXT");
        promptChoiceAMT4.setSynonyms(null);

        List<PromptChoice> impAAMTConcChoices = new ArrayList<>();
        impAAMTConcChoices.add(promptChoiceAMT1);
        impAAMTConcChoices.add(promptChoiceAMT2);
        impAAMTConcChoices.add(promptChoiceAMT3);
        impAAMTConcChoices.add(promptChoiceAMT4);



        store.put("1", impChoices);
        store.put("2", impSusConcChoices);
        store.put("3", impVSConcChoices);
        store.put("4", impSusConcChoices);
        store.put("5", impAAMRConcChoices);
        store.put("6", impAAMTConcChoices);


    }
}
