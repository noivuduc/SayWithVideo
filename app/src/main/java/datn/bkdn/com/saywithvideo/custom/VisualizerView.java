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
    private float mStartPosition;
    private float mEndPosition;
    private boolean mIsStop;

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

    public void setStartPosition(float startPosition) {
        this.mStartPosition = startPosition * getWidth() / mDuration;
    }

    public void setEndPosition(float endPosition) {
        this.mEndPosition = endPosition * getWidth() / mDuration;
    }

    private void init() {
        mForePaint.setStrokeWidth(1f);
        mForePaint.setAntiAlias(true);
        mForePaint.setColor(Color.rgb(0, 128, 255));

        mPaint.setStrokeWidth(3f);
        mPaint.setColor(Color.RED);

        mClearPaint.setColor(Color.TRANSPARENT);
        mEndPosition = -1;

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

        if (mIsStop || (mEndPosition != -1 && mCurrentPosition > mEndPosition)) {
            canvas.drawRect(getLeft(), getTop(), getWidth(), getHeight(), mClearPaint);
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
