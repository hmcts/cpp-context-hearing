package uk.gov.moj.cpp.hearing.nows.events;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Case implements Serializable {
    private final static long serialVersionUID = -5213228465902757928L;

    private String id;
    private String urn;
    private String bailStatus;
    private String custodyTimeLimitDate;
    private List<Offence> offences = new ArrayList<Offence>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrn() {
        return urn;
    }

    public void setUrn(String urn) {
        this.urn = urn;
    }

    public String getBailStatus() {
        return bailStatus;
    }

    public void setBailStatus(String bailStatus) {
        this.bailStatus = bailStatus;
    }

    public String getCustodyTimeLimitDate() {
        return custodyTimeLimitDate;
    }

    public void setCustodyTimeLimitDate(String custodyTimeLimitDate) {
        this.custodyTimeLimitDate = custodyTimeLimitDate;
    }

    public List<Offence> getOffences() {
        return offences;
    }

    public void setOffences(List<Offence> offences) {
        this.offences = offences;
    }

}
