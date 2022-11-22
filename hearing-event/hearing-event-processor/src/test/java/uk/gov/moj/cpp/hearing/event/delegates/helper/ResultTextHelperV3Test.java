package uk.gov.moj.cpp.hearing.event.delegates.helper;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.core.courts.JudicialResultPrompt.judicialResultPrompt;


import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.core.courts.JudicialResult;
import uk.gov.justice.core.courts.ResultLine2;
import uk.gov.moj.cpp.hearing.event.helper.TreeNode;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;

@RunWith(MockitoJUnitRunner.class)
public class ResultTextHelperV3Test {


    public static final String TEXT_FOR_PROMPTS = "Vehicle {makeOfVehicle} {vehicleRegistration} for which clamping order made on {clampingOrderMadeOn} not to be sold. {vehicleToBeReleasedOnPaymentOfTheChargesDue} {vehicleToBeReleasedForthwithWithoutPaymentOfAnyChargesDue}.";
    public static final String TEXT_FOR_CONDITIONAL = "Clamping order made for vehicle {makeOfVehicle} {vehicleRegistration} [and vehicle {makeOfSecondVehicle} {vehicleRegistrationOfSecondVehicle}] to take effect on. [Reasons: {reasons}.]";
    public static final String TEXT_FOR_PROMPTS_WITH_ALL_EMPTY = "Vehicle {makeOfVehicle} for which clamping order made on not to be sold. {vehicleToBeReleasedOnPaymentOfTheChargesDue}.";
    public static final String TEXT_FOR_CONDITIONAL_WITH_ALL_EMPTY = "Clamping order made for vehicle {makeOfVehicle} [and vehicle {makeOfSecondVehicle} {vehicleRegistrationOfSecondVehicle}] to take effect on. [Reasons: {reasons}.]";
    public static final String TEXT_FOR_RESULT_LABEL = "Vehicle {makeOfVehicle} %ResultLabel%.";
    public static final String TEXT_FOR_ALL_PROMPTS = "Vehicle  %Prompts%.";


    @Test
    public void shouldNotSetResultTextPrefixWhenResultTextDoesNotExist(){
        final List<TreeNode<ResultLine2>> treeNodeList = singletonList(new TreeNode<>(randomUUID(), null));

        treeNodeList.get(0).setJudicialResult(JudicialResult.judicialResult().withLabel("Label")
                .withJudicialResultPrompts(Arrays.asList(judicialResultPrompt()
                        .withPromptReference("placeArea")
                        .withValue("area1")
                        .withType("TXT")
                        .build(), judicialResultPrompt()
                        .withPromptReference("fromDate")
                        .withType("DATE")
                        .withValue("01-01-2012")
                        .build(), judicialResultPrompt()
                        .withPromptReference("untilDate")
                        .withType("DATE")
                        .withValue("01-01-2013")
                        .build(), judicialResultPrompt()
                        .withPromptReference("endDateOfExclusion")
                        .withType("DATE")
                        .withValue("01-01-2014")
                        .build(), judicialResultPrompt()
                        .withPromptReference("additionalInformation")
                        .withType("TXT")
                        .withValue("text prompt")
                        .build())
                )
                .build());
        treeNodeList.get(0).setResultDefinition(new TreeNode<>(randomUUID(), ResultDefinition.resultDefinition().setShortCode("NEXT")));


        ResultTextHelperV3.setResultText(treeNodeList);

        assertThat(treeNodeList.get(0).getJudicialResult().getResultText(), CoreMatchers.is ("NEXT - Label"));

    }


    @Test
    public void shouldSetResultTextForPromptDirective(){
        final List<TreeNode<ResultLine2>> treeNodeList = singletonList(new TreeNode<>(randomUUID(), null));

        treeNodeList.get(0)
                .setJudicialResult(JudicialResult.judicialResult().withLabel("LABEL")
                        .withJudicialResultPrompts(Arrays.asList(judicialResultPrompt()
                                .withPromptReference("placeArea")
                                .withValue("area1")
                                .withType("TXT")
                                .build(), judicialResultPrompt()
                                .withPromptReference("fromDate")
                                .withType("DATE")
                                .withValue("01-01-2012")
                                .build(), judicialResultPrompt()
                                .withPromptReference("untilDate")
                                .withType("DATE")
                                .withValue("01-01-2013")
                                .build(), judicialResultPrompt()
                                .withPromptReference("endDateOfExclusion")
                                .withType("DATE")
                                .withValue("01-01-2014")
                                .build(), judicialResultPrompt()
                                .withPromptReference("additionalInformation")
                                .withType("TXT")
                                .withValue("text prompt")
                                .build())
                        )
                        .build());
        treeNodeList.get(0).setResultDefinition(new TreeNode<>(randomUUID(), ResultDefinition.resultDefinition().setShortCode("NEXT")
                .setResultTextTemplate("Exclusion Requirement: Not to enter {placeArea} between {fromDate} and {untilDate}. This exclusion requirement lasts until {endDateOfExclusion}. {additionalInformation}")));


        ResultTextHelperV3.setResultText(treeNodeList);

        assertThat(treeNodeList.get(0).getJudicialResult().getResultText(), CoreMatchers.is ("NEXT - LABEL\nExclusion Requirement: Not to enter area1 between 01-01-2012 and 01-01-2013. This exclusion requirement lasts until 01-01-2014. text prompt"));
    }


