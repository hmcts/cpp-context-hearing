package uk.gov.moj.cpp.hearing.persist.entity.ha;

import javax.persistence.Column;
import javax.persistence.Embeddable;

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
}
