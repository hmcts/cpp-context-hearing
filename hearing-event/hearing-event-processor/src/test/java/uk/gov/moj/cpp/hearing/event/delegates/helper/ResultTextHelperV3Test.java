package uk.gov.moj.cpp.hearing.event.delegates.helper;

import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.core.courts.JudicialResultPrompt.judicialResultPrompt;


import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.core.courts.JudicialResult;
import uk.gov.justice.core.courts.ResultLine2;
import uk.gov.moj.cpp.hearing.event.helper.TreeNode;

@RunWith(MockitoJUnitRunner.class)
public class ResultTextHelperV3Test {


    public static final String TEXT_FOR_PROMTS = "Vehicle {makeOfVehicle} {vehicleRegistration} for which clamping order made on {clampingOrderMadeOn} not to be sold. {vehicleToBeReleasedOnPaymentOfTheChargesDue} {vehicleToBeReleasedForthwithWithoutPaymentOfAnyChargesDue}.";
    public static final String TEXT_FOR_CONDITIONAL = "Clamping order made for vehicle {makeOfVehicle} {vehicleRegistration} [and vehicle {makeOfSecondVehicle} {vehicleRegistrationOfSecondVehicle}] to take effect on. [Reasons: {reasons}.]";
    public static final String TEXT_FOR_PROMTS_WITH_ALL_EMPTY = "Vehicle {makeOfVehicle} for which clamping order made on not to be sold. {vehicleToBeReleasedOnPaymentOfTheChargesDue}.";
    public static final String TEXT_FOR_CONDITIONA_WITH_ALL_EMPTYL = "Clamping order made for vehicle {makeOfVehicle} [and vehicle {makeOfSecondVehicle} {vehicleRegistrationOfSecondVehicle}] to take effect on. [Reasons: {reasons}.]";
    public static final String TEXT_FOR_RESULT_LABEL = "Vehicle {makeOfVehicle} %ResultLabel%.";
    public static final String TEXT_FOR_ALL_PROMPTS = "Vehicle  %Prompts%.";


