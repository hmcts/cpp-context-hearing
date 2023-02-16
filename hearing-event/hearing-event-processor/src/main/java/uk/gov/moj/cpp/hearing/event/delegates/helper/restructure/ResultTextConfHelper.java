package uk.gov.moj.cpp.hearing.event.delegates.helper.restructure;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import uk.gov.justice.core.courts.JudicialResult;
import uk.gov.justice.core.courts.ResultLine;
import uk.gov.justice.core.courts.ResultLine2;
import uk.gov.justice.services.common.configuration.Value;
import uk.gov.moj.cpp.hearing.event.helper.TreeNode;

public class ResultTextConfHelper {

    @Inject
    @Value(key = "liveDateOfResultTextTemplate", defaultValue = "03022000")
    private String liveDateOfResultTextTemplateConf;

    private LocalDate liveDateOfResultTextTemplate;


    @PostConstruct
    public void setDate(){
        liveDateOfResultTextTemplate = LocalDate.parse(liveDateOfResultTextTemplateConf,DateTimeFormatter.ofPattern("ddMMyyyy"));
    }

    public boolean isOldResultDefinition(final LocalDate orderDate){
        return !orderDate.isAfter(liveDateOfResultTextTemplate);
    }

    public boolean isOldResultDefinitionV2(final List<TreeNode<ResultLine2>> resultLines){
        return resultLines.stream()
                .map(TreeNode::getJudicialResult)
                .map(JudicialResult::getOrderedDate)
                .anyMatch(this::isOldResultDefinition);
    }

    public boolean isOldResultDefinition(final List<TreeNode<ResultLine>> resultLines){
        return resultLines.stream()
                .map(TreeNode::getJudicialResult)
                .map(JudicialResult::getOrderedDate)
                .anyMatch(this::isOldResultDefinition);
    }

}
