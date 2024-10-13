package com.archko.editvideo.tracks;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.thuypham.ptithcm.editvideo.R;

/**
 * @author: archko 2023/7/11 :10:15
 */
public class SpeedAdapter extends RecyclerView.Adapter<TracksViewHolder> {

    public SpeedAdapter(SelectSpeedListener selectSpeedListener) {
        this.selectSpeedListener = selectSpeedListener;
        setHasStableIds(true);
    }

    private SelectSpeedListener selectSpeedListener;

    public void setSelectSpeedListener(SelectSpeedListener selectSpeedListener) {
        this.selectSpeedListener = selectSpeedListener;
    }

    Speed[] speedList = {
            new Speed("0.5x", 0.5f, false),
            new Speed("1x", 1.0f, true),
            new Speed("1.5x", 1.5f, false),
            new Speed("2x", 2f, false),
            new Speed("3x", 3f, false)
    };

    @NonNull
    @Override
    public TracksViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.track_item, parent, false);
        return new TracksViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TracksViewHolder viewHolder, int position) {
        Speed speed = speedList[position];
        viewHolder.getExoText().setText(speed.getText());
        viewHolder.itemView.setOnClickListener(view -> {
            if (null != selectSpeedListener) {
                selectSpeedListener.selected(position, speed);
            }
            updateSpeed(speed, position);
        });
        if (speed.getSelected()) {
            viewHolder.getExoCheck().setImageResource(R.drawable.ic_track_check);
        } else {
            viewHolder.getExoCheck().setImageBitmap(null);
        }
    }

    private void updateSpeed(Speed speed, int position) {
        for (Speed s : speedList) {
            s.setSelected(speed.getSpeed() == s.getSpeed());
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return speedList.length;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public interface SelectSpeedListener {

        void selected(int position, Speed speed);
    }

    /*static class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView exoCheck;
        private TextView text;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            exoCheck = itemView.findViewById(R.id.exo_check);
            text = itemView.findViewById(R.id.exo_text);
            text.setPaintFlags(text.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            //Remove underline
            //textView.setPaintFlags(textView.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));
        }
    }*/
}
