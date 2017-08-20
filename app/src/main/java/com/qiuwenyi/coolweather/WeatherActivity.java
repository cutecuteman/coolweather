package com.qiuwenyi.coolweather;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.qiuwenyi.coolweather.gson.Forecast;
import com.qiuwenyi.coolweather.gson.Weather;
import com.qiuwenyi.coolweather.util.HttpUtil;
import com.qiuwenyi.coolweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    //引入布局 activity_weather.xml
    private ScrollView weatherLayout;
    //头布局 title.xml
    private TextView titleCity;//显示城市名
    private TextView titleUpdateTime;//显示更新时间
    //当前天气信息布局 now.xml
    private TextView degreeText;//当前气温
    private TextView weatherInfoText;//当前天气概况
    //显示未来天气信息的布局 forecast.xml
    private LinearLayout forecastLayout;
    //空气质量布局 aqi.xml
    private TextView aqiText;//aqi指数
    private TextView pm25Text;//PM2.5指数
    //生活建议布局 suggestion.xml
    private TextView comfortText;//舒适度
    private TextView carWashText;//洗车指数
    private TextView sportText;//运动指数

    private ImageView bingPicImag;//背景图片

    public SwipeRefreshLayout swipeRefreshLayout;//下拉刷新
    private String mWeatherId;//定义一个mWeaherId变量，用于记录城市的天气id，

    public DrawerLayout drawerLayout;
    private Button navButton;

    /**
     * 在onCreate（）方法中任然是先去获取一些控件的实例，然后会尝试从本地缓存中读取天气数据，那么第一次肯定没有缓存的，
     * 因此就会从Intent中 取出天气id，并调用requestWeather（）方法从服务器请求天气数据，注意，请求数据的时候将ScrollView
     * 进行隐藏，不然空数据的界面看上去会很奇怪。
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /**
         * 由于这个功能是Android5.0及以上的系统才支持的，因此我们先在代码中做一个系统版本号的判断，只有当版本号大于或等于21，
         * 也就是5.0及以上系统时才会执行后面的代码。
         * 接着我们调用getWindow（）.getDecorVIew()方法拿到当前活动的DecorView再调用它的setSystemUiVisibility（）方法
         * 来改变系统UI的显示，这里传入View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN和View.SYSTEM_UI_FLAG_LAYOUT_STABLE就表示
         * 活动的布局会显示再状态栏上面，最后调用一下setStatusBarColor（）方法将状态栏设置成透明色。
         * 仅仅这些代码就让背景图片和状态栏融合再一起了。
         */
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();//调用getWindow（）.getDecorVIew()方法拿到当前活动的DecorView
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);//表示活动的布局会显示再状态栏上面
            getWindow().setStatusBarColor(Color.TRANSPARENT);//最后调用一下setStatusBarColor（）方法将状态栏设置成透明色。
        }

        setContentView(R.layout.activity_weather);
        //初始化控件
        bingPicImag = (ImageView) findViewById(R.id.bing_pic_img);//背景

        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        titleCity = (TextView) findViewById(R.id.title_city);
        titleUpdateTime = (TextView) findViewById(R.id.title_update_time);
        degreeText = (TextView) findViewById(R.id.degree_text);
        weatherInfoText = (TextView) findViewById(R.id.weather_info_text);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        aqiText = (TextView) findViewById(R.id.aqi_text);
        pm25Text = (TextView) findViewById(R.id.pm25_text);
        comfortText = (TextView) findViewById(R.id.comfort_text);
        carWashText = (TextView) findViewById(R.id.car_wash_text);
        sportText = (TextView) findViewById(R.id.sport_text);
        /**
         * 获取SwipeRefreshLayout的实例，然后调用setColorSchemeResources（）方法来设置下拉刷新进度条的颜色。
         * 这里使用了主题中的colorPrimary作为进度条的颜色了，接着定义一个mWeaherId变量，用于记录城市的天气id，
         * 然后调用setOnRefreshListener（）方法来设置一个下拉刷新的监听器，当触发下拉刷新操作的时候，就会回调这个监听器的
         * onRefresh方法，我们在这里去调用requestWeather（）方法的请求天气信息就可以了。
         */

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);
        if (weatherString != null) {
            //有缓存时直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);

            mWeatherId = weather.basic.weatherId;//定义一个mWeaherId变量，用于记录城市的天气id，

            showWeatherInfo(weather);
        } else {
            //无缓存时去服务器查询天气
//            String weatherId = getIntent().getStringExtra("weather_id");//从Intent中 取出天气id
            mWeatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);//注意，请求数据的时候将ScrollView进行隐藏，不然空数据的界面看上去会很奇怪。
