package com.example.veeotech.postaltracking.warehouse.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.lang.reflect.Type;

/**
 * Created by VeeoTech on 2018/4/21.
 */

public class CargoBean implements Serializable {
    private String type;
    private String weight;


    public void setType(String type){
        this.type = type;
    }

    public String getType(){
        return type;
    }

    public String getWeight(){
        return weight;
    }

    public void setWeight(String weight){
        this.weight = weight;
    }


}
