package com.example.postit;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.github.proportionsbarlibrary.ProportionsBar;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;

public class ProportionBarView extends View {

    public ProportionBarView(Context context){
        super(context);
    }

    public ProportionBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ProportionBarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    //draw rounded edges
    private boolean showRoundedCorners;
    //radius of the rounded corners
    private double roundedCornerRadius = 1.4;
    //show GAPS
    private boolean showGaps = true;
    //GAPs' size in % of the container view's width
    private double gapSize = 1.0;
    //GAPs' color
    private int gapColor = getResources().getColor(R.color.white);
    //minimal segment value to be shown in % of the bar width (meaning: values between >0% and <2% will be shown as 2% section)
    private int minimalSegmentValue = 2;
    //list of data values
    private ArrayList<Double> valueDoubleList = new ArrayList<>();
    //    private ArrayList<Double> valueDoubleList = new ArrayList<>();
//    private ArrayList<Float> valueFloatList = new ArrayList<>();
    //height of custom view in % of parent view
    private double scaleH = 100;

    private boolean firstLaunch = true;
    //play animation
    private boolean animated = false;
    //animation duration
    private int animationDuration = 1000;

    //proportion values list
    public List<Float> proportionValueList = new ArrayList<>();

    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private List<Integer> intColors = new ArrayList<>();
    private List<String> stringColors = new ArrayList<>();

    private Queue<Integer> colorQueue = new ArrayDeque<>();
    private final ArrayList<String> defaultColors = new ArrayList<>(Arrays.asList("#81DAF5", "#008db9", "#1c0a63"));

    public ProportionBarView showRoundedCorners(boolean show) {
        this.showRoundedCorners = show;
        return this;
    }

    public ProportionBarView radiusRoundedCorners(double radius) {
        this.roundedCornerRadius = radius;
        return this;
    }

    public ProportionBarView showGaps(boolean show) {
        this.showGaps = show;
        return this;
    }

    public ProportionBarView gapSize(double size) {
        this.gapSize = size;
        return this;
    }

    public ProportionBarView gapColor(int gapColor) {
        this.gapColor = gapColor;
        return this;
    }

    public ProportionBarView gapColor(String gapColor) {
        this.gapColor = Color.parseColor(gapColor);
        return this;
    }

    public ProportionBarView minimalSegmentValue(int minimalSegmentValue) {
        this.minimalSegmentValue = minimalSegmentValue;
        return this;
    }

    public ProportionBarView animated(boolean animated) {
        this.animated = animated;
        return this;
    }

    public ProportionBarView animationDuration(int duration) {
        this.animationDuration = duration;
        return this;
    }

    public ProportionBarView addValues(Number... values) {
        for (Number iterator : values) {
            if (iterator != null)
                this.valueDoubleList.add(Math.abs(Double.valueOf(String.valueOf(iterator))));
        }
        return this;
    }

    public ProportionBarView addColors(Integer... colors) {
        this.intColors.addAll(Arrays.asList(colors));
        return this;
    }

    public ProportionBarView addColors(String... colors) {
        this.stringColors.addAll(Arrays.asList(colors));
        return this;
    }

    public ProportionBarView heightOfBar(int h) {
        this.scaleH = h;
        return this;
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ArrayList<Float> tempDouble = new ArrayList<>();
        //instantiate proportions list
        if (valueDoubleList != null) tempDouble = getProportionValues(valueDoubleList);
        //fill percent list used for segment drawing
        if (valueDoubleList != null) {
            for (int i = 0; i < valueDoubleList.size(); i++)
                proportionValueList.add(tempDouble.get(i));
        }
        //check for the first view launch animation
        if (animated && firstLaunch) playAnimation();
        firstLaunch = false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        initColorQueue();

        //width of container view
        float wBig = getWidth();
        //rounded edge radius
        float r = (float) (wBig * roundedCornerRadius / 100);
        //X coordinate of the last element
        float tempX = 0;
        //height of container view
        float h = (float) (getHeight() * scaleH / 100);

        float wSmall;
        if (!showRoundedCorners) {
            wSmall = wBig;
        } else {
            wSmall = (float) (getWidth() - 2 * r - gapSize * (valueDoubleList.size() - 1));
        }
        //size of gaps (depends from container view width and denominator)
        float gapSize = (float) (wBig * this.gapSize / 100);

        //draw SEGMENTS based in the percent values proportions
        for (int k = 0; k < proportionValueList.size(); k++) {
            if (k == 0) {
                //FIRST segment
                paint.setColor(getColorFromQueue());
                //draw rounded edge
                if (showRoundedCorners) {
                    drawArc(canvas, 0, 2 * r, h, 90);
                    tempX = r;
                }
                //draw first rectangle
                drawRectangle(canvas, tempX - 1, tempX + (wSmall * proportionValueList.get(k) / 100) - gapSize / 2, h);
                tempX += (wSmall * proportionValueList.get(k) / 100) - gapSize / 2;

            } else if (k == proportionValueList.size() - 1) {
                //LAST SEGMENT
                //draw gap
                if (showGaps) {
                    drawGap(canvas, tempX, gapSize, h);
                    tempX += gapSize;
                }
                if (showRoundedCorners) {
                    //draw last rectangle
                    paint.setColor(getColorFromQueue());
                    drawRectangle(canvas, tempX, wBig - r + 1, h);
                    tempX = wBig - r;
                    //draw rounded edge
                    drawArc(canvas, tempX - r, wBig, h, 270);
                } else {
                    paint.setColor(getColorFromQueue());
                    drawRectangle(canvas, tempX, wSmall, h);
                    tempX = 0;
                }
            } else {
                //MID segments
                //draw gap
                if (showGaps) {
                    drawGap(canvas, tempX, gapSize, h);
                    tempX += gapSize;
                }
                //draw mid rectangle
                paint.setColor(getColorFromQueue());
                drawRectangle(canvas, tempX, tempX + (wSmall * proportionValueList.get(k) / 100) - gapSize / 2, h);
                tempX += (wSmall * proportionValueList.get(k) / 100) - gapSize / 2;
            }
        }
    }