    @Test
    public void shouldSetResultTextForPromptDirectiveWithBooleanValues(){
        final List<TreeNode<ResultLine2>> treeNodeList = getTreeNodesForPromptDirective(TEXT_FOR_PROMPTS, "make Of Vehicle", "No", null, null, null);

        ResultTextHelperV3.setResultText(treeNodeList);

        assertThat(treeNodeList.get(0).getJudicialResult().getResultText(), CoreMatchers.is ("NEXH - Result Label\nVehicle make Of Vehicle vehicle Registration Value for which clamping order made on 01-01-2012 not to be sold.  Vehicle to be released forthwith, without payment of any charges due."));
    }


    @Test
    public void shouldSetResultTextForPromptDirectiveWithEmptySomeValues(){
        final List<TreeNode<ResultLine2>> treeNodeList = getTreeNodesForPromptDirective(TEXT_FOR_PROMPTS, "make Of Vehicle", null, null, null, null);

        ResultTextHelperV3.setResultText(treeNodeList);

        assertThat(treeNodeList.get(0).getJudicialResult().getResultText(), CoreMatchers.is ("NEXH - Result Label\nVehicle make Of Vehicle vehicle Registration Value for which clamping order made on 01-01-2012 not to be sold.  Vehicle to be released forthwith, without payment of any charges due."));
    }

    @Test
    public void shouldSetResultTextForPromptDirectiveWithEmptyValues(){
        final List<TreeNode<ResultLine2>> treeNodeList = getTreeNodesForPromptDirective(TEXT_FOR_PROMPTS_WITH_ALL_EMPTY, null, null, null, null, null);

        ResultTextHelperV3.setResultText(treeNodeList);

        assertThat(treeNodeList.get(0).getJudicialResult().getResultText(), CoreMatchers.is ("NEXH - Result Label"));
    }

    @Test
    public void shouldSetResultTextForNameAddressForOrganisation(){
        final List<TreeNode<ResultLine2>> treeNodeList = getTreeNodesForNameAddress("Organisation Name", null, null, null, "");

        ResultTextHelperV3.setResultText(treeNodeList);

        assertThat(treeNodeList.get(0).getJudicialResult().getResultText(), CoreMatchers.is ("NEXB - Label\nTo pay costs to Organisation Name, Address 1, Address 2, Address 3, Address 4, Address 5, E14 9YZ"));
    }

    @Test
    public void shouldSetResultTextForNameAddressForIndividual(){
        final List<TreeNode<ResultLine2>> treeNodeList = getTreeNodesForNameAddress(null, "first name", "middle name", "last name", "");

        ResultTextHelperV3.setResultText(treeNodeList);

        assertThat(treeNodeList.get(0).getJudicialResult().getResultText(), CoreMatchers.is ("NEXB - Label\nTo pay costs to first name middle name last name, Address 1, Address 2, Address 3, Address 4, Address 5, E14 9YZ"));
    }

    @Test
    public void shouldSetResultTextForNameAddressWithEmptyValues(){
        final List<TreeNode<ResultLine2>> treeNodeList = getTreeNodesForNameAddress(null, null, null, null, "~Name");

        ResultTextHelperV3.setResultText(treeNodeList);

        assertThat(treeNodeList.get(0).getJudicialResult().getResultText(), CoreMatchers.is ("NEXB - Label"));
    }

    @Test
    public void shouldSetResultTextForNameAddressForOrganisationWithOnlyName(){
        final List<TreeNode<ResultLine2>> treeNodeList = getTreeNodesForNameAddress("Organisation Name", null, null, null, "~Name");

        ResultTextHelperV3.setResultText(treeNodeList);

        assertThat(treeNodeList.get(0).getJudicialResult().getResultText(), CoreMatchers.is ("NEXB - Label\nTo pay costs to Organisation Name"));
    }


    @Test
    public void shouldSetResultTextForNameAddressForIndividualWithOnlyName(){
        final List<TreeNode<ResultLine2>> treeNodeList = getTreeNodesForNameAddress(null, "first name", "middle name", "last name", "~Name");

        ResultTextHelperV3.setResultText(treeNodeList);

        assertThat(treeNodeList.get(0).getJudicialResult().getResultText(), CoreMatchers.is ("NEXB - Label\nTo pay costs to first name middle name last name"));
    }

