package uk.gov.justice.ccr.notepad.view;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.IsNull.notNullValue;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultType.ADDRESS;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultType.DURATION;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultType.FIXL;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultType.INT;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultType.NAMEADDRESS;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultType.TXT;
import static uk.gov.justice.ccr.notepad.view.ResultPromptViewBuilder.ONEOF;

import uk.gov.justice.ccr.notepad.process.Knowledge;
import uk.gov.justice.ccr.notepad.process.Processor;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultType;
import uk.gov.justice.ccr.notepad.shared.AbstractTest;
import uk.gov.justice.ccr.notepad.view.parser.PartsResolver;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.mortbay.log.Log;

@RunWith(MockitoJUnitRunner.class)
public class ResultPromptViewBuilderTest extends AbstractTest {

    @Spy
    @InjectMocks
    Processor processor = new Processor();

    @InjectMocks
    ResultPromptViewBuilder target;

    @Mock
    private List<PromptChoice> mockPromptChoices;

    @Test
    public void buildFromKnowledge() throws Exception {
        final List<Part> parts = new PartsResolver().getParts("parp");
        final Knowledge knowledge = processor.processParts(parts.stream().map(Part::getValueAsString).collect(Collectors.toList()), LocalDate.now());
        final ResultDefinitionView resultDefinitionView = new ResultDefinitionViewBuilder().buildFromKnowledge(parts, knowledge, new ArrayList<>(), true,  false , "", mockPromptChoices);

        final Knowledge knowledgeResultPrompt = processor.processResultPrompt(resultDefinitionView.getResultCode(), LocalDate.now());
        final String code = randomUUID().toString();
        final Set<NameAddress> nameAddressList = new HashSet<>();
        NameAddress nameAddress1 = NameAddress.nameAddress()
                .withLabel("Org1")
                .withAddressParts(AddressParts.addressParts()
                        .withName("Org1")
                        .withAddress1("10 Downing Street")
                        .withPostCode("SW1A 2AA")
                        .withEmail1("xyz@gmail.com")
                        .build())
                .build();
        nameAddressList.add(nameAddress1);
        final PromptChoice pc1 = new PromptChoice();
        pc1.setComponentType("ONEOF");
        pc1.setCode(code);
        pc1.setType(ResultType.NAMEADDRESS);
        pc1.setPromptRef("pr1");
        pc1.setComponentLabel("Minor creditor name");
        pc1.setListLabel("Select Minor Creditor");
        pc1.setAddressType("Organisation");
        pc1.setLabel("Minor creditor name and address first name");
        pc1.setNameEmail(true);
        pc1.setNameAddressList(nameAddressList);
        pc1.setPartName("FirstName");
        final PromptChoice pc2 = new PromptChoice();
        pc2.setComponentType("ONEOF");
        pc2.setCode(code);
        pc2.setType(ResultType.NAMEADDRESS);
        pc2.setPromptRef("pr2");
        pc2.setComponentLabel("Minor creditor name");
        pc2.setLabel("Minor creditor name and address last name");
        pc2.setPartName("LastName");
        pc1.setAddressType("Organisation");
        pc1.setLabel("Minor creditor name and address first name");
        final List<PromptChoice> promptChoices = knowledgeResultPrompt.getPromptChoices();
        promptChoices.add(pc1);
        promptChoices.add(pc2);

        final ResultPromptView result = target.buildFromKnowledge(knowledgeResultPrompt);
        assertThat(result.getPromptChoices().size(), is(4));
        final PromptChoice p1 = result.getPromptChoices().get(0);
        final PromptChoice p2 = result.getPromptChoices().get(1);
        final PromptChoice p3 = result.getPromptChoices().get(2);
        final PromptChoice p4 = result.getPromptChoices().get(3);
        assertThat(Arrays.asList(p1.getCode().length(), p1.getLabel(), p1.getType(), p1.getRequired())
                , containsInAnyOrder(Arrays.asList(36, "Prohibited activities", TXT, Boolean.TRUE).toArray()));
        assertThat(Arrays.asList(p2.getCode().length(), p2.getLabel(), p2.getType(), p2.getRequired())
                , containsInAnyOrder(Arrays.asList(36, "Period of prohibition", DURATION, Boolean.TRUE).toArray()));
        assertThat(Arrays.asList(p3.getCode().length(), p3.getLabel(), p3.getType(), p3.getRequired())
                , containsInAnyOrder(Arrays.asList(36, "Address Line 1", ADDRESS, Boolean.TRUE).toArray()));
        assertThat(Arrays.asList(p4.getCode().length(), p4.getLabel(), p4.getType(), p4.getNameEmail())
                , containsInAnyOrder(Arrays.asList(36, "Minor creditor name and address first name", null, Boolean.TRUE).toArray()));
        final List<Children> childrenList = p2.getChildren();
        final Children c1 = childrenList.get(0);
        final Children c2 = childrenList.get(1);
        final Children c3 = childrenList.get(2);
        final Children c4 = childrenList.get(3);
        assertThat(Arrays.asList(c1.getType(), c1.getCode(), c1.getLabel())
                , containsInAnyOrder(Arrays.asList(INT, "382ff620-7645-4b1f-9905-4620b8d2f0e9","Years").toArray()));
        assertThat(Arrays.asList(c2.getType(), c2.getCode(), c2.getLabel())
                , containsInAnyOrder(Arrays.asList(INT, "5e51a190-6d8b-4104-8c97-877fe5157cb0", "Months").toArray()));
        assertThat(Arrays.asList(c3.getType(), c3.getCode(), c3.getLabel())
                , containsInAnyOrder(Arrays.asList(INT, "44a5fa6c-da56-401a-b10d-cb02138caa21","Weeks").toArray()));
        assertThat(Arrays.asList(c4.getType(), c4.getCode(), c4.getLabel())
                , containsInAnyOrder(Arrays.asList(INT, "f9758ee7-82ca-4451-9553-9f7e2bade36a","Days").toArray()));

        final List<Children> addressLineChildrenList = p3.getChildren();
        final Children addressLineC1 = addressLineChildrenList.get(0);
        final Children addressLineC2 = addressLineChildrenList.get(1);

        assertThat(Arrays.asList(addressLineC1.getType(), addressLineC1.getLabel())
                , containsInAnyOrder(Arrays.asList(TXT, "Address Line 1").toArray()));
        assertThat(Arrays.asList(addressLineC2.getType(), addressLineC2.getLabel())
                , containsInAnyOrder(Arrays.asList(TXT, "Address Line 2").toArray()));

        assertThat(p4.getComponentType(), is(ONEOF));
        assertThat(p4.getChildren().size(), is(1));
        final Children oneOfNameAddress = p4.getChildren().get(0);
        assertThat(oneOfNameAddress.getType(), is(NAMEADDRESS));
        final List<Children> nameAddressChildrenList = oneOfNameAddress.getChildrenList();
        final Children nameAddressC1 = nameAddressChildrenList.get(0);
        final Children nameAddressC2 = nameAddressChildrenList.get(1);
        assertThat(nameAddressC1.getPartName(), is("FirstName"));
        assertThat(nameAddressC2.getPartName(), is("LastName"));
    }

