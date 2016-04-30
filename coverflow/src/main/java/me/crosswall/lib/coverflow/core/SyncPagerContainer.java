package me.crosswall.lib.coverflow.core;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import me.crosswall.lib.coverflow.core.syncpager.SyncViewPgaer;

/**
 * PagerContainer: A layout that displays a ViewPager with its children that are outside
 * the typical pager bounds.
 * @link(https://gist.github.com/devunwired/8cbe094bb7a783e37ad1)
 */
public class SyncPagerContainer extends FrameLayout implements SyncViewPgaer.OnPageChangeListener {

    private SyncViewPgaer mPager;
    boolean mNeedsRedraw = false;

    public SyncPagerContainer(Context context) {
        super(context);
        init();
    }

    public SyncPagerContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SyncPagerContainer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        //Disable clipping of children so non-selected pages are visible
        setClipChildren(false);

        //Child clipping doesn't work with hardware acceleration in Android 3.x/4.x
        //You need to set this value here if using hardware acceleration in an
        // application targeted at these releases.
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }


    @SuppressLint("MissingSuperCall")
    @Override
    protected void onFinishInflate() {
        try {
            mPager = (SyncViewPgaer) getChildAt(0);
            mPager.addOnPageChangeListener(this);
        } catch (Exception e) {
            throw new IllegalStateException("The root child of PagerContainer must be a ViewPager");
        }
    }

    public SyncViewPgaer getViewPager() {
        return mPager;
    }

    private Point mCenter = new Point();
    private Point mInitialTouch = new Point();
    private Point bindingTouch = new Point();



    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        Log.d("@@@","w:"+w + "\n" + "h: "+ h);
        mCenter.x = w / 2;
        mCenter.y = h / 2;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        //We capture any touches not already handled by the ViewPager
        // to implement scrolling from a touch outside the pager bounds.
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mInitialTouch.x = (int)ev.getX();
                mInitialTouch.y = (int)ev.getY();
            default:
                float deltaX = mCenter.x - mInitialTouch.x;
                float deltaY = mCenter.y - mInitialTouch.y;
                ev.offsetLocation(deltaX, deltaY);
                break;
        }


        return mPager.dispatchTouchEvent(ev);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        //Force the container to redraw on scrolling.
        //Without this the outer pages render initially and then stay static
        if (mNeedsRedraw) invalidate();
    }

    @Override
    public void onPageSelected(int position) { }

    @Override
    public void onPageScrollStateChanged(int state) {
        mNeedsRedraw = (state != SyncViewPgaer.SCROLL_STATE_IDLE);
    }
}
