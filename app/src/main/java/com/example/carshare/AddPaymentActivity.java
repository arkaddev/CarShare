package com.example.carshare;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.example.carshare.Model.Ride;

public class AddPaymentActivity extends AppCompatActivity {

    private EditText editTextDate, editTextTotalCost, editTextTotalDistance;
    private Button buttonSubmit;
    private String token;
    private int userId;
    private int totalDistance;
    private double totalCost;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_payment);

        editTextDate = findViewById(R.id.editTextDate);
        editTextTotalDistance = findViewById(R.id.editTextTotalDistance);
        editTextTotalCost = findViewById(R.id.editTextTotalCost);
        buttonSubmit = findViewById(R.id.buttonSubmit);


        // Ustawienie bieżącej daty w formacie yyyy-MM-dd
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String currentDate = dateFormat.format(new Date());
        editTextDate.setText(currentDate); // Ustawienie daty w polu EditText


        Intent intent = getIntent();
        token = intent.getStringExtra("token");
        userId = intent.getIntExtra("userId", -1);
        totalDistance = intent.getIntExtra("totalDistance", -1);
        totalCost = intent.getDoubleExtra("totalCost", -1);

        editTextTotalDistance.setText(String.valueOf(totalDistance));
        editTextTotalCost.setText(String.valueOf(totalCost));

        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitRide();
            }
        });


    }

    private void submitRide() {
        String date = editTextDate.getText().toString();
        String totalDistanceString = editTextTotalDistance.getText().toString();
        String totalCostString = editTextTotalCost.getText().toString();

        if (date.isEmpty() || totalCostString.isEmpty() || totalDistanceString.isEmpty()) {
            Toast.makeText(this, "Wypełnij wszystkie pola", Toast.LENGTH_SHORT).show();
            return;
        }

        new AddPaymentTask().execute(date, totalCostString, totalDistanceString);
    }

    private class AddPaymentTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String date = params[0];
            String amount = params[1];
            String distance = params[2];

            try {
                URL url = new URL("http://tankujemy.online/payments.php");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Authorization", "Bearer " + token);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                JSONObject postData = new JSONObject();
                postData.put("date", date);
                postData.put("amount", Double.parseDouble(amount));
                postData.put("distance", Integer.parseInt(distance));
                postData.put("userId", userId);


                OutputStream os = connection.getOutputStream();
                os.write(postData.toString().getBytes());
                os.flush();
                os.close();

                int responseCode = connection.getResponseCode();
                Log.d("AddPaymentTask", "Response Code: " + responseCode);  // Dodaj logowanie kodu odpowiedzi

                if (responseCode == 200 || responseCode == 201) {
                    return "success";
                } else {
                    return "error";
                }

            } catch (Exception e) {
                Log.e("AddPaymentTask", "Error adding payment", e);  // Logowanie błędu

                e.printStackTrace();
                return "error";
            }
        }


        @Override
        protected void onPostExecute(String result) {
            if (result.equals("success")) {

                Toast.makeText(AddPaymentActivity.this, "Płatność dodana!", Toast.LENGTH_LONG).show();


                finish();

            } else {
                Toast.makeText(AddPaymentActivity.this, "Błąd dodawania płatności.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}