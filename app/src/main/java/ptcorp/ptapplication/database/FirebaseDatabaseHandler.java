package ptcorp.ptapplication.database;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import ptcorp.ptapplication.main.pojos.LeaderboardScore;


/**
 * Created by LinusHakansson on 2018-02-21.
 *
 */

public class FirebaseDatabaseHandler {
    private static final String TAG = "FirebaseDatabaseHandler";
    private DatabaseReference mDatabaseRef;
    private FirebaseUser user;
    private String userID;
    private HashMap<String,LeaderboardScore> mScoreList;

    private OnDatabaseUpdateListener mListener;

    public FirebaseDatabaseHandler(FirebaseAuth mFirebaseAuth) {
        user = mFirebaseAuth.getCurrentUser();
        if (user != null) userID = user.getUid();
        mScoreList = new HashMap<>();

        FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseRef = mFirebaseDatabase.getReference();
        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                showData(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", databaseError.toException());
            }
        });
    }


    public String getUsername(){
        return user.getEmail().split("@")[0];
    }

    public void updateScoreForUser(LeaderboardScore newScore) {
        LeaderboardScore score;
        if (mScoreList.containsKey(userID)) {
            score = mScoreList.get(userID);
            if (newScore.getWonGames() == 1) {
                score.setWonGames(score.getWonGames() + 1);
            } else {
                score.setLostGames(score.getLostGames() + 1);
            }
        } else{
            score = new LeaderboardScore();
            score.setUsername(getUsername());
            if(newScore.getWonGames() == 1){
                score.setWonGames(1);
            }else{
                score.setLostGames(1);
            }
        }
        saveScore(score);
    }


    private void showData(DataSnapshot dataSnapshot) {
        for (DataSnapshot dsScore : dataSnapshot.child("Leaderboard").child("Scores").getChildren()) {
            LeaderboardScore leaderboardScore = new LeaderboardScore();
            leaderboardScore.setUsername(dsScore.getValue(LeaderboardScore.class).getUsername());
            leaderboardScore.setWonGames(dsScore.getValue(LeaderboardScore.class).getWonGames());
            leaderboardScore.setLostGames(dsScore.getValue(LeaderboardScore.class).getLostGames());
            mScoreList.put(dsScore.getKey() ,leaderboardScore);
        }

        if(mListener != null)
            mListener.updateAdapter(mScoreList);
    }

    public void addOnUpdateListener(OnDatabaseUpdateListener listener){
        mListener = listener;
    }

    private void saveScore(LeaderboardScore score){
        mDatabaseRef.child("Leaderboard").child("Scores").child(userID).setValue(score);
    }

    public interface OnDatabaseUpdateListener {
        void updateAdapter(HashMap<String,LeaderboardScore> scoreMap);
    }

}
