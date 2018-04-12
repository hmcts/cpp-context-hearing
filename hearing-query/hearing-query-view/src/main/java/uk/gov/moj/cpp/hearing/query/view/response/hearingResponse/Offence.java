package uk.gov.moj.cpp.hearing.query.view.response.hearingResponse;

public final class Offence {

    private final String id;
    private final String wording;
    private final Integer count;
    private final String title;
    private final String legislation;
    private final Plea plea;
    private final Verdict verdict;
    private final String convictionDate;

    private Offence(final Builder builder) {
        this.id = builder.id;
        this.wording = builder.wording;
        this.count = builder.count;
        this.title = builder.title;
        this.legislation = builder.legislation;
        this.plea = builder.plea;
        this.verdict = builder.verdict;
        this.convictionDate = builder.convictionDate;
    }
    
   
    public String getId() {
        return id;
    }

    public String getWording() {
        return wording;
    }

    public Integer getCount() {
        return count;
    }

    public String getTitle() {
        return title;
    }

    public String getLegislation() {
        return legislation;
    }

    public Plea getPlea() {
        return plea;
    }

    public Verdict getVerdict() {
        return verdict;
    }

    public String getConvictionDate() {
        return convictionDate;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private String id;
        private String wording;
        private Integer count;
        private String title;
        private String legislation;
        private Plea plea;
        private Verdict verdict;
        private String convictionDate;
        
        public Builder withId(final String id) {
            this.id = id;
            return this;
        }
        
        public Builder withWording(final String wording) {
            this.wording = wording;
            return this;
        }
        
        public Builder withCount(final Integer count) {
            this.count = count;
            return this;
        }
        
        public Builder withTitle(final String title) {
            this.title = title;
            return this;
        }
        
        public Builder withLegislation(final String legislation) {
            this.legislation = legislation;
            return this;
        }
        
        public Builder withPlea(final Plea plea) {
            this.plea = plea;
            return this;
        }
        
        public Builder withVerdict(final Verdict verdict) {
            this.verdict = verdict;
            return this;
        }
        
        public Builder withConvictionDate(final String convictionDate) {
            this.convictionDate = convictionDate;
            return this;
        }
        
        public Offence build() {
            return new Offence(this);
        }
    }
}