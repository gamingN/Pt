package com.example.veeotech.postaltracking.warehouse.bean;

import java.io.Serializable;
import java.util.List;

/**
 * Created by VeeoTech on 2018/4/28.
 */

public class TypeInfoBean {
    /**
     * flag : 1
     * data : [{"type":"A","weight":"1"},{"type":"B","weight":"3"},{"type":"C","weight":"2"}]
     */

    private int flag;
    private List<CargoBean> data;

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public List<CargoBean> getData() {
        return data;
    }

    public void setData(List<CargoBean> data) {
        this.data = data;
    }

    public static class CargoBean implements Serializable {
        /**
         * type : A
         * weight : 1
         */

        private String type;
        private String weight;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getWeight() {
            return weight;
        }

        public void setWeight(String weight) {
            this.weight = weight;
        }
    }
}
