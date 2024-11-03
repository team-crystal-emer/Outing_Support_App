package com.websarva.wings.android.asyncsample;

/*
 * 参考にしたサイト、サンプルコード
 * 「SharedPreferences を使用してシンプルなデータを保存する」 https://developer.android.com/training/data-storage/shared-preferences?hl=ja データ保存で使用
 * 「Androidアプリ開発入門 (4) - TextToSpeech 」 https://note.com/npaka/n/n374eb05fc4b1 ttsについて
 */

import static android.content.Context.MODE_PRIVATE;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import java.util.Locale;

public class PreAlarmReceiver extends BroadcastReceiver {

    private TextToSpeech tts;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {
        //設定ファイルを読み込む
        SharedPreferences prefs = context.getSharedPreferences("emer_app_data_team", MODE_PRIVATE);
        boolean alarm_enable_tts_switch_value = prefs.getBoolean("isTts", false);
        String meeting_area = prefs.getString("meeting_place", "tokyo");
        //ttsが有効であれば
        if(alarm_enable_tts_switch_value == true){
            tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (status == TextToSpeech.SUCCESS) {
                        tts.setLanguage(Locale.getDefault());
                        tts.speak(context.getResources().getString(R.string.alarm_pre_text, meeting_area), TextToSpeech.QUEUE_FLUSH, null);
                    }
                }
            });
        }
        //通知チャンネルを作成
        NotificationChannel channel = new NotificationChannel("default",
                "Default Channel", NotificationManager.IMPORTANCE_DEFAULT);
        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);
        Intent mainIntent = new Intent(context, MainActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, mainIntent, PendingIntent.FLAG_IMMUTABLE);
        // 通知を作成(Builderを使用)
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "default")
                .setSmallIcon(R.drawable.announce)
                .setContentTitle(context.getResources().getString(R.string.alarm_pre_title))
                .setContentText(context.getResources().getString(R.string.alarm_pre_text, meeting_area))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        // 通知を表示
        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(0, builder.build());
    }
}
