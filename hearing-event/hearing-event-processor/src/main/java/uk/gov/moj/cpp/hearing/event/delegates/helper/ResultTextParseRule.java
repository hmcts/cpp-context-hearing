package uk.gov.moj.cpp.hearing.event.delegates.helper;

import static java.lang.Boolean.TRUE;
import static java.lang.Boolean.parseBoolean;
import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsLast;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;


import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import uk.gov.justice.core.courts.JudicialResultPrompt;
import uk.gov.moj.cpp.hearing.event.helper.TreeNode;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt;

@SuppressWarnings("java:S3740")
public class ResultTextParseRule <T>{
    public static final String PROMPT = "PROMPT";
    public static final String NAMEADDRESS = "NAMEADDRESS";
    public static final String BOOLEAN = "BOOLEAN";


    private final String lastChar;

    ResultTextParseRule(){
        lastChar = "";
    }

    ResultTextParseRule(final String lastChar){
        this.lastChar = lastChar;
    }

    public String lastControlChar(){
        return lastChar;
    }

    public static boolean shouldBeParsed(String controlChar){
        return "{%[".contains(controlChar);
    }

    public String parse(final TreeNode<T> node, final String subString){
        switch(lastChar){
            case "}" : return parseForPromptDirective(node, subString);
            case "%" : return parseForVariableResultDirective(node, subString);
            case "]" : return parseForConditionalResultDirective(node, subString);
            default: throw new IllegalArgumentException(lastChar);
        }
    }

    public static ResultTextParseRule fromValue(String v) {
        switch (v){
            case "{" : return new ResultTextParseRule("}");
            case "%" : return new ResultTextParseRule("%");
            case "[" : return new ResultTextParseRule("]");
            default: throw new IllegalArgumentException(v);
        }
    }

    private  String parseForConditionalResultDirective(final TreeNode<T> node, final String pText){
        final String text = pText.substring(1, pText.length()-1);
        return getNewResultText(node, text);
    }

    public String getNewResultText(final TreeNode<T> node, final String text){
        String newResultText = text;
        boolean isChanged = false;
        boolean isOnlyText = true;
        int nextPosition = -1;
        for (int i = 0 ; i < text.length() ; i++){
            if(i <= nextPosition){
                continue;
            }
            final String control = String.valueOf(text.charAt(i));
            if(ResultTextParseRule.shouldBeParsed(control)){
                isOnlyText = false;
                final ResultTextParseRule resultTextParseRule = ResultTextParseRule.fromValue(control);
                final int position = text.indexOf(resultTextParseRule.lastControlChar(), i+1);
                final String subString = text.substring(i, position+1);
                final String newSubString  = resultTextParseRule.parse(node, subString);
                if(!"".equals(newSubString)){
                    isChanged = true;
                }
                newResultText = newResultText.replace(subString, newSubString);
                nextPosition = position;
            }
        }
        if(isChanged || isOnlyText){
            return newResultText;
        }else{
            return "";
        }
    }

    private String parseForVariableResultDirective(final TreeNode<T> node, final String subString){
        final String childCode = subString.substring(1, subString.length()-1);
        switch (childCode) {
            case "AllChildText" :
                return node.getChildren().stream()
                        .filter(child -> ! ofNullable(child.getJudicialResult().getAlwaysPublished()).orElse(false))
                        .map(child -> child.getJudicialResult().getResultText())
                        .filter(Objects::nonNull)
                        .collect(Collectors.joining(", "));
            case "ResultLabel"  :
                return node.getJudicialResult().getLabel();
            case "Prompts" :
                return getAllPrompts(node);
            default:
                return getChildResultTexts(node, childCode);
        }
    }

    private String getAllPrompts(final TreeNode<T> node) {
        return ofNullable(node.getJudicialResult().getJudicialResultPrompts()).map(Collection::stream).orElseGet(Stream::empty)
                .filter(p -> !"hmiSlots".equals(p.getPromptReference()))
                .filter(p -> !TRUE.equals(getDefPrompt(p, node.getResultDefinition().getData().getPrompts()).isHidden()))
                .sorted(comparing(JudicialResultPrompt::getPromptSequence, nullsLast(naturalOrder())))
                .filter(p -> Objects.nonNull(p.getValue()))
                .map(p -> (BOOLEAN.equals(p.getType()) ? "" : p.getLabel() + ": ") + getPromptValue(p))
                .filter(v -> !"".equals(v))
                .collect(Collectors.joining(", "));
    }

    private String getChildResultTexts(final TreeNode<T> node, final String childCode) {
        return node.getChildren().stream()
                .filter(child -> child.getResultDefinition().getData().getShortCode().equals(childCode))
                .map(child -> child.getJudicialResult().getResultText())
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("");
    }

