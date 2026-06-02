package com.example.electricitybillapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class DetailActivity extends AppCompatActivity {

    TextView detailMonth, detailUnit, detailTotal, detailRebate, detailFinal;
    Button btnUpdate, btnDelete;

    AppDatabase db;
    int billId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Enable home button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Bind views
        detailMonth = findViewById(R.id.detail_month);
        detailUnit = findViewById(R.id.detail_unit);
        detailTotal = findViewById(R.id.detail_total);
        detailRebate = findViewById(R.id.detail_rebate);
        detailFinal = findViewById(R.id.detail_final);
        btnUpdate = findViewById(R.id.btn_update);
        btnDelete = findViewById(R.id.btn_delete);

        // Initialize DB
        db = AppDatabase.getInstance(this);

        // Load data from intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            billId = extras.getInt("bill_id");
            detailMonth.setText("Month: " + extras.getString("month"));
            detailUnit.setText("Unit Used: " + extras.getInt("unit") + " kWh");
            detailTotal.setText("Total Charges: RM " + String.format("%.2f", extras.getDouble("total")));
            detailRebate.setText("Rebate: " + extras.getDouble("rebate") + " %");
            detailFinal.setText("Final Cost: RM " + String.format("%.2f", extras.getDouble("final")));
        }

        // Update button → go to EditBillActivity
        btnUpdate.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditBillActivity.class);
            intent.putExtra("bill_id", billId);
            startActivity(intent);
        });

        // Delete button
        btnDelete.setOnClickListener(v -> {
            Bill bill = db.billDao().getBillById(billId);
            if (bill != null) {
                db.billDao().delete(bill);
                Toast.makeText(this, "Bill deleted", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}