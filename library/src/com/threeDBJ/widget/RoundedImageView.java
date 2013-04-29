package com.threeDBJ.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.widget.ImageView;

public class RoundedImageView extends ImageView {
    private static final int DEFAULT_RADIUS = 0;
    private int topLeft, topRight, bottomLeft, bottomRight;
    private float cornerRadius;
    private Bitmap rounded;
    private Paint paint = new Paint();
    private Paint coverPaint = new Paint();
    private BitmapShader shader;
    private RectF rect;
    Matrix drawMatrix;
    int w, h;

    public RoundedImageView(Context context, AttributeSet set) {
        super(context, set);
        TypedArray a = context.obtainStyledAttributes(set, R.styleable.rounded_image_view);

        int radius = a.getDimensionPixelSize(R.styleable.rounded_image_view_cornerRadius, DEFAULT_RADIUS);
        topLeft = topRight = bottomLeft = bottomRight = radius;

        topLeft = a.getDimensionPixelSize(R.styleable.rounded_image_view_cornerTopLeft, topLeft);
        topRight = a.getDimensionPixelSize(R.styleable.rounded_image_view_cornerTopRight, topRight);
        bottomLeft = a.getDimensionPixelSize(R.styleable.rounded_image_view_cornerBottomLeft, bottomLeft);
        bottomRight = a.getDimensionPixelSize(R.styleable.rounded_image_view_cornerBottomRight, bottomRight);
        a.recycle();

        coverPaint.setColor(0);
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(rect != null) {
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint);
            coverCorners(canvas);
        }
    }

    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        DebugLog.e("cash", "set image drawable: "+drawable);
        convertToRounded(drawable);
    }

    @Override
    public void setImageResource(int resource) {
        super.setImageResource(resource);
        DebugLog.e("cash", "set image resource: "+resource);
        convertToRounded(getDrawable());
    }

    @Override
    protected void onMeasure(int measuredWidth, int measuredHeight) {
        super.onMeasure(measuredWidth, measuredHeight);
        w = MeasureSpec.getSize(measuredWidth);
        h = MeasureSpec.getSize(measuredHeight);
        if(w != 0 && h != 0) {
            convertToRounded(getDrawable());
        }
    }

    private void convertToRounded(Drawable d) {
        DebugLog.e("cash", "converting to a rounded bitmap");
        if(d != null) {
            Bitmap b = ((BitmapDrawable)d).getBitmap();
            drawMatrix = getImageMatrix();
            Rect bounds = d.getBounds();
            int dwidth = bounds.right - bounds.left;
            int dheight = bounds.bottom - bounds.top;
            if(drawMatrix != null && dheight != 0 && dwidth != 0) {

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
                DebugLog.e("cash", "drawable dims: "+dwidth+", "+dheight+", "+d.getIntrinsicWidth()+" "+d.getIntrinsicHeight());
                
                drawMatrix.setScale(scale, scale);
                drawMatrix.postTranslate(dx, dy);

                rect = new RectF(new Rect(0, 0, vwidth, vheight));
                final float densityMultiplier = getContext().getResources().getDisplayMetrics().density;
                cornerRadius = topLeft*densityMultiplier;
                setShader(b, topLeft, topRight, bottomLeft, bottomRight, drawMatrix);
                invalidate();
            }

        } else {
            rounded = null;
        }
    }

    public void setShader(Bitmap input, int topLeft, int topRight,
                          int bottomLeft, int bottomRight, Matrix m) {
        shader = new BitmapShader(input, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        shader.setLocalMatrix(m);
        paint.setShader(shader);
        
    }

    private void coverCorners(Canvas canvas) {
        //draw rectangles over the corners we want to be square
        if (topLeft == 0) canvas.drawRect(0, 0, cornerRadius, cornerRadius, paint);
        if (topRight == 0) canvas.drawRect(w-cornerRadius, 0, w, cornerRadius, paint);
        if (bottomLeft == 0) canvas.drawRect(0, h-cornerRadius, cornerRadius, h, paint);
        if (bottomRight == 0) canvas.drawRect(w-cornerRadius, h-cornerRadius, w, h, paint);
    }
}