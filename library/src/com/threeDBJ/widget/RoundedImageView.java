package com.tapmunk.app;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageView;

public class RoundedImageView extends ImageView {
    private int topLeft=0, topRight=0, bottomLeft=0, bottomRight=0;
    int w, h;

    public RoundedImageView(Context context, AttributeSet set) {
        super(context, set);
        TypedArray a = context.obtainStyledAttributes(set, R.styleable.rounded_image_view);
        topLeft = a.getInt(R.styleable.rounded_image_view_cornerTopLeft, 0);
        topRight = a.getInt(R.styleable.rounded_image_view_cornerTopRight, 0);
        bottomLeft = a.getInt(R.styleable.rounded_image_view_cornerBottomLeft, 0);
        bottomRight = a.getInt(R.styleable.rounded_image_view_cornerBottomRight, 0);
        a.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Drawable drawable = getDrawable();
        if(drawable != null) {
            Bitmap b = ((BitmapDrawable)drawable).getBitmap();
            Matrix m = getImageMatrix();
            if(m != null) {
                Rect bounds = drawable.getBounds();
                int dwidth = bounds.right - bounds.left;
                int dheight = bounds.bottom - bounds.top;

                int vwidth = w - getPaddingLeft() - getPaddingRight();
                int vheight = h - getPaddingTop() - getPaddingBottom();
                float scale;
                float dx = 0, dy = 0;

                if (dwidth * vheight > vwidth * dheight) {
                    scale = (float) vheight / (float) dheight; 
                    dx = (vwidth - dwidth * scale) * 0.5f;
                } else {
                    scale = (float) vwidth / (float) dwidth;
                    dy = (vheight - dheight * scale) * 0.5f;
                }
                DebugLog.e("cash", "image scale "+scale+", "+dx+", "+dy);
                
                m.setScale(scale, scale);
                m.postTranslate(dx, dy);
                b = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), m, false);
                b = Bitmap.createBitmap(b, (int)-dx, (int)-dy, w, h);
                //canvas.concat(m);
            }
            Bitmap bitmap = b.copy(Bitmap.Config.ARGB_8888, true);

            Bitmap roundBitmap = getRoundedCornerBitmap(getContext(), bitmap, topLeft, topRight, bottomLeft, bottomRight);
            canvas.drawBitmap(roundBitmap, 0, 0, null);
        } else {
            super.onDraw(canvas);
        }
    }

    @Override
    public void setImageResource(int resource) {
        super.setImageResource(resource);
        DebugLog.e("cash", "set image resource: "+resource);
    }

    @Override
    public void setImageBitmap(Bitmap bitmap) {
        super.setImageBitmap(bitmap);
        DebugLog.e("cash", "set image bitmap: "+bitmap);
    }

    @Override
    protected void onMeasure(int measuredWidth, int measuredHeight) {
        super.onMeasure(measuredWidth, measuredHeight);
        w = MeasureSpec.getSize(measuredWidth);
        h = MeasureSpec.getSize(measuredHeight);
        //requestLayout();
        //invalidate();
    }

    public Bitmap getRoundedCornerBitmap(Context context, Bitmap input, int topLeft, int topRight,
                                         int bottomLeft, int bottomRight) {
        int w = input.getWidth();
        int h = input.getHeight();
        Bitmap output = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final float densityMultiplier = context.getResources().getDisplayMetrics().density;

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, w, h);
        final RectF rectF = new RectF(rect);

        //make sure that our rounded corner is scaled appropriately
        final float roundPx = topLeft*densityMultiplier;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);


        //draw rectangles over the corners we want to be square
        if (topLeft == 0) canvas.drawRect(0, 0, w/2, h/2, paint);
        if (topRight == 0) canvas.drawRect(w/2, 0, w, h/2, paint);
        if (bottomLeft == 0) canvas.drawRect(0, h/2, w/2, h, paint);
        if (bottomRight == 0) canvas.drawRect(w/2, h/2, w, h, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(input, 0, 0, paint);

        return output;
    }
}