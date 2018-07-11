package com.example.veeotech.postaltracking.pickup.bean;

import java.util.List;

/**
 * Created by VeeoTech on 27/4/2018.
 */

public class UnitBean {

    /**
     * flag : 1
     * data : [{"name":"個"},{"name":"件"},{"name":"包"},{"name":"箱"}]
     */

    private int flag;
    /**
     * name : 個
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
        private String name;

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
