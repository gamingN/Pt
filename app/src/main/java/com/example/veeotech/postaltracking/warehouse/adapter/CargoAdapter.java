package com.example.veeotech.postaltracking.warehouse.adapter;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.veeotech.postaltracking.R;
import com.example.veeotech.postaltracking.warehouse.bean.CargoBean;
import com.example.veeotech.postaltracking.warehouse.bean.TypeInfoBean;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by VeeoTech on 2018/4/21.
 */

public class CargoAdapter extends BaseAdapter  {
    private int selectedEditTextPosition = -1;
    private List<TypeInfoBean.CargoBean> cargoBeanList ;
    private Context context;
    private LayoutInflater inflater ;
    private int currentSelect = -1;
    private boolean isEditable = false;
    private int i=0;
    public CargoAdapter(Context context,List<TypeInfoBean.CargoBean> List){
            cargoBeanList = new ArrayList<>(List);
        this.context = context;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }
    @Override
    public int getCount() {
        return cargoBeanList.size();
    }

    @Override
    public Object getItem(int i) {
        return cargoBeanList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        final CargoViewHolder viewHolder;

//        if(view == null){
            view = inflater.inflate(R.layout.item_cargo,viewGroup,false);
            viewHolder = new CargoViewHolder();
            viewHolder.tv_warehouse_cargo_country = (TextView)view.findViewById(R.id.tv_cargo_id);
            viewHolder.et_warehouse_cargo_count = (EditText) view.findViewById(R.id.et_cargo_count);
            viewHolder.imageButton_add = (ImageButton)view.findViewById(R.id.ib_warehouse_cargo_add);
            viewHolder.imageButton_reduce = (ImageButton)view.findViewById(R.id.ib_warehouse_cargo_reduce);
            viewHolder.tv_warehouse_cargo_country.setText(cargoBeanList.get(i).getType());
            viewHolder.et_warehouse_cargo_count.setText(cargoBeanList.get(i).getWeight());
           // viewHolder.et_warehouse_cargo_count.addTextChangedListener(new TextSwitcher(this));
            Log.d("zwx","cargoBeanList"+"國家"+cargoBeanList.get(i).getType()+"重量"+cargoBeanList.get(i).getWeight());
            viewHolder.et_warehouse_cargo_count.setTag(i);
            view.setTag(viewHolder);
//        }else{
//            viewHolder = (CargoViewHolder) view.getTag();
//        }
        if(!isEditable){
            viewHolder.imageButton_add.setEnabled(false);
            viewHolder.imageButton_reduce.setEnabled(false);
            viewHolder.et_warehouse_cargo_count.setEnabled(false);
            viewHolder.imageButton_reduce.setImageResource(R.mipmap.ib_warehouse_item_reduce_disable);
            viewHolder.imageButton_add.setImageResource(R.mipmap.ib_warehouse_item_add_disable);
            viewHolder.et_warehouse_cargo_count.setTextColor(0xffB3B3B3);
        }else if(isEditable){
            viewHolder.imageButton_add.setEnabled(true);
            viewHolder.imageButton_reduce.setEnabled(true);
            viewHolder.et_warehouse_cargo_count.setEnabled(true);
            viewHolder.imageButton_reduce.setImageResource(R.mipmap.ib_warehouse_item_reduce_editable);
            viewHolder.imageButton_add.setImageResource(R.mipmap.ib_warehouse_item_add_editable);
            viewHolder.et_warehouse_cargo_count.setTextColor(0xff000000);
        }
        viewHolder.et_warehouse_cargo_count.clearFocus();
        if(currentSelect == i) {
            viewHolder.et_warehouse_cargo_count.requestFocus();
            viewHolder.et_warehouse_cargo_count.setSelection(viewHolder.et_warehouse_cargo_count.getText().length());
            viewHolder.tv_warehouse_cargo_country.setText(cargoBeanList.get(i).getType());
            viewHolder.et_warehouse_cargo_count.setText(cargoBeanList.get(i).getWeight());
            currentSelect = -1;
        }
        if(viewHolder.et_warehouse_cargo_count.getTag() instanceof TextWatcher){
            viewHolder.et_warehouse_cargo_count.removeTextChangedListener((TextWatcher)(viewHolder.et_warehouse_cargo_count.getTag()));
        }
       //防止oom viewHolder.et_warehouse_cargo_count.addTextChangedListener(null);
        viewHolder.imageButton_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(viewHolder.et_warehouse_cargo_count.getText().toString())) {
                    double result = Double.valueOf(viewHolder.et_warehouse_cargo_count.getText().toString()) + 1;
                    Log.d("jia", "" + result);
                    cargoBeanList.get(i).setWeight(String.valueOf(result));
                    String resultStr = String.valueOf(result);
                    viewHolder.et_warehouse_cargo_count.setText(resultStr);
                }
                viewHolder.et_warehouse_cargo_count.requestFocus();
                viewHolder.et_warehouse_cargo_count.setSelection(viewHolder.et_warehouse_cargo_count.getText().toString().length());
            }
        });
        viewHolder.imageButton_reduce.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(viewHolder.et_warehouse_cargo_count.getText().toString())) {
                   // double result = Double.valueOf(viewHolder.et_warehouse_cargo_count.getText().toString()) - 1;
                    BigDecimal num1 = new BigDecimal(viewHolder.et_warehouse_cargo_count.getText().toString());
                    BigDecimal num2 = new BigDecimal(1);
                    double result = num1.subtract(num2).doubleValue();
                    if (result >= 0) {
                        cargoBeanList.get(i).setWeight(String.valueOf(result));
                        String resultStr = String.valueOf(result);
                        viewHolder.et_warehouse_cargo_count.setText(resultStr);
                    }else if(result<0){
                        result = 0;
                        cargoBeanList.get(i).setWeight(String.valueOf(result));
                        String resultStr = String.valueOf(result);
                        viewHolder.et_warehouse_cargo_count.setText(resultStr);
                    }
                }
                viewHolder.et_warehouse_cargo_count.requestFocus();
                viewHolder.et_warehouse_cargo_count.setSelection(viewHolder.et_warehouse_cargo_count.getText().toString().length());
            }
        });
        viewHolder.et_warehouse_cargo_count.addTextChangedListener(new TextSwitcher(viewHolder));
        viewHolder.et_warehouse_cargo_count.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                    currentSelect = (int) view.getTag();
                  /*  if(viewHolder.et_warehouse_cargo_count.getText().toString().equals("0")) {
                        viewHolder.et_warehouse_cargo_count.setText("");
                    }*/
                }
                return false;
            }
        });
        return view;
    }



    private class CargoViewHolder{
        TextView tv_warehouse_cargo_country;
        EditText et_warehouse_cargo_count;
        ImageButton imageButton_add,imageButton_reduce;
    }



    class TextSwitcher implements TextWatcher{
        private CargoViewHolder cargoViewHolder;

        public TextSwitcher (CargoViewHolder cargoViewHolder){
            this.cargoViewHolder = cargoViewHolder;
        }
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            int position = (int) cargoViewHolder.et_warehouse_cargo_count.getTag();//取tag值
            cargoBeanList.get(position).setWeight(String.valueOf(charSequence));
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    }

    public TypeInfoBean.CargoBean getList(int i){
        return cargoBeanList.get(i);
    }

    public boolean IsEmpty(){
        if(cargoBeanList.size()==0){
            return true;
        }else
            return false;
    }

    public void setEditable(boolean isEditable){
        this.isEditable = isEditable;
        notifyDataSetChanged();
    }
}
