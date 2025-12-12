package uk.gov.moj.cpp.hearing.domain.xhibit;

import java.time.LocalDate;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class LocalDateAdapter extends XmlAdapter<String, LocalDate> {

    @Override
    public LocalDate unmarshal(final String inputDate) {
        if (inputDate == null) {
            return null;
        }
        return LocalDate.parse(inputDate);
    }

    @Override
    public String marshal(final LocalDate inputDate) {
        if (inputDate == null) {
            return null;
        }
        return inputDate.toString();
    }

}
