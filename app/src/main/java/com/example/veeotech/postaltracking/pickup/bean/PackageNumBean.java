package com.example.veeotech.postaltracking.pickup.bean;

import java.util.List;

/**
 * Created by VeeoTech on 25/4/2018.
 */

public class PackageNumBean {

    /**
     * flag : 1
     * data : [{"package_qty":"2","total_qty":"15"}]
     */

    private int flag;
    /**
     * package_qty : 2
     * total_qty : 15
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
        private String package_qty;
        private String total_qty;

        public void setPackage_qty(String package_qty) {
            this.package_qty = package_qty;
        }

        public void setTotal_qty(String total_qty) {
            this.total_qty = total_qty;
        }

        public String getPackage_qty() {
            return package_qty;
        }

        public String getTotal_qty() {
            return total_qty;
        }
    }
}
