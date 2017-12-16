package com.example.r.coolweather.until;

import android.text.TextUtils;

import com.example.r.coolweather.db.City;
import com.example.r.coolweather.db.County;
import com.example.r.coolweather.db.Province;
import com.example.r.coolweather.gson.Weather;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2017/12/14 0014.
 */
//服务器返回的数据都是json格式的，所以注册这个工具类JSONObject来解析和处理这种数据
public class Utility {
    //解析和处理服务器返回的省级数据
    public static boolean handleProvinceResponse(String response){
        if (!TextUtils.isEmpty(response)){
            try {
                JSONArray allProvinces = new JSONArray(response);
                for (int i = 0; i < allProvinces.length(); i++) {
                    JSONObject provincesObject = allProvinces.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceName(provincesObject.getString("name"));
                    province.setProvincrCode(provincesObject.getInt("id"));
                    province.save();
                }
                return true;
            }catch (JSONException e)  {
                e.printStackTrace();
            }

        }
        return false;

    }

    //市级数据
    public static boolean handleCityResponse(String response,int provinceId){

        if (!TextUtils.isEmpty(response)){
            try {
                JSONArray allCities = new JSONArray(response);
                for (int i = 0; i < allCities.length(); i++) {
                    JSONObject cityObject = allCities.getJSONObject(i);
                    City city = new City();
                    city.setCityName(cityObject.getString("name"));
                    city.setCityCode(cityObject.getInt("id"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;//?


            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        return false;//?
    }

    //县级数据
    public static boolean handleCountyResponse(String response,int cityId){
        if (!TextUtils.isEmpty(response)){
            try {
                JSONArray allCounties = new JSONArray(response);
                for (int i = 0; i < allCounties.length(); i++) {
                    JSONObject CountiyObject = allCounties.getJSONObject(i);
                    County county = new County();
                    county.setCountyName(CountiyObject.getString("name"));
                    county.setWeatherId(CountiyObject.getString("weather_id"));
                    county.setCityId(cityId);
                    county.save();
                }
                return true;


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    /**
     * 将返回的JSON数据解析成Weather实体类
     */
    public static Weather handleWeatherResponse(String response){
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather");
            String weatherContent = jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(weatherContent,Weather.class);


        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }
}

