package com.jacup101.esportstalk;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FollowedAdapter extends RecyclerView.Adapter<FollowedAdapter.ViewHolder>{

    private List<String> followed;
    private Context context;

    public FollowedAdapter(List<String> followed, Context context) {
        this.followed = followed;
        this.context = context;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View postView = inflater.inflate(R.layout.item_followed,parent,false);
        ViewHolder viewHolder = new ViewHolder(postView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String followedItem = followed.get(position);

        holder.item.setText(followedItem);
    }

    @Override
    public int getItemCount() {
        return followed.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView item;


        public ViewHolder(View itemView) {
            super(itemView);

            item = itemView.findViewById(R.id.textView_followedItem);

            item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context,CommunityActivity.class);
                    intent.putExtra("community",followed.get(getAdapterPosition()));
                    context.startActivity(intent);

                }
            });
        }

    }
}
