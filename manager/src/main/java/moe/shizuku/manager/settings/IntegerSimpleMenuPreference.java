package moe.shizuku.manager.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.View;

import androidx.annotation.ArrayRes;
import androidx.annotation.NonNull;
import androidx.annotation.StyleableRes;
import androidx.core.content.res.TypedArrayUtils;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import moe.shizuku.manager.R;
import rikka.preference.simplemenu.SimpleMenuPopupWindow;

/**
 * a {@link rikka.preference.SimpleMenuPreference} to implement night mode in user interface settings.
 * a {@link rikka.preference.SimpleMenuPreference} which use integer values array as entryValues.
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */

@SuppressLint("RestrictedApi")
public class IntegerSimpleMenuPreference extends Preference {

    private final SimpleMenuPopupWindow mPopupWindow;
    private View mAnchor;
    private View mItemView;

    private CharSequence[] mEntries;
    private int[] mEntryValues;
    private int mValue;
    private String mSummary;
    private boolean mValueSet;

    @SuppressLint("RestrictedApi")
    public IntegerSimpleMenuPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.ListPreference, defStyleAttr, defStyleRes);

        mEntries = TypedArrayUtils.getTextArray(a, R.styleable.ListPreference_entries,
                R.styleable.ListPreference_android_entries);

        mEntryValues = getIntArray(a, R.styleable.ListPreference_entryValues,
                R.styleable.ListPreference_android_entryValues);

        a.recycle();

        /* Retrieve the Preference summary attribute since it's private
         * in the Preference class.
         */
        a = context.obtainStyledAttributes(attrs,
                R.styleable.Preference, defStyleAttr, defStyleRes);

        mSummary = TypedArrayUtils.getString(a, R.styleable.Preference_summary,
                R.styleable.Preference_android_summary);

        a.recycle();

        a = context.obtainStyledAttributes(
                attrs, R.styleable.SimpleMenuPreference, defStyleAttr, defStyleRes);

        int popupStyle = a.getResourceId(R.styleable.SimpleMenuPreference_android_popupMenuStyle, R.style.Widget_Preference_SimpleMenuPreference_PopupMenu);
        int popupTheme = a.getResourceId(R.styleable.SimpleMenuPreference_android_popupTheme,R.style.ThemeOverlay_Preference_SimpleMenuPreference_PopupMenu);
        Context popupContext;
        if (popupTheme != 0) {
            popupContext = new ContextThemeWrapper(context, popupTheme);
        } else {
            popupContext = context;
        }
        mPopupWindow = new SimpleMenuPopupWindow(popupContext, attrs, R.styleable.SimpleMenuPreference_android_popupMenuStyle, popupStyle);
        mPopupWindow.setOnItemClickListener(i -> {
            int value = getEntryValues()[i];
            if (callChangeListener(value)) {
                setValue(value);
            }
        });

        a.recycle();
    }

    public IntegerSimpleMenuPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Preference_SimpleMenuPreference);
    }

    public IntegerSimpleMenuPreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.simpleMenuPreferenceStyle);
    }

    public IntegerSimpleMenuPreference(Context context) {
        this(context, null);
    }

    @SuppressLint("RestrictedApi")
    private static int[] getIntArray(TypedArray a, @StyleableRes int index,
                                     @StyleableRes int fallbackIndex) {
        int resourceId = TypedArrayUtils.getResourceId(a, index, fallbackIndex, 0);
        return a.getResources().getIntArray(resourceId);
    }

    @Override
    protected void onClick() {
        if (getEntries() == null || getEntries().length == 0) {
            return;
        }

        if (mPopupWindow == null) {
            return;
        }

        mPopupWindow.setEntries(getEntries());
        mPopupWindow.setSelectedIndex(findIndexOfValue(getValue()));

        View container = (View) mItemView   // itemView
                .getParent();               // -> list (RecyclerView)

        mPopupWindow.show(mItemView, container, (int) mAnchor.getX());
    }

    /**
     * Sets the human-readable entries to be shown in the list. This will be
     * shown in subsequent dialogs.
     * <p>
     * Each entry must have a corresponding index in
     * {@link #setEntryValues(int[])}.
     *
     * @param entries The entries.
     * @see #setEntryValues(int[])
     */
    public void setEntries(CharSequence[] entries) {
        mEntries = entries;

        mPopupWindow.requestMeasure();
    }

    /**
     * @param entriesResId The entries array as a resource.
     * @see #setEntries(CharSequence[])
     */
    public void setEntries(@ArrayRes int entriesResId) {
        setEntries(getContext().getResources().getTextArray(entriesResId));
    }

    /**
     * The list of entries to be shown in the list in subsequent dialogs.
     *
     * @return The list as an array.
     */
    public CharSequence[] getEntries() {
        return mEntries;
    }

    /**
     * The array to find the value to save for a preference when an entry from
     * entries is selected. If a user clicks on the second item in entries, the
     * second item in this array will be saved to the preference.
     *
     * @param entryValues The array to be used as values to save for the preference.
     */
    public void setEntryValues(int[] entryValues) {
        mEntryValues = entryValues;
    }

    /**
     * @param entryValuesResId The entry values array as a resource.
     * @see #setEntryValues(int[])
     */
    public void setEntryValues(@ArrayRes int entryValuesResId) {
        setEntryValues(getContext().getResources().getIntArray(entryValuesResId));
    }

    /**
     * Returns the array of values to be saved for the preference.
     *
     * @return The array of values.
     */
    public int[] getEntryValues() {
        return mEntryValues;
    }

    /**
     * Sets the value of the key. This should be one of the entries in
     * {@link #getEntryValues()}.
     *
     * @param value The value to set for the key.
     */
    public void setValue(int value) {
        // Always persist/notify the first time.
        final boolean changed = mValue != value;
        if (changed || !mValueSet) {
            mValue = value;
            mValueSet = true;
            persistInt(value);
            if (changed) {
                notifyChanged();
            }
        }
    }

    /**
     * Returns the summary of this ListPreference. If the summary
     * has a {@linkplain java.lang.String#format String formatting}
     * marker in it (i.e. "%s" or "%1$s"), then the current entry
     * value will be substituted in its place.
     *
     * @return the summary with appropriate string substitution
     */
    @Override
    public CharSequence getSummary() {
        final CharSequence entry = getEntry();
        if (mSummary == null) {
            return super.getSummary();
        } else {
            return String.format(mSummary, entry == null ? "" : entry);
        }
    }

    /**
     * Sets the summary for this Preference with a CharSequence.
     * If the summary has a
     * {@linkplain java.lang.String#format String formatting}
     * marker in it (i.e. "%s" or "%1$s"), then the current entry
     * value will be substituted in its place when it's retrieved.
     *
     * @param summary The summary for the preference.
     */
    @Override
    public void setSummary(CharSequence summary) {
        super.setSummary(summary);
        if (summary == null && mSummary != null) {
            mSummary = null;
        } else if (summary != null && !summary.equals(mSummary)) {
            mSummary = summary.toString();
        }
    }

    /**
     * Sets the value to the given index from the entry values.
     *
     * @param index The index of the value to set.
     */
    public void setValueIndex(int index) {
        if (mEntryValues != null) {
            setValue(mEntryValues[index]);
        }
    }

    /**
     * Returns the value of the key. This should be one of the entries in
     * {@link #getEntryValues()}.
     *
     * @return The value of the key.
     */
    public int getValue() {
        return mValue;
    }

    /**
     * Returns the entry corresponding to the current value.
     *
     * @return The entry corresponding to the current value, or null.
     */
    public CharSequence getEntry() {
        int index = getValueIndex();
        return index >= 0 && mEntries != null ? mEntries[index] : null;
    }

    /**
     * Returns the index of the given value (in the entry values array).
     *
     * @param value The value whose index should be returned.
     * @return The index of the value, or -1 if not found.
     */
    public int findIndexOfValue(int value) {
        if (mEntryValues != null) {
            for (int i = mEntryValues.length - 1; i >= 0; i--) {
                int entryValue = mEntryValues[i];
                if (entryValue == value) {
                    return i;
                }
            }
        }
        return -1;
    }

    private int getValueIndex() {
        return findIndexOfValue(mValue);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, 1);
    }

    @Override
    protected void onSetInitialValue(Object defaultValue) {
        if (defaultValue == null) {
            defaultValue = 0;
        }
        setValue(getPersistedInt((Integer) defaultValue));
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            // No need to save instance state since it's persistent
            return superState;
        }

        final SavedState myState = new SavedState(superState);
        myState.value = getValue();
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        setValue(myState.value);
    }

    private static class SavedState extends BaseSavedState {
        int value;

        public SavedState(Parcel source) {
            super(source);
            value = source.readInt();
        }

        @Override
        public void writeToParcel(@NonNull Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(value);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        mItemView = holder.itemView;
        mAnchor = holder.itemView.findViewById(android.R.id.empty);

        if (mAnchor == null) {
            throw new IllegalStateException("SimpleMenuPreference item layout must contain" +
                    "a view id is android.R.id.empty to support iconSpaceReserved");
        }
    }
}
