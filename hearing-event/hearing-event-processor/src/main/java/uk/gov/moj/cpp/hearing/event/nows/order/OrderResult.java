package uk.gov.moj.cpp.hearing.event.nows.order;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class OrderResult implements Serializable {

    private final static long serialVersionUID = 5323524335441343266L;
    private String label;
    private List<OrderPrompt> prompts = new ArrayList<OrderPrompt>();


    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<OrderPrompt> getPrompts() {
        return prompts;
    }

    public void setPrompts(List<OrderPrompt> prompts) {
        this.prompts = prompts;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String label;
        private List<OrderPrompt> prompts = new ArrayList<OrderPrompt>();

        private Builder() {
        }



        public Builder withLabel(String label) {
            this.label = label;
            return this;
        }

        public Builder withPrompts(List<OrderPrompt> prompts) {
            this.prompts = prompts;
            return this;
        }

        public OrderResult build() {
            OrderResult result = new OrderResult();
            result.setLabel(label);
            result.setPrompts(prompts);
            return result;
        }
    }
}
