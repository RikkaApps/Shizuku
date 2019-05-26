package moe.shizuku.manager.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import moe.shizuku.manager.R;

import static java.util.Objects.requireNonNull;

public class MaterialCircleIconView extends ImageView {

    @NonNull
    private String mIconForegroundChroma;
    @NonNull
    private String mIconBackgroundChroma;
    @NonNull
    private String mColorName;

    public MaterialCircleIconView(Context context) {
        this(context, null);
    }

    public MaterialCircleIconView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.materialCircleIconViewStyle);
    }

    public MaterialCircleIconView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Widget_MaterialCircleIconView_Light);
    }

    public MaterialCircleIconView(Context context, @Nullable AttributeSet attrs,
                                  int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        final TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.MaterialCircleIconView, defStyleAttr, defStyleRes);

        if (a.hasValue(R.styleable.MaterialCircleIconView_iconBackgroundChroma)) {
            mIconBackgroundChroma = requireNonNull(a.getString(
                    R.styleable.MaterialCircleIconView_iconBackgroundChroma));
        } else {
            mIconBackgroundChroma = "50";
        }
        if (a.hasValue(R.styleable.MaterialCircleIconView_iconForegroundChroma)) {
            mIconForegroundChroma = requireNonNull(a.getString(
                    R.styleable.MaterialCircleIconView_iconForegroundChroma));
        } else {
            mIconForegroundChroma = "50";
        }
        if (a.hasValue(R.styleable.MaterialCircleIconView_iconColorName)) {
            mColorName = requireNonNull(a.getString(
                    R.styleable.MaterialCircleIconView_iconColorName));
        } else {
            mColorName = "blue";
        }

        a.recycle();

        updateIconBackgroundColor();
        updateIconForegroundColor();
    }

    @NonNull
    public String getIconForegroundChroma() {
        return mIconForegroundChroma;
    }

    @NonNull
    public String getIconBackgroundChroma() {
        return mIconBackgroundChroma;
    }

    @NonNull
    public String getColorName() {
        return mColorName;
    }

    public void setIconForegroundChroma(@NonNull String iconForegroundChroma) {
        mIconForegroundChroma = iconForegroundChroma;
        updateIconForegroundColor();
    }

    public void setIconBackgroundChroma(@NonNull String iconBackgroundChroma) {
        mIconBackgroundChroma = iconBackgroundChroma;
        updateIconBackgroundColor();
    }

    public void setColorName(@NonNull String colorName) {
        mColorName = colorName;
        updateIconBackgroundColor();
        updateIconForegroundColor();
    }

    @ColorRes
    public int getIconForegroundColorResource() {
        return getResources().getIdentifier(
                "material_" + mColorName + "_" + mIconForegroundChroma, "color", getContext().getPackageName());
    }

    @ColorRes
    public int getIconBackgroundColorResource() {
        return getResources().getIdentifier(
                "material_" + mColorName + "_" + mIconBackgroundChroma, "color", getContext().getPackageName());
    }

    @ColorInt
    public int getIconForegroundColor() {
        return ContextCompat.getColor(getContext(), getIconForegroundColorResource());
    }

    @ColorInt
    public int getIconBackgroundColor() {
        return ContextCompat.getColor(getContext(), getIconBackgroundColorResource());
    }

    private void updateIconForegroundColor() {
        setImageTintList(ColorStateList.valueOf(getIconForegroundColor()));
    }

    private void updateIconBackgroundColor() {
        setBackgroundTintList(ColorStateList.valueOf(getIconBackgroundColor()));
    }

}
