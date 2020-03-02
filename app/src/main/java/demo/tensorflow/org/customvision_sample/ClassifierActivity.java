/*
 * Copyright 2016 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package demo.tensorflow.org.customvision_sample;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Typeface;

import android.media.ImageReader.OnImageAvailableListener;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;
import android.view.Display;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import demo.tensorflow.org.customvision_sample.OverlayView.DrawCallback;
import demo.tensorflow.org.customvision_sample.env.BorderedText;
import demo.tensorflow.org.customvision_sample.env.Logger;

public class ClassifierActivity extends CameraActivity implements OnImageAvailableListener,ResultsView {
    private static final Logger LOGGER = new Logger();

    private static final Size DESIRED_PREVIEW_SIZE = new Size(640, 480);
    private static final float TEXT_SIZE_DIP = 10;

    private Integer sensorOrientation;
    private ICognitiveServicesClassifier classifier;
    private BorderedText borderedText;
    private TextToSpeech myTTS;
    final private List<Classifier.Recognition> results = new ArrayList<>();


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        classifier = new MSCognitiveServicesCustomVisionClassifier(this);
    }

    @Override
    public synchronized void onStop() {
        super.onStop();

        if (classifier != null) {
            classifier.close();
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.camera_connection_fragment;
    }

    @Override
    protected Size getDesiredPreviewFrameSize() {
        return DESIRED_PREVIEW_SIZE;
    }

    @Override
    public void onPreviewSizeChosen(final Size size, final int rotation) {
        final float textSizePx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
        borderedText = new BorderedText(textSizePx);
        borderedText.setTypeface(Typeface.MONOSPACE);

        previewWidth = size.getWidth();
        previewHeight = size.getHeight();

        final Display display = getWindowManager().getDefaultDisplay();
        final int screenOrientation = display.getRotation();

        LOGGER.i("Sensor orientation: %d, Screen orientation: %d", rotation, screenOrientation);

        sensorOrientation = rotation + screenOrientation;

        LOGGER.i("Initializing at size %dx%d", previewWidth, previewHeight);
        rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Config.ARGB_8888);

        yuvBytes = new byte[3][];

        addCallback(
                new DrawCallback() {
                    @Override
                    public void drawCallback(final Canvas canvas) {
                        renderDebug(canvas);
                    }
                });
    }

    protected void processImageRGBbytes(int[] rgbBytes) {
        rgbFrameBitmap.setPixels(rgbBytes, 0, previewWidth, 0, 0, previewWidth, previewHeight);
        //final List<Classifier.Recognition> results = new ArrayList<>();

        runInBackground(
                new Runnable() {
                    @Override
                    public void run() {
                        final long startTime = SystemClock.uptimeMillis();
                        Classifier.Recognition r = classifier.classifyImage(rgbFrameBitmap, sensorOrientation);
                        lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;

                        //final List<Classifier.Recognition> results = new ArrayList<>();

                        LOGGER.i("Detect before: %s", results);

                        if (r.getConfidence() > 0.7) {
                            results.add(r);
                        }

                        LOGGER.i("Detect: %s", results);
                        if (resultsView == null) {
                            resultsView = findViewById(R.id.results);
                        }
                        resultsView.setResults(results);
                        Log.i("Results:",results.toString());
                        requestRender();
                        computing = false;
                        if (postInferenceCallback != null) {
                            postInferenceCallback.run();
                        }
                    }
                });
        myTTS=new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (myTTS.getEngines().size()==0){
                    Toast.makeText(ClassifierActivity.this,"There is no TTS engine on your device",Toast.LENGTH_LONG).show();
                    finish();
                }
                else{
                    myTTS.setLanguage(Locale.US);
                    Log.i("Speak:",results.toString());
                    speak(results.toString());
                }
            }
        });

    }

    @Override
    public void onSetDebug(boolean debug) {
    }

    private void renderDebug(final Canvas canvas) {
        if (!isDebug()) {
            return;
        }

        final Vector<String> lines = new Vector<String>();
        lines.add("Inference time: " + lastProcessingTimeMs + "ms");
        borderedText.drawLines(canvas, 10, canvas.getHeight() - 10, lines);
    }
    private void speak(String message){
        if(Build.VERSION.SDK_INT>=21){
            myTTS.speak(message, TextToSpeech.QUEUE_FLUSH,null,null);
        }
        else{
            myTTS.speak(message,TextToSpeech.QUEUE_FLUSH,null);
        }
    }

    @Override
    public void setResults(List<Classifier.Recognition> results) {

    }
}