    private void drawArc(Canvas canvas, float start, float end, float h, int startAngle) {
        canvas.drawArc(start, 0, end, h, startAngle, 180, true, paint);
    }

    private void drawRectangle(Canvas canvas, float start, float end, float h) {
        canvas.drawRect(start, 0, end, h, paint);
    }

    private void drawGap(Canvas canvas, float start, float gap, float h) {
        paint.setColor(gapColor);
        canvas.drawRect(start, 0, start + gap, h, paint);
    }

    //transform array of values into array of proportions ( % values )
    private ArrayList<Float> getProportionValues(ArrayList<Double> val) {
        double sum = 0;
        ArrayList<Float> percentValues = new ArrayList<>();
        //get sum of all arguments
        for (double iterator : val) sum += iterator;
        //divide each element by sum to get % values
        for (int v = 0; v < val.size(); v++) {
            //check for minimalSegmentValue
            if ((val.get(v) * 100 / sum) > 0 && (val.get(v) * 100 / sum) < minimalSegmentValue) {
                percentValues.add((float) minimalSegmentValue);
            } else {
                percentValues.add((float) (val.get(v) * 100 / sum));
            }
        }
        return percentValues;
    }

    private void initColorQueue() {
        colorQueue.clear();
        if (intColors != null) colorQueue.addAll(intColors);
        if (stringColors != null) colorQueue.addAll(transformStringColorToInt(stringColors));
        //add default color if colorQueue is empty
        if (colorQueue != null) {
            colorQueue.addAll(transformStringColorToInt(defaultColors));
        }
    }

    private List<Integer> transformStringColorToInt(List<String> listString) {
        List<Integer> listInt = new ArrayList<>();
        for (String iterator : listString) listInt.add(Color.parseColor(iterator));
        return listInt;
    }

    //return Int value from queue of intColors
    private Integer getColorFromQueue() {
        Integer temp = colorQueue.poll();
        //adds color back to queue
        colorQueue.offer(temp);
        return temp;
    }

    //currently only for proportionValueList.size() = 3
    public void playAnimation() {
        //setup animations for ProportionBarView4
        AnimatorSet animSet = new AnimatorSet();
        ObjectAnimator animIntList1 = ObjectAnimator
                .ofFloat(this, "FirstSegment", 0, this.proportionValueList.get(0));
        animIntList1.setDuration(animationDuration);
        ObjectAnimator animIntList2 = ObjectAnimator
                .ofFloat(this, "SecondSegment", this.proportionValueList.get(0), this.proportionValueList.get(1));
        animIntList2.setDuration(animationDuration);
        animSet.playTogether(animIntList1, animIntList2);
        animSet.start();
    }

    //setters are needed to animate custom view via external ObjectAnimator
    public void setFirstSegment(float i) {
        this.proportionValueList.set(0, i);
        //redraw custom view on every argument change
        invalidate();
    }

    public void setSecondSegment(float j) {
        this.proportionValueList.set(1, j);
        invalidate();
    }

    public void setThirdSegment(float k) {
        this.proportionValueList.set(2, k);
        invalidate();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);

        ss.firstLaunch = this.firstLaunch;
        return ss;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());

        this.firstLaunch = ss.firstLaunch;
    }

    //nested class for saving view's state
    static class SavedState extends BaseSavedState {
        boolean firstLaunch;

        SavedState(Parcel in) {
            super(in);
            this.firstLaunch = in.readInt() == 1;
        }

        SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(firstLaunch ? 0 : 1);
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }


    public void setDataList(Integer[] integers){
        if(integers==null) return;
        valueDoubleList.clear();
        for (int i = 0; i < integers.length; i++) {
            valueDoubleList.add(integers[i].doubleValue());
        }

        ArrayList<Float> tempDouble = new ArrayList<>();
        proportionValueList.clear();
        //instantiate proportions list
        if (valueDoubleList != null) tempDouble = getProportionValues(valueDoubleList);
        //fill percent list used for segment drawing
        if (valueDoubleList != null) {
            for (int i = 0; i < valueDoubleList.size(); i++)
                proportionValueList.add(tempDouble.get(i));
        }
        invalidate();
    }



}