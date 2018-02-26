package ptcorp.ptapplication;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Pontus on 2018-02-26.
 */

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewholder> {

    private List<LeaderboardScore> games;

    public LeaderboardAdapter(List<LeaderboardScore> list){
        this.games = list;
    }

    @Override
    public LeaderboardAdapter.LeaderboardViewholder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.scoreboard_item, parent, false);
        return new LeaderboardAdapter.LeaderboardViewholder(v);
    }

    @Override
    public void onBindViewHolder(LeaderboardViewholder holder, int position) {
        LeaderboardScore game = games.get(position);

        holder.tvPlayer.setText(game.getUsername());
        holder.tvWins.setText(String.valueOf(game.getWonGames()));
        holder.tvLosses.setText(String.valueOf(game.getLostGames()));
        holder.tvWinrate.setText(String.valueOf(((float)game.getWonGames()/(game.getLostGames()+game.getWonGames()))*100));

    }


    @Override
    public int getItemCount() {
        return games.size();
    }

    class LeaderboardViewholder extends RecyclerView.ViewHolder{

        private TextView tvPlayer, tvWinrate, tvWins, tvLosses;

        public LeaderboardViewholder(View itemView) {
            super(itemView);

            tvPlayer = itemView.findViewById(R.id.tvPlayer);
            tvWinrate = itemView.findViewById(R.id.tvWinrate);
            tvWins = itemView.findViewById(R.id.tvWins);
            tvLosses = itemView.findViewById(R.id.tvLosses);
        }
    }
}
