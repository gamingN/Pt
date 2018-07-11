package com.example.veeotech.postaltracking.pickup.bean;

import java.util.List;

/**
 * Created by VeeoTech on 18/4/2018.
 */

public class OrderCreateBean {

    /**
     * flag : 1
     * data : [{"order_id":"201804185502054"}]
     */

    private int flag;
    /**
     * order_id : 201804185502054
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
        private String order_id;

        public void setOrder_id(String order_id) {
            this.order_id = order_id;
        }

        public String getOrder_id() {
            return order_id;
        }
    }
}