    @Test
    public void shouldSetResultTextForVariableResultDirective(){
        final List<TreeNode<ResultLine2>> treeNodeList = getTreeNodesWithOnlyResultTextTemplate("result Text with %NEXH% and %NEXB%");


        treeNodeList.get(0).addChildren(getTreeNodesForNameAddress("Organisation Name", null, null, null, "~Name"));
        treeNodeList.get(0).addChildren(getTreeNodesForPromptDirective(TEXT_FOR_PROMPTS, "make Of Vehicle", "No", null, null,  null));

        ResultTextHelperV3.setResultText(treeNodeList);

        assertThat(treeNodeList.get(0).getChildren().get(0).getJudicialResult().getResultText(), CoreMatchers.is ("To pay costs to Organisation Name"));
        assertThat(treeNodeList.get(0).getChildren().get(1).getJudicialResult().getResultText(), CoreMatchers.is ("Vehicle make Of Vehicle vehicle Registration Value for which clamping order made on 01-01-2012 not to be sold.  Vehicle to be released forthwith, without payment of any charges due."));
        assertThat(treeNodeList.get(0).getJudicialResult().getResultText(), CoreMatchers.is ("SCode - Label\nresult Text with Vehicle make Of Vehicle vehicle Registration Value for which clamping order made on 01-01-2012 not to be sold.  Vehicle to be released forthwith, without payment of any charges due. and To pay costs to Organisation Name"));

    }

    @Test
    public void shouldSetResultTextForVariableResultDirectiveWithEmptyValues(){
        final List<TreeNode<ResultLine2>> treeNodeList = getTreeNodesWithOnlyResultTextTemplate("result Text with %NEXH% and %NEXB%");

        treeNodeList.get(0).addChildren(getTreeNodesForNameAddress(null, null, null, null, "~Name"));
        treeNodeList.get(0).addChildren(getTreeNodesForPromptDirective(TEXT_FOR_PROMPTS_WITH_ALL_EMPTY, null, "No", null, null,  null));

        ResultTextHelperV3.setResultText(treeNodeList);

        assertThat(treeNodeList.get(0).getChildren().get(0).getJudicialResult().getResultText(), CoreMatchers.is ("NEXB - Label"));
        assertThat(treeNodeList.get(0).getChildren().get(1).getJudicialResult().getResultText(), CoreMatchers.is ("NEXH - Result Label"));
        assertThat(treeNodeList.get(0).getJudicialResult().getResultText(), CoreMatchers.is ("SCode - Label"));

    }


    @Test
    public void shouldSetResultTextForConditionalPromptsWithValues(){
        final List<TreeNode<ResultLine2>> treeNodeList = singletonList(getTreeNode(TEXT_FOR_CONDITIONAL, "make Of Vehicle", "No", "make Of Second Vehicle", "vehicle Registration Of Second Vehicle", "guilty"));


        ResultTextHelperV3.setResultText(treeNodeList);

        assertThat(treeNodeList.get(0).getJudicialResult().getResultText(), CoreMatchers.is ("NEXH - Result Label\nClamping order made for vehicle make Of Vehicle vehicle Registration Value and vehicle make Of Second Vehicle vehicle Registration Of Second Vehicle to take effect on. Reasons: guilty."));
    }

    @Test
    public void shouldSetResultTextForConditionalPromptsWithoutValues(){
        final List<TreeNode<ResultLine2>> treeNodeList = singletonList(getTreeNode(TEXT_FOR_CONDITIONAL, "make Of Vehicle", "No", null, null, "guilty"));

        ResultTextHelperV3.setResultText(treeNodeList);

        assertThat(treeNodeList.get(0).getJudicialResult().getResultText(), CoreMatchers.is ("NEXH - Result Label\nClamping order made for vehicle make Of Vehicle vehicle Registration Value  to take effect on. Reasons: guilty."));
    }

    @Test
    public void shouldSetResultTextForConditionalChildWithValues(){
        final List<TreeNode<ResultLine2>> treeNodeList = getTreeNodesWithOnlyResultTextTemplate("result Text [with %NEXH%] get");

        treeNodeList.get(0).addChildren(getTreeNodesForPromptDirective(TEXT_FOR_CONDITIONAL, "make Of Vehicle", "No", "make Of Second Vehicle", "vehicle Registration Of Second Vehicle", "guilty"));

        ResultTextHelperV3.setResultText(treeNodeList);

        assertThat(treeNodeList.get(0).getJudicialResult().getResultText(), CoreMatchers.is ("SCode - Label\nresult Text with Clamping order made for vehicle make Of Vehicle vehicle Registration Value and vehicle make Of Second Vehicle vehicle Registration Of Second Vehicle to take effect on. Reasons: guilty. get"));
        assertThat(treeNodeList.get(0).getChildren().get(0).getJudicialResult().getResultText(), CoreMatchers.is ("Clamping order made for vehicle make Of Vehicle vehicle Registration Value and vehicle make Of Second Vehicle vehicle Registration Of Second Vehicle to take effect on. Reasons: guilty."));
    }

    @Test
    public void shouldSetResultTextForConditionalChildWithoutValues(){
        final List<TreeNode<ResultLine2>> treeNodeList = getTreeNodesWithOnlyResultTextTemplate("result Text [with %NEXH%] get");

        treeNodeList.get(0).addChildren(getTreeNodesForPromptDirective(TEXT_FOR_CONDITIONAL_WITH_ALL_EMPTY, null, null, null, null, null));

        ResultTextHelperV3.setResultText(treeNodeList);

        assertThat(treeNodeList.get(0).getJudicialResult().getResultText(), CoreMatchers.is ("SCode - Label"));
        assertThat(treeNodeList.get(0).getChildren().get(0).getJudicialResult().getResultText(), CoreMatchers.is ("NEXH - Result Label"));
    }

