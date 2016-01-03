package com.afollestad.polar.util;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewTreeObserver;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.polar.R;


/**
 * @author Aidan Follestad (afollestad)
 */
public class Utils {

//    public static int resolveDrawable(Context context, int drawable) {
//        TypedArray a = context.obtainStyledAttributes(new int[]{drawable});
//        int resId = a.getResourceId(0, 0);
//        a.recycle();
//        return resId;
//    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0)
            result = context.getResources().getDimensionPixelSize(resourceId);
        return result;
    }

//    public static int adjustAlpha(int color, @SuppressWarnings("SameParameterValue") float factor) {
//        int alpha = Math.round(Color.alpha(color) * factor);
//        int red = Color.red(color);
//        int green = Color.green(color);
//        int blue = Color.blue(color);
//        return Color.argb(alpha, red, green, blue);
//    }

    public static void showError(Context context, Exception e) {
        e.printStackTrace();
        new MaterialDialog.Builder(context)
                .title(R.string.error)
                .content(e.getMessage())
                .positiveText(android.R.string.ok)
                .show();
    }

//    @Size(2)
//    public static int[] getScreenDimensions(Activity activity) {
//        final Display display = activity.getWindowManager().getDefaultDisplay();
//        final Point size = new Point();
//        display.getSize(size);
//        return new int[]{size.x, size.y};
//    }

    @ColorInt
    public static int darkenColor(@ColorInt int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f; // value component
        color = Color.HSVToColor(hsv);
        return color;
    }

    public interface LayoutCallback<VT extends View> {
        void onLayout(VT view);
    }

    public static <VT extends View> void waitForLayout(@NonNull final VT view, @NonNull final LayoutCallback<VT> cb) {
        ViewTreeObserver viewTreeObserver = view.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    cb.onLayout(view);
                }
            });
        }
    }

    private Utils() {
        // This utility class should not be instantiated.
    }
}