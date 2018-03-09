package ptcorp.ptapplication.main.pojos;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by Pontus on 2018-02-22.
 */

public class GameScore implements Serializable, Parcelable{

    private String player1, player2, date;
    private int score1, score2;

    public GameScore(String player1, String player2, String date, int score1, int score2) {
        this.player1 = player1;
        this.player2 = player2;
        this.date = date;
        this.score1 = score1;
        this.score2 = score2;
    }


    protected GameScore(Parcel in) {
        player1 = in.readString();
        player2 = in.readString();
        date = in.readString();
        score1 = in.readInt();
        score2 = in.readInt();
    }

    public static final Creator<GameScore> CREATOR = new Creator<GameScore>() {
        @Override
        public GameScore createFromParcel(Parcel in) {
            return new GameScore(in);
        }

        @Override
        public GameScore[] newArray(int size) {
            return new GameScore[size];
        }
    };

    public String getPlayer1() {
        return player1;
    }

    public String getPlayer2() {
        return player2;
    }

    public String getDate() {
        return date;
    }

    public int getScore1() {
        return score1;
    }

    public int getScore2() {
        return score2;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(player1);
        parcel.writeString(player2);
        parcel.writeString(date);
        parcel.writeInt(score1);
        parcel.writeInt(score2);
    }
}
