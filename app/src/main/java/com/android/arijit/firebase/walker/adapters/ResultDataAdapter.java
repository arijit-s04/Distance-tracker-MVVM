package com.android.arijit.firebase.walker.adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.arijit.firebase.walker.R;
import com.android.arijit.firebase.walker.databinding.ItemViewTempBinding;
import com.android.arijit.firebase.walker.interfaces.OnFirebaseResultListener;
import com.android.arijit.firebase.walker.interfaces.OnHistoryItemClickedListener;
import com.android.arijit.firebase.walker.utils.FirebaseUtil;
import com.android.arijit.firebase.walker.models.ForegroundService;
import com.android.arijit.firebase.walker.models.ResultData;
import com.android.arijit.firebase.walker.views.SettingsFragment;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

public class ResultDataAdapter extends RecyclerView.Adapter<ResultDataAdapter.resultViewHolder> {
    private final static String TAG="DataAdapter";
    private final Context context;
    private final ArrayList<ResultData> dataArrayList;
    private final OnHistoryItemClickedListener onItemClickListener;
    private final OnFirebaseResultListener onFirebaseResultListener;

    public ResultDataAdapter(Context context, View root, ArrayList<ResultData> dataList,
                             OnHistoryItemClickedListener listener1, OnFirebaseResultListener listener2) {
        this.dataArrayList = dataList;
        this.context = context;
        this.onItemClickListener = listener1;
        this.onFirebaseResultListener = listener2;
    }

    @NonNull
    @Override
    public resultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View recordView = LayoutInflater.from(parent.getContext())
//                .inflate(R.layout.item_view_temp, parent, false);
        ItemViewTempBinding binding = ItemViewTempBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new resultViewHolder(binding);

    }

    @Override
    public void onBindViewHolder(@NonNull  ResultDataAdapter.resultViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.bindData(dataArrayList.get(position), position);
        holder.itemView.setOnClickListener(v -> {
            if(!ForegroundService.locationViewModel.getTrackState()) {
                this.onItemClickListener.onHistoryItemClicked(position);
            }
            else {
                this.onFirebaseResultListener.onFirebaseResult("Please check the history map after recording");
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataArrayList.size();
    }

    public ResultData getItem(int position){
        return dataArrayList.get(position);
    }


    class resultViewHolder extends RecyclerView.ViewHolder{

        private TextView date,time,distance;
        private ImageView delete;
        private int position;
        private ItemViewTempBinding binding;

        public resultViewHolder(@NonNull ItemViewTempBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
//            date= itemView.findViewById(R.id.item_view_date);
//            time= itemView.findViewById(R.id.item_view_time);
//            distance= itemView.findViewById(R.id.item_view_distance);
//            delete = itemView.findViewById(R.id.item_view_delete);
        }

        @SuppressLint("NotifyDataSetChanged")
        public void bindData(ResultData data, int position){
            this.position = position;
            binding.itemViewDate.setText(data.getDate());
            binding.itemViewTime.setText(data.getTime());
            binding.itemViewDistance.setText(SettingsFragment.distanceFormat(data.getDistanceTravelled()));
//            date.setText(data.getDate());
//            time.setText(data.getTime());
//            distance.setText(SettingsFragment.distanceFormat(data.getDistanceTravelled()));
            binding.itemViewDelete.setOnClickListener(v -> new AlertDialog.Builder(context)
                    .setMessage("This cannot be undone!")
                    .setTitle("Delete")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Delete", (dialog, which) -> {
                        /*
                         * delete the item
                         */
                        String id = getItem(position).getId();
                        FirebaseUtil.deleteData(context, id, onFirebaseResultListener);
                        dataArrayList.remove(position);
                        notifyDataSetChanged();
                    }).create().show());

        }
    }
}
