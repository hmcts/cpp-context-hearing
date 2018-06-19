package uk.gov.moj.cpp.hearing.event.nows.order;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class NowsDocumentOrder implements Serializable {

    private final static long serialVersionUID = -781257956072320445L;
    private String materialId;
    private String priority;
    private String amended;
    private String orderName;
    private String courtCentreName;
    private String courtClerkName;
    private String orderDate;
    private OrderDefendant defendant;
    private List<String> caseUrns = new ArrayList<String>();
    private List<OrderCase> cases = new ArrayList<OrderCase>();
    private String nowText;

    public String getMaterialId() {
        return materialId;
    }

    public void setMaterialId(String materialId) {
        this.materialId = materialId;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getAmended() {
        return amended;
    }

    public void setAmended(String amended) {
        this.amended = amended;
    }

    public String getOrderName() {
        return orderName;
    }

    public void setOrderName(String orderName) {
        this.orderName = orderName;
    }

    public String getCourtCentreName() {
        return courtCentreName;
    }

    public void setCourtCentreName(String courtCentreName) {
        this.courtCentreName = courtCentreName;
    }

    public String getCourtClerkName() {
        return courtClerkName;
    }

    public void setCourtClerkName(String courtClerkName) {
        this.courtClerkName = courtClerkName;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public OrderDefendant getDefendant() {
        return defendant;
    }

    public void setDefendant(OrderDefendant defendant) {
        this.defendant = defendant;
    }

    public List<String> getCaseUrns() {
        return caseUrns;
    }

    public void setCaseUrns(List<String> caseUrns) {
        this.caseUrns = caseUrns;
    }

    public List<OrderCase> getCases() {
        return cases;
    }

    public void setCases(List<OrderCase> cases) {
        this.cases = cases;
    }

    public String getNowText() {
        return nowText;
    }

    public void setNowText(String nowText) {
        this.nowText = nowText;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String materialId;
        private String priority;
        private String amended;
        private String orderName;
        private String courtCentreName;
        private String courtClerkName;
        private String orderDate;
        private OrderDefendant defendant;
        private List<String> caseUrns = new ArrayList<String>();
        private List<OrderCase> cases = new ArrayList<OrderCase>();
        private String nowText;

        private Builder() {
        }



        public Builder withMaterialId(String materialId) {
            this.materialId = materialId;
            return this;
        }

        public Builder withPriority(String priority) {
            this.priority = priority;
            return this;
        }

        public Builder withAmended(String amended) {
            this.amended = amended;
            return this;
        }

        public Builder withOrderName(String orderName) {
            this.orderName = orderName;
            return this;
        }

        public Builder withCourtCentreName(String courtCentreName) {
            this.courtCentreName = courtCentreName;
            return this;
        }

        public Builder withCourtClerkName(String courtClerkName) {
            this.courtClerkName = courtClerkName;
            return this;
        }

        public Builder withOrderDate(String orderDate) {
            this.orderDate = orderDate;
            return this;
        }

        public Builder withDefendant(OrderDefendant defendant) {
            this.defendant = defendant;
            return this;
        }

        public Builder withCaseUrns(List<String> caseUrns) {
            this.caseUrns = caseUrns;
            return this;
        }

        public Builder withCases(List<OrderCase> cases) {
            this.cases = cases;
            return this;
        }

        public Builder withNowText(String nowText) {
            this.nowText = nowText;
            return this;
        }

        public NowsDocumentOrder build() {
            NowsDocumentOrder nowsDocumentOrder = new NowsDocumentOrder();
            nowsDocumentOrder.setMaterialId(materialId);
            nowsDocumentOrder.setPriority(priority);
            nowsDocumentOrder.setAmended(amended);
            nowsDocumentOrder.setOrderName(orderName);
            nowsDocumentOrder.setCourtCentreName(courtCentreName);
            nowsDocumentOrder.setCourtClerkName(courtClerkName);
            nowsDocumentOrder.setOrderDate(orderDate);
            nowsDocumentOrder.setDefendant(defendant);
            nowsDocumentOrder.setCaseUrns(caseUrns);
            nowsDocumentOrder.setCases(cases);
            nowsDocumentOrder.setNowText(nowText);
            return nowsDocumentOrder;
        }
    }
}
