package ffournier.libscoregraphview;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorInt;

public class ScoreFactor implements Parcelable {

    public float mScore;
    public String mTitle;
    public int mColor;

    public ScoreFactor(float mScore, String mTitle, @ColorInt int color) {
        this.mScore = mScore;
        this.mTitle = mTitle;
        this.mColor = color;
    }

    protected ScoreFactor(Parcel in) {
        mScore = in.readFloat();
        mTitle = in.readString();
        mColor = in.readInt();
    }

    public static final Creator<ScoreFactor> CREATOR = new Creator<ScoreFactor>() {
        @Override
        public ScoreFactor createFromParcel(Parcel in) {
            return new ScoreFactor(in);
        }

        @Override
        public ScoreFactor[] newArray(int size) {
            return new ScoreFactor[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeFloat(mScore);
        parcel.writeString(mTitle);
        parcel.writeInt(mColor);
    }
}
