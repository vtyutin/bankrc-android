package com.octoberry.nonamebank.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.octoberry.nonamebank.LoginActivity;
import com.octoberry.nonamebank.R;
import com.octoberry.nonamebank.handler.AccountStatusHandler;
import com.octoberry.nonamebank.handler.Card;
import com.octoberry.nonamebank.handler.Document;

public class CardListAdapter extends ArrayAdapter<Card> {
	private static final String TAG = CardListAdapter.class.getName();
	
	public final static int REQUEST_CAMERA = 0;
	public final static int SELECT_FILE = 1;
	public final static int CREATE_SNILS = 2;
	
	public final static int FIRST_PAGE = 1;
	public final static int SECOND_PAGE = 2;
	
	public final static String DOC_ID = "doc_id";
	public final static String PAGE_INDEX = "page_index";
	public final static String UPLOAD = "upload";
	public final static String PATH = "path";
	
	/* button actions */
	public final static String BUTTON_ACTION_PASSWORD = "password";
	public final static String BUTTON_ACTION_NEXT_STEP = "next_step";
	
	private final Activity parent;
	private final Context context;
	private final ArrayList<Card> cards;
	
	private LinearLayout mMenuLinearLayout;
	private View mShadowView;

	public CardListAdapter(Context context, ArrayList<Card> cards, Activity parent) {
		super(context, R.layout.card, cards);
		this.context = context;
		this.parent = parent;
		this.cards = cards;
		
		mMenuLinearLayout = (LinearLayout) parent.findViewById(R.id.menuLinearLayout);
		mShadowView = parent.findViewById(R.id.shadowView);
	}
	
	public void clearPreferences() {
		Editor editor = context.getSharedPreferences(CardListAdapter.UPLOAD, 0).edit();
		editor.remove(CardListAdapter.DOC_ID);
		editor.remove(CardListAdapter.PAGE_INDEX);
		editor.remove(CardListAdapter.PATH);
		editor.commit();
	}
	
	@Override
	public int getCount() {
		if (cards != null) {
			return cards.size();
		}
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Card card = cards.get(position);
		View rowView = null;
		switch (card.getType()) {
		case Card.TYPE_TEXT:
			rowView = initTextCard(card);
			break;
		case Card.TYPE_SIGNATURE:
			rowView = initSignatureCard(card);
			break;
		case Card.TYPE_SEPARATOR:
			rowView = initSeparatorCard(card);
			break;
		case Card.TYPE_EVENT:
			rowView = initEventCard(card);
			break;
		case Card.TYPE_CREDS:
			rowView = initCredsCard(card);
			break;
		case Card.TYPE_CHECK_LIST:
			rowView = initCheckListCard(card);
			break;
		case Card.TYPE_PASSPORTS:
			rowView = initPassportsCard(card);
			break;
		case Card.TYPE_BUTTON:
			rowView = initButtonCard(card);
			break;
		}
		return rowView;
	}

