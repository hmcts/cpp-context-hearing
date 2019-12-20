package uk.gov.moj.cpp.hearing.xhibit.xmlgenerator;

import static java.util.Optional.empty;
import static java.util.Optional.of;

import java.util.Optional;

@SuppressWarnings({"squid:MethodCyclomaticComplexity"})
public enum XhibitEvent {

    START("10100"),

    RESUME("10500"),

    RESPONDENT_CLOSES("20605"),

    APPELLANT_CLOSES("20608"),

    BENCH_RETIRES("20609"),

    CLOSED_SPEECH_PROSECUTION("20907"),

    CLOSED_CASE_PROSECUTION("20908"),

    DEFENCE_GIVES_CLOSING_SPEECH_REGARDING_DEFENDANT("20909"),

    SUMMING_UP("20911"),

    JURY_RETIRES("20914"),

    VERDICT("20919"),

    REPORTING_RESTRICTION_DIRECTION("21201"),

    END("30500");

    private String value;

    XhibitEvent(final String value) {
        this.value = value;
    }


    public static Optional<XhibitEvent> valueFor(final String value) {

        for (XhibitEvent event : values()) {
            if (event.value.equals(value)) {
                return of(event);
            }
        }
        return empty();
    }
}
