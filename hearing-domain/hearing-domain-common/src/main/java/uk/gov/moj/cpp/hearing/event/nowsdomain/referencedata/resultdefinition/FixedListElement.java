package uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class FixedListElement implements Serializable {

    private static final long serialVersionUID = 7861556632527338638L;

    private String code;
    private String value;
    private String welshValue;
    private String cjsQualifier;

    @JsonCreator
    public FixedListElement(@JsonProperty("code") final String code,
                            @JsonProperty("value") final String value,
                            @JsonProperty("welshValue") final String welshValue,
                            @JsonProperty("cjsQualifier") final String cjsQualifier) {
        this.code = code;
        this.value = value;
        this.welshValue = welshValue;
        this.cjsQualifier = cjsQualifier;
    }

    public FixedListElement() {
    }

    public static FixedListElement fixedListElement() {
        return new FixedListElement();
    }

    public String getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }

    public String getWelshValue() {
        return welshValue;
    }

    public String getCjsQualifier() {
        return cjsQualifier;
    }

    public FixedListElement setCode(String code) {
        this.code = code;
        return this;
    }

    public FixedListElement setValue(String value) {
        this.value = value;
        return this;
    }

    public FixedListElement setWelshValue(String welshValue) {
        this.welshValue = welshValue;
        return this;
    }
    
    public FixedListElement setCjsQualifier(final String cjsQualifier) {
        this.cjsQualifier = cjsQualifier;
        return this;
    }
}
