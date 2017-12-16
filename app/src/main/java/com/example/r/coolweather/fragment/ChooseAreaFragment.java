package com.example.r.coolweather.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.r.coolweather.MainActivity;
import com.example.r.coolweather.R;
import com.example.r.coolweather.WeatherActivity;
import com.example.r.coolweather.db.City;
import com.example.r.coolweather.db.County;
import com.example.r.coolweather.db.Province;
import com.example.r.coolweather.until.HttpUtil;
import com.example.r.coolweather.until.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/12/14 0014.
 */

public class ChooseAreaFragment extends Fragment {
    public static final int LEVEL_PROVINCE=0;
    public static final int LEVEL_CITY=1;
    public static final int LEVEL_COUNTY=2;
    private ProgressDialog progressDialog;
    private TextView titleText;
    private Button backButton;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();

    //省列表
    private List<Province> provinceList;

    //市列表
    private List<City> cityList;

    //县列表
    private List<County> countyList;

    private Province selectedProvince;

    private City selectCity;

    private int currentLevel;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area, container, false);
        titleText =  view.findViewById(R.id.title_text);
        backButton=view.findViewById(R.id.back_button);
        listView= view.findViewById(R.id.list_view);
        adapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,dataList);//适配器加载数据
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel==LEVEL_PROVINCE){//列表中项目的点击事件监听，注册之后到这里写逻辑，先判断当前是哪一级的列表
                    selectedProvince=provinceList.get(position);
                    queryCities();
                }else if (currentLevel==LEVEL_CITY){
                    selectCity=cityList.get(position);
                    queryCounties();
                }else if (currentLevel==LEVEL_COUNTY){
                    String weatherId = countyList.get(position).getWeatherId();
                    if (getActivity()instanceof MainActivity){//判断一个对象是否属于MainActivity的实例，如果是，就跳转到天气页面
                        Intent intent = new Intent(getActivity(), WeatherActivity.class);

                        intent.putExtra("weather_id",weatherId);
                        startActivity(intent);
                        getActivity().finish();// getActivity()是继承的方法
                    }else if (getActivity()instanceof WeatherActivity){//如果本身就在天气页面
                       WeatherActivity activity = (WeatherActivity) getActivity();
                       activity.drawerLayout.closeDrawers();
                       activity.swipeRefresh.setRefreshing(true);
                       activity.requestWeather(weatherId);
                    }

                }
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLevel==LEVEL_COUNTY){
                    queryCities();
                }else if (currentLevel==LEVEL_CITY){
                    queryProvinces();
                }

            }
        });
        queryProvinces();//不论如何，在这里，数据库里一定有省级数据了
    }

    /**
     *查询全国所有的省，优先从数据库查询，如果没有查询到再去服务器上查询
     */
    private void queryProvinces() {
        titleText.setText("中国");
        backButton.setVisibility(View.GONE);
        provinceList= DataSupport.findAll(Province.class);//DataSupport是LitePal自动创建的数据库
        if (provinceList.size()>0){
            dataList.clear();
            for (Province province:provinceList) {
                dataList.add(province.getProvinceName());

            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_PROVINCE;
        }else {//数据库中没有就去服务器获取
            String address = "http:/goulin.tech/api/china";
            queryFromServer(address,"province");
        }


    }



    private void queryCities() {
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList=DataSupport.where("provinceid=?",String.valueOf(selectedProvince.getId())).find(City.class);
        if (cityList.size()>0){//数据库中的大于零不为空
            dataList.clear();
            for (City city:cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_CITY;
        }else {
            int provinceCode= selectedProvince.getProvinceCode();
            String address = "http:/goulin.tech/api/china"+provinceCode;
            queryFromServer(address,"city");

        }


    }

    private void queryCounties() {
        titleText.setText(selectCity.getCityName());//这个selectCity对象就是监听事件获得的对象
        backButton.setVisibility(View.VISIBLE);
        countyList = DataSupport.where("cityid = ?",String.valueOf(selectCity.getId())).find(County.class);
        if (countyList.size()>0){
            dataList.clear();//不是匿名的，是全局声明的变量类型，给遍历数据暂用储存而已
            for (County county: countyList) {
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        }else {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectCity.getCityCode();
            String adress = "http:/goulin.tech/api/china" + provinceCode + "/" + cityCode;//else就组装服务器地址向服务器查询
            queryFromServer(adress, "city");
        }
    }

    /**
     * 根据传入的地址和类型从服务器上查询省市县的数据
     * @param address
     * @param type
     */
    private void queryFromServer(String address, final String type) {
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //通过runonuithread（）方法回到主线程处理逻辑
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;
                if ("province".equals(type)){
                    result= Utility.handleProvinceResponse(responseText);
                }else if ("city".equals(type)){
                    result =Utility.handleCityResponse(responseText,selectedProvince.getId());
                }else if ("county".equals(type)){
                    result =Utility.handleCountyResponse(responseText,selectCity.getId());
                }
                if (result){//接收上面判断过的result 处理返回的响应？
                    getActivity().runOnUiThread(new Runnable() {//主线程，直接显示到页面上
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)){
                                queryProvinces();
                            }else if ("city".equals(type)){
                                queryCities();
                            }else if ("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }
            }
        });

    }

    private void closeProgressDialog() {
        if (progressDialog != null){
            progressDialog.dismiss();
        }

    }
        //显示进度对话框
    private void showProgressDialog() {
        if (progressDialog==null){
            progressDialog=new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载。。。");
            progressDialog.setCancelable(false);
        }
        progressDialog.show();

    }
}
