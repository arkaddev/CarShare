package com.example.carshare;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HomeActivity extends AppCompatActivity {

    private TextView textViewUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        textViewUsers = findViewById(R.id.textViewUsers);

        // Odbieramy token z Intentu
        String token = getIntent().getStringExtra("token");

        if (token != null) {
            // Uruchamiamy zadanie do pobierania danych użytkowników
            new UserDataTask().execute(token);
        } else {
            Toast.makeText(this, "No token found", Toast.LENGTH_SHORT).show();
        }
    }

    private class UserDataTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String token = params[0];

            try {
                // Tworzymy URL i łączenie do /users.php
                URL url = new URL("http://tankujemy.online/users.php");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", "Bearer " + token);

                // Odbieramy odpowiedź
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();

                String responseString = response.toString();

                // Dodaj logowanie odpowiedzi, aby sprawdzić, co otrzymujesz
                Log.d("UserDataTask", "Response: " + responseString);

                return responseString;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    // Sprawdzamy, czy odpowiedź jest obiektem JSON z polem "users"
                    JSONObject response = new JSONObject(result);

                    // Sprawdzamy, czy odpowiedź zawiera pole "users"
                    if (response.has("users")) {
                        JSONArray usersArray = response.getJSONArray("users");

                        // Tworzymy łańcuch tekstowy z listą użytkowników
                        StringBuilder usersList = new StringBuilder();
                        for (int i = 0; i < usersArray.length(); i++) {
                            String username = usersArray.getString(i);
                            usersList.append(username).append("\n");
                        }

                        // Wyświetlamy listę użytkowników
                        textViewUsers.setText("Users:\n" + usersList.toString());
                    } else {
                        textViewUsers.setText("No users found");
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    textViewUsers.setText("Error parsing user data");
                }
            } else {
                textViewUsers.setText("Failed to connect to user data");
            }
        }
    }
}