package com.locart.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatSeekBar;

import com.locart.R;

//this custom seek bar will allow us to change the seek bar
// and thumb image color for older APIs with easy way
public class LocartSeekbar extends AppCompatSeekBar {
    public LocartSeekbar(Context context) {
        super(context);
    }

    public LocartSeekbar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public LocartSeekbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }


    private void init(Context context, AttributeSet attrs) {

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.LocartSeekbar, 0, 0);
        if (array != null) {

            //get given progress color from xml
            int progressColor = array.getColor(R.styleable.LocartSeekbar_progressColor, -1);
            if (progressColor != -1) {
                //change progress color
                Drawable progressDrawable = getProgressDrawable().mutate();
                progressDrawable.setColorFilter(progressColor, android.graphics.PorterDuff.Mode.SRC_IN);
                setProgressDrawable(progressDrawable);
            }
            //get given thumb color from xml
            int thumbColor = array.getColor(R.styleable.LocartSeekbar_thumbColor, -1);
            if (thumbColor != -1) {
                //change progress color
                Drawable thumbDrawable = getThumb().mutate();
                thumbDrawable.setColorFilter(thumbColor, android.graphics.PorterDuff.Mode.SRC_IN);
                setThumb(thumbDrawable);
            }

            array.recycle();
        }


    }

}