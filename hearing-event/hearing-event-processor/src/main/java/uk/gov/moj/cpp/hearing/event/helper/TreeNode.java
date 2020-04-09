package uk.gov.moj.cpp.hearing.event.helper;

import uk.gov.justice.core.courts.JudicialResult;
import uk.gov.justice.core.courts.Level;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@SuppressWarnings("squid:S2384")
public class TreeNode<T> {

    private final UUID id;
    private UUID resultDefinitionId;
    private UUID targetId;
    private UUID applicationId;
    private UUID defendantId;
    private UUID offenceId;
    private final List<TreeNode<T>> children = new ArrayList<>();
    private List<TreeNode<T>> parents = new ArrayList<>();
    private T data;
    private boolean presentInRules;
    private String ruleType;
    private Level level;
    private TreeNode<ResultDefinition> resultDefinition;
    private JudicialResult judicialResult;

    public TreeNode(final UUID id, final T data) {
        this.id = id;
        this.data = data;
    }

    public TreeNode(final UUID id, final T data, final Level level) {
        this.id = id;
        this.data = data;
        this.level = level;
    }

    public List<TreeNode<T>> getChildren() {
        return children;
    }

    public List<TreeNode<T>> getParents() {
        return parents;
    }

    public void addParent(final TreeNode<T> parent) {
        this.parents.add(parent);
    }

    public void addChild(final TreeNode<T> child) {
        this.children.add(child);
    }

    public void removeChild(final TreeNode<T> child) {
        this.children.remove(child);
    }

    public void addChildren(final List<TreeNode<T>> children) {
        children.stream().forEach(child -> {
            child.addParent(this);
            this.addChild(child);
        });
    }

    public void removeAllChildren() {
        this.children.clear();
    }

    public void removeAllParents() {
        this.parents = new ArrayList<>();
    }

    public T getData() {
        return this.data;
    }

    public void setData(final T data) {
        this.data = data;
    }

    public UUID getId() {
        return id;
    }

    public boolean isPresentInRules() {
        return presentInRules;
    }

    public boolean isNotPresentInRules() {
        return !presentInRules;
    }

    public TreeNode<T> markPresentInRules() {
        this.presentInRules = true;
        return this;
    }

    public boolean isNotLeaf() {
        return !this.children.isEmpty();
    }

    public boolean isLeaf() {
        return this.children.isEmpty() && !this.parents.isEmpty();
    }

    public boolean isStandalone() {
        return this.parents.isEmpty() && this.children.isEmpty();
    }

    public String getRuleType() {
        return ruleType;
    }

    public void setRuleType(final String ruleType) {
        this.ruleType = ruleType;
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(final Level level) {
        this.level = level;
    }

    public UUID getResultDefinitionId() {
        return resultDefinitionId;
    }

    public void setResultDefinitionId(final UUID resultDefinitionId) {
        this.resultDefinitionId = resultDefinitionId;
    }

    public UUID getTargetId() {
        return targetId;
    }

    public void setTargetId(final UUID targetId) {
        this.targetId = targetId;
    }

    public TreeNode<ResultDefinition> getResultDefinition() {
        return resultDefinition;
    }

    public void setResultDefinition(final TreeNode<ResultDefinition> resultDefinition) {
        this.resultDefinition = resultDefinition;
    }

    public JudicialResult getJudicialResult() {
        return judicialResult;
    }

    public void setJudicialResult(final JudicialResult judicialResult) {
        this.judicialResult = judicialResult;
    }

    public UUID getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(final UUID applicationId) {
        this.applicationId = applicationId;
    }

    public UUID getDefendantId() {
        return defendantId;
    }

    public void setDefendantId(final UUID defendantId) {
        this.defendantId = defendantId;
    }

    public UUID getOffenceId() {
        return offenceId;
    }

    public void setOffenceId(final UUID offenceId) {
        this.offenceId = offenceId;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final TreeNode<?> treeNode = (TreeNode<?>) o;
        return id.equals(treeNode.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
