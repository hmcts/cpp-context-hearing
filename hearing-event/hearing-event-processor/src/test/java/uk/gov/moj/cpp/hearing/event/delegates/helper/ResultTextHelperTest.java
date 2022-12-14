package uk.gov.moj.cpp.hearing.event.delegates.helper;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.core.courts.JudicialResultPrompt.judicialResultPrompt;


import java.util.UUID;
import org.hamcrest.CoreMatchers;
import uk.gov.justice.core.courts.JudicialResult;
import uk.gov.justice.core.courts.ResultLine;
import uk.gov.moj.cpp.hearing.event.helper.TreeNode;

import java.util.List;

import org.junit.Test;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;

public class ResultTextHelperTest {

    public static final String TEXT_FOR_PROMPTS = "Vehicle {makeOfVehicle} {vehicleRegistration} for which clamping order made on {clampingOrderMadeOn} not to be sold. {vehicleToBeReleasedOnPaymentOfTheChargesDue} {vehicleToBeReleasedForthwithWithoutPaymentOfAnyChargesDue}.";
    public static final String TEXT_FOR_CONDITIONAL = "Clamping order made for vehicle {makeOfVehicle} {vehicleRegistration} [and vehicle {makeOfSecondVehicle} {vehicleRegistrationOfSecondVehicle}] to take effect on. [Reasons: {reasons}.]";
    public static final String TEXT_FOR_PROMPTS_WITH_ALL_EMPTY = "Vehicle {makeOfVehicle} for which clamping order made on not to be sold. {vehicleToBeReleasedOnPaymentOfTheChargesDue}.";
    public static final String TEXT_FOR_CONDITIONAL_WITH_ALL_EMPTY = "Clamping order made for vehicle {makeOfVehicle} [and vehicle {makeOfSecondVehicle} {vehicleRegistrationOfSecondVehicle}] to take effect on. [Reasons: {reasons}.]";
    public static final String TEXT_FOR_RESULT_LABEL = "Vehicle {makeOfVehicle} %ResultLabel%.";
    public static final String TEXT_FOR_ALL_PROMPTS = "Vehicle  %Prompts%.";


    @Test
    public void shouldNotSetResultTextPrefixWhenResultTextDoesNotExist(){
        final List<TreeNode<ResultLine>> treeNodeList = singletonList(new TreeNode<>(randomUUID(), null));

        getTreeNode(treeNodeList);

        ResultTextHelper.setResultText(treeNodeList);

        assertThat(treeNodeList.get(0).getJudicialResult().getResultText(), CoreMatchers.is ("NEXT - Label-Result"));

    }