    @Test
    public void shouldSetResultTextForFixedResultDirective(){
        final List<TreeNode<ResultLine2>> treeNodeList = getTreeNodesWithOnlyResultTextTemplate("result Text with %AllChildText%");

        treeNodeList.get(0).addChildren(getTreeNodesForNameAddress("Organisation Name", null, null, null, "~Name"));
        treeNodeList.get(0).addChildren(getTreeNodesForPromptDirective(TEXT_FOR_PROMPTS, "make Of Vehicle", "No", null, null,  null));

        ResultTextHelperV3.setResultText(treeNodeList);

        assertThat(treeNodeList.get(0).getChildren().get(0).getJudicialResult().getResultText(), CoreMatchers.is ("To pay costs to Organisation Name"));
        assertThat(treeNodeList.get(0).getChildren().get(1).getJudicialResult().getResultText(), CoreMatchers.is ("Vehicle make Of Vehicle vehicle Registration Value for which clamping order made on 01-01-2012 not to be sold.  Vehicle to be released forthwith, without payment of any charges due."));
        assertThat(treeNodeList.get(0).getJudicialResult().getResultText(), CoreMatchers.is ("SCode - Label\nresult Text with To pay costs to Organisation Name, Vehicle make Of Vehicle vehicle Registration Value for which clamping order made on 01-01-2012 not to be sold.  Vehicle to be released forthwith, without payment of any charges due."));

    }

    @Test
    public void shouldSetResultTextForFixedResultDirectiveWithSomeEmptyValues(){
        final List<TreeNode<ResultLine2>> treeNodeList = getTreeNodesWithOnlyResultTextTemplate("result Text with %AllChildText%");

        treeNodeList.get(0).addChildren(getTreeNodesForNameAddress("Organisation Name", null, null, null, "~Name"));
        treeNodeList.get(0).addChildren(getTreeNodesForPromptDirective(TEXT_FOR_PROMPTS_WITH_ALL_EMPTY, null, null, null, null,  null));

        ResultTextHelperV3.setResultText(treeNodeList);

        assertThat(treeNodeList.get(0).getChildren().get(0).getJudicialResult().getResultText(), CoreMatchers.is ("To pay costs to Organisation Name"));
        assertThat(treeNodeList.get(0).getChildren().get(1).getJudicialResult().getResultText(), CoreMatchers.is ("NEXH - Result Label"));
        assertThat(treeNodeList.get(0).getJudicialResult().getResultText(), CoreMatchers.is ("SCode - Label\nresult Text with To pay costs to Organisation Name"));

    }

    @Test
    public void shouldSetResultTextForFixedResultDirectiveWithEmptyValues(){
        final List<TreeNode<ResultLine2>> treeNodeList = getTreeNodesWithOnlyResultTextTemplate("result Text with %AllChildText%");

        treeNodeList.get(0).addChildren(getTreeNodesForNameAddress(null, null, null, null, "~Name"));
        treeNodeList.get(0).addChildren(getTreeNodesForPromptDirective(TEXT_FOR_PROMPTS_WITH_ALL_EMPTY, null, null, null, null,  null));

        ResultTextHelperV3.setResultText(treeNodeList);

        assertThat(treeNodeList.get(0).getChildren().get(0).getJudicialResult().getResultText(), CoreMatchers.is ("NEXB - Label"));
        assertThat(treeNodeList.get(0).getChildren().get(1).getJudicialResult().getResultText(), CoreMatchers.is ("NEXH - Result Label"));
        assertThat(treeNodeList.get(0).getJudicialResult().getResultText(), CoreMatchers.is ("SCode - Label"));

    }

    @Test
    public void shouldSetResultTextForFixedResultDirectiveWithAlwaysPublishedResults(){
        final List<TreeNode<ResultLine2>> treeNodeList = getTreeNodesWithOnlyResultTextTemplate("result Text with %AllChildText%");

        treeNodeList.get(0).addChildren(getTreeNodesForNameAddress("Organisation Name", null, null, null, "~Name"));
        treeNodeList.get(0).addChildren(getTreeNodesForPromptDirective(TEXT_FOR_PROMPTS, "make Of Vehicle", "No", null, null,  null));
        treeNodeList.get(0).addChildren(getTreeNodesForPromptDirectiveWithAlwaysPublished(TEXT_FOR_PROMPTS, "make Of Vehicle", "No"));

        ResultTextHelperV3.setResultText(treeNodeList);

        assertThat(treeNodeList.get(0).getChildren().get(0).getJudicialResult().getResultText(), CoreMatchers.is ("To pay costs to Organisation Name"));
        assertThat(treeNodeList.get(0).getChildren().get(1).getJudicialResult().getResultText(), CoreMatchers.is ("Vehicle make Of Vehicle vehicle Registration Value for which clamping order made on 01-01-2012 not to be sold.  Vehicle to be released forthwith, without payment of any charges due."));
        assertThat(treeNodeList.get(0).getChildren().get(2).getJudicialResult().getResultText(), CoreMatchers.is ("NEXT - Result Label alwaysPublished" +System.lineSeparator() + "Vehicle make Of Vehicle vehicle Registration Value for which clamping order made on 01-01-2012 not to be sold.  Vehicle to be released forthwith, without payment of any charges due."));
        assertThat(treeNodeList.get(0).getJudicialResult().getResultText(), CoreMatchers.is ("SCode - Label\nresult Text with To pay costs to Organisation Name, Vehicle make Of Vehicle vehicle Registration Value for which clamping order made on 01-01-2012 not to be sold.  Vehicle to be released forthwith, without payment of any charges due."));

    }

