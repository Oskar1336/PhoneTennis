package ptcorp.ptapplication.database;

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
import java.util.List;

import ptcorp.ptapplication.main.pojos.LeaderboardScore;


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
    private HashMap<String,LeaderboardScore> mScoreList;

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
//        testSaveScore();
    }

    /*
    * Just a test method
    * Will be deleted.
    * */
    private void testSaveScore(){
        Log.d(TAG, "testSaveScore: called." );
        String [] split  = user.getEmail().split("@");
        String username = split[0];
        LeaderboardScore score = new LeaderboardScore();
        score.setUsername(username);
        score.setWonGames(3);
        score.setLostGames(4);
        saveScore(score);
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
            score.setUsername(newScore.getUsername());
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
            Log.d(TAG, "showData: "+ dsScore);
            LeaderboardScore leaderboardScore = new LeaderboardScore();
            Log.d(TAG, "showData: " + dsScore.toString());
            leaderboardScore.setUsername(dsScore.getValue(LeaderboardScore.class).getUsername());
            leaderboardScore.setWonGames(dsScore.getValue(LeaderboardScore.class).getWonGames());
            leaderboardScore.setLostGames(dsScore.getValue(LeaderboardScore.class).getLostGames());
            mScoreList.put( userID,leaderboardScore);

        }
        List<LeaderboardScore> list = new ArrayList<>(mScoreList.values());
//            mListener.updateAdapter(list);
    }

    public void addOnUpdateListener(OnDatabaseUpdateListener listener){
        mListener = listener;
    }

    private void saveScore(LeaderboardScore score){
        mDatabaseRef.child("Leaderboard").child("Scores").child(userID).setValue(score);
    }

    public interface OnDatabaseUpdateListener {
        void updateAdapter(List<LeaderboardScore> list);
    }
}
