package ptcorp.ptapplication.game.pojos;

import android.os.Parcel;
import android.os.Parcelable;

import ptcorp.ptapplication.game.enums.GameState;

/**
 * Created by LinusHakansson on 2018-03-09.
 */

public class InitGame implements Parcelable {
    private String opponentName;
    private GameState gameState;

    public InitGame(GameState gameState, String myName) {
        this.gameState = gameState;
        this.opponentName = myName;
    }

    protected InitGame(Parcel in) {
        opponentName = in.readString();
        String gameState = in.readString();
        this.gameState = GameState.valueOf(gameState);
    }

    public static final Creator<InitGame> CREATOR = new Creator<InitGame>() {
        @Override
        public InitGame createFromParcel(Parcel in) {
            return new InitGame(in);
        }

        @Override
        public InitGame[] newArray(int size) {
            return new InitGame[size];
        }
    };

    public String getOpponentName() {
        return opponentName;
    }

    public GameState getGameState() {
        return gameState;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(opponentName);
        dest.writeString(gameState.name());
    }
}
