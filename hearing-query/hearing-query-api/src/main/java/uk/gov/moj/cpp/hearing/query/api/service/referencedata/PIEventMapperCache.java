package uk.gov.moj.cpp.hearing.query.api.service.referencedata;

import static java.util.UUID.fromString;

import uk.gov.moj.cpp.external.domain.referencedata.PIEventMapping;
import uk.gov.moj.cpp.external.domain.referencedata.PIEventMappingsList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"squid:S1192", "squid:S1488", "pmd:LocalVariableCouldBeFinal"}) //TBD - Need to be removed as part of stub
@ApplicationScoped
public class PIEventMapperCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(PIEventMapperCache.class);
    private final Map<UUID, PIEventMapping> eventMapperCache = new HashMap<>();
    @Inject
    private ReferenceDataService referenceDataService;

    //CCT-877 - Need to be remove once reference data endpoint is available
    public static PIEventMappingsList stubPIEventMapperCache() {

        List<PIEventMapping> cpPIHearingEventMappings = new ArrayList<>();

        final PIEventMapping piEventMapping1 = new PIEventMapping(fromString("aebfe8d9-74b4-442b-885f-702c818bf6b5"), "1", "Appellant Case Closed");
        final PIEventMapping piEventMapping2 = new PIEventMapping(fromString("50fb4a64-943d-4a2a-afe6-4b5c9e99e043"), "2", "Appellant name.Case Opened");
        final PIEventMapping piEventMapping3 = new PIEventMapping(fromString("2ae725a8-b605-4427-aa1d-b587d5c2d716"), "3", "Appellant name.Submissions");
        final PIEventMapping piEventMapping4 = new PIEventMapping(fromString("8b5afc76-e9fb-4e66-bbb6-f69e7e6d3b75"), "5", "Appellant Sworn");
        final PIEventMapping piEventMapping5 = new PIEventMapping(fromString("72a82077-7853-40cb-8ad6-c2d6d3f67a3f"), "6", "Bench Retire to consider judgement");
        final PIEventMapping piEventMapping6 = new PIEventMapping(fromString("60e58491-22ad-40ce-b7ff-4662d34a5be9"), "8", "Prosecution Case Closed");
        final PIEventMapping piEventMapping7 = new PIEventMapping(fromString("e85b5c05-d4cb-41b3-a2f7-08f3b0354cc1"), "9", "Defence Closing Speech");
        final PIEventMapping piEventMapping8 = new PIEventMapping(fromString("25963c08-0277-4100-8cca-0fbfda30f0e6"), "10", "Prosecution Closing Speech");
        final PIEventMapping piEventMapping9 = new PIEventMapping(fromString("a7dd8f8a-1673-44e5-93fc-e740317243b0"), "11", "witness witness number.continues");
        final PIEventMapping piEventMapping10 = new PIEventMapping(fromString("0df93f18-0a21-40f5-9fb3-da4749cd70fe"), "12", "Hearing Finished");
        final PIEventMapping piEventMapping11 = new PIEventMapping(fromString("ed8ec4b8-2b57-4f7a-9017-0eddcb0acb14"), "13", "Interpreter/Intermediary sworn");
        final PIEventMapping piEventMapping12 = new PIEventMapping(fromString("928f8290-5e82-49fd-bd45-b12c24786eda"), "14", "Jury retires to consider verdict");
        final PIEventMapping piEventMapping13 = new PIEventMapping(fromString("f0ecf365-a3e5-4472-8be1-fbdc31eaccc4"), "15", "Jury Sworn in");
        final PIEventMapping piEventMapping14 = new PIEventMapping(fromString("4a6bb5cd-3d39-4049-80d2-e113437d2a45"), "16", "Legal Submissions");
        final PIEventMapping piEventMapping15 = new PIEventMapping(fromString("e9060336-4821-4f46-969c-e08b33b48071"), "17", "Prosecution Opening");
        final PIEventMapping piEventMapping16 = new PIEventMapping(fromString("160ecb51-29ee-4954-bbbf-daab18a24fbb"), "19", "Case released until [time]");
        final PIEventMapping piEventMapping17 = new PIEventMapping(fromString("089ca562-1661-468f-a93e-8811a32e5ed3"), "20", "Legal Submissions");
        final PIEventMapping piEventMapping18 = new PIEventMapping(fromString("b335327a-7f58-4f26-a2ef-7e07134ba60b"), "21", "Legal Submissions");
        final PIEventMapping piEventMapping19 = new PIEventMapping(fromString("f9104401-8e05-4aa6-81f2-bb56f9a6d65b"), "22", "Witness Evidence Concluded");
        final PIEventMapping piEventMapping20 = new PIEventMapping(fromString("b84d9f05-6ef7-44b0-8dff-f0d0b2689643"), "23", "Reporting Restrictions. For details please contact the Court Manager");
        final PIEventMapping piEventMapping21 = new PIEventMapping(fromString("d60bcffe-631d-4422-88d0-317dd5207c68"), "24", "Respondent Case Closed");
        final PIEventMapping piEventMapping22 = new PIEventMapping(fromString("ef369a7a-dbc4-421b-bb41-3530214643bf"), "25", "Respondent Case Opened");
        final PIEventMapping piEventMapping23 = new PIEventMapping(fromString("64476e43-2138-46d5-b58b-848582cf9b07"), "26", "Resume");
        final PIEventMapping piEventMapping24 = new PIEventMapping(fromString("b71e7d2a-d3b3-4a55-a393-6d451767fc05"), "27", "Case Started");
        final PIEventMapping piEventMapping25 = new PIEventMapping(fromString("174c28b9-8cb5-44ba-9c86-5932346ddac1"), "28", "Summing up");
        final PIEventMapping piEventMapping26 = new PIEventMapping(fromString("504e16fc-6dfa-4276-9721-2b50a9646f4d"), "29", "Defendant name.Sworn");
        final PIEventMapping piEventMapping27 = new PIEventMapping(fromString("0b9d26d8-54d7-4e04-b3bc-be6158a06ef8"), "30", "witness  witness number.sworn");
        final PIEventMapping piEventMapping28 = new PIEventMapping(fromString("3353768b-3b86-46d1-8e1c-3e5804a735a2"), "31", "Verdict");
        final PIEventMapping piEventMapping29 = new PIEventMapping(fromString("a3a9fe0c-a9a7-4e17-b0cd-42606722bbb0"), "32", "Defence.name Case Opened");
        final PIEventMapping piEventMapping30 = new PIEventMapping(fromString("cc00cca8-39ba-431c-b08f-8c6f9be185d1"), "33", "Defence Case Closed.name");
        final PIEventMapping piEventMapping31 = new PIEventMapping(fromString("a0f73f1a-31c9-41d7-b113-574254cf74dc"), "34", "Judge's Directions");
        final PIEventMapping piEventMapping32 = new PIEventMapping(fromString("c8b8b0d5-a923-45be-b9a0-e28f33365fa5"), "35", "Prosecution Case");

        cpPIHearingEventMappings.add(piEventMapping1);
        cpPIHearingEventMappings.add(piEventMapping2);
        cpPIHearingEventMappings.add(piEventMapping3);
        cpPIHearingEventMappings.add(piEventMapping4);
        cpPIHearingEventMappings.add(piEventMapping5);
        cpPIHearingEventMappings.add(piEventMapping6);
        cpPIHearingEventMappings.add(piEventMapping7);
        cpPIHearingEventMappings.add(piEventMapping8);
        cpPIHearingEventMappings.add(piEventMapping9);
        cpPIHearingEventMappings.add(piEventMapping10);
        cpPIHearingEventMappings.add(piEventMapping11);
        cpPIHearingEventMappings.add(piEventMapping12);
        cpPIHearingEventMappings.add(piEventMapping13);
        cpPIHearingEventMappings.add(piEventMapping14);
        cpPIHearingEventMappings.add(piEventMapping15);
        cpPIHearingEventMappings.add(piEventMapping16);
        cpPIHearingEventMappings.add(piEventMapping17);
        cpPIHearingEventMappings.add(piEventMapping18);
        cpPIHearingEventMappings.add(piEventMapping19);
        cpPIHearingEventMappings.add(piEventMapping20);
        cpPIHearingEventMappings.add(piEventMapping21);
        cpPIHearingEventMappings.add(piEventMapping22);
        cpPIHearingEventMappings.add(piEventMapping23);
        cpPIHearingEventMappings.add(piEventMapping24);
        cpPIHearingEventMappings.add(piEventMapping25);
        cpPIHearingEventMappings.add(piEventMapping26);
        cpPIHearingEventMappings.add(piEventMapping27);
        cpPIHearingEventMappings.add(piEventMapping28);
        cpPIHearingEventMappings.add(piEventMapping29);
        cpPIHearingEventMappings.add(piEventMapping30);
        cpPIHearingEventMappings.add(piEventMapping31);
        cpPIHearingEventMappings.add(piEventMapping32);

        PIEventMappingsList piEventMappingsList = new PIEventMappingsList(cpPIHearingEventMappings);

        return piEventMappingsList;
    }

    @PostConstruct
    public void init() {
        //final PIEventMappingsList eventMapping = referenceDataService.listAllPIEventMappings(); // CCT-877 - TBD
        final PIEventMappingsList eventMapping = stubPIEventMapperCache(); //CCT-877 - Need to be remove once reference data endpoint is available

        if (eventMapping.getCpPIHearingEventMappings().isEmpty()) {
            LOGGER.warn("!!PI Hearing Event Mapping is Empty!!");
        }

        eventMapping.getCpPIHearingEventMappings().forEach(event -> eventMapperCache.put(event.getCpHearingEventId(), event));
    }

    public Set<UUID> getCppHearingEventIds() {
        if (CollectionUtils.isEmpty(eventMapperCache.keySet())) {
            init();
        }
        return eventMapperCache.keySet();
    }
}