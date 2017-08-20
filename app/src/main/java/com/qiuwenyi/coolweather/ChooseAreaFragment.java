package com.qiuwenyi.coolweather;

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

import com.qiuwenyi.coolweather.db.City;
import com.qiuwenyi.coolweather.db.County;
import com.qiuwenyi.coolweather.db.Province;
import com.qiuwenyi.coolweather.util.HttpUtil;
import com.qiuwenyi.coolweather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by QiuWenYi on 2017/8/14.
 * 这样我们就把遍历全国省市县的功能完成了，可是碎片不能直接显示在界面上，因此我们还需要把他添加到活动里才行，
 */

public class ChooseAreaFragment extends Fragment {
    private static final String TAG = "ChooseAreaFragment";
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;
    private ProgressDialog progressDialog;
    private TextView titleText;
    private Button backButton;
    private ListView listView;
    private ArrayAdapter<String> adapter;

    private List<String> dataList = new ArrayList<>();

    /**
     * 省列表
     */
    private List<Province> provinceList;

    /**
     * 市列表
     */
    private List<City> cityList;

    /**
     * 县列表
     */
    private List<County> countyList;

    /**
     * 选中的省份
     */
    private Province selectedProvince;

    /**
     * 选中的城市
     */
    private City selectedCity;

    /**
     * 当前选中的级别
     */
    private int currentLevel;

