package com.hisrv.jlistview;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hisrv.lib.jlistview.JListView;
import com.hisrv.lib.jlistview.JListView.HeaderState;
import com.hisrv.lib.jlistview.JListView.OnHeaderListener;

public class HeaderController implements OnHeaderListener {

	private final int ROTATE_ANIM_DURATION = 180;
	private ImageView mArrowImageView;
	private ProgressBar mHeaderPBar;
	private TextView mHeaderText;
	private Animation mRotateUpAnim;
	private Animation mRotateDownAnim;
	
	private OnRefreshListener mOnRefreshListener;
	
	private JListView mJListView;

	public HeaderController(JListView listView, int resHeader) {
		mJListView = listView;
		mJListView.setHeaderView(R.layout.header);
		mJListView.setOnHeaderListener(this);
		initViews(mJListView);
	}
	
	private void initViews(View v) {
		mArrowImageView = (ImageView) v.findViewById(R.id.arrow);
		mHeaderText = (TextView) v.findViewById(R.id.header_text);
		mHeaderPBar = (ProgressBar) v.findViewById(R.id.header_pbar);
		mRotateUpAnim = new RotateAnimation(0.0f, -180.0f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		mRotateUpAnim.setDuration(ROTATE_ANIM_DURATION);
		mRotateUpAnim.setFillAfter(true);
		mRotateDownAnim = new RotateAnimation(-180.0f, 0.0f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		mRotateDownAnim.setDuration(ROTATE_ANIM_DURATION);
		mRotateDownAnim.setFillAfter(true);
	}

	@Override
	public void onHeaderStateChange(HeaderState now, HeaderState prev) {
		if (now == HeaderState.REFRESHING) {
			mArrowImageView.clearAnimation();
			mArrowImageView.setVisibility(View.GONE);
			mHeaderPBar.setVisibility(View.VISIBLE);
		} else {
			mArrowImageView.setVisibility(View.VISIBLE);
			mHeaderPBar.setVisibility(View.GONE);
		}

		switch (now) {
		case NORMAL:
			if (prev == HeaderState.READY) {
				mArrowImageView.startAnimation(mRotateDownAnim);
			}
			if (prev == HeaderState.REFRESHING) {
				mArrowImageView.clearAnimation();
			}
			mHeaderText.setText(R.string.refresh);
			break;
		case READY:
			mArrowImageView.clearAnimation();
			mArrowImageView.startAnimation(mRotateUpAnim);
			mHeaderText.setText(R.string.refresh_release);
			break;
		case REFRESHING:
			mHeaderText.setText(R.string.refreshing);
			if (mOnRefreshListener != null) {
				mOnRefreshListener.onRefresh();
			}
			break;
		}
	}
	
	public void done() {
		mJListView.stopRefresh();
	}
	
	public void setOnRefreshListener(OnRefreshListener l) {
		mOnRefreshListener = l;
	}
	
	public interface OnRefreshListener {
		public void onRefresh();
	}

}
