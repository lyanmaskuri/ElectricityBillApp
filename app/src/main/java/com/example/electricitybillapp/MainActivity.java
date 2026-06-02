package com.example.electricitybillapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    Spinner spinnerMonth;
    EditText editKwh;
    SeekBar seekbarRebate;
    TextView textTotal, textFinal, textRebateLabel;
    Button buttonCalculate, btnViewList, btnAbout;

    AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Bind views
        spinnerMonth = findViewById(R.id.spinner_month);
        editKwh = findViewById(R.id.edit_kwh);
        seekbarRebate = findViewById(R.id.seekbar_rebate);
        textRebateLabel = findViewById(R.id.text_rebate_label);
        textTotal = findViewById(R.id.text_total);
        textFinal = findViewById(R.id.text_final);
        buttonCalculate = findViewById(R.id.button_calculate);
        btnViewList = findViewById(R.id.btn_view_list);
        btnAbout = findViewById(R.id.btn_about);

        // Initialize Room database
        db = AppDatabase.getInstance(this);

        // Load months and insert "Select Month" as hint
        String[] months = getResources().getStringArray(R.array.months_array);
        String[] monthsWithHint = new String[months.length + 1];
        monthsWithHint[0] = "Select Month";
        System.arraycopy(months, 0, monthsWithHint, 1, months.length);

        // Custom adapter for spinner with hint
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                R.layout.spinner_item,
                monthsWithHint
        ) {
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                View view = inflater.inflate(R.layout.spinner_dropdown_item, parent, false);
                TextView text = view.findViewById(R.id.spinner_dropdown_text);
                text.setText(getItem(position));
                return view;
            }
        };
        spinnerMonth.setAdapter(adapter);

        // SeekBar listener
        seekbarRebate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textRebateLabel.setText("Rebate: " + progress + "%");
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Calculate button
        buttonCalculate.setOnClickListener(view -> calculateAndSave());

        // Navigation buttons
        btnViewList.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ListActivity.class)));
        btnAbout.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AboutActivity.class)));
    }

    // Add Help icon in action bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "How to Use")
                .setIcon(android.R.drawable.ic_menu_help)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    // Handle Help icon click
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 1) {
            showHowToUseDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showHowToUseDialog() {
        String message =
                "1. Select Month\n" +
                        "Choose the billing month from the dropdown.\n\n" +
                        "2. Enter kWh Usage\n" +
                        "Type your electricity usage in kilowatt-hours.\n\n" +
                        "3. Set Rebate\n" +
                        "Slide to set your rebate percentage (0% – 5%).\n\n" +
                        "4. Calculate & Save\n" +
                        "Tap the button to calculate your bill and save it.\n\n" +
                        "5. View Saved Bills\n" +
                        "Tap 'View Saved Bills' to see all your past records.\n\n" +
                        "6. Update or Delete\n" +
                        "Tap any bill in the list to view, edit or delete it.";

        new AlertDialog.Builder(this)
                .setTitle("How to Use")
                .setMessage(message)
                .setPositiveButton("Got it!", (dialog, which) -> dialog.dismiss())
                .setCancelable(true)
                .show();
    }

    private void calculateAndSave() {
        String month = spinnerMonth.getSelectedItem().toString();
        String kwhStr = editKwh.getText().toString();

        // Validation
        if (month.equals("Select Month")) {
            Toast.makeText(this, "Please select a month", Toast.LENGTH_SHORT).show();
            return;
        }

        if (kwhStr.isEmpty()) {
            Toast.makeText(this, "Please enter kWh usage", Toast.LENGTH_SHORT).show();
            return;
        }

        int kwh = Integer.parseInt(kwhStr);
        double rebate = seekbarRebate.getProgress();

        // Calculate charges
        double totalCharges = calculateTotalCharges(kwh);
        double finalCost = totalCharges - (totalCharges * rebate / 100.0);

        // Display
        textTotal.setText(String.format("Total Charges: RM %.2f", totalCharges));
        textFinal.setText(String.format("Final Cost: RM %.2f", finalCost));

        // Save to database
        Bill bill = new Bill();
        bill.month = month;
        bill.unitUsed = kwh;
        bill.rebate = rebate;
        bill.totalCharges = totalCharges;
        bill.finalCost = finalCost;
        db.billDao().insert(bill);

        Toast.makeText(this, "Saved to database", Toast.LENGTH_SHORT).show();
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
}