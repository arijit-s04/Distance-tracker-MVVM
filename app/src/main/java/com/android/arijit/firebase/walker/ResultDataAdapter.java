package com.android.arijit.firebase.walker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class ResultDataAdapter extends RecyclerView.Adapter<ResultDataAdapter.resultViewHolder> {
    private String TAG="DataAdapter";
    private Context context;
    private ArrayList<ResultData> dataArrayList;
    private FragmentManager mFragmentManager;
    private View root;

    public ResultDataAdapter(Context context, View root, ArrayList<ResultData> dataList) {
        this.dataArrayList = dataList;
        this.context = context;
        this.root = root;
    }

    @NonNull
    @Override
    public resultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View recordView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_view_temp, parent, false);
        return new resultViewHolder(recordView);

    }

    @Override
    public void onBindViewHolder(@NonNull  ResultDataAdapter.resultViewHolder holder, int position) {
        holder.bindData(dataArrayList.get(position), position);
        holder.itemView.setOnClickListener(v -> {
            if(!HomeFragment.trackState) {
                HistoryFragment.clickedPosition = position;
                HistoryFragment.mapToShow.postValue(true);
            }
            else {
                Snackbar.make(root, "Please check the history map after recording", Snackbar.LENGTH_SHORT)
                        .show();
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

        public resultViewHolder(@NonNull View itemView) {
            super(itemView);
            date=(TextView)itemView.findViewById(R.id.item_view_date);
            time=(TextView)itemView.findViewById(R.id.item_view_time);
            distance=(TextView)itemView.findViewById(R.id.item_view_distance);
            delete = (ImageView) itemView.findViewById(R.id.item_view_delete);
        }

        public void bindData(ResultData data, int position){
            this.position = position;
            date.setText(data.getDate());
            time.setText(data.getTime());
            distance.setText(SettingsFragment.distanceFormat(data.getDistanceTravelled()));
            delete.setOnClickListener(v -> {
                new AlertDialog.Builder(context)
                        .setMessage("This cannot be undone!")
                        .setTitle("Delete")
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                /**
                                 * delete the item
                                 */
                                String id = getItem(position).getId();
                                FirebaseHelper.deleteData(id, root);
                                dataArrayList.remove(position);
                                notifyDataSetChanged();
                            }
                        }).create().show();
            });

        }
    }
}
