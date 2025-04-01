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

public class AddRefuelingActivity extends AppCompatActivity {

    private EditText editTextDate, editTextPricePerLiter, editTextAmountRefueling;
    private Button buttonSubmit;
    private String token;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_refueling);

        editTextDate = findViewById(R.id.editTextDate);
        editTextPricePerLiter = findViewById(R.id.editTextPricePerLiter);
        editTextAmountRefueling = findViewById(R.id.editTextAmountRefueling);
        buttonSubmit = findViewById(R.id.buttonSubmit);


        // Ustawienie bieżącej daty w formacie yyyy-MM-dd
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String currentDate = dateFormat.format(new Date());
        editTextDate.setText(currentDate); // Ustawienie daty w polu EditText


        Intent intent = getIntent();
        token = intent.getStringExtra("token");
        userId = intent.getIntExtra("userId", -1);

        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitRide();
            }
        });


    }

    private void submitRide() {
        String date = editTextDate.getText().toString();
        String pricePerLiter = editTextPricePerLiter.getText().toString();
        String amountRefueling = editTextAmountRefueling.getText().toString();


        if (date.isEmpty() || pricePerLiter.isEmpty() || amountRefueling.isEmpty()) {
            Toast.makeText(this, "Wypełnij wszystkie pola", Toast.LENGTH_SHORT).show();
            return;
        }
/*
        // zamiana String na int
        int initial = Integer.parseInt(initialCounter);
        int finalVal = Integer.parseInt(finalCounter);

        if (initial > finalVal) {
            Toast.makeText(this, "Stan licznika za niski.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (finalVal - initial > 500) {
            Toast.makeText(this, "Różnica między stanem licznika zbyt duża.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Sprawdzenie, czy finalVal ma 6 cyfr
//        if (finalVal < 100000 || finalVal > 999999) {
//            Toast.makeText(this, "Numer końcowy musi mieć 6 cyfr.", Toast.LENGTH_SHORT).show();
//            return;
//        }
*/
        new AddRefuelingTask().execute(date, pricePerLiter, amountRefueling);
    }

    private class AddRefuelingTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String date = params[0];
            String pricePerLiter = params[1];
            String amountRefueling = params[2];

            HttpURLConnection connection = null;

            try {
                URL url = new URL("http://tankujemy.online/refuelings.php");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Authorization", "Bearer " + token);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                JSONObject postData = new JSONObject();
                postData.put("userId", userId);
                postData.put("price_per_liter", Double.parseDouble(pricePerLiter));
                postData.put("amount_refueling", Double.parseDouble(amountRefueling));
                postData.put("date_refueling", date);


                OutputStream os = connection.getOutputStream();
                os.write(postData.toString().getBytes());
                os.flush();
                os.close();

                int responseCode = connection.getResponseCode();
                Log.d("AddRefuelingTask", "Response Code: " + responseCode);  // Dodaj logowanie kodu odpowiedzi

                connection.disconnect();

                if (responseCode == 200 || responseCode == 201) {
                    return "success";
                } else {
                    return "error";
                }



            } catch (Exception e) {
                Log.e("AddRefuelingTask", "Error adding ride", e);  // Logowanie błędu

                e.printStackTrace();
                return "error";
            }
            finally {
                // Wykonanie disconnect w bloku finally, aby upewnić się, że połączenie zostanie zamknięte
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }



        @Override
        protected void onPostExecute(String result) {
            if (result.equals("success")) {

                Toast.makeText(AddRefuelingActivity.this, "Tankowanie dodane!", Toast.LENGTH_LONG).show();

                Intent intent = new Intent(AddRefuelingActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);

                // FLAG_ACTIVITY_CLEAR_TOP – usuwa wszystkie aktywności nad MainActivity w stosie.
                //FLAG_ACTIVITY_SINGLE_TOP – jeśli MainActivity już istnieje, nie tworzy nowej instancji, tylko ją przywraca.

                finish();

            } else {
                Toast.makeText(AddRefuelingActivity.this, "Błąd dodawania tankowania", Toast.LENGTH_SHORT).show();
            }


        }
    }
}