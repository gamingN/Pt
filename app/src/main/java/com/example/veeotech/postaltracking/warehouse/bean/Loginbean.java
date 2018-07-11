package com.example.veeotech.postaltracking.warehouse.bean;

import java.util.List;

/**
 * Created by VeeoTech on 17/4/2018.
 */

public class Loginbean {


    /**
     * flag : 1
     * data : [{"staff_id":"1","pw":"123456","uid":"kiki","role":"pickup"}]
     */

    private int flag;
    /**
     * staff_id : 1
     * pw : 123456
     * uid : kiki
     * role : pickup
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
        private String staff_id;
        private String pw;
        private String uid;
        private String role;

        public void setStaff_id(String staff_id) {
            this.staff_id = staff_id;
        }

        public void setPw(String pw) {
            this.pw = pw;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getStaff_id() {
            return staff_id;
        }

        public String getPw() {
            return pw;
        }

        public String getUid() {
            return uid;
        }

        public String getRole() {
            return role;
        }
    }
}
