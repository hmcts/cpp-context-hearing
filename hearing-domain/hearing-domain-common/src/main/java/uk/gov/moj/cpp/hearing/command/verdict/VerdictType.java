package uk.gov.moj.cpp.hearing.command.verdict;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class VerdictType implements Serializable {

    private static final long serialVersionUID = 1L;
    private UUID id;
    private String category;
    private String categoryType;

    public VerdictType() {
    }

    @JsonCreator
    protected VerdictType(@JsonProperty("id") final UUID id,
            @JsonProperty("category") final String category,
            @JsonProperty("categoryType") final String categoryType) {
        this.id = id;
        this.category = category;
        this.categoryType = categoryType;
    }

    public UUID getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public String getCategoryType() {
        return categoryType;
    }

    public VerdictType setId(UUID id) {
        this.id = id;
        return this;
    }

    public VerdictType setCategory(String category) {
        this.category = category;
        return this;
    }

    public VerdictType setCategoryType(String categoryType) {
        this.categoryType = categoryType;
        return this;
    }

   public static VerdictType verdictType(){
        return new VerdictType();
   }
}