    /**
     * 在onCreateView（）方法中先是获取到了一些控件的实例，然后去初始化ArrayAdapter，并将它设置为ListView的适配器。
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area, container, false);
        titleText = (TextView) view.findViewById(R.id.title_text);
        backButton = (Button) view.findViewById(R.id.back_button);
        listView = (ListView) view.findViewById(R.id.list_view);
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);
        return view;
    }

    /**
     * 在onActivityCreated（）方法中给ListView和backButton设置了点击事件，到这里我们的初始化工作就算是完成了。
     * 最后调用queryProvinces（）方法，加载省级数据
     * 另外还有一点需要注意，再返回按钮的点击事件里会对当前ListView的列表级别进行判断
     * 如果当前是县级列表，那么就返回到市级列表，如果当前是市级列表，那么就返回省级列表，
     * 当返回到升级列表时，返回按钮会自动隐藏，从而也就不需要在作进一步的处理了。
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVINCE) {
                    selectedProvince = provinceList.get(position);
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    selectedCity = cityList.get(position);
                    queryCounties();//加载县级数据
                    /**
                     * 丛省市县列表界面跳转到天气界面，这里在哦那Item Click（）方法中加入了一个if判断
                     * 如果当前级别时LEVEL_COUNTRY,就启动WeatherActivity，并把当前选中县的天气id传递过去。
                     */
                } else if (currentLevel == LEVEL_COUNTY) {
                    String weatherId = countyList.get(position).getWeatherId();
                    /**
                     * 这里我们使用一个Java中的小技巧，instanceof关键字可以用来判断一个对象是否属于某个类的实例，
                     * 我们在碎片中调用getActivity（）方法，然后配合instanceof关键字，就能轻松判断出该碎片是在
                     * MainActivity当中，还是在WeatherActivity当中，如果是在MainActivity当中，那么处理逻辑不变，
                     * 如果是在WeatehrActivity当中，那么就关闭滑动菜单，显示下拉刷新进度条，然后请求新城市的天气信息。
                     */
                    if (getActivity() instanceof MainActivity) {

                    Intent intent = new Intent(getActivity(), WeatherActivity.class);
                    intent.putExtra("weather_id", weatherId);
                    startActivity(intent);
                    getActivity().finish();

                    } else if (getActivity() instanceof WeatherActivity) {
                        WeatherActivity activity = (WeatherActivity) getActivity();
                        activity.drawerLayout.closeDrawers();
                        activity.swipeRefreshLayout.setRefreshing(true);
                        activity.requestWeather(weatherId);
                    }
                }
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentLevel == LEVEL_COUNTY) {//如果当前是县级列表，那么就返回到市级列表，
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {//如果当前是市级列表，那么就返回省级列表，
                    queryProvinces();//当返回到升级列表时，返回按钮会自动隐藏，从而也就不需要在作进一步的处理了。
                }
            }
        });
        queryProvinces();
    }

    /**
     * 查询全国所有的省，优先从数据库查询，如果没有查询到再去服务器上查询
     * 当点击了某个省的时候会进入到ListView的onItemClick（）方法中，
     * 这个时候就会根据当前的级别来判断是去调用queryCities（）方法还是queryCounties（）方法
     * queryCities（）方法是去查询市级数据，erqueryConuties（）方法是去查询县级数据，，这两个方法内部的流程和
     * queryProvinces（）方法基本相同，
     *
     */
    private void queryProvinces() {
        titleText.setText("中国");//首先将头布局的标题设置为中国
        backButton.setVisibility(View.GONE);//将返回按钮隐藏起来，因为省级列表已经不能再返回了，
        provinceList = DataSupport.findAll(Province.class);//然后在调用LitePal的查询接口来从数据库中读取省级数据，
        if (provinceList.size() > 0) {//如果读取到了就直接将数据显示到界面上，
            dataList.clear();
            for (Province province : provinceList) {
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        } else {//如果没有读取到数据就按照接口组装出一个请求地址
            String address = "http://guolin.tech/api/china";
            queryFromServer(address, "province");//然后调用queryFromServer（）方法从服务器上查询数据
        }
    }


    /**
     * 查询选中省内所有的市，优先从数据库查询，如果没有查询到再去服务器上查询
     */
    private void queryCities() {
        titleText.setText(selectedProvince.getProvinceName());//根据选中的省市命名
        backButton.setVisibility(View.VISIBLE);
        cityList = DataSupport.where("provinceid=?", String.valueOf(selectedProvince.getId())).find(City.class);
        if (cityList.size() > 0) {
            dataList.clear();
            for (City city : cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);//默认选中第一条数据
            currentLevel = LEVEL_CITY;
        } else {
            int provinceCode = selectedProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/" + provinceCode;
            queryFromServer(address, "city");
        }
    }

    /**
     * 查询选中市内所有的县，优先从数据库查询，如果没有查询到再去服务器上查询
     *
     */
    private void queryCounties() {
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList = DataSupport.where("cityid=?", String.valueOf(selectedCity.getId())).find(County.class);
        if (countyList.size() > 0) {
            dataList.clear();
            for (County county : countyList) {
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        } else {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/" + provinceCode + "/" + cityCode;
            queryFromServer(address, "county");
        }
    }

    /**
     * 根据传入的地址和类型从服务器上查询省市县数据
     */
    private void queryFromServer(String address, final String type) {
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {//调用HttpUtil的sendOkHttpRequest（）方法来向服务器发送请求
            @Override
            public void onFailure(Call call, IOException e) {
                //通过runOnUiThread（）方法回到主线程处理逻辑
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {//响应的数据会回调到onResponse（）方法中
                String responseText = response.body().string();
                boolean result = false;
                if ("province".equals(type)) {
                    //然后在这里去调用Utilty的handleProvincesResponse（）方法来解析和处理服务器返回的数据，并存储到数据库中
                    result = Utility.handleProvinceResponse(responseText);
                } else if ("city".equals(type)) {
                    result = Utility.handleCityResponse(responseText, selectedProvince.getId());
                } else if ("county".equals(type)) {
                    result = Utility.handleCountyResponse(responseText, selectedCity.getId());
                }
                if (result) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)) {
                                //接下来的一步很关键，在解析和处理完数据之后，我们再次调用了queryProvinces（）方法来重新加载省级数据，
                                // 由于queryProvinces（）方法牵扯到了UI操作，因此必须要在主线程中调用，，这里借助了runOnUiThread（）方法来实现从子线程切换到主线程。
                                //现在数据库中已经存在了数据，因此调用queryProvinces（）方法就直接将数据显示到界面上。
                                queryProvinces();
                            } else if ("city".equals(type)) {
                                queryCities();
                            } else if ("county".equals(type)) {
                                queryCounties();
                            }
                        }
                    });
                }
            }
        });
    }


    /**
     * 显示进度条对话框
     */
    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    /**
     * 关闭进度条对话框
     */
    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

}
