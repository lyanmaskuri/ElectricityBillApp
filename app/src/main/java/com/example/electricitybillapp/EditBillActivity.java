package com.example.electricitybillapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

public class EditBillActivity extends AppCompatActivity {

    Spinner editSpinnerMonth;
    EditText editKwh;
    SeekBar editSeekbarRebate;
    TextView editTextRebateLabel;
    Button btnSaveUpdate;

    AppDatabase db;
    Bill currentBill;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_bill);

        // Enable home button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Edit Bill");
        }

        // Bind views
        editSpinnerMonth = findViewById(R.id.edit_spinner_month);
        editKwh = findViewById(R.id.edit_kwh);
        editSeekbarRebate = findViewById(R.id.edit_seekbar_rebate);
        editTextRebateLabel = findViewById(R.id.edit_text_rebate_label);
        btnSaveUpdate = findViewById(R.id.btn_save_update);

        // Initialize DB
        db = AppDatabase.getInstance(this);

        // Setup month spinner
        String[] months = getResources().getStringArray(R.array.months_array);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, R.layout.spinner_item, months);
        editSpinnerMonth.setAdapter(adapter);

        // Get bill ID from intent
        int billId = getIntent().getIntExtra("bill_id", -1);
        if (billId != -1) {
            currentBill = db.billDao().getBillById(billId);
            populateFields();
        }

        // SeekBar listener
        editSeekbarRebate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                editTextRebateLabel.setText("Rebate: " + progress + "%");
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Save button
        btnSaveUpdate.setOnClickListener(v -> saveUpdate());
    }

    private void populateFields() {
        String[] months = getResources().getStringArray(R.array.months_array);
        for (int i = 0; i < months.length; i++) {
            if (months[i].equals(currentBill.month)) {
                editSpinnerMonth.setSelection(i);
                break;
            }
        }
        editKwh.setText(String.valueOf(currentBill.unitUsed));
        editSeekbarRebate.setProgress((int) currentBill.rebate);
        editTextRebateLabel.setText("Rebate: " + (int) currentBill.rebate + "%");
    }

    private void saveUpdate() {
        String month = editSpinnerMonth.getSelectedItem().toString();
        String kwhStr = editKwh.getText().toString();

        if (kwhStr.isEmpty()) {
            Toast.makeText(this, "Please enter kWh usage", Toast.LENGTH_SHORT).show();
            return;
        }

        int kwh = Integer.parseInt(kwhStr);
        double rebate = editSeekbarRebate.getProgress();

        double totalCharges = calculateTotalCharges(kwh);
        double finalCost = totalCharges - (totalCharges * rebate / 100.0);

        currentBill.month = month;
        currentBill.unitUsed = kwh;
        currentBill.rebate = rebate;
        currentBill.totalCharges = totalCharges;
        currentBill.finalCost = finalCost;

        db.billDao().update(currentBill);

        Toast.makeText(this, "Bill updated successfully!", Toast.LENGTH_SHORT).show();

        // Go back to ListActivity
        Intent intent = new Intent(this, ListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private double calculateTotalCharges(int kwh) {
        double charges = 0;
        if (kwh <= 200) {
            charges = kwh * 0.218;
        } else if (kwh <= 300) {
            charges = 200 * 0.218 + (kwh - 200) * 0.334;
        } else if (kwh <= 600) {
            charges = 200 * 0.218 + 100 * 0.334 + (kwh - 300) * 0.516;
        } else {
            charges = 200 * 0.218 + 100 * 0.334 + 300 * 0.516 + (kwh - 600) * 0.546;
        }
        return charges;
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