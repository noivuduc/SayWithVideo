package datn.bkdn.com.saywithvideo.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class VisualizerView extends View {

    private byte[] mBytes;
    private float[] mPoints;
    private Rect mRect = new Rect();
    private Paint mForePaint = new Paint();
    private Paint mPaint = new Paint();
    private Paint mClearPaint = new Paint();
    private int mDuration;
    private float mCurrentPosition;
    private boolean mIsStop;
    private int mStartPosition;

    public VisualizerView(Context context) {
        super(context);
        init();
    }

    public VisualizerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VisualizerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mForePaint.setStrokeWidth(1f);
        mForePaint.setAntiAlias(true);
        mForePaint.setColor(Color.rgb(0, 128, 255));

        mPaint.setStrokeWidth(3f);
        mPaint.setColor(Color.RED);

        mClearPaint.setColor(Color.WHITE);

        start();
    }

    public void reset() {
        mIsStop = true;
        invalidate();
    }

    public void start() {
        mBytes = null;
        mCurrentPosition = 0;
        mDuration = 0;
        mIsStop = false;
    }

    public void setStartPosition(int mStartPosition) {
        this.mStartPosition = mStartPosition * getWidth() / mDuration;
        invalidate();
    }

    public void setDuration(int duration) {
        mDuration = duration;
    }

    public void updateVisualizer(byte[] bytes, int mCurrentPosition) {
        this.mCurrentPosition = mCurrentPosition * getWidth() / mDuration;
        mBytes = bytes;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mIsStop) {
            canvas.drawRect(getLeft(), getTop(), getWidth(), getHeight(), mClearPaint);
            mIsStop = false;
            mCurrentPosition = 0;
            return;
        }
        if (mBytes == null) {
            return;
        }
        if (mPoints == null || mPoints.length < mBytes.length * 4) {
            mPoints = new float[mBytes.length * 4];
        }
        canvas.drawRect(mStartPosition, getTop(), mCurrentPosition, getHeight(), mPaint);
        mRect.set(0, 0, getWidth(), getHeight());
        for (int i = 0; i < mBytes.length - 1; i++) {
            mPoints[i * 4] = mRect.width() * i / (mBytes.length - 1);
            mPoints[i * 4 + 1] = mRect.height() / 2
                    + ((byte) (mBytes[i] + 128)) * (mRect.height() / 2) / 128;
            mPoints[i * 4 + 2] = mRect.width() * (i + 1) / (mBytes.length - 1);
            mPoints[i * 4 + 3] = mRect.height() / 2
                    + ((byte) (mBytes[i + 1] + 128)) * (mRect.height() / 2)
                    / 128;
        }
        canvas.drawLines(mPoints, mForePaint);
    }
}
