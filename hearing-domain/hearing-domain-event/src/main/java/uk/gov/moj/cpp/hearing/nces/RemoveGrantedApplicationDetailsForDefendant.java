package uk.gov.moj.cpp.hearing.nces;

import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;

@Event("hearing.event.defendant-remove-granted-application")
public class RemoveGrantedApplicationDetailsForDefendant implements Serializable {

    public RemoveGrantedApplicationDetailsForDefendant() {
        // just an event
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {

        private Builder() {
        }


        public RemoveGrantedApplicationDetailsForDefendant build() {
            return new RemoveGrantedApplicationDetailsForDefendant();
        }
    }
}
