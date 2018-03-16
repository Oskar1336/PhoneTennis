package ptcorp.ptapplication.main.pojos;

import android.support.annotation.NonNull;

/**
 * Created by LinusHakansson on 2018-02-21.
 * Carry information about a finished game
 */

public class LeaderboardScore{
    private String username;
    private long wonGames, lostGames;

    public LeaderboardScore() {
        wonGames = 0;
        lostGames = 0;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setWonGames(long wonGames) {
        this.wonGames = wonGames;
    }

    public void setLostGames(long lostGames) {
        this.lostGames = lostGames;
    }

    public String getUsername() {
        return username;
    }

    public long getWonGames() {
        return wonGames;
    }

    public long getLostGames() {
        return lostGames;
    }

}
