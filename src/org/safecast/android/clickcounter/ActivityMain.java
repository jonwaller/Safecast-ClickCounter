package org.safecast.android.clickcounter;

import java.text.DecimalFormat;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

public class ActivityMain extends Activity {

	//One-off screen refresh
	Handler clicksDetectedUiHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			updateUi();
		}
	};

	private void bindControls() {
		Button buttonAbout = (Button) findViewById(R.id.buttonAbout);

		buttonAbout.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				Intent i = new Intent(view.getContext(), ActivityAbout.class);
				startActivity(i);
			}
		});
		
		Button buttonUpload = (Button) findViewById(R.id.buttonUpload);

		buttonUpload.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				//Intent i = new Intent(view.getContext(), ActivityAbout.class);
				//startActivity(i);
			}
		});

		
		final RadioButton radioCount = (RadioButton) findViewById(R.id.radioCount);
		final RadioButton radioCpm = (RadioButton) findViewById(R.id.radioCpm);
		final RadioButton radioMsv = (RadioButton) findViewById(R.id.radioMsv);
		
		radioCount.setTag(MeasurementMode.COUNT);
		radioCpm.setTag(MeasurementMode.CPM);
		radioMsv.setTag(MeasurementMode.MSV);
		
		radioCount.setOnClickListener(radioButtonListener);
		radioCpm.setOnClickListener(radioButtonListener);
		radioMsv.setOnClickListener(radioButtonListener);
	}

	private OnClickListener radioButtonListener = new OnClickListener() {
	    public void onClick(View v) {
	        // Perform action on clicks
	        RadioButton rb = (RadioButton) v;
	        measurementSource.setMode((MeasurementMode)rb.getTag());
	        updateUi();
	    }
	};
	
	private void configureLcdDisplayFont() {
		// Set up display font (Crysta freeware font)
		Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/lcd.ttf");
		TextView tv = (TextView) findViewById(R.id.textLcdDisplay);
		tv.setTypeface(tf);
	}

	private void configureUi() {
		bindControls();
		configureLcdDisplayFont();
	}

	private void updateMeasurementModeDisplay() {
		TextView textMeasurementMode = (TextView) findViewById(R.id.textMeasurementMode);
		textMeasurementMode.setText(measurementSource.getMeasurementModeDisplayName());
	}

	MeasurementSource measurementSource = new MeasurementSource(clicksDetectedUiHandler);
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		configureUi();
		updateUi();
	}

	private void updateLcdDisplay() {
		TextView textLcdDisplay = (TextView) findViewById(R.id.textLcdDisplay);
		
		Double lcdDisplayValue = measurementSource.getValue();
		
		String lcdDisplayText = new DecimalFormat("#.###")
				.format(lcdDisplayValue); // "1.2"
		textLcdDisplay.setText(lcdDisplayText);
		
		textLcdDisplay.postInvalidate(); //Update instantly
	}

	private void updateUi() {
		updateLcdDisplay();
		updateMeasurementModeDisplay();
	}

}