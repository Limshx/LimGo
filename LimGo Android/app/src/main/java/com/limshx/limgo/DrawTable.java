package com.limshx.limgo;

import Kernel.Adapter;
import Kernel.GraphicsOperations;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public class DrawTable extends View implements GraphicsOperations {
    private Context context;
    private Paint paint = new Paint();
    private Canvas canvas;
    Adapter adapter;


    public DrawTable(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        paint.setAntiAlias(true);
        paint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
    }

    public boolean performClick() {
        return super.performClick();
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_UP:
                performClick();
                break;
            case MotionEvent.ACTION_DOWN:
                float x = motionEvent.getX();
                float y = motionEvent.getY();
                adapter.click(x, y);
                doRepaint();
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            default:
                break;
        }
        return super.onTouchEvent(motionEvent);
    }

    protected void onDraw(Canvas canvas) {
//        setDrawTable(this);
        this.canvas = canvas;

        if (null == adapter) {
            int px = getMeasuredWidth();
            // int py = getMeasuredHeight();
            adapter = new Adapter(this, px / 20);
        }

//        paint.setColor(Color.WHITE);
        // canvas.drawRect(0, 0, px, py, paint);
//        canvas.drawPaint(paint);

        adapter.drawBoard();
    }

    @Override
    public void drawLine(int v, int v1, int v2, int v3, int v4) {
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(v4);
        canvas.drawLine(v, v1, v2, v3, paint);
    }

    @Override
    public void fillRect(int v, int v1, int v2, int v3, int i) {
        paint.setColor(i);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(v, v1, v + v2, v1 + v3, paint);
    }

    @Override
    public void drawCircle(int v, int v1, int v2, int color, boolean fillCircle) {
        paint.setColor(color);
        paint.setStyle(fillCircle ? Paint.Style.FILL : Paint.Style.STROKE);
        canvas.drawCircle(v, v1, v2, paint);
    }

//    @Override
//    public void drawString(String str, float x, float y) {
//        paint.setColor(Color.BLACK);
//        paint.setTextSize(32);
//        canvas.drawText(str, x, y, paint);
//    }

    @Override
    public void showMessage(final String s) {
        post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void doRepaint() {
        post(new Runnable() {
            @Override
            public void run() {
                invalidate();
            }
        });
    }
}