    @Test
    public void shouldSetResultTextForFixedResultDirectiveWithEmptyValuesAndAlwaysPublished(){
        final List<TreeNode<ResultLine2>> treeNodeList = getTreeNodesWithOnlyResultTextTemplate("result Text with %AllChildText%");

        treeNodeList.get(0).addChildren(getTreeNodesForNameAddress(null, null, null, null, "~Name"));
        treeNodeList.get(0).addChildren(getTreeNodesForPromptDirective(TEXT_FOR_PROMPTS_WITH_ALL_EMPTY, null, null, null, null,  null));
        treeNodeList.get(0).addChildren(getTreeNodesForPromptDirectiveWithAlwaysPublished(TEXT_FOR_PROMPTS_WITH_ALL_EMPTY, null, null));

        ResultTextHelperV3.setResultText(treeNodeList);

        assertThat(treeNodeList.get(0).getChildren().get(0).getJudicialResult().getResultText(), CoreMatchers.is ("NEXB - Label"));
        assertThat(treeNodeList.get(0).getChildren().get(1).getJudicialResult().getResultText(), CoreMatchers.is ("NEXH - Result Label"));
        assertThat(treeNodeList.get(0).getChildren().get(2).getJudicialResult().getResultText(), CoreMatchers.is ("NEXT - Result Label alwaysPublished"));
        assertThat(treeNodeList.get(0).getJudicialResult().getResultText(), CoreMatchers.is ("SCode - Label"));

    }

    @Test
    public void shouldSetResultTextWithResultLabel(){
        final List<TreeNode<ResultLine2>> treeNodeList = singletonList(getTreeNode(TEXT_FOR_RESULT_LABEL, "make Of Vehicle", "No", "make Of Second Vehicle", "vehicle Registration Of Second Vehicle", "guilty"));

        ResultTextHelperV3.setResultText(treeNodeList);

        assertThat(treeNodeList.get(0).getJudicialResult().getResultText(), CoreMatchers.is ("NEXH - Result Label\nVehicle make Of Vehicle Result Label."));
    }

    @Test
    public void shouldSetResultTextWithAllNoneNullPrompts(){
        final List<TreeNode<ResultLine2>> treeNodeList = singletonList(getTreeNode(TEXT_FOR_ALL_PROMPTS, "value1", "No", null, null, "guilty"));

        ResultTextHelperV3.setResultText(treeNodeList);

        assertThat(treeNodeList.get(0).getJudicialResult().getResultText(), CoreMatchers.is ("NEXH - Result Label\nVehicle  Make of vehicle: value1, Vehicle registration: vehicle Registration Value, Clamping order made on: 01-01-2012, Vehicle to be released forthwith, without payment of any charges due, Additional information: text prompt, Reasons: guilty."));
    }

