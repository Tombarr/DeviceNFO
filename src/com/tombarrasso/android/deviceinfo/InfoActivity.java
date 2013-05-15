package com.tombarrasso.android.deviceinfo;

/*
 * Copyright 2013 Thomas Barrasso
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Java Packages
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Locale;
import java.util.regex.Pattern;

// Crouton Packages
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

// Android Packages
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Patterns;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

@SuppressLint("NewApi")
public final class InfoActivity extends Activity
	implements View.OnClickListener {
	
	private static final String HTTP_METHOD = "POST";
	private static final String BUILD_PROPS = "build.props";
	private static final SparseArray<String> mPhoneArray = new SparseArray<String>();
	static {
		mPhoneArray.put(0, "PHONE_TYPE_NONE");
		mPhoneArray.put(1, "PHONE_TYPE_GSM");
		mPhoneArray.put(2, "PHONE_TYPE_CDMA");
		mPhoneArray.put(3, "PHONE_TYPE_SIP");
	};
	
	// Taken from android.util.Patterns for backwards compatibility.
	public static final Pattern EMAIL_ADDRESS
	    = ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) ? Patterns.EMAIL_ADDRESS :
	    	Pattern.compile(
		        "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
		        "\\@" +
		        "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
		        "(" +
		            "\\." +
		            "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
		        ")+"
		    ));

	private String mDeviceText = null;
	private Menu mMenu = null;
	private boolean errOccurred = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.info_page);
		
		// Set our text so the user can see what we'll send.
		final TextView mTV = (TextView) findViewById(R.id.build_text);
		mTV.setTypeface(Typeface.MONOSPACE);
		mTV.setTextSize(TypedValue.COMPLEX_UNIT_PT, 5);
		try {
			mDeviceText = getDisplayText();
			mTV.setText(mDeviceText);
			errOccurred = false;
		} catch (IOException e) {
			mTV.setText(R.string.error_text);
			errOccurred = true;
		}
		
		// Remove SEND button for < HC, or handle its clicks.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			findViewById(R.id.send).setVisibility(View.GONE);
		} else {
			findViewById(R.id.send).setOnClickListener(this);
		}
		
		// Only show buttons if an error didn't occur.
		if (errOccurred) {
			removeFields();
			Crouton.showText(this, R.string.unknown_error, Style.ALERT);
			return;
		}
	}
	
	/** "SEND" has been hit! Validate our form and {@link send} is. */
	public final void submit() {
		
		// Get necessary information.
		final CharSequence mName = ((TextView) findViewById(R.id.name)).getText(),
						   mEmail = ((TextView) findViewById(R.id.email)).getText(),
						   mNote = ((TextView) findViewById(R.id.note)).getText();
		
		// Check to make sure nothing required is empty.
		if (TextUtils.isEmpty(mName)) {
			Crouton.showText(this, R.string.no_name, Style.ALERT);
			return;
		} else if (TextUtils.isEmpty(mEmail)) {
			Crouton.showText(this, R.string.no_email, Style.ALERT);
			return;
		}
		
		// Check to make sure the email entered is valid.
		if (!EMAIL_ADDRESS.matcher(mEmail).matches()) {
			Crouton.showText(this, R.string.invalid_email, Style.ALERT);
			return;
		}
		
		try {
			// Attempt to send the information to the Browser.
			send(mName, mEmail, mNote);
		} catch (UnsupportedEncodingException e) {
			Crouton.showText(this, R.string.unknown_error, Style.ALERT);
		}
	}
	
	/** Encodes our POST request in Javascript then opens the browser. */
	public final void send(final CharSequence mName, final CharSequence mEmail, final CharSequence mNote)
		throws UnsupportedEncodingException {
	    final String jsUrl = "javascript:" + 
	        "var to = 'http://rootdoes.com/contact.php';" +
	        "var p = {name:'" + URLEncoder.encode(mName.toString(), "UTF-8") +
	        	   "',email:'" + URLEncoder.encode(mEmail.toString(), "UTF-8") +
	        	   "',info:'" + URLEncoder.encode(mDeviceText, "UTF-8") +
	        	   "',note:'" + URLEncoder.encode(mNote.toString(), "UTF-8") + "'};" +
	        "var myForm = document.createElement('form');" +
	        "myForm.method='" + HTTP_METHOD.toUpperCase(Locale.US) + "';" +
	        "myForm.action = to;" +
	        "for (var k in p) {" +
	            "var myInput = document.createElement('input');" +
	            "myInput.setAttribute('type', 'text');" +
	            "myInput.setAttribute('name', k);" +
	            "myInput.setAttribute('value', p[k]);" +
	            "myForm.appendChild(myInput);" +
	        "}" +
	        "document.body.appendChild(myForm);" +
	        "myForm.submit() ;" +
	        "document.body.removeChild(myForm);";
	    final Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(jsUrl));
	    startActivity(browserIntent);
	}
	
	/** Remove all fields, no data to submit. */
	public final void removeFields() {
		
		final int[] mIds = new int[]
			{ R.id.email, R.id.name, R.id.note, R.id.send	 };
		
		// Hide all fields completely.
		for (final int mId : mIds) {
			findViewById(mId).setVisibility(View.GONE);
		}
		
		// Remove the SEND menu button.
		final MenuItem mItem = mMenu.findItem(R.id.send);
		if (null != mItem) {
			mItem.setVisible(false);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getMenuInflater().inflate(R.menu.info, menu);
		}
		mMenu = menu;
		return true;
	}
	
	// Handle "SEND" actionbar menu button.
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case R.id.send: {
				submit();
				return true;
			} default: {
				return super.onOptionsItemSelected(item);
			}
		}
	}

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	/** Obtains all device information through {@link SystemPropertiesProxy}, {@link TelephonyManager}, and {@link Build}. */
	private final String getDisplayText() throws IOException {
		final AssetManager mAssets = getAssets();
		final BufferedReader mStream = new BufferedReader(new InputStreamReader(mAssets.open(BUILD_PROPS)));
		
		String mFullText = "# System properties\n";
		String mLine = null;

		// Attempt to load system properties.
		while ((mLine = mStream.readLine()) != null) {
			final String mProp = SystemPropertiesProxy.get(mLine);
			if (!TextUtils.isEmpty(mProp) && !Build.UNKNOWN.equals(mProp)) {
				mFullText += mLine + '=' + mProp + "\n";
			}
		}
		
		// Obtain Telephony information.
		final TelephonyManager mTele = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		final String netOp = mTele.getNetworkOperatorName(),
					 simOp = mTele.getSimOperatorName(),
					 radVer = ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) ? Build.getRadioVersion() : Build.RADIO);
		final int phoneType = mTele.getPhoneType();
		
		// Telephony constants.
		mFullText += "\n# Telephony information\n";
		if (null != mPhoneArray.get(phoneType)) {
			mFullText += "ro.phone_type=" + mPhoneArray.get(phoneType) + "\n";
		} if (!TextUtils.isEmpty(netOp)) {
			mFullText += "ro.network_name=" + netOp + "\n";
		} if (!TextUtils.isEmpty(simOp)) {
			mFullText += "ro.sim_operator=" + simOp + "\n";
		} if (!TextUtils.isEmpty(radVer)) {
			mFullText += "ro.radio_version=" + radVer + "\n";
		}
			
		return mFullText.trim();
	}

	// Handle "SEND" click events.
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
			case R.id.send: {
				submit();
				return;
			}
		}
	}
}
