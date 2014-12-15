package com.octoberry.rcbankmobile;

import org.json.JSONArray;
import org.json.JSONObject;

import com.octoberry.rcbankmobile.chat.ChatActivity;
import com.octoberry.rcbankmobile.db.DataBaseManager;
import com.octoberry.rcbankmobile.net.AsyncJSONLoader;
import com.octoberry.rcbankmobile.net.JSONResponseListener;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;

public class TimelineActivity extends Activity {

	private PaymentListHandler mPaymentListHandler = new PaymentListHandler();
	private String mToken;
	private TimelineListAdapter mHistoryListAdapter;
	private TimelineListValue[] mTimelineListValues;
    private TimelineListValue[] mFilteredListValues;
	private ListView mHistoryListView;
	private ImageView mCloseImageView;
	private ProgressBar mProgressBar;
    private ImageView mFilterImageView;
    private EditText mFilterEditText;
    private LinearLayout mFilterLayout;
    private ImageView mFilterCloseImageView;
    private ImageView mChatImageView;
    private TextView mAllTextView;
    private TextView mWeekTextView;
    private TextView mMonthTextView;
    private TextView mHalfYearTextView;
    private Timer mTimer = null;

	private ObjectAnimator mAppearAnimator;
	private ObjectAnimator mDisappearAnimator;
	private RelativeLayout mRootLayout;

    private final static int ENTER_LOCK_TIMER_DELAY = 1000; // 1 sec

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
        mChatImageView = (ImageView) findViewById(R.id.chatImageView);

		mCloseImageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				closeActivity();
			}
		});

        mFilterImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mFilterEditText.setText("");
                mFilterLayout.setVisibility(View.VISIBLE);
            }
        });

        mFilterCloseImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mFilterLayout.setVisibility(View.INVISIBLE);
                mFilteredListValues = new TimelineListValue[mTimelineListValues.length];
                for (int index = 0; index < mTimelineListValues.length; index++) {
                    mFilteredListValues[index] = mTimelineListValues[index];
                }
                mHistoryListAdapter.setTimelineValues(mFilteredListValues);
                mHistoryListAdapter.notifyDataSetChanged();
            }
        });

        mAllTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                clearSelection();
                mAllTextView.setTextColor(Color.WHITE);
                updateDateFilter();
            }
        });
        mWeekTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                clearSelection();
                mWeekTextView.setTextColor(Color.WHITE);
                updateDateFilter();
            }
        });
        mMonthTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                clearSelection();
                mMonthTextView.setTextColor(Color.WHITE);
                updateDateFilter();
            }
        });
        mHalfYearTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                clearSelection();
                mHalfYearTextView.setTextColor(Color.WHITE);
                updateDateFilter();
            }
        });

        mFilterEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(final CharSequence charSequence, int i, int i2, int i3) {
                if (mTimer != null) {
                    mTimer.cancel();
                    mTimer.purge();
                }

                if (charSequence.length() > 0) {
                    final String filter = charSequence.toString().toUpperCase();
                    mTimer = new Timer();
                    mTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    mFilterEditText.setEnabled(false);
                                    mProgressBar.setVisibility(View.VISIBLE);
                                }
                            });
                            if (mTimelineListValues != null) {
                                ArrayList<TimelineListValue> list = new ArrayList<TimelineListValue>();
                                for (TimelineListValue value : mTimelineListValues) {
                                    if (((value.getName() != null) && (value.getName().toUpperCase().contains(filter))) ||
                                            ((value.getDescriptin() != null) && (value.getDescriptin().toUpperCase().contains(filter)))) {
                                        list.add(value);
                                    }
                                }
                                mFilteredListValues = new TimelineListValue[list.size()];
                                for (int index = 0; index < list.size(); index++) {
                                    mFilteredListValues[index] = list.get(index);
                                }
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        mHistoryListAdapter.setTimelineValues(mFilteredListValues);
                                        mHistoryListAdapter.notifyDataSetChanged();
                                    }
                                });
                            }
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    mFilterEditText.setEnabled(true);
                                    mProgressBar.setVisibility(View.GONE);
                                }
                            });
                        }
                    }, ENTER_LOCK_TIMER_DELAY);
                } else {
                    mFilteredListValues = new TimelineListValue[mTimelineListValues.length];
                    for (int index = 0; index < mTimelineListValues.length; index++) {
                        mFilteredListValues[index] = mTimelineListValues[index];
                    }
                    mHistoryListAdapter.setTimelineValues(mFilteredListValues);
                    mHistoryListAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        mChatImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TimelineActivity.this, ChatActivity.class);
                startActivity(intent);
            }
        });

        clearSelection();
        mAllTextView.setTextColor(Color.WHITE);

		mToken = DataBaseManager.getInstance(getApplicationContext()).getActiveToken();

        updateDateFilter();
	}

    private void clearSelection() {
        mAllTextView.setTextColor(Color.parseColor("#999999"));
        mWeekTextView.setTextColor(Color.parseColor("#999999"));
        mMonthTextView.setTextColor(Color.parseColor("#999999"));
        mHalfYearTextView.setTextColor(Color.parseColor("#999999"));
    }

    private void updateDateFilter() {
        Calendar currentDate = GregorianCalendar.getInstance();
        Calendar fromDate = GregorianCalendar.getInstance();
        if (mWeekTextView.getCurrentTextColor() == Color.WHITE) {
            fromDate.add(Calendar.DAY_OF_YEAR, -7);
        } else if (mMonthTextView.getCurrentTextColor() == Color.WHITE) {
            fromDate.add(Calendar.MONTH, -1);
        } else if (mHalfYearTextView.getCurrentTextColor() == Color.WHITE) {
            fromDate.add(Calendar.MONTH, -6);
        } else {
            fromDate.set(2000, 0, 1, 0, 0, 0);
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String fromDateString = dateFormat.format(fromDate.getTime());
        String toDateString = dateFormat.format(currentDate.getTime());

        mProgressBar.setVisibility(View.VISIBLE);
        AsyncJSONLoader paymentLoader = new AsyncJSONLoader(this);
        paymentLoader.registryListener(mPaymentListHandler);
        Bundle params2 = new Bundle();
        params2.putString("requestType", "GET");
        params2.putString("endpoint", String.format("/api/bank/timeline?date_to=%s&date_from=%s",
                        toDateString, fromDateString));
        Bundle headerParams2 = new Bundle();
        headerParams2.putString("Authorization", mToken);
        paymentLoader.execute(params2, headerParams2, null);
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

						mTimelineListValues = new TimelineListValue[paymentListArray.length()];
                        mFilteredListValues = new TimelineListValue[paymentListArray.length()];
						for (int i = 0; i < paymentListArray.length(); i++) {
							JSONObject item = paymentListArray.getJSONObject(i);
							String number = item.getString("amount");
							String date = item.getString("date");
							String name = item.getString("corr_name");
							String description = item.getString("description");
                            TimelineListValue listItem = new TimelineListValue(number, date, name, description);
                            mTimelineListValues[i] = listItem;
                            mFilteredListValues[i] = listItem;
						}
						if (mHistoryListAdapter == null) {
							mHistoryListAdapter = new TimelineListAdapter(getApplicationContext());
						}
						mHistoryListAdapter.setTimelineValues(mFilteredListValues);
						mHistoryListView.setAdapter(mHistoryListAdapter);
					} else if (resultCode == 403) {
                        Intent intent = new Intent(TimelineActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
						Toast.makeText(getApplicationContext(), response.getString("message"), Toast.LENGTH_LONG).show();
					}
				} catch (Exception exc) {
					exc.printStackTrace();
				}
			}

		}
	}

	private class TimelineListValue {
		private String number;
		private String date;
		private String name;
		private String description;

		public TimelineListValue(String numberValue, String dateValue,
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

	private class TimelineListAdapter extends ArrayAdapter<Object> {
		private final Context context;
		private TimelineListValue[] timelineValues;

		public TimelineListAdapter(Context context) {
			super(context, R.layout.timeline_list_row);
			this.context = context;
		}

		public void setTimelineValues(TimelineListValue[] timelineValues) {
			this.timelineValues = timelineValues;
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

			numberTextView.setText(timelineValues[position].getNumber());
			dateTextView.setText(timelineValues[position].getDate());
			nameTextView.setText(timelineValues[position].getName());
			descriptionTextView.setText(timelineValues[position].getDescriptin());
			return rowView;
		}

		@Override
		public int getCount() {
			if (timelineValues != null) {
				return timelineValues.length;
			}
			return 0;
		}
	}
}
