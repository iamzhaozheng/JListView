package com.hisrv.lib.jlistview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Scroller;

public class JListView extends ListView implements OnScrollListener {

	public static enum FooterState {
		NORMAL, READY, LOADING
	};

	public static enum HeaderState {
		NORMAL, READY, REFRESHING
	}

	private final static int SCROLLBACK_HEADER = 0;
	private final static int SCROLLBACK_FOOTER = 1;
	private final static int SCROLL_DURATION = 400;
	private final static int PULL_LOAD_MORE_DELTA = 50; 
	private final static float OFFSET_RADIO = 1.8f;

	private float mLastY = -1; 
	private int mTotalItemCount;
	private int mScrollBack;

	private Scroller mScroller;
	private View mFooterView;
	private LinearLayout mHeaderFacade, mHeaderContainer, mFooterFacade;
	private boolean mHeaderEnabled, mFooterEnabled;
	private boolean mIsFooterReady = false;
	private boolean mPullRefreshing, mPullLoading;
	private int mHeaderViewHeight;

	private OnScrollListener mScrollListener; 
	private OnFooterListener mOnFooterListener;
	private OnHeaderListener mOnHeaderListener;
	private HeaderState mHeaderState = HeaderState.NORMAL;

	public JListView(Context context) {
		super(context);
		init(context);
	}

