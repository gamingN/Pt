package com.example.veeotech.postaltracking.pickup.bean;

/**
 * Created by VeeoTech on 27/4/2018.
 */

public class PackageListBean {


    /**
     * package_id : 123
     * qty : 5
     * unit : 件
     * weight : 10.00
     */

    private String package_id;
    private String qty;
    private String unit;
    private String weight;

    public void setPackage_id(String package_id) {
        this.package_id = package_id;
    }

    public void setQty(String qty) {
        this.qty = qty;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getPackage_id() {
        return package_id;
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
