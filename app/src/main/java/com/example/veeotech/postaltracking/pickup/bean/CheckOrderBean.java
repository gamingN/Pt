package com.example.veeotech.postaltracking.pickup.bean;

/**
 * Created by VeeoTech on 28/4/2018.
 */

public class CheckOrderBean {


    /**
     * order_id : 2018042802385456910
     * customer_id : 111
     */

    private String order_id;
    private String customer_id;

    public void setOrder_id(String order_id) {
        this.order_id = order_id;
    }

    public void setCustomer_id(String customer_id) {
        this.customer_id = customer_id;
    }

    public String getOrder_id() {
        return order_id;
    }

    public String getCustomer_id() {
        return customer_id;
    }
}
