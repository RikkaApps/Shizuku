package moe.shizuku.privileged.api.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.widget.TextView;

import moe.shizuku.privileged.api.R;

/**
 * Created by Rikka on 2017/5/14.
 */

public class HtmlTextView extends TextView {

    public HtmlTextView(Context context) {
        this(context, null);
    }

    public HtmlTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HtmlTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public HtmlTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        // Attribute initialization.
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.HtmlTextView,
                defStyleAttr, 0);

        String html = a.getString(R.styleable.HtmlTextView_textHtml);
        setHtmlText(html);

        a.recycle();
    }

    public void setHtmlText(String html) {
        if (html != null) {
            Spanned htmlDescription = Html.fromHtml(html);
            String descriptionWithOutExtraSpace = htmlDescription.toString().trim();

            setText(htmlDescription.subSequence(0, descriptionWithOutExtraSpace.length()));
        } else {
            setText(null);
        }
    }
}
