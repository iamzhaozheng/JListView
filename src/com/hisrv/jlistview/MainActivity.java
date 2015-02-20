package com.hisrv.jlistview;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ArrayAdapter;

import com.hisrv.jlistview.FooterController.OnLoadMoreListener;
import com.hisrv.jlistview.HeaderController.OnRefreshListener;
import com.hisrv.lib.jlistview.JListView;

public class MainActivity extends Activity implements OnRefreshListener, OnLoadMoreListener {

	private JListView mJListView;
	private ArrayAdapter<String> mAdapter;
	private Handler mHandler = new Handler();
	private int mStart = 100;
	private int mEnd = 103;
	
	private HeaderController mHeaderController;
	private FooterController mFooterController;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mJListView = (JListView) findViewById(R.id.jlist);
		mHeaderController = new HeaderController(mJListView, R.layout.header);
		mFooterController = new FooterController(mJListView, R.layout.footer);
		mHeaderController.setOnRefreshListener(this);
		mFooterController.setOnLoadMoreListener(this);
		mAdapter = new ArrayAdapter<String>(this, R.layout.item,
				generateList(mStart, mEnd));
		mJListView.setAdapter(mAdapter);
	}
	
	private List<String> generateList(int from, int end) {
		List<String> ret = new ArrayList<String>();
		for (int i = from; i <= end; i++) {
			ret.add("ITEM " + i);
		}
		return ret;
	}
	

	@Override
	public void onRefresh() {
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				mStart = mStart - 10;
				mAdapter = new ArrayAdapter<String>(MainActivity.this,
						R.layout.item, generateList(mStart, mEnd));
				mJListView.setAdapter(mAdapter);
				mHeaderController.done();
			}
		}, 2000);
		
	}

	@Override
	public void onLoadMore() {
		mHandler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				for (int i = mEnd + 1; i <= mEnd + 10; i ++) {
					mAdapter.add("ITEM" + i);
				}
				mEnd += 10;
				mFooterController.done();
			}
		}, 2000);
		
	}

}
