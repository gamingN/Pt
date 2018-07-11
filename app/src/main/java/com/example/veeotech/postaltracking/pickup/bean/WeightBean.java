package com.example.veeotech.postaltracking.pickup.bean;

import java.util.List;

/**
 * Created by VeeoTech on 28/4/2018.
 */

public class WeightBean {

    /**
     * flag : 1
     * data : [{"qty":"6","unit":"個","weight":"0.00"}]
     */

    private int flag;
    /**
     * qty : 6
     * unit : 個
     * weight : 0.00
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
        private String qty;
        private String unit;
        private String weight;

        public void setQty(String qty) {
            this.qty = qty;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }

        public void setWeight(String weight) {
            this.weight = weight;
        }

        public String getQty() {
            return qty;
        }

        public String getUnit() {
            return unit;
        }

        public String getWeight() {
            return weight;
        }
    }
}
