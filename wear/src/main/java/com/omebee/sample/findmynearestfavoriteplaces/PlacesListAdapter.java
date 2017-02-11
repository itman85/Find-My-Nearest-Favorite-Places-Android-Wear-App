package com.omebee.sample.findmynearestfavoriteplaces;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.wearable.view.WearableRecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by phannguyen on 8/11/16.
 */

public class PlacesListAdapter extends WearableRecyclerView.Adapter<PlacesListAdapter.ViewHolder> {
    private ArrayList<String> data;
    private Context context;
    private ItemSelectedListener itemSelectedListener;

    public PlacesListAdapter(Context context, ArrayList<String> data) {
        this.context = context;
        this.data = data;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView textView;
        private ImageView imageView;

        ViewHolder(View view) {
            super(view);
            textView = (TextView) view.findViewById(R.id.subjects);
        }

        void bind(final int position, final ItemSelectedListener listener) {

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.onItemSelected(position);
                    }
                }
            });
        }
    }

    public void setListener(ItemSelectedListener itemSelectedListener) {
        this.itemSelectedListener = itemSelectedListener;
    }

    @Override
    public PlacesListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(PlacesListAdapter.ViewHolder holder, final int position) {
        if (data != null && !data.isEmpty()) {
            holder.textView.setText(data.get(position));
            holder.bind(position, itemSelectedListener);
        }
    }

    @Override
    public int getItemCount() {
        if (data != null && !data.isEmpty()) {
            return data.size();
        }
        return 0;
    }

    public interface ItemSelectedListener {
        void onItemSelected(int position);
    }

}
