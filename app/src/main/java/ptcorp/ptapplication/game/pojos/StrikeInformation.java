package ptcorp.ptapplication.game.pojos;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by LinusHakansson on 2018-03-01.
 */

public class StrikeInformation implements Parcelable, Serializable {
    private float acceleration, strength, direction;

    public StrikeInformation(float acceleration, float strength, float direction) {
        this.acceleration = acceleration;
        this.strength = strength;
        this.direction = direction;
    }

    public float getAcceleration() {
        return this.acceleration;
    }

    public float getStrength() {
        return this.strength;
    }

    public float getDirection() {
        return this.direction;
    }

    protected StrikeInformation(Parcel in) {
        float [] data = new float[3];
        in.readFloatArray(data);
        this.acceleration = data[0];
        this.strength = data[1];
        this.direction = data[2];
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
        dest.writeFloatArray(new float[]{this.acceleration, this.strength, this.direction});
    }
}
