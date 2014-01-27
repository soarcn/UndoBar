package com.cocosw.undobar;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.animation.Animation;

import com.cocosw.undobar.R.drawable;

public class UndoBarStyle implements Parcelable {

    public static final int DEFAULT_DURATION = 5000;

    int iconRes;
    int titleRes;
    int bgRes = drawable.undobar;
    long duration = DEFAULT_DURATION;
    Animation inAnimation;
    Animation outAnimation;


    public UndoBarStyle(final int icon, final int title) {
        iconRes = icon;
        titleRes = title;
    }

    public UndoBarStyle(final int icon, final int title, final long duration) {
        this(icon, title);
        this.duration = duration;
    }

    public UndoBarStyle(final int icon, final int title, final int bg,
                        final long duration) {
        this(icon, title, duration);
        bgRes = bg;
    }

    @Override
    public String toString() {
        return "UndoBarStyle{" +
                "iconRes=" + iconRes +
                ", titleRes=" + titleRes +
                ", bgRes=" + bgRes +
                ", duration=" + duration +
                ", inAnimation=" + inAnimation +
                ", outAnimation=" + outAnimation +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;

        UndoBarStyle that = (UndoBarStyle) o;

        return bgRes == that.bgRes &&
                duration == that.duration &&
                iconRes == that.iconRes &&
                titleRes == that.titleRes;

    }



    /*
    * Parcelable-related methods.
    */
    public UndoBarStyle(Parcel source) {
        iconRes = source.readInt();
        titleRes = source.readInt();
        bgRes = source.readInt();
        duration = source.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(iconRes);
        dest.writeInt(titleRes);
        dest.writeInt(bgRes);
        dest.writeLong(duration);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<UndoBarStyle> CREATOR = new Parcelable.Creator<UndoBarStyle>() {
        public UndoBarStyle createFromParcel(Parcel source) {
            return new UndoBarStyle(source);
        }

        public UndoBarStyle[] newArray(int size) {
            return new UndoBarStyle[size];
        }
    };
}
