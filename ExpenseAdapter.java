package com.example.expensecalculator;

import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import java.util.List;
import java.util.Locale;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.VH> {

    public interface OnDeleteListener { void onDelete(Expense expense); }

    private final List<Expense> list;
    private final OnDeleteListener listener;

    public ExpenseAdapter(List<Expense> list, OnDeleteListener listener) {
        this.list     = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_expense, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Expense e = list.get(position);
        h.tvName.setText(e.name);
        h.tvAmount.setText("₹" + String.format(Locale.getDefault(), "%.2f", e.amount));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(e));
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvAmount;
        MaterialButton btnDelete;
        VH(View v) {
            super(v);
            tvName    = v.findViewById(R.id.tvName);
            tvAmount  = v.findViewById(R.id.tvAmount);
            btnDelete = v.findViewById(R.id.btnDelete);
        }
    }
}