    @Test
    public void shouldBuildKnowledgeWithOneOf() {

        final Knowledge knowledge = new Knowledge();
        final PromptChoice promptChoice = new PromptChoice();
        final Children children1 =  new Children("Fixed Date", ResultType.DATE, promptChoice.getFixedList(), promptChoice.getChildren(), "aea2ee79-47b4-4023-9a95-1b327e6e03d5");
        final Children children2 =  new Children("Week Commencing", ResultType.DATE, promptChoice.getFixedList(), promptChoice.getChildren(), "3e7ae7bd-f736-4729-ab06-82bf966bc72f");
        promptChoice.addChildren(children1);
        promptChoice.addChildren(children2);

        final List<PromptChoice> promptChoices = new ArrayList<>();
        promptChoice.setLabel("Fixed Date");
        promptChoice.setType(ResultType.DATE);
        promptChoice.setCode(UUID.randomUUID().toString());
        promptChoice.setComponentType(ONEOF);
        promptChoices.add(promptChoice);
        knowledge.setPromptChoices(promptChoices);
        final ResultPromptView result = target.buildFromKnowledge(knowledge);

        result.getPromptChoices().forEach(pc -> {
            pc.getChildren().forEach(c -> {
                assertThat(c.getCode(), is(notNullValue()));
                assertThat(c.getLabel(), is(notNullValue()));
                assertThat(c.getType(), is(notNullValue()));
            });
        });
    }

