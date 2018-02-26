package ptcorp.ptapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.icu.util.Calendar;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pontus on 2018-02-26.
 */

public class GamesDatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "GamesDatabase";
    private static final String TABLE_GAMES = "Games";
    private static final String ID = "id";
    private static final String PLAYER1 = "player1";
    private static final String PLAYER2 = "player2";
    private static final String SCORE1 = "score1";
    private static final String SCORE2 = "score2";
    private static final String CREATION_DATE = "creationdate";
    private static final String
            CREATE_TABLE_GAMES = "CREATE TABLE "
            + TABLE_GAMES + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + CREATION_DATE + " TEXT,"+ PLAYER1
            + " TEXT,"+ PLAYER2 + " TEXT," + SCORE1 + " TEXT," + SCORE2 + " TEXT" + ")";

    public GamesDatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_GAMES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("drop table if exists " + DATABASE_NAME);
        onCreate(db);
    }

    public List<GameScore> getGames()
    {
        List<GameScore> mGamesList = new ArrayList<GameScore>();
        String selectQuery = "SELECT * FROM " +
                TABLE_GAMES;
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor c = db.rawQuery(selectQuery, null);
            if (c.moveToFirst()) {
                do {
                    GameScore mGame = new GameScore(c.getString(c.getColumnIndex(PLAYER1)), c.getString
                            (c.getColumnIndex(PLAYER2)), c.getString(c.getColumnIndex(CREATION_DATE)),
                            Integer.parseInt(c.getString(c.getColumnIndex(SCORE1))),
                            Integer.parseInt(c.getString(c.getColumnIndex(SCORE2))));
                    mGamesList.add(mGame);
                } while (c.moveToNext());
            }
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mGamesList;
    }

    public boolean addGame(GameScore game){
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(CREATION_DATE, game.getDate());

        } catch (Exception e){

        }
    }

}
