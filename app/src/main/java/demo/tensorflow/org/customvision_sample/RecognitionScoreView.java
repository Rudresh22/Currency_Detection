/* Copyright 2015 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package demo.tensorflow.org.customvision_sample;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Toast;

import demo.tensorflow.org.customvision_sample.Classifier.Recognition;
import demo.tensorflow.org.customvision_sample.env.Logger;

import java.util.List;
import java.util.Locale;

public class RecognitionScoreView extends View implements ResultsView {
    private static final float TEXT_SIZE_DIP = 24;
    private List<Recognition> results;
    private final float textSizePx;
    private final Paint fgPaint;

    public RecognitionScoreView(final Context context, final AttributeSet set) {
        super(context, set);

        textSizePx =
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
        fgPaint = new Paint();
        fgPaint.setTextSize(textSizePx);
    }

    @Override
    public void setResults(final List<Recognition> results) {
        this.results = results;
        postInvalidate();
    }

    @Override
    public void onDraw(final Canvas canvas) {

        fgPaint.setColor(Color.WHITE);

        if (results != null && results.size() > 0) {
            int y = (int) (fgPaint.getTextSize() * 1.4f);
            final Recognition recog = results.get(0);
            Log.i("onDraw:",results.toString());
            final int x = (int)(canvas.getWidth() - fgPaint.measureText(recog.getTitle())) / 2;
            canvas.drawText(recog.getTitle(), x, y, fgPaint);
        }

    }

}
