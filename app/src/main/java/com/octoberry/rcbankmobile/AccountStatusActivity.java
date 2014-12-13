package com.octoberry.rcbankmobile;

import java.io.File;
import java.util.ArrayList;

import org.json.JSONObject;

import com.octoberry.rcbankmobile.db.DataBaseManager;
import com.octoberry.rcbankmobile.handler.AccountStatusHandler;
import com.octoberry.rcbankmobile.handler.Card;
import com.octoberry.rcbankmobile.handler.Document;
import com.octoberry.rcbankmobile.net.AsyncFileLoader;
import com.octoberry.rcbankmobile.net.AsyncFileUploader;
import com.octoberry.rcbankmobile.net.FileResponseListener;
import com.octoberry.rcbankmobile.net.JSONResponseListener;
import com.octoberry.rcbankmobile.ui.CardListAdapter;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class AccountStatusActivity extends Activity implements OnRefreshListener {
	private final static String TAG = AccountStatusActivity.class.getName();
	
	private LinearLayout mMenuLinearLayout;
	private TextView mSmsTextView;
	private TextView mEmailTextView;
	private TextView mCancelTextView;
	private View mShadowView;
	
	private SwipeRefreshLayout mSwipeLayout;
	private AccountStatusHandler mAccountStatusHandler;
	
	private FileUploadHandler mFileUploadHandler = new FileUploadHandler();
	private String mToken;
	private ListView mCardsListView;
	private CardListAdapter mCardAdapter;
	private ArrayList<Card> mCards;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_account_status);

		mMenuLinearLayout = (LinearLayout) findViewById(R.id.menuLinearLayout);
		mSmsTextView = (TextView) findViewById(R.id.sendSmsTextView);
		mEmailTextView = (TextView) findViewById(R.id.sendEmailTextView);
		mCancelTextView = (TextView) findViewById(R.id.cancelTextView);
		mShadowView = findViewById(R.id.shadowView);
		
		mShadowView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {	return true; }
		});
		
		mSmsTextView.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				for (Card card: mCards) {
					if (card.getType() == Card.TYPE_CREDS) {
						String uri= "smsto:";
			            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(uri));
			            intent.putExtra("sms_body", card.getCreds());
			            intent.putExtra("compose_mode", true);
			            startActivity(intent);
						break;
					}
				}				
			}
		});
		
		mEmailTextView.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				for (Card card: mCards) {
					if (card.getType() == Card.TYPE_CREDS) {
						Intent intent = new Intent(Intent.ACTION_SEND);
						intent.setType("message/rfc822");
						intent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.ACCOUNT_DETAILS));
						
						File f = new File(android.os.Environment.getExternalStorageDirectory(), "credentials.pdf");
						if (f.exists()) {
							intent.putExtra(Intent.EXTRA_STREAM, Uri.parse( "file://" + f.getAbsolutePath()));
						} else {
							intent.putExtra(Intent.EXTRA_TEXT, card.getCreds());
						}
						try {
						    startActivity(Intent.createChooser(intent, card.getTitle()));
						} catch (android.content.ActivityNotFoundException ex) {
						    Toast.makeText(AccountStatusActivity.this, "There are no email clients installed.", Toast.LENGTH_LONG).show();
						}
						break;
					}
				}
			}
		});
		
		mCancelTextView.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				mMenuLinearLayout.setVisibility(View.GONE);
				mShadowView.setVisibility(View.GONE);
				mCardsListView.setEnabled(true);
			}
		});
	
		mCardsListView = (ListView) findViewById(R.id.cardsListView);
		mCardsListView.setDividerHeight(0);

		mToken = DataBaseManager.getInstance(getApplicationContext()).getActiveToken();
		
		mSwipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
		mSwipeLayout.setOnRefreshListener(this);
		mSwipeLayout.setColorScheme(android.R.color.holo_blue_bright,
				android.R.color.holo_green_light,
				android.R.color.holo_orange_light,
				android.R.color.holo_red_light);

		mAccountStatusHandler = new AccountStatusHandler(this, getApplicationContext(), mSwipeLayout, null);

		initViews();
		
		/* get credentials */	
		/*
		File f = new File(android.os.Environment.getExternalStorageDirectory(), "credentials.pdf");
		if (f.exists() == false) {			
			AsyncFileLoader loader = new AsyncFileLoader(this, f.getAbsolutePath());
			loader.registryListener(new GetCredentialsHandler());
			Bundle params = new Bundle();
			params.putString("requestType", "GET");
			params.putString("endpoint", "/api/organization/creds");
			Bundle headerParams = new Bundle();
			headerParams.putString("Authorization", mToken);
			headerParams.putString("Accept", "/");
			loader.execute(params, headerParams, null);
		}
		*/
	}
	
	@Override
	public void onRefresh() {
		mAccountStatusHandler.checkAccountStatus();
	}

	private void initViews() {
		mCards = getIntent().getParcelableArrayListExtra("cards");
		mCardAdapter = new CardListAdapter(this, mCards, this);
		mCardAdapter.clearPreferences();
		mCardsListView.setAdapter(mCardAdapter);
	}

	class FileUploadHandler implements JSONResponseListener {
		@Override
		public void handleResponse(int result, JSONObject response, String error) {
			SharedPreferences upload = getSharedPreferences(CardListAdapter.UPLOAD, 0);
			if (response != null) {
				Log.d("###", response.toString());
				try {
					int resultCode = response.getInt("status");
					if (resultCode == 200) {
						Log.d("###", "file is uploaded to server");						
						String id = upload.getString(CardListAdapter.DOC_ID, null);
						int pageIndex = upload.getInt(CardListAdapter.PAGE_INDEX, -1);
						for (Card card: mCards) {
							if (card.getDocuments() != null) {
								for (Document doc: card.getDocuments()) {
									if ((id != null) && (doc.getId().equals(id))) {
										switch (pageIndex) {
											case 1:
												doc.setFirstPageUploaded(true);
												break;
											case 2:
												doc.setSecondPageUploaded(true);
												break;	
											default:
												break;
										}
										if (doc.isFirstPageUploaded() && doc.isSecondPageUploaded()) {
											doc.setCompleted(true);
										}
										break;
									}
								}
							}
						}
						mCardAdapter.notifyDataSetChanged();
					}
				} catch (Exception exc) {
					exc.printStackTrace();
				}
			}
			mCardAdapter.clearPreferences();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d("###", "requestCode: " + requestCode + " resultCode: " + resultCode);
		if (resultCode == RESULT_OK) {
			if (requestCode == CardListAdapter.CREATE_SNILS) {
				if (data.getBooleanExtra("completed", false)) {
					for (Card card: mCards) {
						if (card.getType() == Card.TYPE_SIGNATURE) {
							card.setCompleted(true);
							mCardAdapter.notifyDataSetChanged();
							break;
						}
					}
				}
				return;
			}
			Uri uriToResolve;
			String path;
			if (requestCode == CardListAdapter.SELECT_FILE) {
				uriToResolve = data.getData();
				path = getPath(getApplicationContext(), uriToResolve);
			} else {
				SharedPreferences upload = getSharedPreferences(CardListAdapter.UPLOAD, 0);				
				path = upload.getString(CardListAdapter.PATH, null);
				Log.d("###", "path: " + path);
			}			
			if (path == null) {
				Toast.makeText(getApplicationContext(), "can't upload file", Toast.LENGTH_LONG).show();
				mCardAdapter.clearPreferences();
				return;
			}

			AsyncFileUploader asyncLoader = new AsyncFileUploader(this);
			asyncLoader.registryListener(mFileUploadHandler);
			Bundle params = new Bundle();
			params.putString("requestType", "POST");
			params.putString("endpoint", "/api/organization/upload");
			params.putString("filePath", path);
			Bundle headerParams = new Bundle();
			headerParams.putString("Authorization", mToken);
			asyncLoader.execute(params, headerParams, null);
		} else {
			mCardAdapter.clearPreferences();
		}
	}

	private String getPath(final Context context, final Uri uri) {
		// MediaStore (and general)
		if ("content".equalsIgnoreCase(uri.getScheme())) {
			// Return the remote address
			if (isGooglePhotosUri(uri)) {
				String path = uri.getLastPathSegment();
				if (path.startsWith("/"))
					path = path.substring(1);
				return path;
			}

			return getDataColumn(context, uri, null, null);
		}

		return null;
	}

	/**
	 * Get the value of the data column for this Uri. This is useful for
	 * MediaStore Uris, and other file-based ContentProviders.
	 * 
	 * @param context
	 *            The context.
	 * @param uri
	 *            The Uri to query.
	 * @param selection
	 *            (Optional) Filter used in the query.
	 * @param selectionArgs
	 *            (Optional) Selection arguments used in the query.
	 * @return The value of the _data column, which is typically a file path.
	 */
	public static String getDataColumn(Context context, Uri uri,
			String selection, String[] selectionArgs) {

		Cursor cursor = null;
		final String column = "_data";
		final String[] projection = { column };

		try {
			cursor = context.getContentResolver().query(uri, projection,
					selection, selectionArgs, null);
			if (cursor != null && cursor.moveToFirst()) {
				final int index = cursor.getColumnIndexOrThrow(column);
				return cursor.getString(index);
			}
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return null;
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is Google Photos.
	 */
	public static boolean isGooglePhotosUri(Uri uri) {
		return "com.google.android.apps.photos.content".equals(uri
				.getAuthority());
	}
}
