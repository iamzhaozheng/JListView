package com.hisrv.jlistview;

import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hisrv.lib.jlistview.JListView;
import com.hisrv.lib.jlistview.JListView.FooterState;
import com.hisrv.lib.jlistview.JListView.OnFooterListener;

public class FooterController implements OnFooterListener {

	private TextView mFooterText;
	private ProgressBar mFooterPBar;
	private JListView mJListView;
	
	private OnLoadMoreListener mOnLoadMoreListener;

	public FooterController(JListView listView, int resFooter) {
		mJListView = listView;
		mJListView.setFooterView(R.layout.footer);
		mJListView.setOnFooterListener(this);
		initViews(mJListView);
	}
	
	private void initViews(View v) {
		mFooterText = (TextView) v.findViewById(R.id.footer_text);
		mFooterPBar = (ProgressBar) v.findViewById(R.id.footer_pbar); 
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
			if (mOnLoadMoreListener != null) {
				mOnLoadMoreListener.onLoadMore();
			}
			break;
		case READY:
			mFooterPBar.setVisibility(View.GONE);
			mFooterText.setText(R.string.load_more_release);
			break;
		}
	}
	
	public void done() {
		mJListView.stopLoadMore();
	}
	
	public void setOnLoadMoreListener(OnLoadMoreListener l) {
		mOnLoadMoreListener = l;
	}
	
	public interface OnLoadMoreListener {
		public void onLoadMore();
	}

}
