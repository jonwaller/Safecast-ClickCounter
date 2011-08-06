package org.safecast.android.clickcounter;

import android.os.Handler;
import android.os.Message;

public class MeasurementSource {

	Handler clicksDetectedUiHandler;
	
	private MeasurementMode measurementMode=MeasurementMode.COUNT;
	
	public String getMeasurementModeDisplayName() {
		if (measurementMode==MeasurementMode.COUNT) return "Clicks (Total)";
		if (measurementMode==MeasurementMode.CPM) return "Est. clicks per minute (Sampled over 10 secs)";
		if (measurementMode==MeasurementMode.MSV) return "Est. mSv/h";
		return "Unknown mode";
	}
	
	Double clickCount = 0.0;
	MicrophoneThread micThread = null;

	Handler clicksDetectedMeasurementSourceHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			//Called from MicrophoneThread
			assert (msg.what>=1);
			int numClicksDetected=msg.what;
			
			clickCount=clickCount+numClicksDetected;
			if (clicksDetectedUiHandler!=null) {
				clicksDetectedUiHandler.sendEmptyMessage(numClicksDetected);
			}
		}
	};
	
	public MeasurementSource(Handler _clicksDetectedUiHandler) {
		clicksDetectedUiHandler = _clicksDetectedUiHandler;
		initMic();
	}

	private void initMic() {
		micThread = new MicrophoneThread(clicksDetectedMeasurementSourceHandler);
		micThread.start();
	}
	
	long lastTime=System.currentTimeMillis();
	Double lastCount=0.0;
	double minuteInMs = 1000*60; //1 min (duh!)
	double tenSecInMs = 1000*10;
	
	public Double getCpm() {
		long now = System.currentTimeMillis();

		double cpm;		
		double cpMs = (clickCount - lastCount)/(now-lastTime);
		cpm = cpMs*minuteInMs;
		
		if (lastTime > now + tenSecInMs){
			lastTime=now;
			lastCount=clickCount;
		}
		
		return cpm;
	}

	public Double getMsv() {
		//300CPM=1mSv/h (For Inspector Alert model)
		return getCpm()/300;
	}

	public Double getCount() {
		return clickCount;		
	}
	
	public Double getValue() {
		if (measurementMode==MeasurementMode.MSV) return getMsv();
		if (measurementMode==MeasurementMode.CPM) return getCpm();
		if (measurementMode==MeasurementMode.COUNT) return getCount();
		return 0.0;
	}

	public void setMode(MeasurementMode measurementMode) {
		this.measurementMode = measurementMode;
	}
}
