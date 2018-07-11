package com.example.veeotech.postaltracking.warehouse.bean;

import java.util.List;

/**
 * Created by VeeoTech on 2018/4/20.
 */

public class SmallPackgeBean {
    /**
     * flag : 1
     * data : [{"small_id":"1345"},{"small_id":"1235"},{"small_id":"13"},{"small_id":"17"}]
     */

    private int flag;
    private List<SmallIdBean> data;

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public List<SmallIdBean> getData() {
        return data;
    }

    public void setData(List<SmallIdBean> data) {
        this.data = data;
    }

    public static class SmallIdBean {
        /**
         * goods_code : 4
         */

        private String goods_code;

        public String getGoods_code() {
            return goods_code;
        }

        public void setGoods_code(String goods_code) {
            this.goods_code = goods_code;
        }
    }
}
