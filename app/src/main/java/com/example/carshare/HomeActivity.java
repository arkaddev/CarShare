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
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private TextView textViewCounter; // Pole do wyświetlania aktualnego stanu licznika
    private TextView textViewRides;   // Pole do wyświetlania listy przejazdów

    private TextView textViewUserInfo; // Pole do wyświetlania informacji o użytkowniku

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        textViewCounter = findViewById(R.id.textViewCounter); // Inicjalizacja pola tekstowego dla licznika
        textViewRides = findViewById(R.id.textViewUsers); // Inicjalizacja pola tekstowego dla przejazdów

        // Odbieramy token przekazany z innego ekranu (np. logowania)
        String token = getIntent().getStringExtra("token");

        if (token != null) {
            // Pobieramy listę przejazdów, przekazując token
            new RidesDataTask().execute(token);
        } else {
            Toast.makeText(this, "No token found", Toast.LENGTH_SHORT).show();
        }


// wyswietlanie username

        textViewUserInfo = findViewById(R.id.textViewUserInfo); // Inicjalizacja TextView
        String username = getIntent().getStringExtra("username");

        if (username != null) {
            // Wyświetlamy login w odpowiednim TextView
            textViewUserInfo.setText("Witaj: " + username);
        } else {
            textViewUserInfo.setText("Brak danych o użytkowniku.");
        }

    }

    // Klasa do pobierania danych o przejazdach w tle
    private class RidesDataTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String token = params[0];

            try {
                // Tworzymy połączenie z API
                URL url = new URL("http://tankujemy.online/rides.php"); // Adres API
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", "Bearer " + token);

                // Odczytujemy odpowiedź
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();

                Log.d("RidesDataTask", "Response: " + response.toString()); // Logowanie odpowiedzi dla debugowania

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

                    if (response.has("rides")) {
                        JSONArray ridesArray = response.getJSONArray("rides");
                        List<Ride> ridesList = new ArrayList<>();

                        Ride lastRide = null; // Przechowujemy ostatni przejazd (najwyższe ID)

                        for (int i = 0; i < ridesArray.length(); i++) {
                            JSONObject rideObject = ridesArray.getJSONObject(i);

                            int id = rideObject.getInt("id");
                            String date = rideObject.getString("date");
                            int initialCounter = rideObject.getInt("initial_counter");
                            int finalCounter = rideObject.getInt("final_counter");
                            int userId = rideObject.getInt("user_id"); // Pobranie user_id

                            Ride ride = new Ride(id, date, initialCounter, finalCounter, userId);
                            ridesList.add(ride);

                            // Znajdujemy przejazd z najwyższym ID
                            if (lastRide == null || ride.getId() > lastRide.getId()) {
                                lastRide = ride;
                            }
                        }

                        // Ustawienie aktualnego stanu licznika
                        if (lastRide != null) {
                            textViewCounter.setText("Aktualny stan licznika: " + lastRide.getFinalCounter() + " km");
                        } else {
                            textViewCounter.setText("Brak danych o stanie licznika");
                        }

                        // Obliczanie całkowitej przejechanej odległości i przygotowanie tekstu do wyświetlenia
                        int totalDistance = 0;
                        StringBuilder ridesText = new StringBuilder("Przejazdy:\n");
                        for (Ride ride : ridesList) {
                            totalDistance += ride.getDistance();
                            ridesText.append("ID: ").append(ride.getId())
                                    .append(", Data: ").append(ride.getDate())
                                    .append(", Przejechane km: ").append(ride.getDistance())
                                    .append(", Użytkownik ID: ").append(ride.getUserId()) // Dodanie user_id do wyświetlania
                                    .append("\n");
                        }

                        ridesText.append("\nŁączny przebyty dystans: ").append(totalDistance).append(" km");
                        textViewRides.setText(ridesText.toString());
                    } else {
                        textViewCounter.setText("Brak danych o stanie licznika");
                        textViewRides.setText("Brak przejazdów do wyświetlenia.");
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    textViewCounter.setText("Błąd w odczycie licznika.");
                    textViewRides.setText("Błąd podczas przetwarzania danych.");
                }
            } else {
                textViewCounter.setText("Nie udało się pobrać danych o liczniku.");
                textViewRides.setText("Nie udało się pobrać danych o przejazdach.");
            }
        }
    }
}