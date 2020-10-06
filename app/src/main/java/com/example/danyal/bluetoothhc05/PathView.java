package com.example.danyal.bluetoothhc05;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class PathView extends View {
    //画笔
    protected Paint paint;
    //心电图折线
    protected Path path;
    //自身的大小
    private int width, height;

    public int points_snap = 1;

    int tmpX;
    //折现的颜色
    private int lineColor = Color.parseColor("#76f112");

    private List<Integer> list = new ArrayList<>();

    public PathView(Context context) {
        this(context, null);
    }

    public PathView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PathView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        paint = new Paint();
        path = new Path();
    }

    private void drawPath(Canvas canvas) {
        //Log.i("BLE", "drawPath");
        // 重置path
        path.reset();
        paint.reset();
        tmpX = 0;
        path.moveTo(tmpX, height/2);
        //调节好每个波的X轴距离，尽量和滑动的速度保持一致
        for (int i = 0; i < list.size(); i++) {
            path.lineTo(tmpX , height/2 + list.get(i));
            // path.lineTo(tmpX + points_snap, height/2);
            tmpX += points_snap;
        }
        //Log.i("BLE", "TMP=" + tmpX);
        //设置画笔style
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(lineColor);
        paint.setStrokeWidth(2);
        canvas.drawPath(path, paint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        width = w;
        height = h;
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {


        drawPath(canvas);
        //x轴滑动速度，一开始不滑动，当波形图到达最右边的时候开始滑动
        if (list.size() > width / points_snap) {
            scrollBy(points_snap, 0);
        }
    }

    public void setData(int data) {
        //Log.i("BLE", "");
        //定期删除历史数据，防止图片过长导致崩溃
        if (list.size() > width / points_snap) {
            for (int i = 0; i < points_snap; i++) {
                list.remove(i);
            }
            scrollTo(0, 0);
        }
        list.add(-data);
        //Log.i("BLE", "list-size=" + list.size());
        postInvalidate();
    }
}
