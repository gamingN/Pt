package com.example.veeotech.postaltracking.pickup.bean;

import java.util.List;

/**
 * Created by VeeoTech on 28/4/2018.
 */

public class CustomerBean {


    /**
     * flag : 1
     * data : [{"customer_id":"111"}]
     */

    private int flag;
    /**
     * customer_id : 111
     */

    private List<DataEntity> data;

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public void setData(List<DataEntity> data) {
        this.data = data;
    }

    public int getFlag() {
        return flag;
    }

    public List<DataEntity> getData() {
        return data;
    }

    public static class DataEntity {
        private String customer_id;

        public void setCustomer_id(String customer_id) {
            this.customer_id = customer_id;
        }

        public String getCustomer_id() {
            return customer_id;
        }
    }
}
