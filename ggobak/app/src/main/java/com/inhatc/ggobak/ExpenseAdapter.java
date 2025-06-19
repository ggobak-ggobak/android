package com.inhatc.ggobak;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.inhatc.ggobak.model.Expense;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {
    private List<Expense> expenses;
    private NumberFormat numberFormat;

    public ExpenseAdapter(List<Expense> expenses) {
        this.expenses = expenses;
        this.numberFormat = NumberFormat.getNumberInstance(Locale.KOREA);
    }

    public void updateExpenses(List<Expense> newExpenses) {
        this.expenses = newExpenses;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense expense = expenses.get(position);
        holder.categoryText.setText(expense.getCategory());
        holder.amountText.setText(numberFormat.format(expense.getAmount()) + "원");
        holder.memoText.setText(expense.getMemo());
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        TextView categoryText;
        TextView amountText;
        TextView memoText;

        ExpenseViewHolder(View itemView) {
            super(itemView);
            categoryText = itemView.findViewById(R.id.categoryText);
            amountText = itemView.findViewById(R.id.amountText);
            memoText = itemView.findViewById(R.id.memoText);
        }
    }
} 