package ptcorp.ptapplication;

/**
 * Created by LinusHakansson on 2018-02-21.
 * Carry information about a finished game
 */

public class Score {
    private String username;
    private long wonGames, lostGames;

    public Score() {

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
