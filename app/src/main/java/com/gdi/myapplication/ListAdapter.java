package com.gdi.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {
    private List<String> list;

    private static OnItemClickListener itemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View v, int pos);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        itemClickListener = listener;
    }

    public ListAdapter() {
        list = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        View view = inflater.inflate(R.layout.items, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String text = list.get(position);
        holder.txtView.setText(text);

        ViewGroup.LayoutParams l = holder.itemView.getLayoutParams();
        l.height = 200;
        holder.itemView.requestLayout();

//        holder.txtView.setOnClickListener(view -> {
//            int pos = holder.getAdapterPosition();
//
//
//        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void setArrayData(String strData) {
        list.add(strData);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView txtView;

        public ViewHolder(View itemView) {
            super(itemView);

            txtView = itemView.findViewById(R.id.txt_item);

            itemView.getRootView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION) {
                        if(itemClickListener != null) {
                            itemClickListener.onItemClick(view, position);
                        }
                    }
                }
            });
        }

    }
}
