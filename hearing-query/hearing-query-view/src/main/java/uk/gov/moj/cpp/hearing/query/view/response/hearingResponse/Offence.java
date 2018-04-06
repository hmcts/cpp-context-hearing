package uk.gov.moj.cpp.hearing.query.view.response.hearingResponse;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "wording",
        "count",
        "title",
        "legislation",
        "plea",
        "verdict"
})
public class Offence {
    private String id;
    private String wording;
    private Integer count;
    private String title;
    private String legislation;
    private Plea plea;
    private Verdict verdict;
    private String convictionDate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Offence withId(String id) {
        this.id = id;
        return this;
    }

    public String getWording() {
        return wording;
    }

    public void setWording(String wording) {
        this.wording = wording;
    }

    public Offence withWording(String wording) {
        this.wording = wording;
        return this;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Offence withCount(Integer count) {
        this.count = count;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Offence withTitle(String title) {
        this.title = title;
        return this;
    }

    public String getLegislation() {
        return legislation;
    }

    public void setLegislation(String legislation) {
        this.legislation = legislation;
    }

    public Offence withLegislation(String legislation) {
        this.legislation = legislation;
        return this;
    }

    public Plea getPlea() {
        return plea;
    }

    public void setPlea(Plea plea) {
        this.plea = plea;
    }

    public Offence withPlea(Plea plea) {
        this.plea = plea;
        return this;
    }

    public Verdict getVerdict() {
        return verdict;
    }

    public void setVerdict(Verdict verdict) {
        this.verdict = verdict;
    }

    public Offence withVerdict(Verdict verdict) {
        this.verdict = verdict;
        return this;
    }

    public String getConvictionDate() {
        return convictionDate;
    }

    public Offence withConvictionDate(String convictionDate) {
        this.convictionDate = convictionDate;
        return this;
    }


}
