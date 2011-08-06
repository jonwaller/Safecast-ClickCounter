package org.safecast.android.clickcounter;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.os.Handler;
import android.util.Log;

public class MicrophoneThread extends Thread {

	short lowThreshold=500;
	short highThreshold=16384;
	
	Handler clicksDetectedMeasurementSourceHandler = null; 

	private boolean stopped = false;

	public MicrophoneThread(Handler _clicksDetectedMeasurementSourceHandler) {
		clicksDetectedMeasurementSourceHandler=
			_clicksDetectedMeasurementSourceHandler;
		
		android.os.Process
				.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
	}

	private void processAudioChunk(short[] audioChunk) {
		assert (audioChunk.length == 160);
		if (clicksDetectedMeasurementSourceHandler != null) {
			int clicksDetected = countClicksInAudioChunk(audioChunk);
			if ( clicksDetected > 0) {
				clicksDetectedMeasurementSourceHandler.sendEmptyMessage(clicksDetected);
			}
		}
	}

	//The chunk is 0.02secs, so clicks closer than this will be ignored.
	private int countClicksInAudioChunk(short[] audioChunk) {

		assert (audioChunk.length == 160);

		int i = 0;
		for (i = 0; i < audioChunk.length-1; i++) {
			if (Math.abs(audioChunk[i])<lowThreshold &&
				Math.abs(audioChunk[i+1])>highThreshold) {
				//TODO Count more than one click per chunk
				return 1;
			}
		}	
		
		return 0;
	}

	@Override
	public void run() {
		AudioRecord recorder = null;
		short[][] buffers = new short[256][160];
		int ix = 0;

		try {
			int N = AudioRecord.getMinBufferSize(8000,
					AudioFormat.CHANNEL_CONFIGURATION_MONO,
					AudioFormat.ENCODING_PCM_16BIT);

			recorder = new AudioRecord(AudioSource.MIC, 8000,
					AudioFormat.CHANNEL_CONFIGURATION_MONO,
					AudioFormat.ENCODING_PCM_16BIT, N * 10);

			recorder.startRecording();

			while (!stopped) {
				short[] buffer = buffers[ix++ % buffers.length];
				N = recorder.read(buffer, 0, buffer.length);
				processAudioChunk(buffer);
			}
		} catch (Throwable x) {
			// Log error and carry on.
			Log.w("MicrophoneThread.run()",x);
		} finally {
			stopped = true;
		}
	}

}
