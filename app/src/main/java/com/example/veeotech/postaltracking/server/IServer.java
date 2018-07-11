package com.example.veeotech.postaltracking.server;

import com.example.veeotech.postaltracking.pickup.bean.ContentNumBean;
import com.example.veeotech.postaltracking.pickup.bean.CustomerBean;
import com.example.veeotech.postaltracking.pickup.bean.PackageNumBean;
import com.example.veeotech.postaltracking.pickup.bean.UnitBean;
import com.example.veeotech.postaltracking.pickup.bean.WeightBean;
import com.example.veeotech.postaltracking.warehouse.bean.CustomerIdBean;
import com.example.veeotech.postaltracking.warehouse.bean.FlagBean;
import com.example.veeotech.postaltracking.warehouse.bean.ListslugBean;
import com.example.veeotech.postaltracking.warehouse.bean.Loginbean;
import com.example.veeotech.postaltracking.pickup.bean.OrderCreateBean;
import com.example.veeotech.postaltracking.pickup.bean.PackageInfoBean;
import com.example.veeotech.postaltracking.warehouse.bean.OrderBean;
import com.example.veeotech.postaltracking.warehouse.bean.SmallPackgeBean;
import com.example.veeotech.postaltracking.warehouse.bean.TypeBean;
import com.example.veeotech.postaltracking.warehouse.bean.TypeInfoBean;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by VeeoTech on 17/4/2018.
 */

public interface IServer {
    @GET("login.php")
    Call<Loginbean> getloginInfo(@Query("uid") String uid, @Query("pw") String pw);

    @GET("orderCreate.php")
    Call<OrderCreateBean> getOrderInfo(@Query("customer_id") String customer_id,@Query("package_qty") String package_id,@Query("total_qty") String total_qty);

    @GET("orderCreate.php")
    Call<OrderCreateBean> getOrderInfoOne(@Query("customer_id") String customer_id);

    @GET("packageCreate.php")
    Call<PackageInfoBean> getPackageInfo(@Query("order_id") String order_id);

    @GET("feeBind.php")
    Call<CustomerIdBean> getOrderIdInfo(@Query("package_id") String package_id);

    @POST("handleFee.php")
    Call<FlagBean> sendCargoInfo(@Query("customer_id") String customer_id, @Query("order_id")String order_id,@Query("fee_map")String json);

    @GET("handleCountryCode.php")
    Call<SmallPackgeBean> getBigPackgeInfo(@Query("country_code") String country_code,@Query("slug_id") String slug_id);

    @GET("handleGoodsCode.php")
    Call<SmallPackgeBean> getSmallPackgeInfo(@Query("country_code") String country_code,@Query("goods_code") String goods_code,@Query("handle_staff") String handle_staff);

    @GET("deleteCountry.php")
    Call<FlagBean> deleteSmallPackge(@Query("goods_code") String goods_code);

    @GET("orderProvider.php")
    Call<OrderCreateBean> getOrderProvider(@Query("customer_id") String customer_id,@Query("aid") String aid);

    @GET("feeZone.php")
    Call<TypeBean> getType();

    @GET("alterOrderInfo.php")
    Call<OrderCreateBean> alterOrderInfo(@Query("order_id") String order_id,@Query("package_qty") String package_qty,@Query("total_qty") String total_qty);

    @GET("packageScanner.php")
    Call<OrderCreateBean> bindPackageNum(@Query("order_id") String order_id,@Query("package_id") String package_id);

    @GET("packageNumRequire.php")
    Call<PackageNumBean> requirePackageNum(@Query("order_id") String order_id);

    @GET("getUnit.php")
    Call<UnitBean> getUnit();

    @GET("feeBind.php")
    Call<CustomerIdBean> getCustomerId(@Query("package_id") String package_id);

    @GET("packageAlter.php")
    Call<OrderCreateBean> alterPackage(@Query("order_id") String order_id,@Query("package_id") String package_id,@Query("qty") String qty,@Query("unit") String unit,@Query("weight") String weight);

    @GET("packageAllRequire.php")
    Call<String> getPackageAll(@Query("order_id") String order_id);

    @GET("packageDelete.php")
    Call<String> deletePackage(@Query("package_id") String package_id,@Query("order_id") String order_id);

    @GET("packageWeigh.php")
    Call<WeightBean> getWeight(@Query("order_id") String order_id,@Query("package_id") String package_id);

    @GET("orderComfirm.php")
    Call<String> orderComfirm(@Query("order_id") String order_id);

    @GET("checkOrder.php")
    Call<String> checkOrder(@Query("aid") String aid);

    @GET("customerRequire.php")
    Call<CustomerBean> customerRequire(@Query("order_id") String order_id);

    @GET("requireContentNum.php")
    Call<ContentNumBean> requireContentNum(@Query("order_id") String order_id);

    @GET("typeInfo.php")
    Call<TypeInfoBean> getTypeWeight(@Query("order_id") String order_id, @Query("type_map")String json);

    @GET("getOrderId.php")
    Call<OrderBean> getOrderId(@Query("package_id") String package_id);

    @GET("deliverFrom.php")
    Call<FlagBean> updateDeliverInfo(@Query("country_code") String country_code);

    @GET("goodsBoarding.php")
    Call<FlagBean> updateBoardingInfo(@Query("country_code") String country_code);

    @GET("listSlug.php")
    Call<ListslugBean> getSlug();

}
