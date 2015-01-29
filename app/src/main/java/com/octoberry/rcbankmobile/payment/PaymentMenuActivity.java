package com.octoberry.rcbankmobile.payment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;

import com.octoberry.rcbankmobile.GeneralUtils;
import com.octoberry.rcbankmobile.R;
import com.octoberry.rcbankmobile.db.DataBaseManager;
import com.octoberry.rcbankmobile.net.AsyncFileUploader;
import com.octoberry.rcbankmobile.net.AsyncJSONLoader;
import com.octoberry.rcbankmobile.net.JSONResponseListener;
import com.octoberry.rcbankmobile.ui.CardListAdapter;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.Animator.AnimatorListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class PaymentMenuActivity extends Activity {
	private ObjectAnimator mAppearAnimator;
	private ObjectAnimator mDisappearAnimator;
	private RelativeLayout mRootLayout;
	private ImageView mCloseImageView;
	private TextView mManualTextView;
	private TextView mLoadTextView;
	private ProgressBar mProgress;
	
	private static final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_payment_menu);
		
		mRootLayout = (RelativeLayout)findViewById(R.id.rootLayout);
		mCloseImageView = (ImageView)findViewById(R.id.closeImageView);
		mProgress = (ProgressBar)findViewById(R.id.progressBar);
		
		mCloseImageView.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View arg0) {
				closeActivity();
			}
		});
		
		mManualTextView = (TextView)findViewById(R.id.manualTextView);
		mManualTextView.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				Payment.clearPreference(PaymentMenuActivity.this);
				Intent intent = new Intent(PaymentMenuActivity.this, PaymentReceiverActivity.class);
				startActivity(intent);
			}
		});
		
		mLoadTextView = (TextView)findViewById(R.id.loadPhotoTextView);
		mLoadTextView.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				Payment.clearPreference(PaymentMenuActivity.this);
				selectImage();
			}
		});
				
		Uri data = getIntent().getData();
		Log.d("###", "intent data: " + data);
		if (data != null) {
			getIntent().setData(null);
			if (!importData(data)) {
				Toast.makeText(this, "no data", Toast.LENGTH_LONG).show();
				finish();
				return;
			} else {
				String path = restoryPathToFile();
				if (path.toUpperCase().endsWith(".PNG") || path.toUpperCase().endsWith(".JPG")) {
                    GeneralUtils utils = new GeneralUtils();
                    path = utils.decodeImageFromFile(path, null, 200, 200);
                    Log.d("###", "image decoded: " + path);
                }
				if (path == null) {
					Toast.makeText(this, "can't decode image", Toast.LENGTH_LONG).show();
					finish();
					return;
				}
				
				storyUploadInProgress(true);
				mProgress.setVisibility(View.VISIBLE);
				mManualTextView.setEnabled(false);
				mLoadTextView.setEnabled(false);
				AsyncFileUploader asyncLoader = new AsyncFileUploader(this);
				asyncLoader.registryListener(new FileUploadHandler());
				Bundle params = new Bundle();
				params.putString("requestType", "POST");
				params.putString("endpoint", "/api/bank/payment/import");
				params.putString("filePath", path);
				Bundle headerParams = new Bundle();
				headerParams.putString("Authorization", DataBaseManager.getInstance(this).getBankToken());
				headerParams.putString("Accept", "/");
				asyncLoader.execute(params, headerParams, null);
			}
		}
	}
	
	@Override
	protected void onStart() {
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int height = size.y;
		mRootLayout.setTranslationY(height);
		
		mAppearAnimator = ObjectAnimator.ofFloat(mRootLayout, "translationY", height, 0);
		mAppearAnimator.setDuration(1000);
		mDisappearAnimator = ObjectAnimator.ofFloat(mRootLayout, "translationY", 0, height);
		mDisappearAnimator.setDuration(1000);
		
		super.onStart();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		if (isUploadInProgress()) {
			mProgress.setVisibility(View.VISIBLE);
			mManualTextView.setEnabled(false);
			mLoadTextView.setEnabled(false);
		}
		
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
	
	private boolean importData(Uri data) {
		final String scheme = data.getScheme();
		Log.d("###", "import data: schema: " + scheme);
		String path = null;
		if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
			try {
				ContentResolver cr = getContentResolver();
				InputStream is = cr.openInputStream(data);
				if (is == null) {
					return false;
				}
				path = makeFileCopy(is);
				is.close();
				if (path == null) {
					return false;
				}
			} catch (Exception exc) {
				exc.printStackTrace();
				return false;
			}
		} else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
			try {
				FileInputStream fis = new FileInputStream(new File(data.getPath()));
				path = makeFileCopy(fis);
				fis.close();
				if (path == null) {
					return false;
				}
			} catch (Exception exc) {
				exc.printStackTrace();
				return false;
			}
		} else {
			return false;
		}
		storyPathToFile(path);
		return true;
	}

	private String makeFileCopy(InputStream inputStream) {
		try {
			File directory = getFilesDir();
			Calendar cal = Calendar.getInstance();
			String fileName = "attachment" + cal.get(Calendar.DAY_OF_MONTH)
					 + cal.get(Calendar.MONTH) + cal.get(Calendar.YEAR) + ".pdf";
			File file = new File(directory, fileName);
			FileOutputStream fileOutput = new FileOutputStream(file);
			byte[] buffer = new byte[2048];
			int bufferLength = 0;
			while ((bufferLength = inputStream.read(buffer)) > 0) {
				fileOutput.write(buffer, 0, bufferLength);
			}
			fileOutput.close();
			return file.getAbsolutePath();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private void selectImage() {
		Resources res = getResources();
		final String[] items = { res.getString(R.string.PHOTO_TAKE_NEW),
				res.getString(R.string.PHOTO_GALLERY),
				res.getString(R.string.CANCEL_BUTTON) };

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setItems(items, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int item) {
				Resources res = getResources();
				if (items[item].equals(res.getString(R.string.PHOTO_TAKE_NEW))) {
					Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					String fileName;
					Calendar cal = Calendar.getInstance();
					fileName = String.format(Locale.getDefault(), "scan%d%d%d%d%d.jpg", cal.get(Calendar.YEAR),
							cal.get(Calendar.MONTH), cal.get(Calendar.DATE), cal.get(Calendar.HOUR), cal.get(Calendar.MINUTE));
					File f = new File(android.os.Environment.getExternalStorageDirectory() + "/octoberry", fileName);
					storyPathToFile(f.getAbsolutePath());
					intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
					startActivityForResult(intent, CardListAdapter.REQUEST_CAMERA);
				} else if (items[item].equals(res.getString(R.string.PHOTO_GALLERY))) {
					Intent intent = new Intent(Intent.ACTION_PICK,
							android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
					intent.setType("image/*");
					startActivityForResult(Intent.createChooser(intent, "Select File"), CardListAdapter.SELECT_FILE);
				} else if (items[item].equals(res.getString(R.string.CANCEL_BUTTON))) {
					dialog.dismiss();
				}
			}
		});
		builder.show();
	}
	
	class PaymentDetailsHandler implements JSONResponseListener {
		@Override
		public void handleResponse(int result, JSONObject response, String error) {
			if (response != null) {
				Log.d("###", response.toString());
				try {
					int resultCode = response.getInt("status");
					if (resultCode == 200) {
						JSONObject resultObject = response.getJSONObject("result");
						
						if (!resultObject.isNull("completed")) {
							if (resultObject.getBoolean("completed") == false) {
								mProgress.setVisibility(View.VISIBLE);
								mManualTextView.setEnabled(false);
								mLoadTextView.setEnabled(false);
								worker.schedule(requestPaymentDetailsTask, 3, TimeUnit.SECONDS);
								return;
							}
						}
						storyUploadId(-1);
						boolean isDataAvailable = false;
						Payment payment = new Payment();
						if (!resultObject.isNull("invoice")) {
							JSONObject invoice = resultObject.getJSONObject("invoice");
							if (!invoice.isNull("corr_name")) {
								payment.setCorrName(invoice
										.getString("corr_name"));
								isDataAvailable = true;
							}
							if (!invoice.isNull("corr_inn")) {
								payment.setCorrInn(invoice
										.getString("corr_inn"));
								isDataAvailable = true;
							}
							if (!invoice.isNull("corr_bank_bik")) {
								payment.setCorrBankBik(invoice
										.getString("corr_bank_bik"));
								isDataAvailable = true;
							}
							if (!invoice.isNull("corr_account_number")) {
								payment.setCorrAccountNumber(invoice
										.getString("corr_account_number"));
								isDataAvailable = true;
							}
							if (!invoice.isNull("corr_kpp")) {
								payment.setCorrKpp(invoice
										.getString("corr_kpp"));
								isDataAvailable = true;
							}
							if (!invoice.isNull("amount")) {
								Double amount = invoice
										.getDouble("amount");
								payment.setAmount(amount);
							}
						}
						if (isDataAvailable) {
							storyUploadInProgress(false);
							payment.addToPreference(PaymentMenuActivity.this);
							Intent intent = new Intent(PaymentMenuActivity.this, PaymentReceiverActivity.class);
							startActivity(intent);
							finish();
						} else {
							Toast.makeText(PaymentMenuActivity.this, "no data parsed", Toast.LENGTH_LONG).show();
						}
					} else {
						Toast.makeText(PaymentMenuActivity.this, "error: " + response.getString("message"), Toast.LENGTH_LONG).show();
					}
				} catch (Exception exc) {
					exc.printStackTrace();
				}
			}
			storyUploadInProgress(false);
			storyUploadId(-1);
			mProgress.setVisibility(View.GONE);
			mManualTextView.setEnabled(true);
			mLoadTextView.setEnabled(true);
		}		
	}
	
	Runnable requestPaymentDetailsTask = new Runnable() {
		public void run() {
			AsyncJSONLoader loader = new AsyncJSONLoader(
					PaymentMenuActivity.this);
			loader.registryListener(new PaymentDetailsHandler());
			Bundle params = new Bundle();
			params.putString("requestType", "GET");
			params.putString("endpoint", String.format(
					"/api/bank/payment/import/%d", getUploadId()));
			Bundle headerParams = new Bundle();
			headerParams.putString("Authorization", DataBaseManager
					.getInstance(PaymentMenuActivity.this).getBankToken());
			loader.execute(params, headerParams, null);
		}
	};
	
	
	Runnable compressImageTask = new Runnable() {
		public void run() {
			String path = restoryPathToFile();
            GeneralUtils utils = new GeneralUtils();
			path = utils.decodeImageFromFile(path, null, 200, 200);
			if (path == null) {
				clearPathToFile();
				storyUploadInProgress(false);
				Toast.makeText(PaymentMenuActivity.this, "can't upload file", Toast.LENGTH_LONG).show();
				finish();
				return;
			}
			
			AsyncFileUploader asyncLoader = new AsyncFileUploader(PaymentMenuActivity.this);
			asyncLoader.registryListener(new FileUploadHandler());
			Bundle params = new Bundle();
			params.putString("requestType", "POST");
			params.putString("endpoint", "/api/bank/payment/import");
			params.putString("filePath", path);
			Bundle headerParams = new Bundle();
			headerParams.putString("Authorization", DataBaseManager.getInstance(PaymentMenuActivity.this).getBankToken());
			headerParams.putString("Accept", "/");
			asyncLoader.execute(params, headerParams, null);
			
			clearPathToFile();
		}
	};
	
	class FileUploadHandler implements JSONResponseListener {
		@Override
		public void handleResponse(int result, JSONObject response, String error) {
			if (response != null) {
				Log.d("###", response.toString());
				try {
					int resultCode = response.getInt("status");
					if (resultCode == 200) {
						JSONObject resultObject = response.getJSONObject("result");
						if (!resultObject.isNull("import_id")) {
							storyUploadId(resultObject.getLong("import_id"));
							AsyncJSONLoader loader = new AsyncJSONLoader(PaymentMenuActivity.this);
							loader.registryListener(new PaymentDetailsHandler());
							Bundle params = new Bundle();
							params.putString("requestType", "GET");
							params.putString("endpoint", String.format("/api/bank/payment/import/%d", getUploadId()));
							Bundle headerParams = new Bundle();
							headerParams.putString("Authorization", DataBaseManager.getInstance(PaymentMenuActivity.this).getBankToken());
							loader.execute(params, headerParams, null);
							return;
						} else {
							Toast.makeText(PaymentMenuActivity.this, "error: no import id available", Toast.LENGTH_LONG).show();
						}
					} else {
						Toast.makeText(PaymentMenuActivity.this, "error: " + response.getString("message"), Toast.LENGTH_LONG).show();
					}
				} catch (Exception exc) {
					exc.printStackTrace();
				}
			} else {
				Toast.makeText(PaymentMenuActivity.this, "server is unavailable", Toast.LENGTH_LONG).show();
			}
			storyUploadInProgress(false);
			mProgress.setVisibility(View.GONE);
			mManualTextView.setEnabled(true);
			mLoadTextView.setEnabled(true);
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d("###", "requestCode: " + requestCode + " resultCode: " + resultCode);
		if (resultCode == RESULT_OK) {
			Uri uriToResolve;
			String path = null;
			if (requestCode == CardListAdapter.SELECT_FILE) {
				uriToResolve = data.getData();
				path = getPath(getApplicationContext(), uriToResolve);
			} else if (requestCode == CardListAdapter.REQUEST_CAMERA) {
				path = restoryPathToFile();
			}
			Log.d("###", "path: " + path);
			if (path == null) {
				Toast.makeText(getApplicationContext(), "can't upload file", Toast.LENGTH_LONG).show();
				return;
			} else {
				storyPathToFile(path);
			}
			worker.schedule(compressImageTask, 0, TimeUnit.MILLISECONDS);
			storyUploadInProgress(true);
			mProgress.setVisibility(View.VISIBLE);
			mManualTextView.setEnabled(false);
			mLoadTextView.setEnabled(false);
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
	
	private void storyPathToFile(String path) {
		SharedPreferences pref = getSharedPreferences("upload", 0);
		Editor edit = pref.edit();
		edit.putString("path_to_file", path);
		edit.commit();
	}
	
	private String restoryPathToFile() {
		SharedPreferences pref = getSharedPreferences("upload", 0);
		return pref.getString("path_to_file", null);
	}
	
	private void clearPathToFile() {
		SharedPreferences pref = getSharedPreferences("upload", 0);
		Editor edit = pref.edit();
		edit.remove("path_to_file");
		edit.commit();
	}
	
	private void storyUploadInProgress(boolean isInProgress) {
		SharedPreferences pref = getSharedPreferences("upload", 0);
		Editor edit = pref.edit();
		edit.putBoolean("in_progress", isInProgress);
		edit.commit();
	}
	
	private boolean isUploadInProgress() {
		SharedPreferences pref = getSharedPreferences("upload", 0);
		return pref.getBoolean("in_progress", false);
	}
	
	private void storyUploadId(long uploadId) {
		SharedPreferences pref = getSharedPreferences("upload", 0);
		Editor edit = pref.edit();
		edit.putLong("upload_id", uploadId);
		edit.commit();
	}
	
	private long getUploadId() {
		SharedPreferences pref = getSharedPreferences("upload", 0);
		return pref.getLong("upload_id", -1);
	}

}
