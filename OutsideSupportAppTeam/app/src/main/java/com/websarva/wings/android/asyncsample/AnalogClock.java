package com.websarva.wings.android.asyncsample;

/*
 * 参考にしたサイト、サンプルコード
 * 「ビュークラスを作成する」 https://developer.android.com/training/custom-views/create-view?hl=ja Viewの作成
 * 「onDraw で自力で描画するカスタムビューを作る」 https://maku77.github.io/android/ui/create-custom-view.html Viewの作成
 * 「Javaを使ってアナログ時計を作ろうと思います（ソース編③『役割ごとにメソッドに分けた+文字盤追加』）」https://yatcho.hatenadiary.org/entry/20090421/1240308092　時計作成において、Mathなどの計算式を参考
 */
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class AnalogClock extends View {
    // グローバル変数
    private int hours; //時
    private int minutes; //分
    private Paint paint; //円周描画用のペイント
    private Paint hourPaint; //長針描画用のペイント
    private Paint minPaint; //短針描画用のペイント
    private Paint hourlinePaint; //文字盤描画用のペイント
    private Paint centerPaint; //中心円描画用のペイント

    // Viewのサイズを指定
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // 横幅と縦幅が同じ値を持つように設定
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int size = Math.min(width, height); //Math.minで最小サイズにあわせる(長い方にすると見切れちゃうよ)
        setMeasuredDimension(size, size);
    }

    // コンストラクタを設定する関数
    public AnalogClock(Context context, AttributeSet attrs) {
        super(context, attrs);

        paint = new Paint();
        paint.setStrokeWidth(10f); //線の太さ
        paint.setAntiAlias(true); //アンチエイリアスを有効にする

        minPaint = new Paint();
        minPaint.setColor(Color.BLUE); //長針の色は青
        minPaint.setStrokeWidth(10f);
        minPaint.setAntiAlias(true); // アンチエイリアスを有効にする

        hourPaint = new Paint();
        hourPaint.setColor(Color.RED); //短針の色は赤で
        hourPaint.setStrokeWidth(15f); //太くしておく
        hourPaint.setAntiAlias(true);

        hourlinePaint = new Paint();
        hourlinePaint.setColor(Color.GRAY); //グレーにしてみる
        hourlinePaint.setStrokeWidth(5f);
        hourlinePaint.setAntiAlias(true);

        centerPaint = new Paint();
        centerPaint.setColor(Color.BLACK); //中心円は黒で
        centerPaint.setAntiAlias(true);
    }

    //時間を設定するメソッドを作ることで自由に時間をセットできる
    public void setTime(int hours, int minutes) {
        this.hours = hours;
        this.minutes = minutes;
        invalidate(); //ビューを再描画
    }

    // 描画処理をする
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //変数作成
        int centerX = getWidth() / 2; //中心のX座標
        int centerY = getHeight() / 2; //中心のY座標(高さ/2すればでてくる)
        int radius = Math.min(centerX, centerY) - 30; //半径(余白も入れておく)
        //円を描画
        paint.setStyle(Paint.Style.STROKE); //線
        canvas.drawCircle(centerX, centerY, radius, paint);
        //長針を描画
        double minuteAngle = Math.toRadians(minutes * 360 / 60 - 90); //長針が指す角度を計算し、ラジアンに変換する
        int minuteHandRadius = (int) (radius * 0.8); //長針の長さを半径の80%にしておく
        canvas.drawLine(centerX, centerY, (float) (centerX + minuteHandRadius * Math.cos(minuteAngle)), (float) (centerY + minuteHandRadius * Math.sin(minuteAngle)), minPaint); //計算式
        //短針を描画
        double hourAngle = Math.toRadians((hours % 12 + minutes / 60.0) * 360 / 12 - 90); //短針が指す角度を計算し、ラジアンに変換する
        int hourHandRadius = (int) (radius * 0.6); //短針の長さを半径の60%にしておく
        canvas.drawLine(centerX, centerY, (float) (centerX + hourHandRadius * Math.cos(hourAngle)), (float) (centerY + hourHandRadius * Math.sin(hourAngle)), hourPaint); //計算式
        //文字盤を描画
        drawHourLines(canvas, centerX, centerY, radius);
        //中心円を描画
        canvas.drawCircle(centerX, centerY, 10, centerPaint); //サイズは１0ぐらいでいいかも
    }
    // 文字盤を描画する関数
    private void drawHourLines(Canvas canvas, int centerX, int centerY, int radius) {
        for (int hour = 1; hour <= 12; hour++) { //12回繰り返す
            double hourAngle = Math.toRadians(hour * 360 / 12 - 90); // 時間ごとの角度を計算し、ラジアンに変換する
            int hourLineRadius = (int) (radius * 0.9); // 文字盤の線の長さを半径の90%にしておく
            //長針や短針と違い、centerからの描画じゃないため、めんどうな作業になるが、x,yのstartとendの変数を作って描画
            int startX = (int) (centerX + hourLineRadius * Math.cos(hourAngle));
            int startY = (int) (centerY + hourLineRadius * Math.sin(hourAngle));
            int endX = (int) (centerX + radius * Math.cos(hourAngle));
            int endY = (int) (centerY + radius * Math.sin(hourAngle));
            canvas.drawLine(startX, startY, endX, endY, hourlinePaint);
        }
    }
}