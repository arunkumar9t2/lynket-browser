package arun.com.chromer.webheads;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import arun.com.chromer.R;
import arun.com.chromer.util.Util;

/**
 * Created by Arun on 04/02/2016.
 */
public class RemoveHeadCircle extends View {

    public static final int REMOVE_HEAD_DP = 72;

    public static final int EXTRA_DP = 20;

    private Paint mBgPaint;


    public RemoveHeadCircle(Context context) {
        super(context);
        mBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBgPaint.setColor(ContextCompat.getColor(getContext(), R.color.md_red_500));
        mBgPaint.setStyle(Paint.Style.FILL);

        float shadwR = context.getResources().getDimension(R.dimen.web_head_shadow_radius);
        float shadwDx = context.getResources().getDimension(R.dimen.web_head_shadow_dx);
        float shadwDy = context.getResources().getDimension(R.dimen.web_head_shadow_dy);

        mBgPaint.setShadowLayer(shadwR, shadwDx, shadwDy, 0x85000000);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int size = Util.dpToPx(REMOVE_HEAD_DP + EXTRA_DP);
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(0, PorterDuff.Mode.CLEAR);
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, (float) (getWidth() / 2.4), mBgPaint);

        drawDeleteIcon(canvas);
    }

    private void drawDeleteIcon(Canvas canvas) {
        Bitmap deleteIcon = new IconicsDrawable(getContext())
                .icon(GoogleMaterial.Icon.gmd_delete)
                .color(Color.WHITE)
                .sizeDp(18).toBitmap();
        int cHeight = canvas.getClipBounds().height();
        int cWidth = canvas.getClipBounds().width();
        float x = cWidth / 2f - deleteIcon.getWidth() / 2;
        float y = cHeight / 2f - deleteIcon.getHeight() / 2;
        canvas.drawBitmap(deleteIcon, x, y, null);
    }
}
