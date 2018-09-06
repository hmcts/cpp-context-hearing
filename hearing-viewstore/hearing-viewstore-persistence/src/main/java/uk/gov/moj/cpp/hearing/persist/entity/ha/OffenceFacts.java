package uk.gov.moj.cpp.hearing.persist.entity.ha;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public class OffenceFacts {

    @Column(name = "vehicle_registration")
    private String vehicleRegistration;

    @Column(name = "alcohol_reading_amount")
    private String alcoholReadingAmount;

    @Column(name = "alcohol_reading_method")
    private String alcoholReadingMethod;

    public String getVehicleRegistration() {
        return vehicleRegistration;
    }

    public void setVehicleRegistration(String vehicleRegistration) {
        this.vehicleRegistration = vehicleRegistration;
    }

    public String getAlcoholReadingAmount() {
        return alcoholReadingAmount;
    }

    public void setAlcoholReadingAmount(String alcoholReadingAmount) {
        this.alcoholReadingAmount = alcoholReadingAmount;
    }

    public String getAlcoholReadingMethod() {
        return alcoholReadingMethod;
    }

    public void setAlcoholReadingMethod(String alcoholReadingMethod) {
        this.alcoholReadingMethod = alcoholReadingMethod;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final OffenceFacts that = (OffenceFacts) o;
        return Objects.equals(vehicleRegistration, that.vehicleRegistration) &&
                Objects.equals(alcoholReadingAmount, that.alcoholReadingAmount) &&
                Objects.equals(alcoholReadingMethod, that.alcoholReadingMethod);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vehicleRegistration, alcoholReadingAmount, alcoholReadingMethod);
    }
}
