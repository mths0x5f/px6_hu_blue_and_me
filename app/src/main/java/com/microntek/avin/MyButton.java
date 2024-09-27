package com.microntek.avin;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;

/* loaded from: classes.dex */
public class MyButton extends FrameLayout {
    ImageView imageView;
    ImageView imageView2;

    public MyButton(Context context) {
        super(context);
    }

    public MyButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        FrameLayout.LayoutParams lp2;
        TypedArray a = context.obtainStyledAttributes(attrs, C0005R.styleable.MyButton);
        int resId = a.getResourceId(2, 0);
        int width = a.getDimensionPixelSize(5, 10);
        int height = a.getDimensionPixelSize(0, 10);
        int resId2 = a.getResourceId(4, 0);
        if (resId2 != 0) {
            int width2 = a.getDimensionPixelSize(6, 0);
            int height2 = a.getDimensionPixelSize(1, 0);
            if (width2 == 0 || height2 == 0) {
                lp2 = new FrameLayout.LayoutParams(-1, -1, 17);
            } else {
                lp2 = new FrameLayout.LayoutParams(width2, height2, 17);
            }
            ImageView imageView = new ImageView(context);
            this.imageView2 = imageView;
            imageView.setImageResource(resId2);
            this.imageView2.setScaleType(ImageView.ScaleType.FIT_XY);
            this.imageView2.setDuplicateParentStateEnabled(true);
            this.imageView2.setLayoutParams(lp2);
            addView(this.imageView2);
        }
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(width, height, 17);
        ImageView imageView2 = new ImageView(context);
        this.imageView = imageView2;
        imageView2.setImageResource(resId);
        this.imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        this.imageView.setDuplicateParentStateEnabled(true);
        this.imageView.setLayoutParams(lp);
        addView(this.imageView);
    }

    public void setImageResource(int resId) {
        this.imageView.setImageResource(resId);
    }

    @Override // android.view.View
    public void setEnabled(boolean enable) {
        super.setEnabled(enable);
        if (enable) {
            this.imageView.setAlpha(255);
            getBackground().setAlpha(255);
        } else {
            this.imageView.setAlpha(80);
            getBackground().setAlpha(80);
        }
    }
}
