package com.octoberry.rcbankmobile;

import org.json.JSONArray;
import org.json.JSONObject;

import com.octoberry.rcbankmobile.db.DataBaseManager;
import com.octoberry.rcbankmobile.net.AsyncJSONLoader;
import com.octoberry.rcbankmobile.net.JSONResponseListener;

import android.graphics.Color;
import android.os.Bundle;
import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class TimelineActivity extends Activity {

	private PaymentListHandler mPaymentListHandler = new PaymentListHandler();
	private String mToken;
	private HistoryListAdapter mHistoryListAdapter;
	private HistoryListValue[] mHistoryListValues;
	private ListView mHistoryListView;
	private ImageView mCloseImageView;
	private ProgressBar mProgressBar;
    private ImageView mFilterImageView;
    private EditText mFilterEditText;
    private LinearLayout mFilterLayout;
    private ImageView mFilterCloseImageView;
    private TextView mAllTextView;
    private TextView mWeekTextView;
    private TextView mMonthTextView;
    private TextView mHalfYearTextView;

	private ObjectAnimator mAppearAnimator;
	private ObjectAnimator mDisappearAnimator;
	private RelativeLayout mRootLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_timeline);

		mRootLayout = (RelativeLayout) findViewById(R.id.rootRelativeLayout);

		mHistoryListView = (ListView) findViewById(R.id.historyListView);
		mCloseImageView = (ImageView) findViewById(R.id.closeImageView);
		mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mFilterImageView = (ImageView) findViewById(R.id.filterImageView);
        mFilterEditText = (EditText) findViewById(R.id.filterEditText);
        mFilterLayout = (LinearLayout) findViewById(R.id.filterLayout);
        mFilterCloseImageView = (ImageView) findViewById(R.id.filterCloseImageView);
        mAllTextView = (TextView) findViewById(R.id.allTextView);
        mWeekTextView = (TextView) findViewById(R.id.weekTextView);
        mMonthTextView = (TextView) findViewById(R.id.monthTextView);
        mHalfYearTextView = (TextView) findViewById(R.id.halfYearTextView);

		mCloseImageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				closeActivity();
			}
		});

        mFilterImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mFilterLayout.setVisibility(View.VISIBLE);
            }
        });

        mFilterCloseImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mFilterLayout.setVisibility(View.INVISIBLE);
            }
        });

        mAllTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                clearSelection();
                mAllTextView.setTextColor(Color.WHITE);
            }
        });
        mWeekTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                clearSelection();
                mWeekTextView.setTextColor(Color.WHITE);
            }
        });
        mMonthTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                clearSelection();
                mMonthTextView.setTextColor(Color.WHITE);
            }
        });
        mHalfYearTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                clearSelection();
                mHalfYearTextView.setTextColor(Color.WHITE);
            }
        });

		mToken = DataBaseManager.getInstance(getApplicationContext())
				.getActiveToken();
		mProgressBar.setVisibility(View.VISIBLE);

		AsyncJSONLoader paymentLoader = new AsyncJSONLoader(this);
		paymentLoader.registryListener(mPaymentListHandler);
		Bundle params2 = new Bundle();
		params2.putString("requestType", "GET");
		params2.putString("endpoint",
				"/api/bank/timeline?date_to=2015-11-13&date_from=2014-11-01");
		Bundle headerParams2 = new Bundle();
		headerParams2.putString("Authorization", mToken);
		paymentLoader.execute(params2, headerParams2, null);
	}

    private void clearSelection() {
        mAllTextView.setTextColor(Color.parseColor("#999999"));
        mWeekTextView.setTextColor(Color.parseColor("#999999"));
        mMonthTextView.setTextColor(Color.parseColor("#999999"));
        mHalfYearTextView.setTextColor(Color.parseColor("#999999"));
    }

	@Override
	protected void onStart() {
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int height = size.y;
		mRootLayout.setTranslationY(height);

		mAppearAnimator = ObjectAnimator.ofFloat(mRootLayout, "translationY",
				height, 0);
		mAppearAnimator.setDuration(1000);
		mDisappearAnimator = ObjectAnimator.ofFloat(mRootLayout,
				"translationY", 0, height);
		mDisappearAnimator.setDuration(1000);

		super.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mAppearAnimator.start();
	}

	@Override
	public void onBackPressed() {
		closeActivity();
	}

	private void closeActivity() {
		mDisappearAnimator.addListener(new AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animation) {
			}

			@Override
			public void onAnimationRepeat(Animator animation) {
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				finish();
			}

			@Override
			public void onAnimationCancel(Animator animation) {
			}
		});
		mDisappearAnimator.start();
	}

	class PaymentListHandler implements JSONResponseListener {
		@Override
		public void handleResponse(int result, JSONObject response, String error) {
			mProgressBar.setVisibility(View.GONE);
			if (response != null) {
				Log.d("###", response.toString());
				try {
					int resultCode = response.getInt("status");

					if (resultCode == 200) {
						JSONArray paymentListArray = response.getJSONArray("result");

						mHistoryListValues = new HistoryListValue[paymentListArray
								.length()];
						for (int i = 0; i < paymentListArray.length(); i++) {
							JSONObject item = paymentListArray.getJSONObject(i);
							String number = item.getString("amount");
							String date = item.getString("date");
							String name = item.getString("corr_name");
							String description = item.getString("description");
							HistoryListValue listItem = new HistoryListValue(number, date, name, description);
							mHistoryListValues[i] = listItem;
						}
						if (mHistoryListAdapter == null) {
							mHistoryListAdapter = new HistoryListAdapter(
									getApplicationContext());
						}
						mHistoryListAdapter
								.setHistoryValues(mHistoryListValues);
						mHistoryListView.setAdapter(mHistoryListAdapter);
					} else {
						Toast.makeText(getApplicationContext(),
								response.getString("message"),
								Toast.LENGTH_LONG).show();
					}
				} catch (Exception exc) {
					exc.printStackTrace();
				}
			}

		}
	}

	private class HistoryListValue {
		private String number;
		private String date;
		private String name;
		private String description;

		public HistoryListValue(String numberValue, String dateValue,
				String nameValue, String descriptionValue) {
			number = numberValue;
			date = dateValue;
			name = nameValue;
			description = descriptionValue;
		}

		String getNumber() {
			return number;
		}

		String getDate() {
			return date;
		}

		String getName() {
			return name;
		}

		String getDescriptin() {
			return description;
		}
	}

	private class HistoryListAdapter extends ArrayAdapter<Object> {
		private final Context context;
		private HistoryListValue[] historyValues;

		public HistoryListAdapter(Context context) {
			super(context, R.layout.timeline_list_row);
			this.context = context;
		}

		public void setHistoryValues(HistoryListValue[] historyValues) {
			this.historyValues = historyValues;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			View rowView;
			if (convertView == null) {
				rowView = inflater.inflate(R.layout.timeline_list_row, parent,
						false);
			} else {
				rowView = convertView;
			}
			TextView numberTextView = (TextView) rowView
					.findViewById(R.id.numberTextView);
			TextView dateTextView = (TextView) rowView
					.findViewById(R.id.dateTextView);
			TextView nameTextView = (TextView) rowView
					.findViewById(R.id.nameTextView);
			TextView descriptionTextView = (TextView) rowView
					.findViewById(R.id.descriptionTextView);

			numberTextView.setText(historyValues[position].getNumber());
			dateTextView.setText(historyValues[position].getDate());
			nameTextView.setText(historyValues[position].getName());
			descriptionTextView.setText(historyValues[position].getDescriptin());
			return rowView;
		}

		@Override
		public int getCount() {
			if (historyValues != null) {
				return historyValues.length;
			}
			return 0;
		}
	}
}
