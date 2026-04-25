package com.iflytek.voicedemo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ScenarioAdapter extends RecyclerView.Adapter<ScenarioAdapter.ViewHolder> {
    private List<Scenario> scenarios;
    private OnItemClickListener listener;
    private Context context;

    public interface OnItemClickListener {
        void onItemClick(Scenario scenario);
    }

    public ScenarioAdapter(Context context, List<Scenario> scenarios, OnItemClickListener listener) {
        this.context = context;
        this.scenarios = scenarios;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_scenario_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Scenario scenario = scenarios.get(position);
        holder.tvTitle.setText(scenario.getTitle());
        holder.tvDesc.setText(scenario.getDescription());
        
        // Placeholder icon logic
        int resId = context.getResources().getIdentifier(scenario.getIconName(), "drawable", context.getPackageName());
        if (resId != 0) {
            holder.ivIcon.setImageResource(resId);
        } else {
            holder.ivIcon.setImageResource(R.drawable.icon); // fallback
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(scenario));
    }

    @Override
    public int getItemCount() {
        return scenarios.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvDesc;
        ImageView ivIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_scenario_title);
            tvDesc = itemView.findViewById(R.id.tv_scenario_desc);
            ivIcon = itemView.findViewById(R.id.iv_scenario_icon);
        }
    }
}
