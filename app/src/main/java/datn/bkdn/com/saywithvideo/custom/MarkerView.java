package datn.bkdn.com.saywithvideo.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

public class MarkerView extends ImageView {

    public interface CustomListener {
        void markerDraw();

        void markerTouchStart(MarkerView customImageView);

        void markerTouchEnd(MarkerView customImageView, float x);

        void markerMove(MarkerView customImageView, float x);
    }

    private CustomListener mListener;

    public void setListener(CustomListener listener) {
        this.mListener = listener;
    }

    public MarkerView(Context context) {
        super(context);
    }

    public MarkerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MarkerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                requestFocus();
                mListener.markerTouchStart(this);
                break;
            case MotionEvent.ACTION_MOVE:
                mListener.markerMove(this, event.getRawX());
                break;
            case MotionEvent.ACTION_UP:
                mListener.markerTouchEnd(this, event.getRawX());
                break;
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mListener != null)
            mListener.markerDraw();
    }
}
