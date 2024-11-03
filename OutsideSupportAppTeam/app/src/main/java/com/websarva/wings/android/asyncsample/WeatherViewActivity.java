package com.websarva.wings.android.asyncsample;

/*
 * 参考にしたサイト、サンプルコード
 * 「スパン」 https://developer.android.com/guide/topics/text/spans?hl=ja SpannableString使用の際に参考
 */

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;
import java.util.TimeZone;

public class WeatherViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_view);
        //updateUIという関数を作って処理する(設定画面から戻る場合でもちゃんと設定を反映させるため)
        updateUI();
        Button back_button = findViewById(R.id.back_button);
        //戻るボタンが押されたら遷移を終了
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        //設定ボタンが押されたら設定画面へ遷移
        Button setting_button = findViewById(R.id.setting_button);
        setting_button.setOnClickListener(v -> {
            Intent intent = new Intent(WeatherViewActivity.this, SettingActivity.class);
            startActivity(intent);
        });
    }

    //天候情報表示関数
    private void updateUI() {
        //設定ファイルを読み込む
        SharedPreferences dataprefs = getSharedPreferences("emer_app_data_team", MODE_PRIVATE);
        // 出発地と到着地の情報を取得
        String departurePlace = dataprefs.getString("departure", "tokyo");
        String destinationPlace = dataprefs.getString("destination", "tokyo");
        // 天候情報を生成する関数の呼び出し
        displayWeatherInfo(R.id.departureweather, R.id.departuretemp, R.id.departureweatherimage, departurePlace, getResources().getString(R.string.weatherview_class_departure));
        displayWeatherInfo(R.id.destinationweather, R.id.destinationtemp, R.id.destinationweatherimage, destinationPlace, getResources().getString(R.string.weatherview_class_deatination));
    }

    //天候情報生成関数
    private void displayWeatherInfo(int nameViewId, int tempViewId, int imageViewId, String place, String str) {
        TextView nameView = findViewById(nameViewId);
        TextView tempView = findViewById(tempViewId);
        ImageView imageView = findViewById(imageViewId);

        //出発地or目的地の名前表示
        nameView.setText(str + "：" + place);

        String weatherResult = weatherAPIHelper.receiveWeatherInfo(place);
        if(weatherResult.isEmpty()){ //天候情報が空なら天候情報がないと表示
            tempView.setText(getResources().getString(R.string.tourism_class_no_weatherinfo));
            imageView.setImageResource(R.drawable.question);
        }else {
            //weatherAPIから、気温、湿度、風速を取得
            String weatherTemp = weatherAPIHelper.getWeatherAPIData(weatherResult, "temp");
            String weatherHumidity = weatherAPIHelper.getWeatherAPIData(weatherResult, "humidity");
            String weatherWind = weatherAPIHelper.getWeatherAPIData(weatherResult, "wind");
            //一旦天候情報の文字列を作成
            String weatherInfo = getResources().getString(R.string.tourism_class_temp) + weatherTemp + "\n" + getResources().getString(R.string.tourism_class_humidity) + weatherHumidity + "\n" + getResources().getString(R.string.tourism_class_wind) + weatherWind;
            // SpannableString を作成
            SpannableString spannableString = new SpannableString(weatherInfo);
            // 三項演算子を使い、気温が15℃以上なら赤色、未満なら青色にする
            int tempColor = Double.parseDouble(weatherTemp.replace("℃", "")) >= 15.0 ? Color.RED : Color.BLUE;
            //setSpan(ForegroundColorSpan("色"), スタート位置(int), 終了位置(int), Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
            spannableString.setSpan(new ForegroundColorSpan(tempColor), weatherInfo.indexOf(weatherTemp), weatherInfo.indexOf(getResources().getString(R.string.tourism_class_humidity)), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            //湿度の色変更
            int humidityValue = Integer.parseInt(weatherHumidity.replace("%", ""));
            int humidityColor = getHumidityColor(humidityValue);
            spannableString.setSpan(new ForegroundColorSpan(humidityColor), weatherInfo.indexOf(weatherHumidity), weatherInfo.indexOf(getResources().getString(R.string.tourism_class_wind)), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            //風速の色変更
            double windSpeed = Double.parseDouble(weatherWind.replace("m/s", ""));
            int windColor = getWindSpeedColor(windSpeed);
            spannableString.setSpan(new ForegroundColorSpan(windColor), weatherInfo.indexOf(weatherWind), weatherInfo.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            // TextViewに設定
            tempView.setText(spannableString);
            //天気イメージ設定
            String weatherCondition = weatherAPIHelper.getWeatherAPIData(weatherResult, "weather");
            int imageResourceId = weatherAPIHelper.getWeatherImage(weatherCondition);
            if(imageResourceId == R.drawable.clearsky){
                //入力した地域のタイムゾーンの値を取得する
                String timezone = weatherAPIHelper.getWeatherAPIData(weatherResult, "timezone");
                //カレンダー変数を作ってタイムゾーンから地域の時刻(時)を取得
                Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
                c.add(Calendar.SECOND, Integer.parseInt(timezone));
                int timeOfDay = c.get(Calendar.HOUR_OF_DAY);
                //18時から翌6時までの場合
                if(timeOfDay >= 18 || timeOfDay < 6) imageView.setImageResource(R.drawable.moon); //画像を月に変更する
                else imageView.setImageResource(R.drawable.clearsky); //そもそもデフォルトの画像がclearskyなのでこのコードが無くても画像は表示される
            }else{
                imageView.setImageResource(imageResourceId);
            }
        }
    }

    //風速の色取得
    private int getWindSpeedColor(double windSpeed) {
        if (windSpeed >= 0 && windSpeed <= 4) return Color.CYAN; // 空色
        else if (windSpeed <= 10) return Color.parseColor("#009AFF"); // 濃い水色
        else if (windSpeed <= 15) return Color.parseColor("#34CE00"); // 黄緑
        else if (windSpeed <= 20) return Color.parseColor("#FF9800"); // 薄いオレンジ
        else if (windSpeed <= 25) return Color.parseColor("#FF6500"); // レンジ
        else return Color.RED; //赤
    }

    //湿度の色取得
    private int getHumidityColor(int humidityValue) {
        if (humidityValue <= 40) return Color.BLUE; //青
        else if (humidityValue <= 59) return Color.parseColor("#00FF00"); // 薄緑
        else return Color.RED; //赤
    }

    //画面が再開した時、UIを再度描画する
    protected void onResume() {
        super.onResume();
        updateUI();
    }
}