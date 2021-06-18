package uk.gov.moj.cpp.hearing.query.view.service.ctl.model;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public class PublicHoliday {
    private final UUID id;
    private final String division;
    private final String title;
    private final LocalDate date;

    public PublicHoliday(final UUID id,
                         final String division,
                         final String title,
                         final LocalDate date) {
        this.id = id;
        this.division = division;
        this.title = title;
        this.date = date;
    }

    public UUID getId() {
        return id;
    }

    public String getDivision() {
        return division;
    }

    public String getTitle() {
        return title;
    }

    public LocalDate getDate() {
        return date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PublicHoliday)) {
            return false;
        }
        final PublicHoliday that = (PublicHoliday) o;
        final boolean idAndDivisionEquals = Objects.equals(id, that.id) && Objects.equals(division, that.division);
        final boolean titleAndDate = Objects.equals(title, that.title) && Objects.equals(date, that.date);
        return idAndDivisionEquals && titleAndDate;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, division, title, date);
    }
}
