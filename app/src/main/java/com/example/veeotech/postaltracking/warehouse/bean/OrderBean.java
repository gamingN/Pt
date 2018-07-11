package com.example.veeotech.postaltracking.warehouse.bean;

import java.util.List;

/**
 * Created by VeeoTech on 2018/5/2.
 */

public class OrderBean {
    /**
     * flag : 1
     * data : [{"order_id":"440088994411"}]
     */

    private int flag;
    private List<DataBean> data;

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * order_id : 440088994411
         */

        private String order_id;

        public String getOrder_id() {
            return order_id;
        }

        public void setOrder_id(String order_id) {
            this.order_id = order_id;
        }
    }
}
