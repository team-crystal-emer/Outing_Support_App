package com.websarva.wings.android.asyncsample;

import android.util.Log;

import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class weatherAPIHelper {

    static String getWeatherAPIData(String result, String type){
        // 都市名。
        String cityName = "";
        // 天気。
        String weather = "";
        // 緯度
        String latitude = "";
        // 経度。
        String longitude = "";
        try {
            // ルートJSONオブジェクトを生成。
            JSONObject rootJSON = new JSONObject(result);
            if(type == "weather"){
                // 天気情報JSON配列オブジェクトを取得。
                JSONArray weatherJSONArray = rootJSON.getJSONArray("weather");
                // 現在の天気情報JSONオブジェクトを取得。
                JSONObject weatherJSON = weatherJSONArray.getJSONObject(0);
                // 現在の天気情報文字列を取得。
                return weatherJSON.getString("main");
            }
            if(type == "humidity"){
                // 天気情報JSON配列オブジェクトを取得。
                JSONObject mainJSON = rootJSON.getJSONObject("main");
                // 湿度情報を取得。
                int humidity = mainJSON.getInt("humidity");
                return humidity + "%";
            }

            if(type == "wind"){
                // 天気情報JSON配列オブジェクトを取得。
                JSONObject windObject = rootJSON.getJSONObject("wind");
                // 風速情報を取得。
                double windSpeed = windObject.getDouble("speed");
                return String.format("%.1f m/s", windSpeed);
            }

            if(type == "temp"){
                // 天気情報JSON配列オブジェクトを取得。
                JSONObject mainJSON = rootJSON.getJSONObject("main");
                // 気温情報を取得。
                double temp = mainJSON.getDouble("temp");
                return String.format("%.1f ℃", temp - 273.15);
            }
            if(type == "timezone"){
                // タイムゾーンを取得
                String timezone = rootJSON.getString("timezone");
                return timezone;
            }
        }
        catch(JSONException ex) {
            Log.e(DEBUG_TAG, "JSON解析失敗", ex);
        }
        return cityName;
    }

    /**
     * お天気情報のURL。
     */
    private static final String WEATHERINFO_URL = "https://api.openweathermap.org/data/2.5/weather?lang=ja";
    /**
     * お天気APIにアクセスすするためのAPIキー。
     * ※※※※※この値は各自のものに書き換える!!※※※※※
     */
    private static final String APP_ID = "553bf5778fc6a258c2d46f7eeff7fcad";
    private static final String DEBUG_TAG = "AsyncSample";

    /**
     * お天気情報の取得処理を行うメソッド。
     */
    @UiThread
    static String receiveWeatherInfo(String place) {
        String urlFull = WEATHERINFO_URL + "&q=" + place + "&appid=" + APP_ID;
        weatherAPIHelper.WeatherInfoBackgroundReceiver backgroundReceiver = new weatherAPIHelper.WeatherInfoBackgroundReceiver(urlFull);
        ExecutorService executorService  = Executors.newSingleThreadExecutor();
        Future<String> future = executorService.submit(backgroundReceiver);
        String result = "";
        try {
            result = future.get();
        }
        catch(ExecutionException ex) {
            Log.w(DEBUG_TAG, "非同期処理結果の取得で例外発生: ", ex);
        }
        catch(InterruptedException ex) {
            Log.w(DEBUG_TAG, "非同期処理結果の取得で例外発生: ", ex);
        }
        return result;
    }

    //天気情報をイメージ画像に変換
    public static int getWeatherImage(String text) {
        if ("Clear sky".equals(text)) {
            return R.drawable.clearsky;
        } else if ("Clear".equals(text)) {
            return R.drawable.clearsky;
        } else if ("Clouds".equals(text)) {
            return R.drawable.clouds;
        } else if ("Rain".equals(text)) {
            return R.drawable.rain;
        } else if ("Thunderstorm".equals(text)) {
            return R.drawable.thunderstorm;
        } else if ("Snow".equals(text)) {
            return R.drawable.snow;
        } else if ("Mist".equals(text)) {
            return R.drawable.fog;
        } else if ("Fog".equals(text)) {
            return R.drawable.fog;
        } else if ("Drizzle".equals(text)) {
            return R.drawable.drizzle;
        } else if ("Heavy rain".equals(text)) {
            return R.drawable.heavyrain;
        } else if ("Thunderstorm with rain".equals(text)) {
            return R.drawable.thunderstormwithrain;
        } else if ("Thunderstorm with snow".equals(text)) {
            return R.drawable.thunderstormwithsnow;
        } else {
            // デフォルトの画像
            return R.drawable.question;
        }
    }

    /**
     * 非同期でお天気情報APIにアクセスするためのクラス。
     */
    private static class WeatherInfoBackgroundReceiver implements Callable<String> {
        /**
         * お天気情報を取得するURL。
         */
        private final String _urlFull;

        /**
         * コンストラクタ。
         * 非同期でお天気情報Web APIにアクセスするのに必要な情報を取得する。
         *
         * @param urlFull お天気情報を取得するURL。
         */
        public WeatherInfoBackgroundReceiver(String urlFull) {
            _urlFull = urlFull;
        }

        @WorkerThread
        @Override
        public String call() {
            // 天気情報サービスから取得したJSON文字列。天気情報が格納されている。
            String result = "";
            // HTTP接続を行うHttpURLConnectionオブジェクトを宣言。finallyで解放するためにtry外で宣言。
            HttpURLConnection con = null;
            // HTTP接続のレスポンスデータとして取得するInputStreamオブジェクトを宣言。同じくtry外で宣言。
            InputStream is = null;
            try {
                // URLオブジェクトを生成。
                URL url = new URL(_urlFull);
                // URLオブジェクトからHttpURLConnectionオブジェクトを取得。
                con = (HttpURLConnection) url.openConnection();
                // 接続に使ってもよい時間を設定。
                con.setConnectTimeout(1000);
                // データ取得に使ってもよい時間。
                con.setReadTimeout(1000);
                // HTTP接続メソッドをGETに設定。
                con.setRequestMethod("GET");
                // 接続。
                con.connect();
                // HttpURLConnectionオブジェクトからレスポンスデータを取得。
                is = con.getInputStream();
                // レスポンスデータであるInputStreamオブジェクトを文字列に変換。
                result = is2String(is);
            }
            catch(MalformedURLException ex) {
                Log.e(DEBUG_TAG, "URL変換失敗", ex);
            }
            // タイムアウトの場合の例外処理。
            catch(SocketTimeoutException ex) {
                Log.w(DEBUG_TAG, "通信タイムアウト", ex);
            }
            catch(IOException ex) {
                Log.e(DEBUG_TAG, "通信失敗", ex);
            }
            finally {
                // HttpURLConnectionオブジェクトがnullでないなら解放。
                if(con != null) {
                    con.disconnect();
                }
                // InputStreamオブジェクトがnullでないなら解放。
                if(is != null) {
                    try {
                        is.close();
                    }
                    catch(IOException ex) {
                        Log.e(DEBUG_TAG, "InputStream解放失敗", ex);
                    }
                }
            }
            return result;
        }

        /**
         * InputStreamオブジェクトを文字列に変換するメソッド。 変換文字コードはUTF-8。
         *
         * @param is 変換対象のInputStreamオブジェクト。
         * @return 変換された文字列。
         * @throws IOException 変換に失敗した時に発生。
         */
        private String is2String(InputStream is) throws IOException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            StringBuffer sb = new StringBuffer();
            char[] b = new char[1024];
            int line;
            while(0 <= (line = reader.read(b))) {
                sb.append(b, 0, line);
            }
            return sb.toString();
        }
    }

}