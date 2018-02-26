package ptcorp.ptapplication;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by LinusHakansson on 2018-02-21.
 */

public class FirebaseDatabaseHandler {
    private static final String TAG = "FirebaseDatabaseHandler";
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseRef;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser user;
    private String userID;
    private HashMap<String,Score> mScoreList;
    private OnDatabaseUpdateListener mListener;

    public FirebaseDatabaseHandler(FirebaseAuth mFirebaseAuth) {
        this.mFirebaseAuth = mFirebaseAuth;
        user = mFirebaseAuth.getCurrentUser();
        userID = user.getUid();
        mScoreList = new HashMap<>();

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseRef = mFirebaseDatabase.getReference();
        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                //TODO: Update recyclerView adapter from here
                showData(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", databaseError.toException());
            }
        });
        testSaveScore();
    }

    /*
    * Just a test method
    * Will be deleted.
    * */
    private void testSaveScore(){
        Log.d(TAG, "testSaveScore: called." );
        String [] split  = user.getEmail().split("@");
        String username = split[0];
        Score score = new Score();
        score.setUsername(username);
        score.setWonGames(3);
        score.setLostGames(4);
        saveScore(score);
    }

    public void updateScoreForUser(Score newScore) {
        Score score;
        if (mScoreList.containsKey(userID)) {
            score = mScoreList.get(userID);
            if (score.getWonGames() == newScore.getWonGames()) {
                score.setLostGames(score.getLostGames() + 1);
            } else {
                score.setWonGames(score.getWonGames() + 1);
            }
        } else{
            score = new Score();
            if(newScore.getWonGames() == 1){
                score.setWonGames(1);
            }else{
                score.setLostGames(1);
            }
        }
        saveScore(score);
    }


    private void showData(DataSnapshot dataSnapshot) {
        Log.d(TAG, "showData:" + dataSnapshot.toString());

        for (DataSnapshot dsScore : dataSnapshot.child("Leaderboard").child("Scores").getChildren()) {
            Log.d(TAG, "showData: "+ dsScore); // TODO: FIXA DETTA
            Score score = new Score();
            Log.d(TAG, "showData: " + dsScore.toString());
            score.setUsername(dsScore.getValue(Score.class).getUsername());
            score.setWonGames(dsScore.getValue(Score.class).getWonGames());
            score.setLostGames(dsScore.getValue(Score.class).getLostGames());
            mScoreList.put(userID, score);
        }
        List<Score> list = new ArrayList<>(mScoreList.values());
//            mListener.updateAdapter(list);
    }

    public void addOnUpdateListener(OnDatabaseUpdateListener listener){
        mListener = listener;
    }

    private void saveScore(Score score){
        mDatabaseRef.child("Leaderboard").child("Scores").child(userID).setValue(score);
    }

    public interface OnDatabaseUpdateListener {
        void updateAdapter(List<Score> list);
    }
}
