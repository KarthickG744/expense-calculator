package com.example.expensecalculator;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private RecyclerView rvExpenses;
    private TextView tvSelectedDate, tvMonthTotal;
    private FloatingActionButton fabAdd;
    private Button btnCalculate;

    private String selectedDate;
    private DatabaseHelper db;
    private ExpenseAdapter adapter;
    private List<Expense> currentExpenses = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DatabaseHelper(this);

        calendarView   = findViewById(R.id.calendarView);
        rvExpenses     = findViewById(R.id.rvExpenses);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        tvMonthTotal   = findViewById(R.id.tvMonthTotal);
        fabAdd         = findViewById(R.id.fabAdd);
        btnCalculate   = findViewById(R.id.btnCalculate);

        // Set today as default selected date
        selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        tvSelectedDate.setText("Date: " + selectedDate);

        // Setup RecyclerView
        adapter = new ExpenseAdapter(currentExpenses, expense -> {
            // On delete
            new AlertDialog.Builder(this)
                .setTitle("Delete Expense")
                .setMessage("Delete \"" + expense.name + "\" (₹" + expense.amount + ")?")
                .setPositiveButton("Delete", (d, w) -> {
                    db.deleteExpense(expense.id);
                    loadExpensesForDate(selectedDate);
                })
                .setNegativeButton("Cancel", null)
                .show();
        });
        rvExpenses.setLayoutManager(new LinearLayoutManager(this));
        rvExpenses.setAdapter(adapter);

        // Calendar date change
        calendarView.setOnDateChangeListener((view, year, month, day) -> {
            selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day);
            tvSelectedDate.setText("Date: " + selectedDate);
            loadExpensesForDate(selectedDate);
        });

        // Add expense button
        fabAdd.setOnClickListener(v -> showAddExpenseDialog());

        // Calculate button
        btnCalculate.setOnClickListener(v -> showMonthlySummary());

        loadExpensesForDate(selectedDate);
    }

    private void loadExpensesForDate(String date) {
        currentExpenses.clear();
        currentExpenses.addAll(db.getExpensesForDate(date));
        adapter.notifyDataSetChanged();
        updateMonthTotal();
    }

    private void updateMonthTotal() {
        String yearMonth = selectedDate.substring(0, 7); // yyyy-MM
        double total = db.getMonthTotal(yearMonth);
        tvMonthTotal.setText("Month Total: ₹" + String.format(Locale.getDefault(), "%.2f", total));
    }

    private void showAddExpenseDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_expense, null);
        EditText etName   = view.findViewById(R.id.etExpenseName);
        EditText etAmount = view.findViewById(R.id.etExpenseAmount);

        new AlertDialog.Builder(this)
            .setTitle("Add Expense for " + selectedDate)
            .setView(view)
            .setPositiveButton("Add", (d, w) -> {
                String name   = etName.getText().toString().trim();
                String amount = etAmount.getText().toString().trim();
                if (!name.isEmpty() && !amount.isEmpty()) {
                    db.addExpense(selectedDate, name, Double.parseDouble(amount));
                    loadExpensesForDate(selectedDate);
                } else {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showMonthlySummary() {
        String yearMonth = selectedDate.substring(0, 7);
        Map<String, Double> summary = db.getMonthlySummary(yearMonth);
        double total = db.getMonthTotal(yearMonth);

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Double> entry : summary.entrySet()) {
            sb.append("• ").append(entry.getKey())
              .append("  →  ₹").append(String.format(Locale.getDefault(), "%.2f", entry.getValue()))
              .append("\n");
        }
        sb.append("\n━━━━━━━━━━━━━━━━━━━━\n");
        sb.append("Total:  ₹").append(String.format(Locale.getDefault(), "%.2f", total));

        new AlertDialog.Builder(this)
            .setTitle("Summary — " + yearMonth)
            .setMessage(sb.length() > 0 ? sb.toString() : "No expenses this month.")
            .setPositiveButton("OK", null)
            .show();
    }
}
