package uk.gov.justice.ccr.notepad.stub;


import static uk.gov.justice.ccr.notepad.model.Part.Type.RESULT;

import uk.gov.justice.ccr.notepad.model.Part;
import uk.gov.justice.ccr.notepad.model.ResultChoice;
import uk.gov.justice.ccr.notepad.model.ResultDefinition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

public class DummyResultDefinitionGenerator {

    public static ResultDefinition getResultDefinition(String originalText) {
        ResultDefinition resultDefinition = new ResultDefinition();
        resultDefinition.setOriginalText(originalText);

        List<Part> parts = new ArrayList<>();
        String[] partsText = StringUtils.split(originalText, " ");
        String perfactMatch = "";
        for (String part : partsText) {
            Part partResult = new Part();
            Set<ResultChoice> choices = getMatchedResultChoices(part);

            if(part.startsWith("|")){
                partResult.setType(PromptType.TXT.name());
                partResult.setLabel(part);
                partResult.setState(Part.State.UNRESOLVED);
                partResult.setResultChoices(null);
            }
            else if (!choices.isEmpty()) {
                partResult.setResultChoices(choices);
                partResult.setState(Part.State.UNRESOLVED);
                partResult.setLabel(part);
                perfactMatch = perfactMatch + part + "|";
            } else {
                partResult.setType(PromptType.getType(part));
                partResult.setLabel(part);
                partResult.setState(Part.State.UNRESOLVED);
                partResult.setResultChoices(null);
                //promptChoiceType.setText(Part.Type.PROMPT.name());

            }
            partResult.setText(part);
            parts.add(partResult);
        }
        resultDefinition.setParts(parts);


        if (!getMatchedResultChoices(perfactMatch).isEmpty()) {
            String[] matchParts = StringUtils.split(perfactMatch, "|");
            List<String> asArray = Arrays.asList(matchParts);
            for (Part p : resultDefinition.getParts()) {
                if (asArray.contains(p.getText())) {
                    p.setState(Part.State.RESOLVED);
                   // p.setText();
                    p.setType(store.get(p.getText().toUpperCase()).getType());
                    p.setLabel(store.get(p.getText().toUpperCase()).getDisplayLebel(p.getText()));
                    p.setResultChoices(null);
                }
            }
            resultDefinition.setResultCode(store.get(perfactMatch.toUpperCase()).getCode());
        }
        return resultDefinition;
    }

    private enum PromptType {
        INT, TXT, ENUM, DURATION;

        public static String getType(String text) {
            if (StringUtils.containsIgnoreCase(text, "yr")) {
                return ENUM.name();
            } else if (StringUtils.isNumeric(text)) {
                return INT.name();
            } else if (StringUtils.isAlphanumeric(text)) {
                return TXT.name();
            }
            return ENUM.name();
        }
    }

    private static Set<ResultChoice> getMatchedResultChoices(String part) {
        part = part.toUpperCase();
        Set<ResultChoice> matchedResultChoices = new HashSet<>();
        Set<String> keys = new HashSet<>(store.keySet());
        for (String key : keys) {
            if (key.indexOf(part) > -1 && store.get(part) != null) {
                ResultChoice resultChoice = store.get(key);
                ResultChoice resultChoiceEnriched = new ResultChoice(resultChoice.getCode(), resultChoice.getLabel());
                resultChoiceEnriched.setType(resultChoice.getType());
                resultChoiceEnriched.setSynonymMatch(store.get(part).getDisplayLebel(part));
                matchedResultChoices.add(resultChoiceEnriched);
            }
        }
        return matchedResultChoices;
    }

    private ResultChoice getResultChoices(String part) {


        return null;
    }

    private static Map<String, ResultChoice> store = new HashMap<>();

    static {
        ResultChoice resultChoiceImp = new ResultChoice("1", "Imprisonment", "Imprisonment");
        resultChoiceImp.setSynonyms(Arrays.asList("imp|", "imp", "impr", "impris"));
        resultChoiceImp.setType(RESULT.name());

        ResultChoice resultChoiceSus = new ResultChoice("2", "Suspended Imprisonment", "Suspended");
        resultChoiceSus.setSynonyms(Arrays.asList("sus", "susp", "sus|imp|", "imp|sus|"));
        resultChoiceSus.setType(RESULT.name());

        ResultChoice resultChoiceVS = new ResultChoice("3", "Victim Surcharge","Victim Surcharge");
        resultChoiceVS.setSynonyms(Arrays.asList("vic sur", "vs|"));
        resultChoiceVS.setType(RESULT.name());


        ResultChoice resultChoiceConc = new ResultChoice("4", "Suspended Imprisonment Concurrent", "Concurrent");
        resultChoiceConc.setSynonyms(Arrays.asList("impsuscon", "imp|sus|conc|", "conc", "con"));
        resultChoiceConc.setType(RESULT.name());

        ResultChoice resultChoiceAAMR = new ResultChoice("5", "Alcohol Abstinence and Monitoring Requirements", "Alcohol Abstinence and Monitoring Requirements");
        resultChoiceAAMR.setSynonyms(Arrays.asList("alc", "req", "alc|abs|mon|req|", "abs", "mon"));
        resultChoiceAAMR.setType(RESULT.name());

        ResultChoice resultChoiceATR = new ResultChoice("6", "Alcohol Treatment Requirements", "Alcohol Treatment Requirements");
        resultChoiceATR.setSynonyms(Arrays.asList("alc", "req" ,"alc|tra|req|","tra"));
        resultChoiceATR.setType(RESULT.name());

        resultChoiceImp.getSynonyms().forEach(synonyms -> store.put(synonyms.toUpperCase(), resultChoiceImp));

        resultChoiceSus.getSynonyms().forEach(synonyms -> store.put(synonyms.toUpperCase(), resultChoiceSus));

        resultChoiceVS.getSynonyms().forEach(synonyms -> store.put(synonyms.toUpperCase(), resultChoiceVS));

        resultChoiceConc.getSynonyms().forEach(synonyms -> store.put(synonyms.toUpperCase(), resultChoiceConc));

        resultChoiceAAMR.getSynonyms().forEach(synonyms -> store.put(synonyms.toUpperCase(), resultChoiceAAMR));

        resultChoiceATR.getSynonyms().forEach(synonyms -> store.put(synonyms.toUpperCase(), resultChoiceATR));

    }

    public static void main(String a[]) {

    }
}
