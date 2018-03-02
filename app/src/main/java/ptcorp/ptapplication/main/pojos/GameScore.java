package ptcorp.ptapplication.main.pojos;

/**
 * Created by Pontus on 2018-02-22.
 */

public class GameScore {

    private String player1, player2, date;
    private int score1, score2;

    public GameScore(String player1, String player2, String date, int score1, int score2) {
        this.player1 = player1;
        this.player2 = player2;
        this.date = date;
        this.score1 = score1;
        this.score2 = score2;
    }

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
}
