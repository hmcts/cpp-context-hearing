package uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FixedList {

    private UUID id;

    private LocalDate startDate;

    private LocalDate endDate;

    private List<FixedListElement> elements;

    public static FixedList fixedList() {
        return new FixedList();
    }

    public UUID getId() {
        return id;
    }

    public FixedList setId(UUID id) {
        this.id = id;
        return this;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public FixedList setStartDate(LocalDate startDate) {
        this.startDate = startDate;
        return this;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public FixedList setEndDate(LocalDate endDate) {
        this.endDate = endDate;
        return this;
    }

    public List<FixedListElement> getElements() {
        return elements;
    }

    public FixedList setElements(List<FixedListElement> elements) {
        this.elements = elements;
        return this;
    }
}
