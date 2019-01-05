package ffournier.libscoregraphview;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorInt;

/**
 * Class ScoreFactor
 * class which contains the data of a score
 * Score
 * Title
 * Color
 */
public class ScoreFactor implements Parcelable {

    //Declaration
    public float mScore;
    public String mTitle;
    public int mColor;

    /**
     * Constructor
     * @param score : the score in percent between 0 and 1
     * @param title : the title of score
     * @param color : the color of score
     */
    public ScoreFactor(float score, String title, @ColorInt int color) {
        this.mScore = score;
        this.mTitle = title;
        this.mColor = color;
    }

    /**
     * Constructor Parcelable
     * @param in : Parcel
     */
    protected ScoreFactor(Parcel in) {
        mScore = in.readFloat();
        mTitle = in.readString();
        mColor = in.readInt();
    }

    /**
     * Creator for Parcelable
     */
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