    @Test
    public void shouldSetDependantResultDefinitionGroup(){
        final TreeNode<ResultLine2> communityOrderEnglandWales = new TreeNode<>(fromString("664ba2b9-7e05-4a59-b776-054a99bd5ca3"), null);
        communityOrderEnglandWales.setJudicialResult(JudicialResult.judicialResult().withLabel("Label").build());

        communityOrderEnglandWales.setResultDefinition(new TreeNode<>(randomUUID(), ResultDefinition.resultDefinition().setShortCode("S")
                .setResultTextTemplate("Parent result Text")
                .setDependantResultDefinitionGroup("Community Requirement")));

        final TreeNode<ResultLine2>  isElectronicMonitoringRequired = new TreeNode<>(fromString("9643720b-c103-4634-a224-c79d2d7eb26e"), null);
        isElectronicMonitoringRequired.setJudicialResult(JudicialResult.judicialResult()
                .withLabel("Is electronic monitoring required")
                .build()
        );
        isElectronicMonitoringRequired.setResultDefinition(new TreeNode<>(randomUUID(), ResultDefinition.resultDefinition()
                .setResultTextTemplate("Is electronic monitoring required result Text")
                .setShortCode("HMRCFPP")));

        final TreeNode<ResultLine2> communityRequirements = new TreeNode<>(fromString("0007ee1d-869f-454f-8142-fc43cabd4b44"), null);
        communityRequirements.setJudicialResult(JudicialResult.judicialResult()
                .withLabel("Community requirements")
                .build()
        );

        communityRequirements.setResultDefinition(new TreeNode<>(randomUUID(), ResultDefinition.resultDefinition()
                .setResultTextTemplate("Community requirements result Text")));

        final TreeNode<ResultLine2> unpaidWork = new TreeNode<>(fromString("16b4bb67-5a8c-43cb-9daf-01ffd03da6b3"), null);
        unpaidWork.setJudicialResult(JudicialResult.judicialResult()
                .withLabel("Unpaid work")
                .withAlwaysPublished(true)
                .build()
        );
        unpaidWork.setResultDefinition(new TreeNode<>(randomUUID(), ResultDefinition.resultDefinition()
                .setResultTextTemplate("Unpaid work result Text")
                .setDependantResultDefinitionGroup("Community Requirement")
                .setShortCode("UPWR")));

        final TreeNode<ResultLine2> rehabilitationActivity = new TreeNode<>(fromString("c835f05e-1780-498a-8802-728f21ba9a6c"), null);
        rehabilitationActivity.setJudicialResult(JudicialResult.judicialResult()
                .withLabel("Rehabilitation activity")
                .withAlwaysPublished(true)
                .build()
        );
        rehabilitationActivity.setResultDefinition(new TreeNode<>(randomUUID(), ResultDefinition.resultDefinition()
                .setResultTextTemplate("Rehabilitation activity result Text")
                .setDependantResultDefinitionGroup("Community Requirement")
                .setShortCode("RAR")));

        communityOrderEnglandWales.addChildren(Arrays.asList(isElectronicMonitoringRequired,communityRequirements));
        communityRequirements.addChildren(Arrays.asList(unpaidWork, rehabilitationActivity));

        ResultTextHelperV3.setResultText(singletonList(communityOrderEnglandWales));

        assertThat(communityOrderEnglandWales.getJudicialResult().getResultText(), CoreMatchers.is ("S - Label\nParent result Text"+System.lineSeparator()+"Unpaid work result Text"+System.lineSeparator()+"Rehabilitation activity result Text"));
    }

    @Test
    public void shouldSetResultTextForNoResultText(){
        final List<TreeNode<ResultLine2>> treeNodeList = singletonList(getTreeNode(TEXT_FOR_CONDITIONAL, "make Of Vehicle", "No", "make Of Second Vehicle", "vehicle Registration Of Second Vehicle", "guilty"));
        treeNodeList.get(0).addChildren(getTreeNodesWithoutResultTextTemplate());

        ResultTextHelperV3.setResultText(treeNodeList);

        assertThat(treeNodeList.get(0).getChildren().get(0).getJudicialResult().getResultText(), CoreMatchers.is ("SCode - Label"));
        assertThat(treeNodeList.get(0).getJudicialResult().getResultText(), CoreMatchers.is ("NEXH - Result Label\nClamping order made for vehicle make Of Vehicle vehicle Registration Value and vehicle make Of Second Vehicle vehicle Registration Of Second Vehicle to take effect on. Reasons: guilty."));
    }

    private List<TreeNode<ResultLine2>> getTreeNodesForPromptDirective(final String templateText, final String makeOfVehicle, final String vehicleToBeReleasedOnPaymentOfTheChargesDue,
                                                                      final String makeOfSecondVehicle, final String vehicleRegistrationOfSecondVehicle, final String reasons) {
        return singletonList(getTreeNode(templateText, makeOfVehicle, vehicleToBeReleasedOnPaymentOfTheChargesDue, makeOfSecondVehicle, vehicleRegistrationOfSecondVehicle, reasons));
    }

    private TreeNode<ResultLine2> getTreeNode(final String templateText, final String makeOfVehicle, final String vehicleToBeReleasedOnPaymentOfTheChargesDue,
                                             final String makeOfSecondVehicle, final String vehicleRegistrationOfSecondVehicle, final String reasons) {

        final TreeNode<ResultLine2> treeNode = new TreeNode<>(randomUUID(), null);
        treeNode.setJudicialResult(JudicialResult.judicialResult()
                .withLabel("Result Label")
                .withJudicialResultPrompts(Arrays.asList(judicialResultPrompt()
                        .withLabel("Make of vehicle")
                        .withPromptReference("makeOfVehicle")
                        .withValue(makeOfVehicle)
                        .withType("TXT")
                        .build(), judicialResultPrompt()
                        .withLabel("Vehicle registration")
                        .withPromptReference("vehicleRegistration")
                        .withValue("vehicle Registration Value")
                        .withType("TXT")
                        .build(), judicialResultPrompt()
                        .withLabel("Clamping order made on")
                        .withPromptReference("clampingOrderMadeOn")
                        .withType("DATE")
                        .withValue("01-01-2012")
                        .build(), judicialResultPrompt()
                        .withPromptReference("vehicleToBeReleasedOnPaymentOfTheChargesDue")
                        .withType("BOOLEAN")
                        .withLabel("Vehicle to be released on payment of the charges due")
                        .withValue(vehicleToBeReleasedOnPaymentOfTheChargesDue)
                        .build(), judicialResultPrompt()
                        .withPromptReference("vehicleToBeReleasedForthwithWithoutPaymentOfAnyChargesDue")
                        .withLabel("Vehicle to be released forthwith, without payment of any charges due")
                        .withType("BOOLEAN")
                        .withValue("Yes")
                        .build(), judicialResultPrompt()
                        .withLabel("Additional information")
                        .withPromptReference("additionalInformation")
                        .withType("TXT")
                        .withValue("text prompt")
                        .build(), judicialResultPrompt()
                        .withLabel("Make of second vehicle")
                        .withPromptReference("makeOfSecondVehicle")
                        .withType("TXT")
                        .withValue(makeOfSecondVehicle)
                        .build(), judicialResultPrompt()
                        .withLabel("Vehicle registration of second vehicle")
                        .withPromptReference("vehicleRegistrationOfSecondVehicle")
                        .withType("TXT")
                        .withValue(vehicleRegistrationOfSecondVehicle)
                        .build(), judicialResultPrompt()
                        .withLabel("Reasons")
                        .withPromptReference("reasons")
                        .withType("TXT")
                        .withValue(reasons)
                        .build())
                )
                .build());

        treeNode.setResultDefinition(new TreeNode<>(randomUUID(), ResultDefinition.resultDefinition()
                .setResultTextTemplate(templateText)
                .setShortCode("NEXH")));
        return treeNode;
    }

