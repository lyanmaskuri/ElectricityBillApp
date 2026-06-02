package com.example.electricitybillapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class ListActivity extends AppCompatActivity {

    ListView listView;
    AppDatabase db;
    List<Bill> bills;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        listView = findViewById(R.id.list_view);
        db = AppDatabase.getInstance(this);
        bills = db.billDao().getAll();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                getFormattedListItems()
        );

        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Bill selectedBill = bills.get(position);
            Intent intent = new Intent(ListActivity.this, DetailActivity.class);
            intent.putExtra("bill_id", selectedBill.id); // 👈 added
            intent.putExtra("month", selectedBill.month);
            intent.putExtra("unit", selectedBill.unitUsed);
            intent.putExtra("rebate", selectedBill.rebate);
            intent.putExtra("total", selectedBill.totalCharges);
            intent.putExtra("final", selectedBill.finalCost);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh list when returning from EditBillActivity or DetailActivity
        bills = db.billDao().getAll();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                getFormattedListItems()
        );
        listView.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private String[] getFormattedListItems() {
        String[] items = new String[bills.size()];
        for (int i = 0; i < bills.size(); i++) {
            items[i] = bills.get(i).month + ": RM " + String.format("%.2f", bills.get(i).finalCost);
        }
        return items;
    }
}