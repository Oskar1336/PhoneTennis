package ptcorp.ptapplication.game.pojos;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by LinusHakansson on 2018-03-02.
 */

public class GameSettings implements Parcelable {
    private final int mPlayerStarting;
    private int mGameBalls = 7;

    public GameSettings(int mPlayerStarting) {
        this.mPlayerStarting = mPlayerStarting;
    }

    protected GameSettings(Parcel in) {
        mPlayerStarting = in.readInt();
        mGameBalls = in.readInt();
    }

    public static final Creator<GameSettings> CREATOR = new Creator<GameSettings>() {
        @Override
        public GameSettings createFromParcel(Parcel in) {
            return new GameSettings(in);
        }

        @Override
        public GameSettings[] newArray(int size) {
            return new GameSettings[size];
        }
    };

    public int getmPlayerStarting() {
        return mPlayerStarting;
    }

    public int getmGameBalls() {
        return mGameBalls;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mPlayerStarting);
        dest.writeInt(mGameBalls);
    }
}
