package ptcorp.ptapplication;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by LinusHakansson on 2018-02-21.
 */

public class FirebaseDatabaseHandler {
    private static final String TAG = "FirebaseDatabaseHandler";
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseRef;
    private FirebaseAuth mFirebaseAuth;
    private ArrayList<Score> mScoreList;
    private OnDatabaseUpdateListener mListener;

    public FirebaseDatabaseHandler(FirebaseAuth mFirebaseAuth) {
        this.mFirebaseAuth = mFirebaseAuth;

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
    }

    private void showData(DataSnapshot dataSnapshot) {
        Log.d(TAG, "showData:" + dataSnapshot.toString());

        for (DataSnapshot dsScore : dataSnapshot.child("Leaderboard").child("Scores").getChildren()) {
            Score score = new Score();
            Log.d(TAG, "showData: " + dsScore.toString());
            score.setUsername(dsScore.getValue(Score.class).getUsername());
            score.setWonGames(dsScore.getValue(Score.class).getWonGames());
            score.setLostGames(dsScore.getValue(Score.class).getLostGames());
            mScoreList.add(score);
        }
    }

    public void addOnUpdateListener(OnDatabaseUpdateListener listener){
        mListener = listener;
    }

    public void saveScore(Score score){
        //TODO: implement this method
    }

    public interface OnDatabaseUpdateListener {
        void updateAdapter(ArrayList<Score> list);
    }
}
