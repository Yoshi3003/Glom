package com.abborg.glom.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.abborg.glom.R;
import com.abborg.glom.model.Circle;

import java.util.Collections;
import java.util.List;

/**
 * Created by Boat on 17/9/58.
 */
public class NavigationDrawerAdapter extends RecyclerView.Adapter<NavigationDrawerAdapter.MyViewHolder> {

    List<Circle> data = Collections.emptyList();

    private LayoutInflater inflater;

    private Context context;

    public NavigationDrawerAdapter(Context context, List<Circle> data) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.data = data;
    }

    public void delete(int position) {
        data.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.nav_drawer_row, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Circle current = data.get(position);
        int memberCount = current.getUsers().size();
        if (memberCount > 1)
            holder.title.setText(current.getTitle() + " (" + memberCount + ")");
        else
            holder.title.setText(current.getTitle());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title;

        public MyViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.circleTitle);
        }
    }
}
