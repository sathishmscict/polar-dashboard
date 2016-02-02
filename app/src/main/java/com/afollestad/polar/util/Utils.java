package com.afollestad.polar.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.AppCompatImageView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.util.DialogUtils;
import com.afollestad.polar.R;
import com.afollestad.polar.config.Config;

import java.util.ArrayList;


/**
 * @author Aidan Follestad (afollestad)
 */
public abstract class Utils {

//    public static int resolveDrawable(Context context, int drawable) {
//        TypedArray a = context.obtainStyledAttributes(new int[]{drawable});
//        int resId = a.getResourceId(0, 0);
//        a.recycle();
//        return resId;
//    }

//    public static int getStatusBarHeight(Context context) {
//        int result = 0;
//        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
//        if (resourceId > 0)
//            result = context.getResources().getDimensionPixelSize(resourceId);
//        return result;
//    }

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

    @Nullable
    public static Drawable tintDrawable(@Nullable Drawable drawable, @ColorInt int color) {
        if (drawable == null) return null;
        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable, color);
        return drawable;
    }

    public static void setOverflowButtonColor(@NonNull Activity activity, final @ColorInt int color) {
        final String overflowDescription = activity.getString(R.string.abc_action_menu_overflow_description);
        final ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
        final ViewTreeObserver viewTreeObserver = decorView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                final ArrayList<View> outViews = new ArrayList<>();
                decorView.findViewsWithText(outViews, overflowDescription,
                        View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION);
                if (outViews.isEmpty()) return;
                final AppCompatImageView overflow = (AppCompatImageView) outViews.get(0);
                tintDrawable(overflow.getDrawable(), color);
                removeOnGlobalLayoutListener(decorView, this);
            }
        });
    }

    @SuppressWarnings("deprecation")
    public static void removeOnGlobalLayoutListener(View v, ViewTreeObserver.OnGlobalLayoutListener listener) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            v.getViewTreeObserver().removeGlobalOnLayoutListener(listener);
        } else {
            v.getViewTreeObserver().removeOnGlobalLayoutListener(listener);
        }
    }

    public static Drawable createCardSelector(Context context) {
        final int accentColor = DialogUtils.resolveColor(context, R.attr.colorAccent);
        final boolean darkTheme = Config.get().darkTheme();
        final int activated = TintUtils.adjustAlpha(accentColor, darkTheme ? 0.5f : 0.3f);
        final int pressed = TintUtils.adjustAlpha(accentColor, darkTheme ? 0.75f : 0.6f);

        final StateListDrawable baseSelector = new StateListDrawable();
        baseSelector.addState(new int[]{android.R.attr.state_activated}, new ColorDrawable(activated));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return new RippleDrawable(ColorStateList.valueOf(accentColor),
                    baseSelector, new ColorDrawable(Color.WHITE));
        }

        baseSelector.addState(new int[]{}, new ColorDrawable(Color.TRANSPARENT));
        baseSelector.addState(new int[]{android.R.attr.state_pressed}, new ColorDrawable(pressed));
        return baseSelector;
    }
}