    private List<TreeNode<ResultLine2>> getTreeNodesForNameAddress(final String organisationName, final String firstName, final String middleName, final String lastName, final String suffix) {
        final TreeNode<ResultLine2> treeNode = new TreeNode<>(randomUUID(), null);
        final UUID promptId1 = randomUUID();
        final UUID promptId2 = randomUUID();
        final UUID promptId3 = randomUUID();
        final UUID promptId4 = randomUUID();
        final UUID promptId5 = randomUUID();
        final UUID promptId6 = randomUUID();
        final UUID promptId7 = randomUUID();
        final UUID promptId8 = randomUUID();
        final UUID promptId9 = randomUUID();
        final UUID promptId10 = randomUUID();
        treeNode.setJudicialResult(JudicialResult.judicialResult()
                .withLabel("Label")
                .withJudicialResultPrompts(asList(judicialResultPrompt()
                                .withPromptReference("minorcreditornameandaddressOrganisationName")
                                .withValue(organisationName)
                                .withType("NAMEADDRESS")
                                .withJudicialResultPromptTypeId(promptId1)
                                .build(),
                        judicialResultPrompt()
                                .withPromptReference("minorcreditornameandaddressOrganisationName")
                                .withValue(firstName)
                                .withType("NAMEADDRESS")
                                .withJudicialResultPromptTypeId(promptId2)
                                .build(),
                        judicialResultPrompt()
                                .withPromptReference("minorcreditornameandaddressOrganisationName")
                                .withValue(middleName)
                                .withType("NAMEADDRESS")
                                .withJudicialResultPromptTypeId(promptId3)
                                .build(),
                        judicialResultPrompt()
                                .withPromptReference("minorcreditornameandaddressOrganisationName")
                                .withValue(lastName)
                                .withType("NAMEADDRESS")
                                .withJudicialResultPromptTypeId(promptId4)
                                .build(),
                        judicialResultPrompt()
                                .withPromptReference("minorcreditornameandaddressAddress1")
                                .withValue("Address 1")
                                .withType("NAMEADDRESS")
                                .withJudicialResultPromptTypeId(promptId5)
                                .build(),
                        judicialResultPrompt()
                                .withPromptReference("minorcreditornameandaddressAddress2")
                                .withValue("Address 2")
                                .withType("NAMEADDRESS")
                                .withJudicialResultPromptTypeId(promptId6)
                                .build(),
                        judicialResultPrompt()
                                .withPromptReference("minorcreditornameandaddressAddress3")
                                .withValue("Address 3")
                                .withType("NAMEADDRESS")
                                .withJudicialResultPromptTypeId(promptId7)
                                .build(),
                        judicialResultPrompt()
                                .withPromptReference("minorcreditornameandaddressAddress4")
                                .withValue("Address 4")
                                .withType("NAMEADDRESS")
                                .withJudicialResultPromptTypeId(promptId8)
                                .build(),
                        judicialResultPrompt()
                                .withPromptReference("minorcreditornameandaddressAddress5")
                                .withValue("Address 5")
                                .withType("NAMEADDRESS")
                                .withJudicialResultPromptTypeId(promptId9)
                                .build(),
                        judicialResultPrompt()
                                .withPromptReference("minorcreditornameandaddressPostCode")
                                .withValue("E14 9YZ")
                                .withType("NAMEADDRESS")
                                .withJudicialResultPromptTypeId(promptId10)
                                .build())
                )
                .build());

        treeNode.setResultDefinition(new TreeNode<>(randomUUID(), ResultDefinition.resultDefinition()
                .setResultTextTemplate("To pay costs to {minorCreditorNameAndAddress" + suffix + "}")
                .setPrompts(asList(Prompt.prompt().setId(promptId1).setPartName("OrganisationName")
                                .setReference("minorcreditornameandaddressOrganisationName"),
                        Prompt.prompt().setId(promptId2).setPartName("FirstName")
                                .setReference("minorcreditornameandaddressOrganisationName"),
                        Prompt.prompt().setId(promptId3).setPartName("MiddleName")
                                .setReference("minorcreditornameandaddressOrganisationName"),
                        Prompt.prompt().setId(promptId4).setPartName("LastName")
                                .setReference("minorcreditornameandaddressOrganisationName"),
                        Prompt.prompt().setId(promptId5).setPartName("AddressLine1")
                                .setReference("minorcreditornameandaddressAddress1"),
                        Prompt.prompt().setId(promptId6).setPartName("AddressLine2")
                                .setReference("minorcreditornameandaddressAddress2"),
                        Prompt.prompt().setId(promptId7).setPartName("AddressLine3")
                                .setReference("minorcreditornameandaddressAddress3"),
                        Prompt.prompt().setId(promptId8).setPartName("AddressLine4")
                                .setReference("minorcreditornameandaddressAddress4"),
                        Prompt.prompt().setId(promptId9).setPartName("AddressLine5")
                                .setReference("minorcreditornameandaddressAddress5"),
                        Prompt.prompt().setId(promptId10).setPartName("PostCode")
                                .setReference("minorcreditornameandaddressPostCode")))
                .setShortCode("NEXB")));
        return singletonList(treeNode);
    }

