package com.example.veeotech.postaltracking.pickup.bean;

import java.util.List;

/**
 * Created by VeeoTech on 19/4/2018.
 */

public class PackageInfoBean {

    /**
     * flag : 1
     * data : [{"package_id":"04173973649"}]
     */

    private int flag;
    /**
     * package_id : 04173973649
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
        private String package_id;

        public void setPackage_id(String package_id) {
            this.package_id = package_id;
        }

        public String getPackage_id() {
            return package_id;
        }
    }
}
