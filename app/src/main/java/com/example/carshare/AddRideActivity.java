package com.example.carshare;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class AddRideActivity extends AppCompatActivity {

    private EditText editTextDate, editTextInitialCounter, editTextFinalCounter;
    private Button buttonSubmit;
    private String token;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_ride);

        editTextDate = findViewById(R.id.editTextDate);
        editTextInitialCounter = findViewById(R.id.editTextInitialCounter);
        editTextFinalCounter = findViewById(R.id.editTextFinalCounter);
        buttonSubmit = findViewById(R.id.buttonSubmit);

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

                OutputStream os = connection.getOutputStream();
                os.write(postData.toString().getBytes());
                os.flush();
                os.close();

                int responseCode = connection.getResponseCode();
                return responseCode == 200 ? "success" : "error";

            } catch (Exception e) {
                e.printStackTrace();
                return "error";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.equals("success")) {
                Toast.makeText(AddRideActivity.this, "Przejazd dodany!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(AddRideActivity.this, "Błąd dodawania przejazdu", Toast.LENGTH_SHORT).show();
            }
        }
    }
}