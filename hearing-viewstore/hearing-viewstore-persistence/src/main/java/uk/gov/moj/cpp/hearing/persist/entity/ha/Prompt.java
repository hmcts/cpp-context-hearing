package uk.gov.moj.cpp.hearing.persist.entity.ha;

import java.util.Objects;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "ha_prompt")
public class Prompt {

    @Id
    @Column(name = "id")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "result_line_id")
    private ResultLine resultLine;

    @Column(name = "fixed_list_code")
    private String fixedListCode;

    @Column(name = "prompt_reference")
    private String promptReference;

    @Column(name = "label")
    private String label;

    @Column(name = "value")
    private String value;

    @Column(name = "welsh_value")
    private String welshValue;

    public Prompt() {
        //For JPA
    }

    public static Prompt prompt() {
        return new Prompt();
    }

    public UUID getId() {
        return id;
    }

    public Prompt setId(UUID id) {
        this.id = id;
        return this;
    }

    public ResultLine getResultLine() {
        return resultLine;
    }

    public Prompt setResultLine(ResultLine resultLine) {
        this.resultLine = resultLine;
        return this;
    }

    public String getFixedListCode() {
        return fixedListCode;
    }

    public Prompt setFixedListCode(String fixedListCode) {
        this.fixedListCode = fixedListCode;
        return this;
    }

    public String getLabel() {
        return label;
    }

    public Prompt setLabel(String label) {
        this.label = label;
        return this;
    }

    public String getValue() {
        return value;
    }

    public Prompt setValue(String value) {
        this.value = value;
        return this;
    }

    public String getWelshValue() {
        return welshValue;
    }

    public Prompt setWelshValue(String welshValue) {
        this.welshValue = welshValue;
        return this;
    }

    public String getPromptReference() {
        return promptReference;
    }

    public void setPromptReference(String promptReference) {
        this.promptReference = promptReference;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Prompt prompt = (Prompt) o;
        return Objects.equals(id, prompt.id);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id);
    }
}