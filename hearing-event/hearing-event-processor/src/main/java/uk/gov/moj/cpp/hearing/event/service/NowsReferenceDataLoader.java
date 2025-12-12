package uk.gov.moj.cpp.hearing.event.service;

import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toMap;
import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.enveloper.Enveloper.envelop;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.event.helper.TreeNode;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.AllFixedList;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.AllResultDefinitions;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinitionRuleType;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.json.JsonObject;

@SuppressWarnings({"squid:S3358", "squid:S1612"})
public class NowsReferenceDataLoader {
    private static final String GET_ALL_RESULT_DEFINITIONS_REQUEST_ID = "referencedata.get-all-result-definitions";
    private static final String GET_ALL_FIXED_LIST = "referencedata.get-all-fixed-list";
    private static final String ON_QUERY_PARAMETER = "on";

    @Inject
    @ServiceComponent(Component.EVENT_PROCESSOR)
    private Requester requester;

    @Inject
    private Enveloper enveloper;

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    private List<String> trim(final List<String> strs) {
        return strs == null ? null : strs.stream().map(s -> s == null ? null : s.trim()).collect(Collectors.toList());
    }

    private void trimUserGroups(final ResultDefinition resultDefinition) {
        resultDefinition.setUserGroups(trim(resultDefinition.getUserGroups()));
        resultDefinition.getPrompts().forEach(p -> p.setUserGroups(trim(p.getUserGroups())));
    }

    public Map<UUID, TreeNode<ResultDefinition>> loadAllResultDefinitionAsTree(final JsonEnvelope context, final LocalDate localDate) {

        final AllResultDefinitions allResultDefinition = getAllResultDefinition(context, localDate);

        final Map<UUID, TreeNode<ResultDefinition>> treeNodeMap = initialiseTree(allResultDefinition.getResultDefinitions());

        mapChildren(treeNodeMap);

        return treeNodeMap;
    }

    public AllResultDefinitions getAllResultDefinition(final JsonEnvelope context, final LocalDate localDate) {
        final String strLocalDate = localDate.toString();
        final JsonEnvelope requestEnvelope = enveloper.withMetadataFrom(context, GET_ALL_RESULT_DEFINITIONS_REQUEST_ID)
                .apply(createObjectBuilder().add(ON_QUERY_PARAMETER, strLocalDate).build());

        final JsonEnvelope jsonResultEnvelope = requester.requestAsAdmin(requestEnvelope);
        final AllResultDefinitions allResultDefinitions = jsonObjectToObjectConverter.convert(jsonResultEnvelope.payloadAsJsonObject(), AllResultDefinitions.class);
        allResultDefinitions.getResultDefinitions().forEach(rd -> trimUserGroups(rd));
        return allResultDefinitions;
    }

    private Map<UUID, TreeNode<ResultDefinition>> initialiseTree(final List<ResultDefinition> resultDefinitions) {
        final Map<UUID, TreeNode<ResultDefinition>> treeNodeMap = new HashMap<>();

        treeNodeMap.putAll(resultDefinitions.stream().collect(toMap(ResultDefinition::getId, rd -> new TreeNode<>(rd.getId(), rd))));
        return treeNodeMap;
    }

    private void mapChildren(final Map<UUID, TreeNode<ResultDefinition>> treeNodeMap) {
        treeNodeMap.forEach((key, value) -> {
            final List<ResultDefinitionRuleType> resultDefinitionRules = value.getData().getResultDefinitionRules();
            if (resultDefinitionRules != null && !resultDefinitionRules.isEmpty()) {
                resultDefinitionRules.forEach(node -> {
                    final UUID childResultDefinitionId = node.getChildResultDefinitionId();
                    final TreeNode<ResultDefinition> childTreeNode = treeNodeMap.get(childResultDefinitionId);
                    if (nonNull(childTreeNode)) {
                        childTreeNode.addParent(value);
                        childTreeNode.markPresentInRules();
                        childTreeNode.setRuleType(node.getRuleType());
                        value.addChild(childTreeNode);
                    }
                });
                value.markPresentInRules();
            }
        });
    }

    public AllFixedList loadAllFixedList(final JsonEnvelope context, final LocalDate localDate) {
        final String strLocalDate = localDate.toString();
        final Envelope<JsonObject> requestEnvelope = envelop(createObjectBuilder().add(ON_QUERY_PARAMETER, strLocalDate)
                .build())
                .withName(GET_ALL_FIXED_LIST)
                .withMetadataFrom(context);

        return requester.request(requestEnvelope, AllFixedList.class).payload();
    }
}
