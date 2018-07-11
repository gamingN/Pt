package com.example.veeotech.postaltracking.warehouse.bean;

import java.util.List;

/**
 * Created by VeeoTech on 11/5/2018.
 */

public class ListslugBean {

    /**
     * flag : 1
     * data : [{"slug_id":"Test_SLUG_id#$@","name":"Test#$@"}]
     */

    private int flag;
    /**
     * slug_id : Test_SLUG_id#$@
     * name : Test#$@
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
        private String slug_id;
        private String name;

        public void setSlug_id(String slug_id) {
            this.slug_id = slug_id;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSlug_id() {
            return slug_id;
        }

        public String getName() {
            return name;
        }
    }
}
