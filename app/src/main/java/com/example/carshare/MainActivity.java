package com.example.carshare;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    private EditText editTextUsername, editTextPassword;
    private Button buttonLogin;
    private TextView textViewMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewMessage = findViewById(R.id.textViewMessage);

        buttonLogin.setOnClickListener(v -> {
            String username = editTextUsername.getText().toString();
            String password = editTextPassword.getText().toString();

            // Uruchamiamy zadanie do logowania
            new LoginTask().execute(username, password);
        });
    }

    private class LoginTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String username = params[0];
            String password = params[1];

            try {
                // Tworzymy URL i łączenie
                URL url = new URL("http://tankujemy.online/login2.php");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                // Tworzymy JSON z danymi logowania
                JSONObject jsonData = new JSONObject();
                jsonData.put("username", username);
                jsonData.put("password", password);

                // Wysyłamy dane JSON
                OutputStream os = connection.getOutputStream();
                os.write(jsonData.toString().getBytes());
                os.flush();

                // Odbieramy odpowiedź
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();

                return response.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    JSONObject response = new JSONObject(result);

                    // Sprawdzamy, czy odpowiedź zawiera token
                    if (response.has("token")) {
                        String token = response.getString("token");

                        String userId = response.getString("user_id");

                        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                        intent.putExtra("token", token);  // Przekazujemy token
                        intent.putExtra("username", editTextUsername.getText().toString());
                        intent.putExtra("userId", userId);  // Przekazujemy id użytkownika
                        startActivity(intent);
                        finish();  // Zakończenie bieżącej aktywności



                        Toast.makeText(MainActivity.this, "Zalogowano", Toast.LENGTH_SHORT).show();
                    } else {
                        // Jeśli nie ma tokenu, wyświetl komunikat o błędzie
                        textViewMessage.setText("Invalid credentials");
                        textViewMessage.setVisibility(View.VISIBLE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    textViewMessage.setText("Error parsing response");
                    textViewMessage.setVisibility(View.VISIBLE);
                }
            } else {
                textViewMessage.setText("Błędne dane logowania lub brak połączenia internetowego");
                textViewMessage.setVisibility(View.VISIBLE);
            }
        }
    }


}