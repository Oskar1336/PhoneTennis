package ptcorp.ptapplication.game.pojos;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by LinusHakansson on 2018-03-01.
 */

public class StrikeInformation implements Parcelable {
    private int acceleration, strength, direction;

    public StrikeInformation(int acceleration, int strength, int direction) {
        this.acceleration = acceleration;
        this.strength = strength;
        this.direction = direction;
    }

    public int getAcceleration() {
        return this.acceleration;
    }

    public int getStrength() {
        return this.strength;
    }

    public int getDirection() {
        return this.direction;
    }

    protected StrikeInformation(Parcel in) {
        int [] data = new int[3];
        in.readIntArray(data);
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
        dest.writeIntArray(new int[]{this.acceleration, this.strength, this.direction});
    }
}
