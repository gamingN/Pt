package com.example.veeotech.postaltracking.warehouse.bean;

import java.util.List;

/**
 * Created by VeeoTech on 2018/4/24.
 */

public class TypeBean {
    /**
     * flag : 1
     * data : [{"type":"A"},{"type":"B"},{"type":"C"},{"type":"D"},{"type":"E"}]
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
         * type : A
         */

        private String type;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }
}