    private List<TreeNode<ResultLine2>> getTreeNodesForPromptDirectiveWithAlwaysPublished(final String templateText, final String makeOfVehicle, final String vehicleToBeReleasedOnPaymentOfTheChargesDue) {

        final TreeNode<ResultLine2> treeNode = new TreeNode<>(randomUUID(), null);
        treeNode.setJudicialResult(JudicialResult.judicialResult()
                .withLabel("Result Label alwaysPublished")
                .withAlwaysPublished(true)
                .withJudicialResultPrompts(Arrays.asList(judicialResultPrompt()
                        .withLabel("Make of vehicle")
                        .withPromptReference("makeOfVehicle")
                        .withValue(makeOfVehicle)
                        .withType("TXT")
                        .build(), judicialResultPrompt()
                        .withLabel("Vehicle registration")
                        .withPromptReference("vehicleRegistration")
                        .withValue("vehicle Registration Value")
                        .withType("TXT")
                        .build(), judicialResultPrompt()
                        .withLabel("Clamping order made on")
                        .withPromptReference("clampingOrderMadeOn")
                        .withType("DATE")
                        .withValue("01-01-2012")
                        .build(), judicialResultPrompt()
                        .withPromptReference("vehicleToBeReleasedOnPaymentOfTheChargesDue")
                        .withType("BOOLEAN")
                        .withLabel("Vehicle to be released on payment of the charges due")
                        .withValue(vehicleToBeReleasedOnPaymentOfTheChargesDue)
                        .build(), judicialResultPrompt()
                        .withPromptReference("vehicleToBeReleasedForthwithWithoutPaymentOfAnyChargesDue")
                        .withLabel("Vehicle to be released forthwith, without payment of any charges due")
                        .withType("BOOLEAN")
                        .withValue("Yes")
                        .build(), judicialResultPrompt()
                        .withLabel("Additional information")
                        .withPromptReference("additionalInformation")
                        .withType("TXT")
                        .withValue("text prompt")
                        .build(), judicialResultPrompt()
                        .withLabel("Make of second vehicle")
                        .withPromptReference("makeOfSecondVehicle")
                        .withType("TXT")
                        .build(), judicialResultPrompt()
                        .withLabel("Vehicle registration of second vehicle")
                        .withPromptReference("vehicleRegistrationOfSecondVehicle")
                        .withType("TXT")
                        .build(), judicialResultPrompt()
                        .withLabel("Reasons")
                        .withPromptReference("reasons")
                        .withType("TXT")
                        .build())
                )
                .build());

        treeNode.setResultDefinition(new TreeNode<>(randomUUID(), ResultDefinition.resultDefinition()
                .setResultTextTemplate(templateText)
                .setShortCode("NEXT")));
        return singletonList(treeNode);
    }


    private List<TreeNode<ResultLine2>> getTreeNodesWithOnlyResultTextTemplate(final String resultTextTemplate) {
        final List<TreeNode<ResultLine2>> treeNodeList = singletonList(new TreeNode<>(randomUUID(), null));
        treeNodeList.get(0).setJudicialResult(JudicialResult.judicialResult().withLabel("Label").build());
        treeNodeList.get(0).setResultDefinition(new TreeNode<>(randomUUID(), ResultDefinition.resultDefinition().setShortCode("SCode")
                .setResultTextTemplate(resultTextTemplate)));
        return treeNodeList;
    }
    private List<TreeNode<ResultLine2>> getTreeNodesWithoutResultTextTemplate() {
        final List<TreeNode<ResultLine2>> treeNodeList = singletonList(new TreeNode<>(randomUUID(), null));
        treeNodeList.get(0).setJudicialResult(JudicialResult.judicialResult().withLabel("Label").build());
        treeNodeList.get(0).setResultDefinition(new TreeNode<>(randomUUID(), ResultDefinition.resultDefinition().setShortCode("SCode")));
        return treeNodeList;
    }

}
