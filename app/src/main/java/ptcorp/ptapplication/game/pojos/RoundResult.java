package ptcorp.ptapplication.game.pojos;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by LinusHakansson on 2018-03-01.
 */

public class RoundResult implements Parcelable, Serializable {
    private int hostPoints=0, clientPoints=0;
    private final static int GAME_POINTS = 7;
    private RoundStatus roundStatus;
    private boolean isGameOver;

    public RoundResult() {
        // Default
    }

    private enum RoundStatus {
        HOSTWON,
        CLIENTWON
    }

    public void setGameOver(boolean gameOver) {
        this.isGameOver = gameOver;
    }

    public void setHostPoints() {
        this.hostPoints++;
        if (hostPoints==GAME_POINTS)
            isGameOver = true;
    }

    public void setClientPoints() {
        this.clientPoints++;
        if (clientPoints==GAME_POINTS){
            isGameOver = true;
        }
    }

    public void setRoundStatus(RoundStatus roundStatus) {
        this.roundStatus = roundStatus;
    }

    public int getHostPoints() {
        return hostPoints;
    }

    public int getClientPoints() {
        return clientPoints;
    }
    public boolean isGameOver() {
        return this.isGameOver;
    }

    public RoundStatus getRoundStatus() {
        return roundStatus;
    }

    protected RoundResult(Parcel in) {
        this.hostPoints = in.readInt();
        this.clientPoints = in.readInt();
        String roundStats = in.readString(); // enum parceled as String
        this.roundStatus = RoundStatus.valueOf(roundStats);
        this.isGameOver = in.readByte() != 0x00; // boolean...

    }

    public static final Creator<RoundResult> CREATOR = new Creator<RoundResult>() {
        @Override
        public RoundResult createFromParcel(Parcel in) {
            return new RoundResult(in);
        }

        @Override
        public RoundResult[] newArray(int size) {
            return new RoundResult[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(hostPoints);
        dest.writeInt(clientPoints);
        dest.writeString(roundStatus.name()); // send enum as string
        dest.writeByte((byte) (isGameOver ? 0x01 : 0x00)); // true or false
    }
}