    private  String parseForPromptDirective(final TreeNode<T> node, final String subString){
        final boolean isOnlyName = subString.contains("~Name");
        final String promptName ;
        if(isOnlyName){
            promptName = Optional.of(subString.replace("~Name", "")).map(pName -> pName.substring(1, pName.length()-1)).orElse(null);
        }else{
            promptName = subString.substring(1, subString.length()-1);
        }
        final Map<String,List<JudicialResultPrompt>> promptMap = ofNullable(node.getJudicialResult().getJudicialResultPrompts()).map(Collection::stream).orElseGet(Stream::empty)
                .filter(p -> p.getPromptReference().toLowerCase().startsWith(promptName.toLowerCase()))
                .collect(Collectors.groupingBy(p -> p.getPromptReference().equals(promptName) ? PROMPT : NAMEADDRESS));

        final List<Prompt> defPromptMap =  node.getResultDefinition().getData().getPrompts();

        if(promptMap.get(PROMPT) != null){
            final JudicialResultPrompt prompt = promptMap.get(PROMPT).get(0);
            return getPromptValue(prompt);
        }else if (promptMap.get(NAMEADDRESS) != null) {
            return getNameAddressValue(isOnlyName, promptMap, defPromptMap);
        }else {
            return "";
        }
    }

    private static String getNameAddressValue(final boolean isOnlyName, final Map<String, List<JudicialResultPrompt>> promptMap, final List<Prompt> defPromptMap) {
        final List<JudicialResultPrompt> prompts = promptMap.get(NAMEADDRESS);
        String value = Stream.of(
                prompts.stream().filter(p -> "OrganisationName".equals(getDefPrompt(p, defPromptMap).getPartName())).map(ResultTextParseRule::getPromptValue),
                prompts.stream().filter(p -> "FirstName".equals(getDefPrompt(p, defPromptMap).getPartName())).map(ResultTextParseRule::getPromptValue),
                prompts.stream().filter(p -> "MiddleName".equals(getDefPrompt(p, defPromptMap).getPartName())).map(ResultTextParseRule::getPromptValue),
                prompts.stream().filter(p -> "LastName".equals(getDefPrompt(p, defPromptMap).getPartName())).map(ResultTextParseRule::getPromptValue))
                .flatMap(v -> v)
                .filter(v -> !"".equals(v))
                .collect(Collectors.joining(" "));
        if(!isOnlyName){
            final String otherValues = Stream.of(prompts.stream().filter(p -> "AddressLine1".equals(getDefPrompt(p, defPromptMap).getPartName())).map(ResultTextParseRule::getPromptValue),
                    prompts.stream().filter(p -> "AddressLine2".equals(getDefPrompt(p, defPromptMap).getPartName())).map(ResultTextParseRule::getPromptValue),
                    prompts.stream().filter(p -> "AddressLine3".equals(getDefPrompt(p, defPromptMap).getPartName())).map(ResultTextParseRule::getPromptValue),
                    prompts.stream().filter(p -> "AddressLine4".equals(getDefPrompt(p, defPromptMap).getPartName())).map(ResultTextParseRule::getPromptValue),
                    prompts.stream().filter(p -> "AddressLine5".equals(getDefPrompt(p, defPromptMap).getPartName())).map(ResultTextParseRule::getPromptValue),
                    prompts.stream().filter(p -> "PostCode".equals(getDefPrompt(p, defPromptMap).getPartName())).map(ResultTextParseRule::getPromptValue))
                    .flatMap(v -> v)
                    .filter(v -> !",".equals(v))
                    .collect(Collectors.joining(", "));
            value = Stream.of(value, otherValues)
                    .filter(v -> !"".equals(v))
                    .collect(Collectors.joining(", "));
        }
        return value;
    }

    private static String getPromptValue(final JudicialResultPrompt prompt) {
        if(prompt.getValue() == null || (BOOLEAN.equals(prompt.getType()) && !(parseBoolean(prompt.getValue()) || "Yes".equals(prompt.getValue())))){
            return "";
        }else if(BOOLEAN.equals(prompt.getType())){
            return prompt.getLabel();
        }else{
            return prompt.getValue();
        }
    }

    private static Prompt getDefPrompt(JudicialResultPrompt judicialResultPrompt, List<Prompt> prompts) {
        return prompts.stream()
                .filter(promptDef -> promptDef.getId().equals(judicialResultPrompt.getJudicialResultPromptTypeId()) && (isNull(judicialResultPrompt.getPromptReference()) || judicialResultPrompt.getPromptReference().equals(promptDef.getReference())))
                .findFirst().orElseGet(Prompt::new);
    }
}
