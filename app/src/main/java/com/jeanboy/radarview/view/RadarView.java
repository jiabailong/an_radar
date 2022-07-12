package com.jeanboy.radarview.view;

import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import java.util.List;


public class RadarView extends View {

    private final static String TAG = RadarView.class.getSimpleName();

    private List<RadarData> dataList;

    private int count = 6;//雷达网圈数
    private int linecount = 5;//线条数量
    private float angle;//多边形弧度
    private float radius;
    private float maxValue = 100f;
    private Paint mainPaint;//雷达区画笔
    private Paint valuePaint;//数据区画笔
    private Paint textPaint;//文本画笔
    private Paint outPaint;//文本画笔

    private int mainColor = 0xFF888888;//雷达区颜色
    private int valueColor =0xB3808080;//数据区颜色
    private int light_valueColor =0x30CCCCCC;//数据区颜色
    private int textColor = 0xFF808080;//文本颜色

    private int outColor =0xFFFFC8B1;//文本颜色
    private float mainLineWidth = 0.5f;//雷达网线宽度dp
    private float valueLineWidth = 2f;//数据区边宽度dp
    private float outLineWidth = 4f;//数据区边宽度dp
    private float valuePointRadius = 4f;//数据区圆点半径dp
    private float textSize = 14f;//字体大小sp

    private int mWidth, mHeight;

    public RadarView(Context context) {
        this(context, null);
    }

    public RadarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RadarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setup();
    }

    private void setup() {
        mainPaint = new Paint();
        mainPaint.setAntiAlias(true);
        mainPaint.setColor(mainColor);
        mainPaint.setStyle(Paint.Style.STROKE);

        valuePaint = new Paint();
        valuePaint.setAntiAlias(true);
        valuePaint.setColor(valueColor);
        valuePaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(textColor);
        setLayerType(LAYER_TYPE_SOFTWARE, null);

        outPaint = new Paint();
        outPaint.setAntiAlias(true);
        outPaint.setStrokeWidth(outLineWidth);
        outPaint.setStyle(Paint.Style.FILL);
        outPaint.setColor(outColor);
        outPaint.setMaskFilter(new BlurMaskFilter(40, BlurMaskFilter.Blur.OUTER));


    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        radius = Math.min(h, w) / 2 * 0.6f;
        mWidth = w;
        mHeight = h;
        postInvalidate();
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.translate(mWidth / 2, mHeight / 2);

        if (isDataListValid()) {
            drawSpiderweb(canvas);
            drawText(canvas);
            drawRegion(canvas);
        }
    }

    /**
     * 绘制蜘蛛网
     *
     * @param canvas
     */
    private void drawSpiderweb(Canvas canvas) {
        mainPaint.setStrokeWidth(dip2px(getContext(), mainLineWidth));
        mainPaint.setPathEffect(new DashPathEffect(new float[]{4, 4}, 0));

        Path linePath = new Path();
        float r = radius / (count - 1);//蜘蛛丝之间的间距
        for (int i = 0; i < count; i++) {
            float curR = r * i;//当前半径
            if(i==count - 1){
                canvas.drawCircle(0,0,curR,outPaint);

            }else{
                canvas.drawCircle(0,0,curR,mainPaint);

            }
            for (int j = 0; j < linecount; j++) {
                float x = (float) (curR * Math.sin(angle / 2 + angle * j));
                float y = (float) (curR * Math.cos(angle / 2 + angle * j));
                if (i == count - 1) {//当绘制最后一环时绘制连接线
                    linePath.reset();
                    linePath.moveTo(0, 0);
                    linePath.lineTo(x, y);
                    canvas.drawPath(linePath, mainPaint);
                    valuePaint.setStyle(Paint.Style.FILL);
                    canvas.drawCircle(x, y, dip2px(getContext(), valuePointRadius), valuePaint);

                }
            }

        }
    }

    /**
     * 绘制文本
     *
     * @param canvas
     */
    private void drawText(Canvas canvas) {
        textPaint.setTextSize(sp2px(getContext(), textSize));
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        float fontHeight = fontMetrics.descent - fontMetrics.ascent;
        for (int i = 0; i < linecount; i++) {
            float x = (float) ((radius + fontHeight * 2) * Math.sin(angle / 2 + angle * i));
            float y = (float) ((radius + fontHeight * 2) * Math.cos(angle / 2 + angle * i));
            String title = dataList.get(i).getTitle();
            String content= dataList.get(i).getContent();
            float dis = textPaint.measureText(title);//文本长度
            textPaint.setTypeface(Typeface.DEFAULT);
            canvas.drawText(title, x - dis / 2, y, textPaint);
            Rect rect = new Rect();
            textPaint.getTextBounds(title, 0, title.length(), rect);
            int h = rect.height();
            int strWidth=rect.width();
            float contentWidth = textPaint.measureText(content);
            textPaint.setTypeface(Typeface.DEFAULT_BOLD);
            canvas.drawText(content, x - dis / 2+(strWidth-contentWidth)/2, y+h, textPaint);
        }
    }

    /**
     * 绘制区域
     *
     * @param canvas
     */
    private void drawRegion(Canvas canvas) {
        valuePaint.setStrokeWidth(dip2px(getContext(), valueLineWidth));
        Path path = new Path();
        path.reset();

        for (int i = 0; i < linecount; i++) {
            double percent = dataList.get(i).getPercentage() / maxValue;
            float x = (float) (radius * Math.sin(angle / 2 + angle * i) * percent);
            float y = (float) (radius * Math.cos(angle / 2 + angle * i) * percent);
            if (i == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
        }
        path.close();
        valuePaint.setStyle(Paint.Style.FILL);
        valuePaint.setColor(light_valueColor);

        canvas.drawPath(path, valuePaint);
        //绘制填充区域
        valuePaint.setColor(valueColor);
        valuePaint.setStyle(Paint.Style.STROKE);
        CornerPathEffect cornerPathEffect = new CornerPathEffect(20);
        valuePaint.setPathEffect(cornerPathEffect);

        canvas.drawPath(path, valuePaint);
    }


    private boolean isDataListValid() {
        return dataList != null && dataList.size() >= 3;
    }


    public void setDataList(List<RadarData> dataList) {
        if (dataList == null || dataList.size() < 3) {
            throw new RuntimeException("The number of data can not be less than 3");
        } else {
            this.dataList = dataList;
            linecount=dataList.size();
            angle = (float) (Math.PI * 2 / linecount);
            invalidate();
        }
    }
    public void setCircleCount(int count){
        this.count=count;
    }


    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

}
