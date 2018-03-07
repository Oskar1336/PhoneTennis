package ptcorp.ptapplication.game.pojos;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by LinusHakansson on 2018-03-01.
 */

public class StrikeInformation implements Parcelable, Serializable {
    private float timeInAir, direction;

    public StrikeInformation(float timeInAir, float direction) {
        this.timeInAir = timeInAir;
        this.direction = direction;
    }

    public float getTimeInAir() {
        return this.timeInAir;
    }


    public float getDirection() {
        return this.direction;
    }

    protected StrikeInformation(Parcel in) {
        float [] data = new float[2];
        in.readFloatArray(data);
        this.timeInAir = data[0];
        this.direction = data[1];
    }

    public static final Creator<StrikeInformation> CREATOR = new Creator<StrikeInformation>() {
        @Override
        public StrikeInformation createFromParcel(Parcel in) {
            return new StrikeInformation(in);
        }

        @Override
        public StrikeInformation[] newArray(int size) {
            return new StrikeInformation[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloatArray(new float[]{this.timeInAir, this.direction});
    }
}
