package com.example.Paint;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;


import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

public class PaintView extends View implements View.OnTouchListener {

    //Variable initiliazaion
    private static final float TOUCH_TOLERANCE = 4;
    private final Stack<Image> mImages = new Stack<>();
    private final Stack<Image> mUndoneImages = new Stack<>();
    private final Paint mBitmapPaint = new Paint(Paint.DITHER_FLAG);
    private final boolean mTxtMode = false;
    private final float touchTolerance = 4;
    float mLastTouchX;
    float mLastTouchY;
    private float mX, mY;
    private Path mPath;
    private Paint mPaint;
    private int mBrushColor;
    private float mStrokeWidth;
    private int mEraserColor;
    private float mEraserWidth;
    private int mOldBrushColor;
    private int mFillColor;
    private float mOldStrokeWidth;
    private Bitmap mBitmap;
    private Context mContext;
    private Canvas mCanvas;
    private boolean mClear = false;
    private int mBackgroundColor;
    private boolean mEraseMode = false;
    private boolean mCircleMode = false;
    private boolean mLineMode = false;
    private boolean mColorFillMode = false;
    private boolean mRectangleMode = false;
    private int mWidth, mHeight;
    private OnDragListener mListener;
    private android.R.attr start;


    public PaintView(Context context) {
        super(context);
    }

    public PaintView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        mPath = new Path();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(getResources().getColor(R.color.black));
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(8);
        mContext = context;
        mBackgroundColor = getResources().getColor(R.color.white);
        mFillColor = getResources().getColor(R.color.black);
    }

    public void init(int height, int width) {
        mHeight = height;
        mWidth = width;
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        mCanvas.drawColor(mBackgroundColor);
        mBrushColor = getResources().getColor(R.color.black);
        mImages.push(new Image(mBitmap));
    }

    public int getColor() {
        return mBrushColor;
    }

