package com.octoberry.rcbankmobile;

import org.json.JSONArray;
import org.json.JSONObject;

import com.flurry.android.FlurryAgent;
import com.octoberry.rcbankmobile.chat.ChatActivity;
import com.octoberry.rcbankmobile.db.CsvManager;
import com.octoberry.rcbankmobile.db.DataBaseManager;
import com.octoberry.rcbankmobile.net.AsyncFileLoader;
import com.octoberry.rcbankmobile.net.AsyncJSONLoader;
import com.octoberry.rcbankmobile.net.FileResponseListener;
import com.octoberry.rcbankmobile.net.JSONResponseListener;
import com.octoberry.rcbankmobile.payment.Payment;
import com.octoberry.rcbankmobile.payment.PaymentDetailsActivity;
import com.octoberry.rcbankmobile.payment.PaymentReceiverActivity;
import com.octoberry.rcbankmobile.swipeview.BaseSwipeListViewListener;
import com.octoberry.rcbankmobile.swipeview.SwipeListView;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class TimelineActivity extends Activity {
    private final static String TAG = TimelineActivity.class.getName();

	private PaymentListHandler mPaymentListHandler = new PaymentListHandler();
	private String mToken;
	private TimelineListAdapter mHistoryListAdapter;
	Payment[] mTimelineListValues;
    private Payment[] mFilteredListValues;
	private SwipeListView mHistoryListView;
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
    private ImageView mCsvImageView;
    private Timer mTimer = null;

	private ObjectAnimator mAppearAnimator;
	private ObjectAnimator mDisappearAnimator;
	private RelativeLayout mRootLayout;
    private String mAccountId = "";

    private final static int ENTER_LOCK_TIMER_DELAY = 1000; // 1 sec

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_timeline);

		mRootLayout = (RelativeLayout) findViewById(R.id.rootRelativeLayout);

		mHistoryListView = (SwipeListView) findViewById(R.id.historyListView);
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
        mCsvImageView = (ImageView) findViewById(R.id.csvImageView);

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
                mFilteredListValues = new Payment[mTimelineListValues.length];
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
                                ArrayList<Payment> list = new ArrayList<Payment>();
                                for (Payment value : mTimelineListValues) {
                                    if (((value.getName() != null) && (value.getName().toUpperCase().contains(filter))) ||
                                            ((value.getDescription() != null) && (value.getDescription().toUpperCase().contains(filter)))) {
                                        list.add(value);
                                    }
                                }
                                mFilteredListValues = new Payment[list.size()];
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
                    if (mTimelineListValues != null) {
                        mFilteredListValues = new Payment[mTimelineListValues.length];
                        for (int index = 0; index < mTimelineListValues.length; index++) {
                            mFilteredListValues[index] = mTimelineListValues[index];
                        }
                        mHistoryListAdapter.setTimelineValues(mFilteredListValues);
                        mHistoryListAdapter.notifyDataSetChanged();
                    }
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

        mCsvImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                CsvManager manager = new CsvManager("timeline.csv");
                manager.writeCsvHeader(Payment.getHeaders());
                for (Payment payment: mFilteredListValues) {
                    manager.writeCsvLine(payment.getValues());
                }
                manager.saveToFile();

                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("message/rfc822");
                intent.putExtra(Intent.EXTRA_SUBJECT, "timeline");

                File f = new File(manager.getFilePath());
                if (f.exists()) {
                    intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + f.getAbsolutePath()));
                }
                try {
                    startActivity(Intent.createChooser(intent, getResources().getString(R.string.SHARE_PAYMENT_REQUEST)));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(TimelineActivity.this, "There are no email clients installed.", Toast.LENGTH_LONG).show();
                }
            }
        });

        clearSelection();
        mAllTextView.setTextColor(Color.WHITE);

		mToken = DataBaseManager.getInstance(getApplicationContext()).getBankToken();

        if (getIntent().getStringExtra("account_id") != null) {
            mAccountId = "/" + getIntent().getStringExtra("account_id");
        }

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
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String fromDateString = dateFormat.format(fromDate.getTime());
        String toDateString = dateFormat.format(currentDate.getTime());

        mProgressBar.setVisibility(View.VISIBLE);
        AsyncJSONLoader paymentLoader = new AsyncJSONLoader(this);
        paymentLoader.registryListener(mPaymentListHandler);
        Bundle params2 = new Bundle();
        params2.putString("requestType", "GET");
        params2.putString("endpoint", String.format("/api/bank/timeline%s?date_to=%s&date_from=%s",
                        mAccountId, toDateString, fromDateString));
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

						mTimelineListValues = new Payment[paymentListArray.length()];
                        mFilteredListValues = new Payment[paymentListArray.length()];
						for (int i = 0; i < paymentListArray.length(); i++) {
							JSONObject item = paymentListArray.getJSONObject(i);
                            Payment listItem = new Payment();
                            if (!item.isNull("amount")) {
                                listItem.setAmount(item.getDouble("amount"));
                            }
                            if (!item.isNull("date")) {
                                listItem.setDate(item.getString("date"));
                            }
                            if (!item.isNull("corr_name")) {
                                listItem.setCorrName(item.getString("corr_name"));
                            }
                            if (!item.isNull("description")) {
                                listItem.setDescription(item.getString("description"));
                            }
                            if (!item.isNull("bank_name")) {
                                listItem.setBankName(item.getString("bank_name"));
                            }
                            if (!item.isNull("corr_bank_bik")) {
                                listItem.setCorrBankBik(item.getString("corr_bank_bik"));
                            }
                            if (!item.isNull("id")) {
                                listItem.setId(item.getLong("id"));
                            }
                            if (!item.isNull("corr_bank_name")) {
                                listItem.setCorrBankName(item.getString("corr_bank_name"));
                            }
                            if (!item.isNull("name")) {
                                listItem.setName(item.getString("name"));
                            }
                            if (!item.isNull("opertype")) {
                                listItem.setOperType(item.getString("opertype"));
                            }
                            if (!item.isNull("inn")) {
                                listItem.setInn(item.getString("inn"));
                            }
                            if (!item.isNull("bank_place")) {
                                listItem.setBankPlace(item.getString("bank_place"));
                            }
                            if (!item.isNull("account_number")) {
                                listItem.setAccountNumber(item.getString("account_number"));
                            }
                            if (!item.isNull("account_id")) {
                                listItem.setAccountId(item.getString("account_id"));
                            }
                            if (!item.isNull("status")) {
                                listItem.setStatus(item.getString("status"));
                            }
                            if (!item.isNull("bank_corr_account")) {
                                listItem.setBankCorrAccount(item.getString("bank_corr_account"));
                            }
                            if (!item.isNull("signed")) {
                                listItem.setSigned(item.getBoolean("signed"));
                            }
                            if (!item.isNull("amount_words")) {
                                listItem.setAmountWords(item.getString("amount_words"));
                            }
                            if (!item.isNull("number")) {
                                listItem.setNumber(item.getString("number"));
                            }
                            if (!item.isNull("register_stamp")) {
                                listItem.setRegisterStamp(item.getString("register_stamp"));
                            }
                            if (!item.isNull("corr_inn")) {
                                listItem.setCorrInn(item.getString("corr_inn"));
                            }
                            if (!item.isNull("corr_cutname")) {
                                listItem.setCorrCutName(item.getString("corr_cutname"));
                            }
                            if (!item.isNull("bank_bik")) {
                                listItem.setBankBik(item.getString("bank_bik"));
                            }
                            if (!item.isNull("corr_account_number")) {
                                listItem.setCorrAccountNumber(item.getString("corr_account_number"));
                            }
                            if (!item.isNull("urgenttype")) {
                                listItem.setUrgentType(item.getString("urgenttype"));
                            }
                            if (!item.isNull("corr_bank_place")) {
                                listItem.setCorrBankPlace(item.getString("corr_bank_place"));
                            }
                            if (!item.isNull("corr_kpp")) {
                                listItem.setCorrKpp(item.getString("corr_kpp"));
                            }
                            if (!item.isNull("kpp")) {
                                listItem.setKpp(item.getString("kpp"));
                            }
                            if (!item.isNull("corr_bank_corr_account")) {
                                listItem.setCorrBankCorrAccount(item.getString("corr_bank_corr_account"));
                            }
                            if (!item.isNull("decline_stamp")) {
                                listItem.setDeclineStamp(item.getString("decline_stamp"));
                            }
                            if (!item.isNull("sendtype_caption")) {
                                listItem.setSendTypeCaption(item.getString("sendtype_caption"));
                            }
                            if (!item.isNull("nds_text")) {
                                String ndsText = item.getString("nds_text");
                                listItem.setNdsText(ndsText);
                                if (ndsText.startsWith(getResources().getString(R.string.NDS_10P))) {
                                    listItem.setNds(10);
                                } else if (ndsText.startsWith(getResources().getString(R.string.NDS_18P))) {
                                    listItem.setNds(18);
                                }
                            }

                            mTimelineListValues[i] = listItem;
                            mFilteredListValues[i] = listItem;
						}
						if (mHistoryListAdapter == null) {
							mHistoryListAdapter = new TimelineListAdapter(getApplicationContext());
						}
						mHistoryListAdapter.setTimelineValues(mFilteredListValues);
						mHistoryListView.setAdapter(mHistoryListAdapter);
                        mHistoryListAdapter.notifyDataSetChanged();
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

	private class TimelineListAdapter extends ArrayAdapter<Object> {
		private final Context context;
		private Payment[] timelineValues;

		public TimelineListAdapter(Context context) {
			super(context, R.layout.timeline_list_row);
			this.context = context;
		}

		public void setTimelineValues(Payment[] timelineValues) {
			this.timelineValues = timelineValues;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			final View rowView;
            if (convertView == null) {
                rowView = inflater.inflate(R.layout.timeline_list_row, parent, false);
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

			numberTextView.setText("" + timelineValues[position].getAmount().doubleValue());
			dateTextView.setText(timelineValues[position].getDate());
			nameTextView.setText(timelineValues[position].getCorrName());
            descriptionTextView.setText(timelineValues[position].getDescription());

            mHistoryListView.setSwipeListViewListener(new BaseSwipeListViewListener() {
                @Override
                public int onChangeSwipeMode(int position) {
                    if (timelineValues[position].getAmount() < 0) {
                        return SwipeListView.SWIPE_MODE_BOTH;
                    }
                    return SwipeListView.SWIPE_MODE_NONE;
                }

                @Override
                public void onClickFrontView(int position) {
                    Context context = TimelineActivity.this;
                    Payment.clearPreference(context);
                    timelineValues[position].addToPreference(context);
                    Intent intent = new Intent(context, PaymentDetailsActivity.class);
                    startActivity(intent);
                }
            });

            final int index = position;
            TextView repeatPaymentTextView = (TextView)rowView.findViewById(R.id.repeatTextView);
            TextView sharePaymentTextView = (TextView)rowView.findViewById(R.id.shareTextView);
            repeatPaymentTextView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    timelineValues[index].addToPreference(TimelineActivity.this);
                    Intent intent = new Intent(TimelineActivity.this, PaymentReceiverActivity.class);
                    startActivity(intent);
                    mHistoryListView.closeOpenedItems();
                }
            });
            sharePaymentTextView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    ProgressBar progressBar = (ProgressBar)rowView.findViewById(R.id.progressBar);
                    progressBar.setVisibility(View.VISIBLE);
                    File f = new File(android.os.Environment.getExternalStorageDirectory() + "/octoberry", String.format("%s.pdf", timelineValues[index].getId()));
                    AsyncFileLoader loader = new AsyncFileLoader(TimelineActivity.this, f.getAbsolutePath());
                    loader.registryListener(new PaymentPdfLoadListener(timelineValues[index], progressBar));
                    Bundle params = new Bundle();
                    params.putString("requestType", "GET");
                    params.putString("endpoint", String.format("/api/bank/payment/%s.pdf", timelineValues[index].getId()));
                    Bundle headerParams = new Bundle();
                    headerParams.putString("Authorization", mToken);
                    headerParams.putString("Accept", "application/pdf");
                    loader.execute(params, headerParams, null);
                }
            });

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

    private class PaymentPdfLoadListener implements FileResponseListener {
        private Payment payment;
        private ProgressBar progressBar;
        public PaymentPdfLoadListener(Payment payment, ProgressBar progressBar) {
            this.payment = payment;
            this.progressBar = progressBar;
        }
        @Override
        public void handleResponse(int result, String pathToFile) {
            progressBar.setVisibility(View.GONE);
            mHistoryListView.closeOpenedItems();
            if (result == 200) {
                Log.d(TAG, "Credentials file downloaded: " + pathToFile);

                // log flurry event
                Map<String, String> articleParams = new HashMap<String, String>();
                articleParams.put("path", pathToFile);
                FlurryAgent.logEvent("payment pdf downloaded", articleParams);

                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("message/rfc822");
                intent.putExtra(Intent.EXTRA_SUBJECT, String.format(getResources().getString(R.string.SHARE_PAYMENT_TITLE), payment.getName()));
                intent.putExtra(Intent.EXTRA_TEXT, String.format(getResources().getString(R.string.SHARE_PAYMENT_BODY), payment.getName(), payment.getStatus()));

                File f = new File(pathToFile);
                if (f.exists()) {
                    intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + f.getAbsolutePath()));
                }
                try {
                    startActivity(Intent.createChooser(intent, getResources().getString(R.string.SHARE_PAYMENT_REQUEST)));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(TimelineActivity.this, "There are no email clients installed.", Toast.LENGTH_LONG).show();
                }
            } else if (result == 403) {
                Intent intent = new Intent(TimelineActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(TimelineActivity.this, "failed to load payment pdf", Toast.LENGTH_LONG).show();
                Log.e(TAG, "can't load credentials file. response code: " + result);

                // log flurry event
                Map<String, String> articleParams = new HashMap<String, String>();
                articleParams.put("result", "" + result);
                FlurryAgent.logEvent("payment pdf download failed", articleParams);
            }
        }
    }
}