	private View initTextCard(Card card) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.text_card, null, false);
		if (card.getTitle() != null) {
			TextView titleTextView = (TextView)rowView.findViewById(R.id.titleTextView);
			titleTextView.setText(card.getTitle());
			titleTextView.setVisibility(View.VISIBLE);
		}
		if (card.getDescription() != null) {
			TextView descriptionTextView = (TextView)rowView.findViewById(R.id.descriptionTextView);
			descriptionTextView.setText(card.getDescription());
			descriptionTextView.setVisibility(View.VISIBLE);
		}
		return rowView;
	}

	private View initSignatureCard(Card card) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.signature_card, null, false);
		TextView descriptionTextView = (TextView)rowView.findViewById(R.id.descriptionTextView);
		descriptionTextView.setText(card.getTitle());
		ImageView completedImageView = (ImageView)rowView.findViewById(R.id.checkImageView);
		if (card.isCompleted()) {
			completedImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.todo_check));
		} else {
			completedImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.todo_un_check));
		}
		rowView.setOnClickListener(new OnClickListener() {				
			@Override
			public void onClick(View v) {
				//Intent intent = new Intent(context, SnilsActivity.class);
				//((Activity)context).startActivityForResult(intent, CREATE_SNILS);
			}
		});
		return rowView;
	}

	private View initSeparatorCard(Card card) {
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.separator_card, null, false);
		
		return rowView;
	}

	private View initEventCard(Card card) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.event_card, null, false);
		((TextView)rowView.findViewById(R.id.titleTextView)).setText(card.getTitle());
		((TextView)rowView.findViewById(R.id.descriptionTextView)).setText(card.getDescription());
		((TextView)rowView.findViewById(R.id.addressTextView)).setText(card.getAddress());
		final Card cardEvent = card;
		LinearLayout addEventLayout = (LinearLayout)rowView.findViewById(R.id.addEventLayout);
		if ((cardEvent.getStartDate() != null) && (cardEvent.getEndDate() != null)) {
			addEventLayout.setVisibility(View.VISIBLE);
			addEventLayout.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Calendar cal = Calendar.getInstance();
					cal.setTime(cardEvent.getStartDate());
					Intent intent = new Intent(Intent.ACTION_EDIT);
					intent.setType("vnd.android.cursor.item/event");
					intent.putExtra("beginTime", cardEvent.getStartDate().getTime());
					intent.putExtra("endTime", cardEvent.getEndDate().getTime());
					intent.putExtra("title", cardEvent.getCalendarTitle());
					intent.putExtra("eventLocation",cardEvent.getAddress());
					((Activity)context).startActivity(intent);
				}
			});
		} else {
			addEventLayout.setVisibility(View.GONE);
		}			
		return rowView;
	}

	private View initCredsCard(Card card) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.creds_card, null, false);			
		((TextView)rowView.findViewById(R.id.titleTextView)).setText(card.getTitle());
		rowView.setOnClickListener(new OnClickListener() {				
			@Override
			public void onClick(View v) {
				mMenuLinearLayout.setVisibility(View.VISIBLE);	
				mShadowView.setVisibility(View.VISIBLE);
			}
		});
		return rowView;
	}

	private View initCheckListCard(Card card) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		ViewGroup rowView = (ViewGroup)inflater.inflate(R.layout.check_list_card, null, false);
		((TextView)rowView.findViewById(R.id.titleTextView)).setText(card.getTitle());
		if (card.getDocuments() != null) {
			for (Document document: card.getDocuments()) {					
				View elementView = inflater.inflate(R.layout.check_list_card_row, null, false);
				((TextView)elementView.findViewById(R.id.titleTextView)).setText(document.getTitle());
				ImageView checkImageView = (ImageView)elementView.findViewById(R.id.checkImageView);
				if (document.isCompleted()) {
					checkImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.todo_check));
				} else {
					checkImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.todo_un_check));
				}
				elementView.setOnClickListener(new CheckDocumentListener(document.getId()));					
				rowView.addView(elementView);
			}
		}
		return rowView;
	}
	
	private View initPassportsCard(Card card) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		ViewGroup rowView = (ViewGroup)inflater.inflate(R.layout.check_list_card, null, false);
		((TextView)rowView.findViewById(R.id.titleTextView)).setText(card.getTitle());
		if (card.getDocuments() != null) {
			for (Document document: card.getDocuments()) {					
				View elementView = inflater.inflate(R.layout.passports_card_row, null, false);
				TextView titleTextView = (TextView)elementView.findViewById(R.id.titleTextView); 
				titleTextView.setText(document.getTitle());
				
				ProgressBar firstPageProgress = (ProgressBar)elementView.findViewById(R.id.firstPageProgress);
				ProgressBar secondPageProgress = (ProgressBar)elementView.findViewById(R.id.secondPageProgress);
				
				firstPageProgress.setVisibility(View.GONE);
				secondPageProgress.setVisibility(View.GONE);
				
				TextView firstPageTextView = (TextView)elementView.findViewById(R.id.firstPageTextView);
				TextView secondPageTextView = (TextView)elementView.findViewById(R.id.secondPageTextView);
				RelativeLayout firstPageLayout = (RelativeLayout)elementView.findViewById(R.id.firstPageLayout);
				RelativeLayout secondPageLayout = (RelativeLayout)elementView.findViewById(R.id.secondPageLayout);
				if (document.isCompleted()) {
					titleTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.todo_check, 0, 0, 0);
				} else {
					titleTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.todo_un_check, 0, 0, 0);
				}
				if (document.isFirstPageUploaded()) {
					firstPageTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.downloaded, 0);
				} else {
					firstPageTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.upload, 0);
				}
				if (document.isSecondPageUploaded()) {
					secondPageTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.downloaded, 0);
				} else {
					secondPageTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.upload, 0);
				}
				firstPageLayout.setOnClickListener(new UploadDocumentListener(document.getId(), FIRST_PAGE));
				secondPageLayout.setOnClickListener(new UploadDocumentListener(document.getId(), SECOND_PAGE));
				
				SharedPreferences upload = context.getSharedPreferences(UPLOAD, 0);
				String documentId = upload.getString(DOC_ID, null);
				int pageIndex = upload.getInt(PAGE_INDEX, -1);
				if ((documentId != null) && (document.getId().equals(documentId))) {
					switch (pageIndex) {
						case 1:
							firstPageProgress.setVisibility(View.VISIBLE);
							break;
						case 2:
							secondPageProgress.setVisibility(View.VISIBLE);
							break;
						default:
							firstPageProgress.setVisibility(View.GONE);
							secondPageProgress.setVisibility(View.GONE);
							break;
					}
				} else {
					firstPageProgress.setVisibility(View.GONE);
					secondPageProgress.setVisibility(View.GONE);
				}
				
				rowView.addView(elementView);
			}
		}
		return rowView;
	}

	private View initButtonCard(Card card) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.button_card, null, false);
		TextView buttonTextView = (TextView)rowView.findViewById(R.id.buttonTextView);
		buttonTextView.setText(card.getTitle());
		final String action = card.getAction();
		if (action != null) {
			buttonTextView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (action.equals(BUTTON_ACTION_PASSWORD)) {
						Intent intent = new Intent(context, LoginActivity.class);
						parent.finish();
						parent.startActivity(intent);
					} else if (action.equals(BUTTON_ACTION_NEXT_STEP)) {
						AccountStatusHandler handler = new AccountStatusHandler(parent, context, null, parent.getClass());
						handler.nextStep();
					} else {
						Log.w(TAG, "invalid button action: " + action);
					}
				}
			});
		} else {
			Log.e(TAG, "Button action is null");
		}
		return rowView;
	}
	
	class UploadDocumentListener implements OnClickListener {
		private String documentId = null;
		private int pageIndex = -1;
		UploadDocumentListener(String documentId, int pageIndex) {
			this.documentId = documentId;
			this.pageIndex = pageIndex;
		}
		@Override
		public void onClick(View v) {
			if (context.getSharedPreferences(UPLOAD, 0).getString(DOC_ID, null) != null) {
				Toast.makeText(context, context.getResources().getString(R.string.UPLOAD_IN_PROGRESS), Toast.LENGTH_LONG).show();
			} else {
				Editor editor = context.getSharedPreferences(UPLOAD, 0).edit();
				editor.putString(DOC_ID, documentId);
				editor.putInt(PAGE_INDEX, pageIndex);
				editor.commit();
				selectImage();
				notifyDataSetChanged();
			}
		}
	}
	
	class CheckDocumentListener implements OnClickListener {
		private String documentId = null;
		CheckDocumentListener(String documentId) {
			this.documentId = documentId;
		}
		@Override
		public void onClick(View v) {
			for (Card card: cards) {
				if ((card.getType() == Card.TYPE_CHECK_LIST) && card.getDocuments() != null) {
					for (Document doc: card.getDocuments()) {
						if ((doc.getId() != null) && (doc.getId().equals(documentId))) {
							doc.setCompleted(!doc.isCompleted());
							notifyDataSetChanged();
							break;
						}
					}
				}
			}
		}
	}
	
	private void selectImage() {
		Resources res = context.getResources();
		final String[] items = { res.getString(R.string.PHOTO_TAKE_NEW),
				res.getString(R.string.PHOTO_GALLERY),
				res.getString(R.string.CANCEL_BUTTON) };

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setItems(items, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int item) {
				Resources res = context.getResources();
				SharedPreferences upload = context.getSharedPreferences(UPLOAD, 0);
				Editor editor = upload.edit();
				if (items[item].equals(res.getString(R.string.PHOTO_TAKE_NEW))) {
					Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					String fileName;
					Calendar cal = Calendar.getInstance();
					fileName = String.format(Locale.getDefault(), "scan_%d-%d-%d %d-%d.jpg", cal.get(Calendar.YEAR),
							cal.get(Calendar.MONTH), cal.get(Calendar.DATE), cal.get(Calendar.HOUR), cal.get(Calendar.MINUTE));
					File f = new File(android.os.Environment.getExternalStorageDirectory() + "/octoberry", fileName);
					editor.putString(PATH, f.getAbsolutePath());
					editor.commit();
					intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
					parent.startActivityForResult(intent, REQUEST_CAMERA);
				} else if (items[item].equals(res.getString(R.string.PHOTO_GALLERY))) {
					Intent intent = new Intent(Intent.ACTION_PICK,
							android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
					intent.setType("image/*");
					parent.startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
				} else if (items[item].equals(res.getString(R.string.CANCEL_BUTTON))) {
					editor.remove(DOC_ID);
					editor.remove(PAGE_INDEX);
					editor.commit();
					dialog.dismiss();
				}
			}
		});
		builder.show();
	}
}