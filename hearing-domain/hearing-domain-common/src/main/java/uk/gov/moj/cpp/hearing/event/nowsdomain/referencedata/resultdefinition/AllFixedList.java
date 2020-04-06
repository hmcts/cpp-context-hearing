package uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition;

import java.util.ArrayList;
import java.util.List;

public class AllFixedList {
    private List<FixedList> fixedListCollection = new ArrayList<>();

    public static AllFixedList allFixedList() {
        return new AllFixedList();
    }

    public List<FixedList> getFixedListCollection() {
        return this.fixedListCollection;
    }

    public AllFixedList setFixedListCollection(List<FixedList> fixedListCollection) {
        this.fixedListCollection = fixedListCollection;
        return this;
    }
}