//   public void setMostRecentBitmap(){
//        if (!mImages.isEmpty()){
//            Bitmap lastBitmap = mImages.peek().bitmap;
//            setBitmap(lastBitmap);
//        }
//   }

    public void setColor(int color) {
        mBrushColor = color;
        mOldBrushColor = color;
    }

    public void setStrokeWidth(float width) {
        mStrokeWidth = width;
        mOldStrokeWidth = width;
    }

    public void setCircleMode(boolean mode) {
        mCircleMode = mode;
    }

    public void setColorFillMode(boolean mode) {
        mColorFillMode = mode;
    }

    public int getFillColor() {
        return mFillColor;
    }

    public void setFillColor(int color) {
        mFillColor = color;
    }
    // set bitmap passed as an argument as the main bitmap

    public void setBitmap(Bitmap bitmap) {
        Bitmap workingBitmap = Bitmap.createBitmap(bitmap);
        Bitmap mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true);
        mBitmap = mutableBitmap;
        mCanvas = new Canvas(mutableBitmap);
        mPath.reset();
        mImages.push(new Image(mBitmap));
        invalidate();
    }

    public int getBackgroundColor() {
        return mBackgroundColor;
    }

    public void setBackgroundColor(int color) {
        mBackgroundColor = color;
        invalidate();
    }

    public void drawCurrentPathOnTheMostRecentBitmap() {
        if (!mImages.isEmpty()) {
            mCanvas.drawBitmap(mImages.peek().bitmap, 0, 0, mBitmapPaint);
            mCanvas.drawPath(mPath, mPaint);
            mPath.reset();
        } else
            mCanvas.drawColor(mBackgroundColor);
        invalidate();
    }

    public void Undo() {
        if (!mImages.isEmpty()) {
            mPath.reset();
            if (mImages.size() > 1)
                mUndoneImages.push(mImages.pop());

            Bitmap lastBitmap = mImages.peek().bitmap;
            mCanvas.drawBitmap(lastBitmap, 0, 0, mBitmapPaint);
            invalidate();
        }
    }

    public void clearCanvas() {
        mClear = true;
        invalidate();
    }

    public void erase() {
        mEraseMode = true;
        mEraserColor = mBackgroundColor;
        mEraserWidth = 8;
    }

    public void setEraserWidth(float eraserWidth) {
        mEraserWidth = eraserWidth;
    }

    public void setEraseMode(boolean eraseMode) {
        mEraseMode = eraseMode;
    }

    public void setLineMode(boolean lineMode) {
        mLineMode = lineMode;
    }

    public void setRectangleMode(boolean rectangleMode) {
        mRectangleMode = rectangleMode;
    }

    public Bitmap floodFill(Bitmap image, Point node, int targetColor, int replacementColor) // color filling algorithm, returns colored bitmap
    {
        int width = image.getWidth();
        int height = image.getHeight();
        int target = targetColor;
        int replacement = replacementColor;
        if (target != replacement) {
            Queue<Point> queue = new LinkedList<Point>();
            do {
                int x = node.x;
                int y = node.y;
                while (x > 0 && image.getPixel(x - 1, y) == target) {
                    x--;
                }
                boolean spanUp = false;
                boolean spanDown = false;
                while (x < width && image.getPixel(x, y) == target) {
                    image.setPixel(x, y, replacement);
                    if (!spanUp && y > 0 && image.getPixel(x, y - 1) == target) {
                        queue.add(new Point(x, y - 1));
                        spanUp = true;
                    } else if (spanUp && y > 0 && image.getPixel(x, y - 1) != target) {
                        spanUp = false;
                    }
                    if (!spanDown && y < height - 1 && image.getPixel(x, y + 1) == target) {
                        queue.add(new Point(x, y + 1));
                        spanDown = true;
                    } else if (spanDown && y < height - 1 && image.getPixel(x, y + 1) != target) {
                        spanDown = false;
                    }
                    x++;
                }
            } while ((node = queue.poll()) != null);
        }
        return image;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mClear) {
            mBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
            mCanvas.drawColor(mBackgroundColor);
            mImages.clear();
            mUndoneImages.clear();
            mImages.push(new Image(mBitmap));
            mPath.reset();
            mClear = false;
            return;
        }

        if (mEraseMode) {
            mBrushColor = mEraserColor;
            mStrokeWidth = mEraserWidth;
        } else {
            mBrushColor = mOldBrushColor;
            mStrokeWidth = mOldStrokeWidth;
        }

        //Draw Current Path
        mPaint.setColor(mBrushColor);
        mPaint.setStrokeWidth(mStrokeWidth);
        mCanvas.drawPath(mPath, mPaint);
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    private void touchStart(float x, float y) {
        if (mColorFillMode) {
            //Fill bitmap with color and set it as the main
            Bitmap filledBitmap = floodFill(mBitmap, new Point((int) x, (int) y), mBitmap.getPixel((int) x, (int) y), mFillColor);
            setBitmap(filledBitmap);
            return;
        }
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void touchMove(float x, float y) {
        MotionEvent event = null;
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);


        if (mColorFillMode)
            return;
        if (mCircleMode) {
            // calculate radius of the circle
            float radius = (float) Math.sqrt(dx * dx + dy * dy);
            drawCurrentPathOnTheMostRecentBitmap();
            mPath.addCircle(mX, mY, radius, Path.Direction.CW);


        } else if (mLineMode) {

            drawCurrentPathOnTheMostRecentBitmap();
            mPath.moveTo(mX, mY);
            mPath.lineTo(x, y);


        } else if (mRectangleMode) {
            drawCurrentPathOnTheMostRecentBitmap();
            //Draws Rectangle
            mPath.moveTo(mX, mY);
            mPath.lineTo(x, mY);
            mPath.moveTo(x, mY);
            mPath.lineTo(x, y);
            mPath.moveTo(mX, mY);
            mPath.lineTo(mX, y);
            mPath.moveTo(mX, y);
            mPath.lineTo(x, y);
            mPath.moveTo(x, y);

        } else {
            // draws path, which is not a circle, line or a rectangle
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
                mX = x;
                mY = y;
            }
        }
    }

    private void touchUp(float mx, float my) {
        if (mColorFillMode)
            return;

        mImages.push(new Image(mBitmap)); // add the current bitmap to the bitmap stack
        mPath = new Path();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float mx = event.getX();
        float my = event.getY();


        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStart(mx, my);

                invalidate();
                break;

            case MotionEvent.ACTION_MOVE:
                touchMove(mx, my);


                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touchUp(mx, my);
                invalidate();
                break;
        }
        return true;
    }


    public void setEraserMode(boolean b) {
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }
}

