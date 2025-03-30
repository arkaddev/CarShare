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

public class AddRideActivity extends AppCompatActivity {

    private EditText editTextDate, editTextInitialCounter, editTextFinalCounter;
    private Button buttonSubmit;
    private Button buttonCorrect;
    private String token;
    private int userId;
    private int counter;

    private int correctValue = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_ride);

        editTextDate = findViewById(R.id.editTextDate);
        editTextInitialCounter = findViewById(R.id.editTextInitialCounter);
        editTextFinalCounter = findViewById(R.id.editTextFinalCounter);
        buttonSubmit = findViewById(R.id.buttonSubmit);
        buttonCorrect = findViewById(R.id.buttonCorrect);

        // Ustawienie bieżącej daty w formacie yyyy-MM-dd
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String currentDate = dateFormat.format(new Date());
        editTextDate.setText(currentDate); // Ustawienie daty w polu EditText


        Intent intent = getIntent();
        token = intent.getStringExtra("token");
        userId = intent.getIntExtra("userId", -1);
        counter = getIntent().getIntExtra("counter", 0);

        editTextInitialCounter.setText(String.valueOf(counter));

//        buttonSubmit.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                submitRide();
//            }
//        });

        // Obsługa kliknięcia przycisku buttonSubmit
        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                correctValue = 0;  // Ustawiamy correct na 0
                submitRide();
            }
        });

        // Obsługa kliknięcia przycisku buttonCorrect
        buttonCorrect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                correctValue = 1;  // Ustawiamy correct na 1
                submitRide();
            }
        });

    }

    private void submitRide() {
        String date = editTextDate.getText().toString();
        String initialCounter = editTextInitialCounter.getText().toString();
        String finalCounter = editTextFinalCounter.getText().toString();

        if (date.isEmpty() || initialCounter.isEmpty() || finalCounter.isEmpty()) {
            Toast.makeText(this, "Wypełnij wszystkie pola", Toast.LENGTH_SHORT).show();
            return;
        }

        new AddRideTask().execute(date, initialCounter, finalCounter);
    }

    private class AddRideTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String date = params[0];
            String initialCounter = params[1];
            String finalCounter = params[2];

            try {
                URL url = new URL("http://tankujemy.online/rides.php");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Authorization", "Bearer " + token);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                JSONObject postData = new JSONObject();
                postData.put("user_id", userId);
                postData.put("date", date);
                postData.put("initial_counter", Integer.parseInt(initialCounter));
                postData.put("final_counter", Integer.parseInt(finalCounter));
                postData.put("archive", 0);
                postData.put("correct", correctValue);

                OutputStream os = connection.getOutputStream();
                os.write(postData.toString().getBytes());
                os.flush();
                os.close();

                int responseCode = connection.getResponseCode();
                Log.d("AddRideTask", "Response Code: " + responseCode);  // Dodaj logowanie kodu odpowiedzi

                if (responseCode == 200 || responseCode == 201) {
                    return "success";
                } else {
                    return "error";
                }

            } catch (Exception e) {
                Log.e("AddRideTask", "Error adding ride", e);  // Logowanie błędu

                e.printStackTrace();
                return "error";
            }
        }


        @Override
        protected void onPostExecute(String result) {
            if (result.equals("success")) {

                Toast.makeText(AddRideActivity.this, "Przejazd dodany!", Toast.LENGTH_LONG).show();


                finish();

            } else {
                Toast.makeText(AddRideActivity.this, "Błąd dodawania przejazdu", Toast.LENGTH_SHORT).show();
            }
        }
    }
}