	public JListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public JListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		mScroller = new Scroller(context, new DecelerateInterpolator());
		super.setOnScrollListener(this);
		mFooterEnabled = false;
		mFooterFacade = new LinearLayout(context);
		mHeaderEnabled = false;
		mHeaderFacade = new LinearLayout(context);
		mHeaderFacade.setOrientation(LinearLayout.VERTICAL);
		mHeaderFacade.setGravity(Gravity.CENTER_HORIZONTAL);
		mHeaderContainer = new LinearLayout(context);
		mHeaderContainer.setContentDescription("header_container");
		mHeaderFacade.addView(mHeaderContainer);
		addHeaderView(mHeaderFacade);
	}

	/**
	 * @param resHeader
	 *            the header view resource id for pulling to refresh, 0 will disable the
	 *            refresh feature
	 */
	public void setHeaderView(int resHeader) {
		if (resHeader == 0) {
			mHeaderEnabled = false;
		} else {
			LayoutInflater.from(getContext()).inflate(resHeader, mHeaderContainer, true);
			mHeaderEnabled = true;
			mHeaderContainer.getViewTreeObserver().addOnGlobalLayoutListener(
					new OnGlobalLayoutListener() {
						@SuppressWarnings("deprecation")
						@Override
						public void onGlobalLayout() {
							mHeaderViewHeight = mHeaderContainer.getHeight();
							getViewTreeObserver().removeGlobalOnLayoutListener(
									this);
							setHeaderVisibleHeight(0);
						}
					});
		}
	}

	/**
	 * call this before setAdapter
	 * 
	 * @param resFooter
	 *            the footer view resource id for loading more, 0 will disable the
	 *            loading more feature
	 */
	public void setFooterView(int resFooter) {
		if (resFooter == 0) {
			mFooterEnabled = false;
		} else {
			mFooterEnabled = true;
			mFooterView = LayoutInflater.from(getContext()).inflate(resFooter, this, false);
			mFooterFacade.addView(mFooterView);
			mFooterView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					startLoadMore();
				}
			});
		}
		setFooterDividersEnabled(mFooterEnabled);
	}

	/**
	 * stop refresh, reset header view.
	 */
	public void stopRefresh() {
		if (mPullRefreshing == true) {
			mPullRefreshing = false;
			resetHeaderHeight();
		}
	}

	/**
	 * stop load more, reset footer view.
	 */
	public void stopLoadMore() {
		if (mPullLoading == true) {
			mPullLoading = false;
			changeFooterState(FooterState.NORMAL);
		}
	}

	private void changeFooterState(FooterState state) {
		if (mOnFooterListener != null) {
			mOnFooterListener.onFooterStateChange(state);
		}
	}

	private void changeHeaderState(HeaderState state) {
		if (state == mHeaderState) {
			return;
		}
		if (mOnHeaderListener != null) {
			mOnHeaderListener.onHeaderStateChange(state, mHeaderState);
			mHeaderState = state;
		}
	}

	private void updateHeaderHeight(float delta) {
		setHeaderVisibleHeight((int) delta + mHeaderContainer.getHeight());
		if (mHeaderEnabled && !mPullRefreshing) {
			if (mHeaderContainer.getHeight() > mHeaderViewHeight) {
				changeHeaderState(HeaderState.READY);
			} else {
				changeHeaderState(HeaderState.NORMAL);
			}
		}
		setSelection(0);
	}

	private void setHeaderVisibleHeight(int height) {
		if (height < 0) {
			height = 0;
		}
		ViewGroup.LayoutParams lp = mHeaderContainer
				.getLayoutParams();
		lp.height = height;
		mHeaderContainer.setLayoutParams(lp);
	}

	private void resetHeaderHeight() {
		int height = mHeaderContainer.getHeight();
		if (height == 0)
			return;
		if (mPullRefreshing && height <= mHeaderViewHeight) {
			return;
		}
		int finalHeight = 0;
		if (mPullRefreshing && height > mHeaderViewHeight) {
			finalHeight = mHeaderViewHeight;
		}
		mScrollBack = SCROLLBACK_HEADER;
		mScroller.startScroll(0, height, 0, finalHeight - height,
				SCROLL_DURATION);
		invalidate();
	}

	private int getFooterBottomMargin() {
		return ((LinearLayout.LayoutParams) mFooterView.getLayoutParams()).bottomMargin;
	}

	private void setFooterBottomMargin(int height) {
		if (height < 0)
			return;
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mFooterView
				.getLayoutParams();
		lp.bottomMargin = height;
		mFooterView.setLayoutParams(lp);
	}

	private void updateFooterHeight(float delta) {
		int height = getFooterBottomMargin() + (int) delta;
		if (mFooterEnabled && !mPullLoading) {
			if (height > PULL_LOAD_MORE_DELTA) {
				changeFooterState(FooterState.READY);
			} else {
				changeFooterState(FooterState.NORMAL);
			}
		}
		setFooterBottomMargin(height);

	}

	private void resetFooterHeight() {
		int bottomMargin = getFooterBottomMargin();
		if (bottomMargin > 0) {
			mScrollBack = SCROLLBACK_FOOTER;
			mScroller.startScroll(0, bottomMargin, 0, -bottomMargin,
					SCROLL_DURATION);
			invalidate();
		}
	}

	private void startLoadMore() {
		mPullLoading = true;
		changeFooterState(FooterState.LOADING);
	}

	public void setOnFooterListener(OnFooterListener l) {
		mOnFooterListener = l;
	}

	public void setOnHeaderListener(OnHeaderListener l) {
		mOnHeaderListener = l;
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (mLastY == -1) {
			mLastY = ev.getRawY();
		}

		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mLastY = ev.getRawY();
			break;
		case MotionEvent.ACTION_MOVE:
			final float deltaY = ev.getRawY() - mLastY;
			mLastY = ev.getRawY();
			if (getFirstVisiblePosition() == 0
					&& (mHeaderContainer.getHeight() > 0 || deltaY > 0)) {
				updateHeaderHeight(deltaY / OFFSET_RADIO);
			} else if (getLastVisiblePosition() == mTotalItemCount - 1
					&& (getFooterBottomMargin() > 0 || deltaY < 0)) {
				if (getFirstVisiblePosition() > 0) {
					updateFooterHeight(-deltaY / OFFSET_RADIO);
				}
			}
			break;
		default:
			mLastY = -1;
			if (getFirstVisiblePosition() == 0) {
				if (mHeaderEnabled
						&& mHeaderContainer.getHeight() > mHeaderViewHeight) {
					mPullRefreshing = true;
					changeHeaderState(HeaderState.REFRESHING);
				}
				resetHeaderHeight();
			} else if (getLastVisiblePosition() == mTotalItemCount - 1) {
				if (mFooterEnabled
						&& getFooterBottomMargin() > PULL_LOAD_MORE_DELTA
						&& !mPullLoading) {
					startLoadMore();
				}
				resetFooterHeight();
			}
			break;
		}
		return super.onTouchEvent(ev);
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		if (!mIsFooterReady && mFooterEnabled) {
			mIsFooterReady = true;
			addFooterView(mFooterFacade);
		}
		super.setAdapter(adapter);
	}

	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			if (mScrollBack == SCROLLBACK_HEADER) {
				setHeaderVisibleHeight(mScroller.getCurrY());
			} else {
				setFooterBottomMargin(mScroller.getCurrY());
			}
			postInvalidate();
		}
		super.computeScroll();
	}

	@Override
	public void setOnScrollListener(OnScrollListener l) {
		mScrollListener = l;
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		mTotalItemCount = totalItemCount;
		if (mScrollListener != null) {
			mScrollListener.onScroll(view, firstVisibleItem, visibleItemCount,
					totalItemCount);
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (mScrollListener != null) {
			mScrollListener.onScrollStateChanged(view, scrollState);
		}
	}

	public interface OnFooterListener {
		public void onFooterStateChange(FooterState state);
	}

	public interface OnHeaderListener {
		public void onHeaderStateChange(HeaderState now, HeaderState prev);
	}
}
