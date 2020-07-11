package uk.gov.justice.ccr.notepad.view;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Objects;

@JsonInclude(value = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class NameAddress implements Serializable {

    private static final long serialVersionUID = 2186548466720532701L;


    private String label;

    private AddressParts addressParts;

    @JsonCreator
    private NameAddress(@JsonProperty("label") final String label, @JsonProperty("addressParts") final AddressParts addressParts) {
        this.label = label;
        this.addressParts = addressParts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final NameAddress that = (NameAddress) o;
        return Objects.equals(label, that.label) &&
                Objects.equals(addressParts, that.addressParts);
    }

    @Override
    public int hashCode() {
        return label != null ? label.hashCode() : 0;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public AddressParts getAddressParts() {
        return addressParts;
    }

    public void setAddressParts(AddressParts addressParts) {
        this.addressParts = addressParts;
    }

    public static NameAddress.NameAddressBuilder nameAddress() {
        return new NameAddress.NameAddressBuilder();
    }

    public static class NameAddressBuilder {

        private String label;
        private AddressParts addressParts;


        public NameAddress.NameAddressBuilder withLabel(String label) {
            this.label = label;
            return this;
        }

        public NameAddress.NameAddressBuilder withAddressParts(AddressParts addressParts) {
            this.addressParts = addressParts;
            return this;
        }

        public NameAddress build() {
            return new NameAddress(label, addressParts);
        }
    }


}
