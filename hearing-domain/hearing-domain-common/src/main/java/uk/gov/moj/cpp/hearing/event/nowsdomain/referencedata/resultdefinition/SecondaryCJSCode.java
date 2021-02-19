package uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SecondaryCJSCode {
    private String cjsCode;

    private String text;

    public String getCjsCode() {
        return cjsCode;
    }

    public String getText() {
        return text;
    }

    public void setCjsCode(String cjsCode) {
        this.cjsCode = cjsCode;
    }

    public void setText(String text) {
        this.text = text;
    }
}
