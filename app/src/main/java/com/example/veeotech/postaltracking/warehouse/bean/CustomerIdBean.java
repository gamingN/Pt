package com.example.veeotech.postaltracking.warehouse.bean;

import java.util.List;

/**
 * Created by VeeoTech on 2018/4/19.
 */

public class CustomerIdBean {
    /**
     * flag : 1
     * data : [{"order_id":"99999","customer_id":"111"},{"order_id":"88888","customer_id":"222"}]
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
         * order_id : 99999
         * customer_id : 111
         */

        private String order_id;
        private String customer_id;

        public String getOrder_id() {
            return order_id;
        }

        public void setOrder_id(String order_id) {
            this.order_id = order_id;
        }

        public String getCustomer_id() {
            return customer_id;
        }

        public void setCustomer_id(String customer_id) {
            this.customer_id = customer_id;
        }
    }
}
