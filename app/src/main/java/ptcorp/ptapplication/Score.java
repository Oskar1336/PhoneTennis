package ptcorp.ptapplication;

/**
 * Created by LinusHakansson on 2018-02-21.
 * Carry information about a finished game
 */

public class Score {
    private String username;
    private int wonGames, lostGames;

    public Score(String username, int wonGames, int lostGames) {
        this.username = username;
        this.wonGames = wonGames;
        this.lostGames = lostGames;
    }

    public String getUsername() {
        return username;
    }

    public int getWonGames() {
        return wonGames;
    }

    public int getLostGames() {
        return lostGames;
    }
}
