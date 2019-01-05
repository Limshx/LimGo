package com.limshx.limgo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import com.limshx.limgo.kernel.Adapter;
import com.limshx.limgo.kernel.GraphicsOperations;

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
        performClick();
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_UP:
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
        this.canvas = canvas;
        if (null == adapter) {
            int px = getMeasuredWidth();
            adapter = new Adapter(this, px / 20);
        }
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