//            requestWeather(weatherId);//并调用requestWeather（）方法从服务器请求天气数据
            requestWeather(mWeatherId);
        }
        /**
         * 然后调用setOnRefreshListener（）方法来设置一个下拉刷新的监听器，当触发下拉刷新操作的时候，就会回调这个监听器的
         * onRefresh方法，我们在这里去调用requestWeather（）方法的请求天气信息就可以了。
         */
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId);
            }
        });
        /**
         * 加载必应每日一图
         * 首先在onCreate（）方法中获取了新增控件ImageVIew的实例，然后尝试从SharePreferences中读取缓存的背景图片，
         * 如果缓存的话就直接用Glide来加载图片，如果没有的话就调用loadBingPic（）方法请求今日必应背景图。
         */
        String bingPic = prefs.getString("bing_pic", null);
        if (bingPic != null) {
            Glide.with(this).load(bingPic).into(bingPicImag);
        } else {
            loadBingPic();
        }
        /**
         * 首先在onCreate（）方法中获取到新增的DrawerLayout和Button的实例，然后在Button的点击事件中调用DrawerLayout的openDrawer（）
         * 的方法打开滑动菜单就可以了。
         * 不过现在还没结束，因为这仅仅是打开了滑动菜单而已，我们还需要处理城市后的逻辑才行，这个工作就必须要在ChooseAreaFragment中进行了，
         * 因为之前选中了某个城市后跳转到WeatherActivity的，而现在由于我们本来就是在WeatherActivity当中的，因此并不需要跳转，
         * 只是去请求新选择城市的天气信息就可以了。
         */
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navButton = (Button) findViewById(R.id.nav_button);
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);//调用DrawerLayout的openDrawer（）的方法打开滑动菜单就可以了。
            }
        });
    }

    /**
     * loadBingPic（）先是调用了HttpUtil。sendOkHttpRequest（）方法获取到必应背景图片的链接，然后将这个链接缓存到SharePreferences当中
     * 再将当前线程切换到主线程，最后使用Glide来加载这张图片就可以了，另外需要注意的是，在requestWeather（）方法的最后也需要在调用一下loadBingPic（）
     * 方法，这样在每次请求天气信息的时候同时也会刷新新的背景图片，
     */
    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImag);//最后使用Glide来加载这张图片就可以了
                    }
                });
            }
        });
    }

    /**
     * 根据天气id请求城市天气信息
     *
     * requetWeather（）方法中先是使用了参数中传入的天气id和我们之前申请好的APIKey拼装出一个接口地址，接着调用HttpUtil。sendOkHttpRequest（）
     * 方法来向该地址发出请求，服务器会将相应城市的天气信息以JSON格式返回，然后我们在onResponse（）回调中先调用Utility。handleWeatherResponse（）
     * 方法将返回的JSON数据转换成Weather对象，在将当前线程切换到主线程，然后进行判断，如果服务器返回的status状态时ok，就说明请求天气成功了，
     * 此时将返回的数据缓存到SharePreferences当中，并调用showWeatherInfo（）方法来进行内容显示。
     *
     * @param weatherId
     */
    public void requestWeather(String weatherId) {
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=5d69a1fbae4044119e5f9807dc0c2d6b";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {//调用HttpUtil。sendOkHttpRequest（）方法来向该地址发出请求
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        //另外不要忘记，当请求结束后，还需要调用SwipeRefreshLayout的setRefreshing（）方法并传入false，用于表示刷新事件的结束，并隐藏刷新进度条。
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText=response.body().string();//服务器会将相应城市的天气信息以JSON格式返回
                final Weather weather = Utility.handleWeatherResponse(responseText);//回调中先调用Utility.handleWeatherResponse()方法将返回的JSON数据转换成Weather对象
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {//在将当前线程切换到主线程
                        if (weather != null && "ok".equals(weather.status)) {//然后进行判断，如果服务器返回的status状态时ok，就说明请求天气成功了，
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather", responseText);
                            editor.apply();//此时将返回的数据缓存到SharePreferences当中，
                            showWeatherInfo(weather);//并调用showWeatherInfo（）方法来进行内容显示。
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                        //另外不要忘记，当请求结束后，还需要调用SwipeRefreshLayout的setRefreshing（）方法并传入false，用于表示刷新事件的结束，并隐藏刷新进度条。
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        });
        loadBingPic();//这样在每次请求天气信息的时候同时也会刷新新的背景图片
    }

    /**
     * 处理并展示Weather实体类中的数据
     *
     * 从Weather对象中获取数据，然后显示到相应的控件上，注意在未来几天天气预报的部分我们使用了一个for循环来处理每天的天气信息，
     * 再循环中动态加载forecast_item。xml布局并设置相应的数据，然后添加到父布局当中，设置完了所有数据之后，记得要将ScrollView
     * 重新变成可见。
     * 这样我们再次进入WeatherActivity时，由于缓存已经存在了，因此直接解析并显示天气数据，而不会再次发起网络请求了。
     */
    private void showWeatherInfo(Weather weather) {
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature + "℃";
        String weatherInfo = weather.now.more.info;
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();
        for (Forecast forecast : weather.forecastList) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
            TextView dateText = view.findViewById(R.id.date_text);
            TextView infoText = view.findViewById(R.id.info_text);
            TextView maxText = view.findViewById(R.id.max_text);
            TextView minText = view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            forecastLayout.addView(view);
        }
        if (weather.aqi != null) {
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        String comfort = "舒适度：" + weather.suggestion.comfort.info;
        String carWash = "洗车指数：" + weather.suggestion.carWash.info;
        String sport = "运动建议：" + weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);
    }
}
