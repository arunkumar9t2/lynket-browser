package arun.com.chromer.webheads;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.view.View;

import arun.com.chromer.R;
import arun.com.chromer.util.Util;

/**
 * Created by Arun on 04/02/2016.
 */
public class RemoveHeadCircle extends View {

    public static final int REMOVE_HEAD_DP = 72;

    public static final int EXTRA_DP = 10;

    private Paint mBgPaint;


    public RemoveHeadCircle(Context context) {
        super(context);
        mBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBgPaint.setColor(ContextCompat.getColor(getContext(), R.color.md_red_500));
        mBgPaint.setStyle(Paint.Style.FILL);
        mBgPaint.setShadowLayer(4.0f, 1.0f, 2.0f, 0x85000000);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int size = Util.dpToPx(REMOVE_HEAD_DP + 10);
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(0, PorterDuff.Mode.CLEAR);
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, (float) (getWidth() / 2.4), mBgPaint);
    }

}
