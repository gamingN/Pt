package com.example.veeotech.postaltracking.pickup.bean;

import java.util.List;

/**
 * Created by VeeoTech on 28/4/2018.
 */

public class ContentNumBean {

    /**
     * flag : 1
     * data : [{"total_package":"5","total_content":"36"}]
     */

    private int flag;
    /**
     * total_package : 5
     * total_content : 36
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
        private String total_package;
        private String total_content;

        public void setTotal_package(String total_package) {
            this.total_package = total_package;
        }

        public void setTotal_content(String total_content) {
            this.total_content = total_content;
        }

        public String getTotal_package() {
            return total_package;
        }

        public String getTotal_content() {
            return total_content;
        }
    }
}
