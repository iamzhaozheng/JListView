package com.hisrv.jlistview;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hisrv.lib.jlistview.JListView;
import com.hisrv.lib.jlistview.JListView.FooterState;
import com.hisrv.lib.jlistview.JListView.HeaderState;
import com.hisrv.lib.jlistview.JListView.OnFooterListener;
import com.hisrv.lib.jlistview.JListView.OnHeaderListener;

public class MainActivity extends Activity implements OnHeaderListener,
		OnFooterListener {

	private final int ROTATE_ANIM_DURATION = 180;
	private JListView mJListView;
	private ArrayAdapter<String> mAdapter;
	private ImageView mArrowImageView;
	private ProgressBar mHeaderPBar;
	private TextView mHeaderText;
	private Animation mRotateUpAnim;
	private Animation mRotateDownAnim;
	private TextView mFooterText;
	private ProgressBar mFooterPBar;
	private Handler mHandler = new Handler();
	private int mStart = 100;
	private int mEnd = 103;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mJListView = (JListView) findViewById(R.id.jlist);
		mJListView.setFooterView(R.layout.footer);
		mJListView.setHeaderView(R.layout.header);
		mJListView.setOnHeaderListener(this);
		mJListView.setOnFooterListener(this);
		mAdapter = new ArrayAdapter<String>(this, R.layout.item,
				generateList(mStart, mEnd));
		mJListView.setAdapter(mAdapter);
		initHeaderViews();
		initFooterViews();
	}
	
	private void initFooterViews() {
		mFooterText = (TextView) findViewById(R.id.footer_text);
		mFooterPBar = (ProgressBar) findViewById(R.id.footer_pbar); 
	}
	
	private void initHeaderViews() {
		mArrowImageView = (ImageView) findViewById(R.id.arrow);
		mHeaderText = (TextView) findViewById(R.id.header_text);
		mHeaderPBar = (ProgressBar) findViewById(R.id.header_pbar);
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

	private List<String> generateList(int from, int end) {
		List<String> ret = new ArrayList<String>();
		for (int i = from; i <= end; i++) {
			ret.add("ITEM " + i);
		}
		return ret;
	}
	
	@Override
	public void onFooterStateChange(FooterState state) {
		switch (state) {
		case NORMAL:
			mFooterPBar.setVisibility(View.GONE);
			mFooterText.setText(R.string.load_more);
			break;
		case LOADING:
			mFooterPBar.setVisibility(View.VISIBLE);
			mFooterText.setText("");
			mHandler.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					for (int i = mEnd + 1; i <= mEnd + 10; i ++) {
						mAdapter.add("ITEM" + i);
					}
					mEnd += 10;
					mJListView.stopLoadMore();
				}
			}, 2000);
			break;
		case READY:
			mFooterPBar.setVisibility(View.GONE);
			mFooterText.setText(R.string.load_more_release);
			break;
		}
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
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					mStart = mStart - 10;
					mAdapter = new ArrayAdapter<String>(MainActivity.this, R.layout.item,
							generateList(mStart, mEnd));
					mJListView.setAdapter(mAdapter);
					mJListView.stopRefresh();
				}
			}, 2000);
			break;
		}
	}
}
