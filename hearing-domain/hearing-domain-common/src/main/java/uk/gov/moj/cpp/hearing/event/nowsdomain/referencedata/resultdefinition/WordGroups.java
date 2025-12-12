package uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition;

import java.util.List;

public class WordGroups {

    private List<String> wordGroup;

    public static WordGroups wordGroups() {
        return new WordGroups();
    }

    public List<String> getWordGroup() {
        return this.wordGroup;
    }

    public WordGroups setWordGroup(List<String> wordGroup) {
        this.wordGroup = wordGroup;
        return this;
    }
}