    @Test
    public void shouldBuildFromKnowledgeWithOneOf() throws Exception {
        final List<Part> parts = new PartsResolver().getParts("ATRNR");
        final Knowledge knowledge = processor.processParts(parts.stream().map(Part::getValueAsString).collect(Collectors.toList()), LocalDate.now());
        final ResultDefinitionView resultDefinitionView = new ResultDefinitionViewBuilder().buildFromKnowledge(parts, knowledge, new ArrayList<>(),true, false , "", mockPromptChoices);
        final String code = randomUUID().toString();
        final Set<NameAddress> nameAddressList = new HashSet<>();
        NameAddress nameAddress1 = NameAddress.nameAddress()
                .withLabel("Org1")
                .withAddressParts(AddressParts.addressParts()
                        .withName("Org1")
                        .withAddress1("10 Downing Street")
                        .withPostCode("SW1A 2AA")
                        .withEmail1("xyz@gmail.com")
                        .build())
                .build();
        nameAddressList.add(nameAddress1);
        final PromptChoice pc1 = new PromptChoice();
        pc1.setComponentType("ONEOF");
        pc1.setCode(code);
        pc1.setType(ResultType.NAMEADDRESS);
        pc1.setPromptRef("pr1");
        pc1.setComponentLabel("Minor creditor name");
        pc1.setListLabel("Select Minor Creditor");
        pc1.setAddressType("Organisation");
        pc1.setLabel("Minor creditor name and address first name");
        pc1.setNameEmail(true);
        pc1.setNameAddressList(nameAddressList);
        pc1.setPartName("FirstName");
        final PromptChoice pc2 = new PromptChoice();
        pc2.setComponentType("ONEOF");
        pc2.setCode(code);
        pc2.setType(ResultType.NAMEADDRESS);
        pc2.setPromptRef("pr2");
        pc2.setComponentLabel("Minor creditor name");
        pc2.setLabel("Minor creditor name and address last name");
        pc2.setPartName("LastName");
        pc1.setAddressType("Organisation");
        pc1.setLabel("Minor creditor name and address first name");

        final Knowledge knowledgeWithNAMEADDRESSWithInOneOf = processor.processResultPrompt(resultDefinitionView.getResultCode(), LocalDate.now());
        final List<PromptChoice> promptChoices = knowledgeWithNAMEADDRESSWithInOneOf.getPromptChoices();
        promptChoices.add(pc1);
        promptChoices.add(pc2);
        knowledgeWithNAMEADDRESSWithInOneOf.setPromptChoices(promptChoices);

        final ResultPromptView result = target.buildFromKnowledge(knowledgeWithNAMEADDRESSWithInOneOf);

        final ObjectMapper objectMapper = new ObjectMapper();
        final String JsonResponse = objectMapper.writeValueAsString(result);
        Log.info("Response : " + JsonResponse);

        assertThat(result.getPromptChoices().size()
                , is(5)
        );
        final PromptChoice p1 = result.getPromptChoices().get(0);
        final PromptChoice p2 = result.getPromptChoices().get(1);
        final PromptChoice p3 = result.getPromptChoices().get(2);
        final PromptChoice p4 = result.getPromptChoices().get(3);
        final PromptChoice p5 = result.getPromptChoices().get(4);

        assertThat(Arrays.asList(p1.getCode().length(), p1.getLabel(), p1.getType(), p1.getRequired(), p1.getHidden())
                , containsInAnyOrder(Arrays.asList(36, "Treatment institution / place", TXT, Boolean.TRUE, Boolean.FALSE).toArray()));

        assertThat(Arrays.asList(p2.getCode().length(), p2.getLabel(), p2.getType(), p2.getRequired(), p2.getHidden())
                , containsInAnyOrder(Arrays.asList(36, "Under direction of", TXT, Boolean.TRUE, Boolean.FALSE).toArray()));

        assertThat(Arrays.asList(p3.getCode().length(), p3.getLabel(), p3.getType(), p3.getRequired(), p3.getHidden())
                , containsInAnyOrder(Arrays.asList(36, "Period of treatment", DURATION, Boolean.TRUE, Boolean.FALSE).toArray()));

        assertThat(Arrays.asList(p4.getCode().length(), p4.getLabel(), p4.getComponentType(), p4.getRequired(), p4.getHidden())
                , containsInAnyOrder(Arrays.asList(36, "Period of treatment-oneOf", "ONEOF", Boolean.TRUE, Boolean.FALSE).toArray()));

        assertThat(Arrays.asList(p5.getCode().length(), p5.getLabel(), p5.getType(), p5.getRequired(), p5.getHidden())
                , containsInAnyOrder(Arrays.asList(36, "Treatment intervals", TXT, Boolean.FALSE, Boolean.FALSE).toArray()));

        final List<Children> childrenList = p3.getChildren();
        final Children c1 = childrenList.get(0);
        final Children c2 = childrenList.get(1);
        final Children c3 = childrenList.get(2);
        final Children c4 = childrenList.get(3);
        assertThat(Arrays.asList(c1.getType(), c1.getCode(), c1.getLabel()), containsInAnyOrder(Arrays.asList(INT, "8c0775df-5ef0-4f46-83a3-da50e6d9395f","Years").toArray()));
        assertThat(Arrays.asList(c2.getType(), c2.getCode(), c2.getLabel()), containsInAnyOrder(Arrays.asList(INT, "67c73a42-ef21-4211-aaa3-578f7f9061dc","Months").toArray()));
        assertThat(Arrays.asList(c3.getType(), c3.getCode(), c3.getLabel()), containsInAnyOrder(Arrays.asList(INT, "6e9d4a84-3b64-4f77-b37e-e9428a15e72a","Weeks").toArray()));
        assertThat(Arrays.asList(c4.getType(), c4.getCode(), c4.getLabel()), containsInAnyOrder(Arrays.asList(INT, "5214e666-ef32-4a15-8ab0-cc0600e10c36","Days").toArray()));

        final List<Children> oneOfChildrenList = p4.getChildren();
        assertThat(oneOfChildrenList.size(), is(4));
        final Children gc1 = oneOfChildrenList.get(0);
        final Children gc2 = oneOfChildrenList.get(1);
        final Children gc3 = oneOfChildrenList.get(2);
        final Children gc4 = oneOfChildrenList.get(3);

        assertThat(Arrays.asList(gc1.getType(), gc1.getCode(), gc1.getLabel()), containsInAnyOrder(Arrays.asList(TXT, "b9f6aed0-382a-49a9-803d-d19cfade22b1" ,"Additional information").toArray()));
        assertThat(Arrays.asList(gc2.getType(), gc2.getCode(), gc2.getLabel()), containsInAnyOrder(Arrays.asList(FIXL, "47337f1c-e343-4093-884f-035ba96c4db0","Conviction / acquittal").toArray()));
        assertThat(Arrays.asList(gc3.getType(), gc3.getCode(), gc3.getLabel()), containsInAnyOrder(Arrays.asList(DURATION, "8c0775df-5ef0-4f46-83a3-da50e6d9395f","Period of treatment-oneOf").toArray()));
        assertThat(Arrays.asList(gc4.getType(), gc4.getCode(), gc4.getLabel(),gc4.getListLabel(), gc4.getAddressType()), containsInAnyOrder(Arrays.asList(NAMEADDRESS, code,"Minor creditor name", "Select Minor Creditor", "Organisation").toArray()));

        final Set<NameAddress> gc4NameAddressList = gc4.getNameAddressList();
        assertThat(gc4NameAddressList.size(), is(1));
        assertThat(gc4NameAddressList, is(nameAddressList));

        final List<Children> oneOfGrandChildrenList = gc3.getChildrenList();
        final Children gcc1 = oneOfGrandChildrenList.get(0);
        final Children gcc2 = oneOfGrandChildrenList.get(1);
        final Children gcc3 = oneOfGrandChildrenList.get(2);
        final Children gcc4 = oneOfGrandChildrenList.get(3);
        assertThat(Arrays.asList(gcc1.getType(), gcc1.getCode(), gcc1.getLabel()), containsInAnyOrder(Arrays.asList(INT, "8c0775df-5ef0-4f46-83a3-da50e6d9395f","Years").toArray()));
        assertThat(Arrays.asList(gcc2.getType(), gcc2.getCode(), gcc2.getLabel()), containsInAnyOrder(Arrays.asList(INT, "67c73a42-ef21-4211-aaa3-578f7f9061dc","Months").toArray()));
        assertThat(Arrays.asList(gcc3.getType(), gcc3.getCode(), gcc3.getLabel()), containsInAnyOrder(Arrays.asList(INT, "6e9d4a84-3b64-4f77-b37e-e9428a15e72a","Weeks").toArray()));
        assertThat(Arrays.asList(gcc4.getType(), gcc4.getCode(), gcc4.getLabel()), containsInAnyOrder(Arrays.asList(INT, "5214e666-ef32-4a15-8ab0-cc0600e10c36", "Days").toArray()));

        final List<Children> oneOfNameAddressChildrenList  = gc4.getChildrenList();
        final Children gcc5 = oneOfNameAddressChildrenList.get(0);
        final Children gcc6= oneOfNameAddressChildrenList.get(1);
        assertThat(Arrays.asList(gcc5.getType(), gcc5.getLabel(), gcc5.getPartName()), containsInAnyOrder(Arrays.asList(TXT, "Minor creditor name and address first name", "FirstName").toArray()));
        assertThat(Arrays.asList(gcc6.getType(),  gcc6.getLabel(), gcc6.getPartName()), containsInAnyOrder(Arrays.asList(TXT,"Minor creditor name and address last name", "LastName").toArray()));
    }

}