    private void getTreeNode(final List<TreeNode<ResultLine>> treeNodeList) {
        treeNodeList.get(0).setJudicialResult(JudicialResult.judicialResult()
                        .withLabel("Label-Result")
                .withJudicialResultPrompts(asList(judicialResultPrompt()
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

        treeNodeList.get(0).setResultDefinition(new TreeNode<>(randomUUID(), ResultDefinition.resultDefinition()
                .setShortCode("NEXT")));
    }


    @Test
    public void shouldSetResultTextForPromptDirectiveWithBooleanValues(){
        final List<TreeNode<ResultLine>> treeNodeList = getTreeNodesForPromptDirective(TEXT_FOR_PROMPTS, "make Of Vehicle", "No", null, null, null);

        ResultTextHelper.setResultText(treeNodeList);

        assertThat(treeNodeList.get(0).getJudicialResult().getResultText(), CoreMatchers.is ("NEXH - Result Label\nVehicle make Of Vehicle vehicle Registration Value for which clamping order made on 01-01-2012 not to be sold.  Vehicle to be released forthwith, without payment of any charges due."));
    }


    @Test
    public void shouldSetResultTextForPromptDirectiveWithEmptySomeValues(){
        final List<TreeNode<ResultLine>> treeNodeList = getTreeNodesForPromptDirective(TEXT_FOR_PROMPTS, "make Of Vehicle", null, null, null, null);

        ResultTextHelper.setResultText(treeNodeList);

        assertThat(treeNodeList.get(0).getJudicialResult().getResultText(), CoreMatchers.is ("NEXH - Result Label\nVehicle make Of Vehicle vehicle Registration Value for which clamping order made on 01-01-2012 not to be sold.  Vehicle to be released forthwith, without payment of any charges due."));
    }

    @Test
    public void shouldSetResultTextForPromptDirectiveWithEmptyValues(){
        final List<TreeNode<ResultLine>> treeNodeList = getTreeNodesForPromptDirective(TEXT_FOR_PROMPTS_WITH_ALL_EMPTY, null, null, null, null, null);

        ResultTextHelper.setResultText(treeNodeList);

        assertThat(treeNodeList.get(0).getJudicialResult().getResultText(), CoreMatchers.is ("NEXH - Result Label\nVehicle  for which clamping order made on not to be sold. ."));
    }

    @Test
    public void shouldSetResultTextForNameAddressForOrganisation(){
        final List<TreeNode<ResultLine>> treeNodeList = getTreeNodesForNameAddress("Organisation Name", null, null, null, "");

        ResultTextHelper.setResultText(treeNodeList);

        assertThat(treeNodeList.get(0).getJudicialResult().getResultText(), CoreMatchers.is ("NEXB - Label-Result\nTo pay costs to Organisation Name, Address 1, Address 2, Address 3, Address 4, Address 5, E14 9YZ"));
    }

    @Test
    public void shouldSetResultTextForNameAddressForIndividual(){
        final List<TreeNode<ResultLine>> treeNodeList = getTreeNodesForNameAddress(null, "first name", "middle name", "last name", "");

        ResultTextHelper.setResultText(treeNodeList);

        assertThat(treeNodeList.get(0).getJudicialResult().getResultText(), CoreMatchers.is ("NEXB - Label-Result\nTo pay costs to first name middle name last name, Address 1, Address 2, Address 3, Address 4, Address 5, E14 9YZ"));
    }

    @Test
    public void shouldSetResultTextForNameAddressWithEmptyValues(){
        final List<TreeNode<ResultLine>> treeNodeList = getTreeNodesForNameAddress(null, null, null, null, "~Name");

        ResultTextHelper.setResultText(treeNodeList);

        assertThat(treeNodeList.get(0).getJudicialResult().getResultText(), CoreMatchers.is ("NEXB - Label-Result\nTo pay costs to "));
    }

    @Test
    public void shouldSetResultTextForNameAddressForOrganisationWithOnlyName(){
        final List<TreeNode<ResultLine>> treeNodeList = getTreeNodesForNameAddress("Organisation Name", null, null, null, "~Name");

        ResultTextHelper.setResultText(treeNodeList);

        assertThat(treeNodeList.get(0).getJudicialResult().getResultText(), CoreMatchers.is ("NEXB - Label-Result\nTo pay costs to Organisation Name"));
    }


    @Test
    public void shouldSetResultTextForNameAddressForIndividualWithOnlyName(){
        final List<TreeNode<ResultLine>> treeNodeList = getTreeNodesForNameAddress(null, "first name", "middle name", "last name", "~Name");

        ResultTextHelper.setResultText(treeNodeList);

        assertThat(treeNodeList.get(0).getJudicialResult().getResultText(), CoreMatchers.is ("NEXB - Label-Result\nTo pay costs to first name middle name last name"));
    }

    @Test
    public void shouldSetResultTextForVariableResultDirective(){
        final List<TreeNode<ResultLine>> treeNodeList = getTreeNodesWithOnlyResultTextTemplate("result Text with %NEXH% and %NEXB%");


        treeNodeList.get(0).addChildren(getTreeNodesForNameAddress("Organisation Name", null, null, null, "~Name"));
        treeNodeList.get(0).addChildren(getTreeNodesForPromptDirective(TEXT_FOR_PROMPTS, "make Of Vehicle", "No", null, null,  null));

        ResultTextHelper.setResultText(treeNodeList);

        assertThat(treeNodeList.get(0).getChildren().get(0).getJudicialResult().getResultText(), CoreMatchers.is ("To pay costs to Organisation Name"));
        assertThat(treeNodeList.get(0).getChildren().get(1).getJudicialResult().getResultText(), CoreMatchers.is ("Vehicle make Of Vehicle vehicle Registration Value for which clamping order made on 01-01-2012 not to be sold.  Vehicle to be released forthwith, without payment of any charges due."));
        assertThat(treeNodeList.get(0).getJudicialResult().getResultText(), CoreMatchers.is ("NEXT - Label\nresult Text with Vehicle make Of Vehicle vehicle Registration Value for which clamping order made on 01-01-2012 not to be sold.  Vehicle to be released forthwith, without payment of any charges due. and To pay costs to Organisation Name"));

    }

    @Test
    public void shouldSetResultTextForVariableResultDirectiveWithEmptyValues(){
        final List<TreeNode<ResultLine>> treeNodeList = getTreeNodesWithOnlyResultTextTemplate("result Text with %NEXH% and %NEXB%");

        treeNodeList.get(0).addChildren(getTreeNodesForNameAddress(null, null, null, null, "~Name"));
        treeNodeList.get(0).addChildren(getTreeNodesForPromptDirective(TEXT_FOR_PROMPTS_WITH_ALL_EMPTY, null, "No", null, null,  null));

        ResultTextHelper.setResultText(treeNodeList);

        assertThat(treeNodeList.get(0).getChildren().get(0).getJudicialResult().getResultText(), CoreMatchers.is ("To pay costs to "));
        assertThat(treeNodeList.get(0).getChildren().get(1).getJudicialResult().getResultText(), CoreMatchers.is ("Vehicle  for which clamping order made on not to be sold. ."));
        assertThat(treeNodeList.get(0).getJudicialResult().getResultText(), CoreMatchers.is ("NEXT - Label\nresult Text with Vehicle  for which clamping order made on not to be sold. . and To pay costs to "));

    }


    @Test
    public void shouldSetResultTextForConditionalPromptsWithValues(){
        final List<TreeNode<ResultLine>> treeNodeList = singletonList(getTreeNode(TEXT_FOR_CONDITIONAL, "make Of Vehicle", "No", "make Of Second Vehicle", "vehicle Registration Of Second Vehicle", "guilty"));


        ResultTextHelper.setResultText(treeNodeList);

        assertThat(treeNodeList.get(0).getJudicialResult().getResultText(), CoreMatchers.is ("NEXH - Result Label\nClamping order made for vehicle make Of Vehicle vehicle Registration Value and vehicle make Of Second Vehicle vehicle Registration Of Second Vehicle to take effect on. Reasons: guilty."));
    }

    @Test
    public void shouldSetResultTextForConditionalPromptsWithoutValues(){
        final List<TreeNode<ResultLine>> treeNodeList = singletonList(getTreeNode(TEXT_FOR_CONDITIONAL, "make Of Vehicle", "No", null, null, "guilty"));

        ResultTextHelper.setResultText(treeNodeList);

        assertThat(treeNodeList.get(0).getJudicialResult().getResultText(), CoreMatchers.is ("NEXH - Result Label\nClamping order made for vehicle make Of Vehicle vehicle Registration Value  to take effect on. Reasons: guilty."));
    }

    @Test
    public void shouldSetResultTextForConditionalChildWithValues(){
        final List<TreeNode<ResultLine>> treeNodeList = getTreeNodesWithOnlyResultTextTemplate("result Text [with %NEXH%] get");

        treeNodeList.get(0).addChildren(getTreeNodesForPromptDirective(TEXT_FOR_CONDITIONAL, "make Of Vehicle", "No", "make Of Second Vehicle", "vehicle Registration Of Second Vehicle", "guilty"));

        ResultTextHelper.setResultText(treeNodeList);

        assertThat(treeNodeList.get(0).getJudicialResult().getResultText(), CoreMatchers.is ("NEXT - Label\nresult Text with Clamping order made for vehicle make Of Vehicle vehicle Registration Value and vehicle make Of Second Vehicle vehicle Registration Of Second Vehicle to take effect on. Reasons: guilty. get"));
        assertThat(treeNodeList.get(0).getChildren().get(0).getJudicialResult().getResultText(), CoreMatchers.is ("Clamping order made for vehicle make Of Vehicle vehicle Registration Value and vehicle make Of Second Vehicle vehicle Registration Of Second Vehicle to take effect on. Reasons: guilty."));
    }

    @Test
    public void shouldSetResultTextForConditionalChildWithoutValues(){
        final List<TreeNode<ResultLine>> treeNodeList = getTreeNodesWithOnlyResultTextTemplate("result Text [with %NEXH%] get");

        treeNodeList.get(0).addChildren(getTreeNodesForPromptDirective(TEXT_FOR_CONDITIONAL_WITH_ALL_EMPTY, null, null, null, null, null));

        ResultTextHelper.setResultText(treeNodeList);

        assertThat(treeNodeList.get(0).getJudicialResult().getResultText(), CoreMatchers.is ("NEXT - Label\nresult Text with Clamping order made for vehicle   to take effect on.  get"));
        assertThat(treeNodeList.get(0).getChildren().get(0).getJudicialResult().getResultText(), CoreMatchers.is ("Clamping order made for vehicle   to take effect on. "));
    }

    @Test
    public void shouldSetResultTextForFixedResultDirective(){
        final List<TreeNode<ResultLine>> treeNodeList = getTreeNodesWithOnlyResultTextTemplate("result Text with %AllChildText%");

        treeNodeList.get(0).addChildren(getTreeNodesForNameAddress("Organisation Name", null, null, null, "~Name"));
        treeNodeList.get(0).addChildren(getTreeNodesForPromptDirective(TEXT_FOR_PROMPTS, "make Of Vehicle", "No", null, null,  null));

        ResultTextHelper.setResultText(treeNodeList);

        assertThat(treeNodeList.get(0).getChildren().get(0).getJudicialResult().getResultText(), CoreMatchers.is ("To pay costs to Organisation Name"));
        assertThat(treeNodeList.get(0).getChildren().get(1).getJudicialResult().getResultText(), CoreMatchers.is ("Vehicle make Of Vehicle vehicle Registration Value for which clamping order made on 01-01-2012 not to be sold.  Vehicle to be released forthwith, without payment of any charges due."));
        assertThat(treeNodeList.get(0).getJudicialResult().getResultText(), CoreMatchers.is ("NEXT - Label\nresult Text with To pay costs to Organisation Name, Vehicle make Of Vehicle vehicle Registration Value for which clamping order made on 01-01-2012 not to be sold.  Vehicle to be released forthwith, without payment of any charges due."));

    }

    @Test
    public void shouldSetResultTextForFixedResultDirectiveWithSomeEmptyValues(){
        final List<TreeNode<ResultLine>> treeNodeList = getTreeNodesWithOnlyResultTextTemplate("result Text with %AllChildText%");

        treeNodeList.get(0).addChildren(getTreeNodesForNameAddress("Organisation Name", null, null, null, "~Name"));
        treeNodeList.get(0).addChildren(getTreeNodesForPromptDirective(TEXT_FOR_PROMPTS_WITH_ALL_EMPTY, null, null, null, null,  null));

        ResultTextHelper.setResultText(treeNodeList);

        assertThat(treeNodeList.get(0).getChildren().get(0).getJudicialResult().getResultText(), CoreMatchers.is ("To pay costs to Organisation Name"));
        assertThat(treeNodeList.get(0).getChildren().get(1).getJudicialResult().getResultText(), CoreMatchers.is ("Vehicle  for which clamping order made on not to be sold. ."));
        assertThat(treeNodeList.get(0).getJudicialResult().getResultText(), CoreMatchers.is ("NEXT - Label\nresult Text with To pay costs to Organisation Name, Vehicle  for which clamping order made on not to be sold. ."));

    }

    @Test
    public void shouldSetResultTextForFixedResultDirectiveWithEmptyValues(){
        final List<TreeNode<ResultLine>> treeNodeList = getTreeNodesWithOnlyResultTextTemplate("result Text with %AllChildText%");

        treeNodeList.get(0).addChildren(getTreeNodesForNameAddress(null, null, null, null, "~Name"));
        treeNodeList.get(0).addChildren(getTreeNodesForPromptDirective(TEXT_FOR_PROMPTS_WITH_ALL_EMPTY, null, null, null, null,  null));

        ResultTextHelper.setResultText(treeNodeList);

        assertThat(treeNodeList.get(0).getChildren().get(0).getJudicialResult().getResultText(), CoreMatchers.is ("To pay costs to "));
        assertThat(treeNodeList.get(0).getChildren().get(1).getJudicialResult().getResultText(), CoreMatchers.is ("Vehicle  for which clamping order made on not to be sold. ."));
        assertThat(treeNodeList.get(0).getJudicialResult().getResultText(), CoreMatchers.is ("NEXT - Label\nresult Text with To pay costs to , Vehicle  for which clamping order made on not to be sold. ."));

    }

    @Test
    public void shouldSetResultTextForFixedResultDirectiveWithAlwaysPublishedResults(){
        final List<TreeNode<ResultLine>> treeNodeList = getTreeNodesWithOnlyResultTextTemplate("result Text with %AllChildText%");

        treeNodeList.get(0).addChildren(getTreeNodesForNameAddress("Organisation Name", null, null, null, "~Name"));
        treeNodeList.get(0).addChildren(getTreeNodesForPromptDirective(TEXT_FOR_PROMPTS, "make Of Vehicle", "No", null, null,  null));
        treeNodeList.get(0).addChildren(getTreeNodesForPromptDirectiveWithAlwaysPublished(TEXT_FOR_PROMPTS, "make Of Vehicle", "No"));

        ResultTextHelper.setResultText(treeNodeList);

        assertThat(treeNodeList.get(0).getChildren().get(0).getJudicialResult().getResultText(), CoreMatchers.is ("To pay costs to Organisation Name"));
        assertThat(treeNodeList.get(0).getChildren().get(1).getJudicialResult().getResultText(), CoreMatchers.is ("Vehicle make Of Vehicle vehicle Registration Value for which clamping order made on 01-01-2012 not to be sold.  Vehicle to be released forthwith, without payment of any charges due."));
        assertThat(treeNodeList.get(0).getChildren().get(2).getJudicialResult().getResultText(), CoreMatchers.is ("NEXT - Result Label alwaysPublished" +System.lineSeparator() + "Vehicle make Of Vehicle vehicle Registration Value for which clamping order made on 01-01-2012 not to be sold.  Vehicle to be released forthwith, without payment of any charges due."));
        assertThat(treeNodeList.get(0).getJudicialResult().getResultText(), CoreMatchers.is ("NEXT - Label\nresult Text with To pay costs to Organisation Name, Vehicle make Of Vehicle vehicle Registration Value for which clamping order made on 01-01-2012 not to be sold.  Vehicle to be released forthwith, without payment of any charges due."));

    }

    @Test
    public void shouldSetResultTextForFixedResultDirectiveWithEmptyValuesAndAlwaysPublished(){
        final List<TreeNode<ResultLine>> treeNodeList = getTreeNodesWithOnlyResultTextTemplate("result Text with %AllChildText%");

        treeNodeList.get(0).addChildren(getTreeNodesForNameAddress(null, null, null, null, "~Name"));
        treeNodeList.get(0).addChildren(getTreeNodesForPromptDirective(TEXT_FOR_PROMPTS_WITH_ALL_EMPTY, null, null, null, null,  null));
        treeNodeList.get(0).addChildren(getTreeNodesForPromptDirectiveWithAlwaysPublished(TEXT_FOR_PROMPTS_WITH_ALL_EMPTY, null, null));

        ResultTextHelper.setResultText(treeNodeList);

        assertThat(treeNodeList.get(0).getChildren().get(0).getJudicialResult().getResultText(), CoreMatchers.is ("To pay costs to "));
        assertThat(treeNodeList.get(0).getChildren().get(1).getJudicialResult().getResultText(), CoreMatchers.is ("Vehicle  for which clamping order made on not to be sold. ."));
        assertThat(treeNodeList.get(0).getChildren().get(2).getJudicialResult().getResultText(), CoreMatchers.is ("NEXT - Result Label alwaysPublished\nVehicle  for which clamping order made on not to be sold. ."));
        assertThat(treeNodeList.get(0).getJudicialResult().getResultText(), CoreMatchers.is ("NEXT - Label\nresult Text with To pay costs to , Vehicle  for which clamping order made on not to be sold. ."));

    }

    @Test
    public void shouldSetResultTextWithResultLabel(){
        final List<TreeNode<ResultLine>> treeNodeList = singletonList(getTreeNode(TEXT_FOR_RESULT_LABEL, "make Of Vehicle", "No", "make Of Second Vehicle", "vehicle Registration Of Second Vehicle", "guilty"));

        ResultTextHelper.setResultText(treeNodeList);

        assertThat(treeNodeList.get(0).getJudicialResult().getResultText(), CoreMatchers.is ("NEXH - Result Label\nVehicle make Of Vehicle Result Label."));
    }

    @Test
    public void shouldSetResultTextWithAllNoneNullPrompts(){
        final List<TreeNode<ResultLine>> treeNodeList = singletonList(getTreeNode(TEXT_FOR_ALL_PROMPTS, "value1", "No", null, null, "guilty"));

        ResultTextHelper.setResultText(treeNodeList);

        assertThat(treeNodeList.get(0).getJudicialResult().getResultText(), CoreMatchers.is ("NEXH - Result Label\nVehicle  Make of vehicle: value1, Vehicle registration: vehicle Registration Value, Clamping order made on: 01-01-2012, Vehicle to be released forthwith, without payment of any charges due, Additional information: text prompt, Reasons: guilty."));
    }

    @Test
    public void shouldSetDependantResultDefinitionGroup(){
        final TreeNode<ResultLine> communityOrderEnglandWales = new TreeNode<>(fromString("664ba2b9-7e05-4a59-b776-054a99bd5ca3"), null);
        communityOrderEnglandWales.setJudicialResult(JudicialResult.judicialResult().withLabel("Label")
                .build());

        communityOrderEnglandWales.setResultDefinition(new TreeNode<>(randomUUID(), ResultDefinition.resultDefinition()
                .setResultTextTemplate("Parent result Text")
                .setShortCode("PRT")
                .setDependantResultDefinitionGroup("Community Requirement")));

        final TreeNode<ResultLine>  isElectronicMonitoringRequired = new TreeNode<>(fromString("9643720b-c103-4634-a224-c79d2d7eb26e"), null);
        isElectronicMonitoringRequired.setJudicialResult(JudicialResult.judicialResult()
                .withLabel("Is electronic monitoring required")
                .build()
        );
        isElectronicMonitoringRequired.setResultDefinition(new TreeNode<>(randomUUID(), ResultDefinition.resultDefinition()
                .setResultTextTemplate("Is electronic monitoring required result Text")
                .setShortCode("HMRCFPP")));

        final TreeNode<ResultLine> communityRequirements = new TreeNode<>(fromString("0007ee1d-869f-454f-8142-fc43cabd4b44"), null);
        communityRequirements.setJudicialResult(JudicialResult.judicialResult()
                .withLabel("Community requirements")
                .build()
        );

        communityRequirements.setResultDefinition(new TreeNode<>(randomUUID(), ResultDefinition.resultDefinition()
                .setResultTextTemplate("Community requirements result Text")));

        final TreeNode<ResultLine> unpaidWork = new TreeNode<>(fromString("16b4bb67-5a8c-43cb-9daf-01ffd03da6b3"), null);
        unpaidWork.setJudicialResult(JudicialResult.judicialResult()
                .withLabel("Unpaid work")
                .withAlwaysPublished(true)
                .build()
        );
        unpaidWork.setResultDefinition(new TreeNode<>(randomUUID(), ResultDefinition.resultDefinition()
                .setResultTextTemplate("Unpaid work result Text")
                .setDependantResultDefinitionGroup("Community Requirement")
                .setShortCode("UPWR")));

        final TreeNode<ResultLine> rehabilitationActivity = new TreeNode<>(fromString("c835f05e-1780-498a-8802-728f21ba9a6c"), null);
        rehabilitationActivity.setJudicialResult(JudicialResult.judicialResult()
                .withLabel("Rehabilitation activity")
                .withAlwaysPublished(true)
                .build()
        );
        rehabilitationActivity.setResultDefinition(new TreeNode<>(randomUUID(), ResultDefinition.resultDefinition()
                .setResultTextTemplate("Rehabilitation activity result Text")
                .setDependantResultDefinitionGroup("Community Requirement")
                .setShortCode("RAR")));

        communityOrderEnglandWales.addChildren(asList(isElectronicMonitoringRequired,communityRequirements));
        communityRequirements.addChildren(asList(unpaidWork, rehabilitationActivity));

        ResultTextHelper.setResultText(singletonList(communityOrderEnglandWales));

        assertThat(communityOrderEnglandWales.getJudicialResult().getResultText(), CoreMatchers.is ("PRT - Label\nParent result Text"+System.lineSeparator()+"Unpaid work result Text"+System.lineSeparator()+"Rehabilitation activity result Text"));
    }

    @Test
    public void shouldSetResultTextForNoResultText(){
        final List<TreeNode<ResultLine>> treeNodeList = singletonList(getTreeNode(TEXT_FOR_CONDITIONAL, "make Of Vehicle", "No", "make Of Second Vehicle", "vehicle Registration Of Second Vehicle", "guilty"));
        treeNodeList.get(0).addChildren(getTreeNodesWithoutResultTextTemplate());

        ResultTextHelper.setResultText(treeNodeList);

        assertThat(treeNodeList.get(0).getChildren().get(0).getJudicialResult().getResultText(), CoreMatchers.is ("NEXT - Label"));
        assertThat(treeNodeList.get(0).getJudicialResult().getResultText(), CoreMatchers.is ("NEXH - Result Label\nClamping order made for vehicle make Of Vehicle vehicle Registration Value and vehicle make Of Second Vehicle vehicle Registration Of Second Vehicle to take effect on. Reasons: guilty."));
    }

    private List<TreeNode<ResultLine>> getTreeNodesForPromptDirective(final String templateText, final String makeOfVehicle, final String vehicleToBeReleasedOnPaymentOfTheChargesDue,
                                                                       final String makeOfSecondVehicle, final String vehicleRegistrationOfSecondVehicle, final String reasons) {
        return singletonList(getTreeNode(templateText, makeOfVehicle, vehicleToBeReleasedOnPaymentOfTheChargesDue, makeOfSecondVehicle, vehicleRegistrationOfSecondVehicle, reasons));
    }

    private TreeNode<ResultLine> getTreeNode(final String templateText, final String makeOfVehicle, final String vehicleToBeReleasedOnPaymentOfTheChargesDue,
                                             final String makeOfSecondVehicle, final String vehicleRegistrationOfSecondVehicle, final String reasons) {

        final TreeNode<ResultLine> treeNode = new TreeNode<>(randomUUID(), null);
        treeNode.setJudicialResult(JudicialResult.judicialResult()
                .withLabel("Result Label")
                        .withAlwaysPublished(false)
                .withJudicialResultPrompts(asList(judicialResultPrompt()
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

    private List<TreeNode<ResultLine>> getTreeNodesForNameAddress(final String organisationName, final String firstName, final String middleName, final String lastName, final String suffix) {
        final TreeNode<ResultLine> treeNode = new TreeNode<>(randomUUID(), null);
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
                .withLabel("Label-Result")
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

    private List<TreeNode<ResultLine>> getTreeNodesForPromptDirectiveWithAlwaysPublished(final String templateText, final String makeOfVehicle, final String vehicleToBeReleasedOnPaymentOfTheChargesDue) {

        final TreeNode<ResultLine> treeNode = new TreeNode<>(randomUUID(), null);
        treeNode.setJudicialResult(JudicialResult.judicialResult()
                .withLabel("Result Label alwaysPublished")
                .withAlwaysPublished(true)
                .withJudicialResultPrompts(asList(judicialResultPrompt()
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


    private List<TreeNode<ResultLine>> getTreeNodesWithOnlyResultTextTemplate(final String resultTextTemplate) {
        final List<TreeNode<ResultLine>> treeNodeList = singletonList(new TreeNode<>(randomUUID(), null));
        treeNodeList.get(0)
                .setJudicialResult(JudicialResult.judicialResult().withLabel("Label")
                        .build());
        treeNodeList.get(0).setResultDefinition(new TreeNode<>(randomUUID(), ResultDefinition.resultDefinition()
                .setShortCode("NEXT")
                .setResultTextTemplate(resultTextTemplate)));
        return treeNodeList;
    }

    private List<TreeNode<ResultLine>> getTreeNodesWithoutResultTextTemplate() {
        final List<TreeNode<ResultLine>> treeNodeList = singletonList(new TreeNode<>(randomUUID(), null));
        treeNodeList.get(0)
                .setJudicialResult(JudicialResult.judicialResult().withLabel("Label")
                        .build());
        treeNodeList.get(0).setResultDefinition(new TreeNode<>(randomUUID(), ResultDefinition.resultDefinition()
                .setShortCode("NEXT")));
        return treeNodeList;
    }
}