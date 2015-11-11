package com.guokrspace.dududriver.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.guokrspace.dududriver.util.DisplayUtil;

/**
 * Created by hyman on 15/10/30.
 */
public class ListenProgressView extends View{

    private Paint translucentPaint = null;

    private Paint scannerPaint = null;

    private Paint innerCirclePaint = null;

    private Paint textPaint = null;

    private boolean isListening = false;

    private float bRadius = 0;

    private float sRadius = 0;//内圆半径即为外圆半径减去diff的值

    private float diff = DisplayUtil.dp2px(6);

    private int size;//自定义view的宽、高

    private int scannerColor = Color.parseColor("#ffff4444");

    private static final String texts[] =  new String [] {"点击听单", "听单中"};

    private int index = 0;

    private String text = texts[index];

    private Handler mHandler = new Handler();

    private Matrix matrix = null;

    private static final float ANGLE_START = 0.0f;

    private static final float ANGLE_END = 360.0f;

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            ValueAnimator angleAnim = ValueAnimator.ofFloat(ANGLE_START, ANGLE_END);
            angleAnim.setDuration(3000);
            angleAnim.setRepeatCount(ValueAnimator.INFINITE);
            angleAnim.setInterpolator(new LinearInterpolator());
            angleAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    matrix = new Matrix();
                    matrix.preRotate((Float) animation.getAnimatedValue(), size / 2, size / 2);
                    ListenProgressView.this.invalidate();
                }
            });
            angleAnim.start();
        }
    };

    public ListenProgressView(Context context) {
        this(context, null);
    }

    public ListenProgressView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ListenProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        translucentPaint = initTranslucentPaint();
        innerCirclePaint = initInnerCirclePaint();
        textPaint = initTextPaint();

    }

    private Paint initTextPaint() {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setAntiAlias(true);
        paint.setTextSize(DisplayUtil.dp2px(20));
        return paint;
    }

    private Paint initInnerCirclePaint() {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.parseColor("#FF2F3B4A"));
        paint.setAntiAlias(true);
        return paint;
    }

    private Paint initScannerPaint() {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Shader shader = new SweepGradient(size / 2, size / 2, new int[]{Color.WHITE, scannerColor, scannerColor, scannerColor}, null);
        paint.setShader(shader);
        paint.setAntiAlias(true);
        return paint;
    }

    private Paint initTranslucentPaint() {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLACK);
        paint.setAlpha(100);
        return paint;
    }

    public boolean isListening() {
        return isListening;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int minSize = (int) (getTextWith(textPaint, text) + Math.ceil(2 * diff));
        int wantSize = 2 * minSize;
        int measuredSize = measureSize(widthMeasureSpec, wantSize, minSize);
        setMeasuredDimension(measuredSize, measuredSize);

        size = measuredSize;
        bRadius = measuredSize / 2;
        sRadius = bRadius - diff;

        scannerPaint = initScannerPaint();
        mHandler.post(runnable);
    }

    private int measureSize(int widthMeasureSpec, int wantSize, int minSize) {
        int result = 0;
        int specMode = MeasureSpec.getMode(widthMeasureSpec);
        int specSize = MeasureSpec.getSize(widthMeasureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = wantSize;
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return Math.max(result, minSize);
    }

    private int getTextWith(Paint paint, String text) {
        int iRet = 0;
        if (text != null && text.length() > 0) {
            int len = text.length();
            float[] widths = new float[len];
            paint.getTextWidths(text, widths);
            for (int i = 0; i < len; i++) {
                iRet += Math.ceil(widths[i]);
            }
        }
        return iRet;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(size / 2, size / 2, bRadius, translucentPaint);
        if (isListening) {
            int sc = canvas.save();
            canvas.concat(matrix);
            canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
            canvas.drawCircle(size / 2, size / 2, bRadius, scannerPaint);
            canvas.drawCircle(size / 2, size / 2, sRadius, innerCirclePaint);
            canvas.restoreToCount(sc);
        }
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        int h = (int) (getMeasuredHeight() / 2 + (fontMetrics.descent - fontMetrics.ascent) / 2 - fontMetrics.descent);
        canvas.drawText(text, size / 2, h, textPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        getParent().requestDisallowInterceptTouchEvent(true);
        int action = event.getAction() & MotionEvent.ACTION_MASK;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                //切换当前的听单状态
                isListening = ! isListening;
                index++;
                text = texts[index % 2];
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }


}
