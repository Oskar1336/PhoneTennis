package ptcorp.ptapplication.game.pojos;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by LinusHakansson on 2018-03-06.
 */

public class PlayerPositions implements Parcelable, Serializable {
    private float mClientPosition, mHostPosition;

    protected PlayerPositions(Parcel in) {
        mClientPosition = in.readFloat();
        mHostPosition = in.readFloat();
    }

    public PlayerPositions(){}

    public static final Creator<PlayerPositions> CREATOR = new Creator<PlayerPositions>() {
        @Override
        public PlayerPositions createFromParcel(Parcel in) {
            return new PlayerPositions(in);
        }

        @Override
        public PlayerPositions[] newArray(int size) {
            return new PlayerPositions[size];
        }
    };

    public float getmClientPosition() {
        return mClientPosition;
    }

    public void setmClientPosition(float mClientPosition) {
        this.mClientPosition = mClientPosition;
    }

    public float getmHostPosition() {
        return mHostPosition;
    }

    public void setmHostPosition(float mHostPosition) {
        this.mHostPosition = mHostPosition;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(mClientPosition);
        dest.writeFloat(mHostPosition);
    }
}