    @Test
    public void shouldNotSetResultTextWhenTempleDoesNotExist(){
        final List<TreeNode<ResultLine2>> treeNodeList = singletonList(new TreeNode<>(randomUUID(), null));

        treeNodeList.get(0).setJudicialResult(JudicialResult.judicialResult()
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


        ResultTextHelperV3.setResultText(treeNodeList);

        assertThat(treeNodeList.get(0).getJudicialResult().getResultText(), is (nullValue()));

    }


    @Test
    public void shouldSetResultTextForPromptDirective(){
        final List<TreeNode<ResultLine2>> treeNodeList = singletonList(new TreeNode<>(randomUUID(), null));

        treeNodeList.get(0).setJudicialResult(JudicialResult.judicialResult()
                .withResultTextTemplate("Exclusion Requirement: Not to enter {placeArea} between {fromDate} and {untilDate}. This exclusion requirement lasts until {endDateOfExclusion}. {additionalInformation}")
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


        ResultTextHelperV3.setResultText(treeNodeList);

        assertThat(treeNodeList.get(0).getJudicialResult().getResultText(), is ("Exclusion Requirement: Not to enter area1 between 01-01-2012 and 01-01-2013. This exclusion requirement lasts until 01-01-2014. text prompt"));
    }


    @Test
    public void shouldSetResultTextForPromptDirectiveWithBooleanValues(){
        final List<TreeNode<ResultLine2>> treeNodeList = getTreeNodesForPromptDirective(TEXT_FOR_PROMTS, "make Of Vehicle", "No", null, null, null);

        ResultTextHelperV3.setResultText(treeNodeList);

        assertThat(treeNodeList.get(0).getJudicialResult().getResultText(), is ("Vehicle make Of Vehicle vehicle Registration Value for which clamping order made on 01-01-2012 not to be sold.  Vehicle to be released forthwith, without payment of any charges due."));
    }


    @Test
    public void shouldSetResultTextForPromptDirectiveWithEmptySomeValues(){
        final List<TreeNode<ResultLine2>> treeNodeList = getTreeNodesForPromptDirective(TEXT_FOR_PROMTS, "make Of Vehicle", null, null, null, null);

        ResultTextHelperV3.setResultText(treeNodeList);

        assertThat(treeNodeList.get(0).getJudicialResult().getResultText(), is ("Vehicle make Of Vehicle vehicle Registration Value for which clamping order made on 01-01-2012 not to be sold.  Vehicle to be released forthwith, without payment of any charges due."));
    }

    @Test
    public void shouldSetResultTextForPromptDirectiveWithEmptyValues(){
        final List<TreeNode<ResultLine2>> treeNodeList = getTreeNodesForPromptDirective(TEXT_FOR_PROMTS_WITH_ALL_EMPTY, null, null, null, null, null);

        ResultTextHelperV3.setResultText(treeNodeList);

        assertThat(treeNodeList.get(0).getJudicialResult().getResultText(), is (nullValue()));
    }

    @Test
    public void shouldSetResultTextForNameAddressForOrganisation(){
        final List<TreeNode<ResultLine2>> treeNodeList = getTreeNodesForNameAddress("Organisation Name", null, null, null, "");

        ResultTextHelperV3.setResultText(treeNodeList);

        assertThat(treeNodeList.get(0).getJudicialResult().getResultText(), is ("To pay costs to Organisation Name, Address 1, Address 2, Address 3, Address 4, Address 5, E14 9YZ"));
    }

    @Test
    public void shouldSetResultTextForNameAddressForIndividual(){
        final List<TreeNode<ResultLine2>> treeNodeList = getTreeNodesForNameAddress(null, "first name", "middle name", "last name", "");

        ResultTextHelperV3.setResultText(treeNodeList);

        assertThat(treeNodeList.get(0).getJudicialResult().getResultText(), is ("To pay costs to first name middle name last name, Address 1, Address 2, Address 3, Address 4, Address 5, E14 9YZ"));
    }

    @Test
    public void shouldSetResultTextForNameAddressWithEmptyValues(){
        final List<TreeNode<ResultLine2>> treeNodeList = getTreeNodesForNameAddress(null, null, null, null, "~Name");

        ResultTextHelperV3.setResultText(treeNodeList);

        assertThat(treeNodeList.get(0).getJudicialResult().getResultText(), is (nullValue()));
    }

    @Test
    public void shouldSetResultTextForNameAddressForOrganisationWithOnlyName(){
        final List<TreeNode<ResultLine2>> treeNodeList = getTreeNodesForNameAddress("Organisation Name", null, null, null, "~Name");

        ResultTextHelperV3.setResultText(treeNodeList);

        assertThat(treeNodeList.get(0).getJudicialResult().getResultText(), is ("To pay costs to Organisation Name"));
    }


    @Test
    public void shouldSetResultTextForNameAddressForIndividualWithOnlyName(){
        final List<TreeNode<ResultLine2>> treeNodeList = getTreeNodesForNameAddress(null, "first name", "middle name", "last name", "~Name");

        ResultTextHelperV3.setResultText(treeNodeList);

        assertThat(treeNodeList.get(0).getJudicialResult().getResultText(), is ("To pay costs to first name middle name last name"));
    }

    @Test
    public void shouldSetResultTextForVariableResultDirective(){
        final List<TreeNode<ResultLine2>> treeNodeList = singletonList(new TreeNode<>(randomUUID(), null));
        treeNodeList.get(0).setJudicialResult(JudicialResult.judicialResult()
                .withResultTextTemplate("result Text with %NEXH% and %NEXB%")
                .build()
        );


        treeNodeList.get(0).addChildren(getTreeNodesForNameAddress("Organisation Name", null, null, null, "~Name"));
        treeNodeList.get(0).addChildren(getTreeNodesForPromptDirective(TEXT_FOR_PROMTS, "make Of Vehicle", "No", null, null,  null));

        ResultTextHelperV3.setResultText(treeNodeList);

        assertThat(treeNodeList.get(0).getChildren().get(0).getJudicialResult().getResultText(), is ("To pay costs to Organisation Name"));
        assertThat(treeNodeList.get(0).getChildren().get(1).getJudicialResult().getResultText(), is ("Vehicle make Of Vehicle vehicle Registration Value for which clamping order made on 01-01-2012 not to be sold.  Vehicle to be released forthwith, without payment of any charges due."));
        assertThat(treeNodeList.get(0).getJudicialResult().getResultText(), is ("result Text with Vehicle make Of Vehicle vehicle Registration Value for which clamping order made on 01-01-2012 not to be sold.  Vehicle to be released forthwith, without payment of any charges due. and To pay costs to Organisation Name"));

    }

    @Test
    public void shouldSetResultTextForVariableResultDirectiveWithEmptyValues(){
        final List<TreeNode<ResultLine2>> treeNodeList = singletonList(new TreeNode<>(randomUUID(), null));
        treeNodeList.get(0).setJudicialResult(JudicialResult.judicialResult()
                .withResultTextTemplate("result Text with %NEXH% and %NEXB%")
                .build()
        );


        treeNodeList.get(0).addChildren(getTreeNodesForNameAddress(null, null, null, null, "~Name"));
        treeNodeList.get(0).addChildren(getTreeNodesForPromptDirective(TEXT_FOR_PROMTS_WITH_ALL_EMPTY, null, "No", null, null,  null));

        ResultTextHelperV3.setResultText(treeNodeList);

        assertThat(treeNodeList.get(0).getChildren().get(0).getJudicialResult().getResultText(), is (nullValue()));
        assertThat(treeNodeList.get(0).getChildren().get(1).getJudicialResult().getResultText(), is (nullValue()));
        assertThat(treeNodeList.get(0).getJudicialResult().getResultText(), is (nullValue()));

    }


    @Test
    public void shouldSetResultTextForConditionalPromptsWithValues(){
        final List<TreeNode<ResultLine2>> treeNodeList = singletonList(new TreeNode<>(randomUUID(), null));

        treeNodeList.get(0).setJudicialResult(getJudicialResult(TEXT_FOR_CONDITIONAL, "make Of Vehicle", "No", "make Of Second Vehicle", "vehicle Registration Of Second Vehicle", "quilty"));

        ResultTextHelperV3.setResultText(treeNodeList);

        assertThat(treeNodeList.get(0).getJudicialResult().getResultText(), is ("Clamping order made for vehicle make Of Vehicle vehicle Registration Value and vehicle make Of Second Vehicle vehicle Registration Of Second Vehicle to take effect on. Reasons: quilty."));
    }

    @Test
    public void shouldSetResultTextForConditionalPromptsWithoutValues(){
        final List<TreeNode<ResultLine2>> treeNodeList = singletonList(new TreeNode<>(randomUUID(), null));

        treeNodeList.get(0).setJudicialResult(getJudicialResult(TEXT_FOR_CONDITIONAL, "make Of Vehicle", "No", null, null, "quilty"));

        ResultTextHelperV3.setResultText(treeNodeList);

        assertThat(treeNodeList.get(0).getJudicialResult().getResultText(), is ("Clamping order made for vehicle make Of Vehicle vehicle Registration Value  to take effect on. Reasons: quilty."));
    }

    @Test
    public void shouldSetResultTextForConditionalChildWithValues(){
        final List<TreeNode<ResultLine2>> treeNodeList = singletonList(new TreeNode<>(randomUUID(), null));
        treeNodeList.get(0).setJudicialResult(JudicialResult.judicialResult()
                .withResultTextTemplate("result Text [with %NEXH%] get")
                .build()
        );


        treeNodeList.get(0).addChildren(getTreeNodesForPromptDirective(TEXT_FOR_CONDITIONAL, "make Of Vehicle", "No", "make Of Second Vehicle", "vehicle Registration Of Second Vehicle", "quilty"));

        ResultTextHelperV3.setResultText(treeNodeList);

        assertThat(treeNodeList.get(0).getJudicialResult().getResultText(), is ("result Text with Clamping order made for vehicle make Of Vehicle vehicle Registration Value and vehicle make Of Second Vehicle vehicle Registration Of Second Vehicle to take effect on. Reasons: quilty. get"));
        assertThat(treeNodeList.get(0).getChildren().get(0).getJudicialResult().getResultText(), is ("Clamping order made for vehicle make Of Vehicle vehicle Registration Value and vehicle make Of Second Vehicle vehicle Registration Of Second Vehicle to take effect on. Reasons: quilty."));
    }

    @Test
    public void shouldSetResultTextForConditionalChildWithoutValues(){
        final List<TreeNode<ResultLine2>> treeNodeList = singletonList(new TreeNode<>(randomUUID(), null));
        treeNodeList.get(0).setJudicialResult(JudicialResult.judicialResult()
                .withResultTextTemplate("result Text [with %NEXH%] get")
                .build()
        );


        treeNodeList.get(0).addChildren(getTreeNodesForPromptDirective(TEXT_FOR_CONDITIONA_WITH_ALL_EMPTYL, null, null, null, null, null));

        ResultTextHelperV3.setResultText(treeNodeList);

        assertThat(treeNodeList.get(0).getJudicialResult().getResultText(), is (nullValue()));
        assertThat(treeNodeList.get(0).getChildren().get(0).getJudicialResult().getResultText(), is (nullValue()));
    }

    @Test
    public void shouldSetResultTextForFixedResultDirective(){
        final List<TreeNode<ResultLine2>> treeNodeList = singletonList(new TreeNode<>(randomUUID(), null));
        treeNodeList.get(0).setJudicialResult(JudicialResult.judicialResult()
                .withResultTextTemplate("result Text with %AllChildText%")
                .build()
        );

        treeNodeList.get(0).addChildren(getTreeNodesForNameAddress("Organisation Name", null, null, null, "~Name"));
        treeNodeList.get(0).addChildren(getTreeNodesForPromptDirective(TEXT_FOR_PROMTS, "make Of Vehicle", "No", null, null,  null));

        ResultTextHelperV3.setResultText(treeNodeList);

        assertThat(treeNodeList.get(0).getChildren().get(0).getJudicialResult().getResultText(), is ("To pay costs to Organisation Name"));
        assertThat(treeNodeList.get(0).getChildren().get(1).getJudicialResult().getResultText(), is ("Vehicle make Of Vehicle vehicle Registration Value for which clamping order made on 01-01-2012 not to be sold.  Vehicle to be released forthwith, without payment of any charges due."));
        assertThat(treeNodeList.get(0).getJudicialResult().getResultText(), is ("result Text with To pay costs to Organisation Name, Vehicle make Of Vehicle vehicle Registration Value for which clamping order made on 01-01-2012 not to be sold.  Vehicle to be released forthwith, without payment of any charges due."));

    }

    @Test
    public void shouldSetResultTextForFixedResultDirectiveWithSomeEmptyValues(){
        final List<TreeNode<ResultLine2>> treeNodeList = singletonList(new TreeNode<>(randomUUID(), null));
        treeNodeList.get(0).setJudicialResult(JudicialResult.judicialResult()
                .withResultTextTemplate("result Text with %AllChildText%")
                .build()
        );

        treeNodeList.get(0).addChildren(getTreeNodesForNameAddress("Organisation Name", null, null, null, "~Name"));
        treeNodeList.get(0).addChildren(getTreeNodesForPromptDirective(TEXT_FOR_PROMTS_WITH_ALL_EMPTY, null, null, null, null,  null));

        ResultTextHelperV3.setResultText(treeNodeList);

        assertThat(treeNodeList.get(0).getChildren().get(0).getJudicialResult().getResultText(), is ("To pay costs to Organisation Name"));
        assertThat(treeNodeList.get(0).getChildren().get(1).getJudicialResult().getResultText(), is (nullValue()));
        assertThat(treeNodeList.get(0).getJudicialResult().getResultText(), is ("result Text with To pay costs to Organisation Name"));

    }

    @Test
    public void shouldSetResultTextForFixedResultDirectiveWithEmptyValues(){
        final List<TreeNode<ResultLine2>> treeNodeList = singletonList(new TreeNode<>(randomUUID(), null));
        treeNodeList.get(0).setJudicialResult(JudicialResult.judicialResult()
                .withResultTextTemplate("result Text with %AllChildText%")
                .build()
        );

        treeNodeList.get(0).addChildren(getTreeNodesForNameAddress(null, null, null, null, "~Name"));
        treeNodeList.get(0).addChildren(getTreeNodesForPromptDirective(TEXT_FOR_PROMTS_WITH_ALL_EMPTY, null, null, null, null,  null));

        ResultTextHelperV3.setResultText(treeNodeList);

        assertThat(treeNodeList.get(0).getChildren().get(0).getJudicialResult().getResultText(), is (nullValue()));
        assertThat(treeNodeList.get(0).getChildren().get(1).getJudicialResult().getResultText(), is (nullValue()));
        assertThat(treeNodeList.get(0).getJudicialResult().getResultText(), is (nullValue()));

    }

    @Test
    public void shouldSetResultTextWithResultLabel(){
        final List<TreeNode<ResultLine2>> treeNodeList = singletonList(new TreeNode<>(randomUUID(), null));

        treeNodeList.get(0).setJudicialResult(getJudicialResult(TEXT_FOR_RESULT_LABEL, "make Of Vehicle", "No", "make Of Second Vehicle", "vehicle Registration Of Second Vehicle", "quilty"));

        ResultTextHelperV3.setResultText(treeNodeList);

        assertThat(treeNodeList.get(0).getJudicialResult().getResultText(), is ("Vehicle make Of Vehicle Result Label."));
    }

    @Test
    public void shouldSetResultTextWithAllNoneNullPropmts(){
        final List<TreeNode<ResultLine2>> treeNodeList = singletonList(new TreeNode<>(randomUUID(), null));

        treeNodeList.get(0).setJudicialResult(getJudicialResult(TEXT_FOR_ALL_PROMPTS, "value1", "No", null, null, "quilty"));

        ResultTextHelperV3.setResultText(treeNodeList);

        assertThat(treeNodeList.get(0).getJudicialResult().getResultText(), is ("Vehicle  Make of vehicle: value1, Vehicle registration: vehicle Registration Value, Clamping order made on: 01-01-2012, Vehicle to be released forthwith, without payment of any charges due, Additional information: text prompt, Reasons: quilty."));
    }


    private List<TreeNode<ResultLine2>> getTreeNodesForNameAddress(final String organisationName, final String firstName, final String middleName, final String lastName, final String suffix) {
        final List<TreeNode<ResultLine2>> treeNodeList = singletonList(new TreeNode<>(randomUUID(), null));
        treeNodeList.get(0).setJudicialResult(getJudicialResultForNameAddress(organisationName, firstName, middleName, lastName, suffix));
        return treeNodeList;
    }

    private List<TreeNode<ResultLine2>> getTreeNodesForPromptDirective(final String templateText, final String makeOfVehicle, final String vehicleToBeReleasedOnPaymentOfTheChargesDue,
                                                                       final String makeOfSecondVehicle, final String vehicleRegistrationOfSecondVehicle, final String reasons) {
        final List<TreeNode<ResultLine2>> treeNodeList = singletonList(new TreeNode<>(randomUUID(), null));

        treeNodeList.get(0).setJudicialResult(getJudicialResult(templateText, makeOfVehicle, vehicleToBeReleasedOnPaymentOfTheChargesDue, makeOfSecondVehicle, vehicleRegistrationOfSecondVehicle, reasons));
        return treeNodeList;
    }

    private JudicialResult getJudicialResult(final String templateText, final String makeOfVehicle, final String vehicleToBeReleasedOnPaymentOfTheChargesDue,
                                             final String makeOfSecondVehicle, final String vehicleRegistrationOfSecondVehicle, final String reasons) {
        return JudicialResult.judicialResult()
                .withResultTextTemplate(templateText)
                .withShortCode("NEXH")
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
                .build();
    }

    private JudicialResult getJudicialResultForNameAddress(final String organisationName, final String firstName, final String middleName, final String lastName, final String suffix) {
        return JudicialResult.judicialResult()
                .withResultTextTemplate("To pay costs to {minorCreditorNameAndAddress" + suffix + "}")
                .withShortCode("NEXB")
                .withJudicialResultPrompts(Arrays.asList(judicialResultPrompt()
                                .withPromptReference("minorcreditornameandaddressOrganisationName")
                                .withValue(organisationName)
                                .withType("NAMEADDRESS")
                                .withPartName("OrganisationName")
                                .build(),
                        judicialResultPrompt()
                                .withPromptReference("minorcreditornameandaddressOrganisationName")
                                .withValue(firstName)
                                .withType("NAMEADDRESS")
                                .withPartName("FirstName")
                                .build(),
                        judicialResultPrompt()
                                .withPromptReference("minorcreditornameandaddressOrganisationName")
                                .withValue(middleName)
                                .withType("NAMEADDRESS")
                                .withPartName("MiddleName")
                                .build(),
                        judicialResultPrompt()
                                .withPromptReference("minorcreditornameandaddressOrganisationName")
                                .withValue(lastName)
                                .withType("NAMEADDRESS")
                                .withPartName("LastName")
                                .build(),
                        judicialResultPrompt()
                                .withPromptReference("minorcreditornameandaddressAddress1")
                                .withValue("Address 1")
                                .withType("NAMEADDRESS")
                                .withPartName("AddressLine1")
                                .build(),
                        judicialResultPrompt()
                                .withPromptReference("minorcreditornameandaddressAddress2")
                                .withValue("Address 2")
                                .withType("NAMEADDRESS")
                                .withPartName("AddressLine2")
                                .build(),
                        judicialResultPrompt()
                                .withPromptReference("minorcreditornameandaddressAddress3")
                                .withValue("Address 3")
                                .withType("NAMEADDRESS")
                                .withPartName("AddressLine3")
                                .build(),
                        judicialResultPrompt()
                                .withPromptReference("minorcreditornameandaddressAddress4")
                                .withValue("Address 4")
                                .withType("NAMEADDRESS")
                                .withPartName("AddressLine4")
                                .build(),
                        judicialResultPrompt()
                                .withPromptReference("minorcreditornameandaddressAddress5")
                                .withValue("Address 5")
                                .withType("NAMEADDRESS")
                                .withPartName("AddressLine5")
                                .build(),
                        judicialResultPrompt()
                                .withPromptReference("minorcreditornameandaddressPostCode")
                                .withValue("E14 9YZ")
                                .withType("NAMEADDRESS")
                                .withPartName("PostCode")
                                .build())
                )
                .build();
    }

}
