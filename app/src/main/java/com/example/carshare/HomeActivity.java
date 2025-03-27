package com.example.carshare;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
    private TextView textViewTotalRidesById; // Pole do wyświetlania przejazdow uzytkowika

    private TextView textViewUserInfo; // Pole do wyświetlania informacji o użytkowniku

    private Ride lastRide;

    private int currentUserId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Inicjalizacja pola tekstowego dla licznika
        textViewCounter = findViewById(R.id.textViewCounter);

        // Inicjalizacja pola tekstowego dla przejazdow uzytkowika
        textViewTotalRidesById = findViewById(R.id.textViewTotalRidesById);


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

        // Pobieramy user_id z zamiaru aktualnego użytkownika
        currentUserId = Integer.parseInt(getIntent().getStringExtra("userId"));


        if (username != null) {
            // Wyświetlamy login w odpowiednim TextView
            textViewUserInfo.setText("Zalogowany: " + username + " ID: " + currentUserId);
        } else {
            textViewUserInfo.setText("Brak danych o użytkowniku.");
        }



//przekierowanie dla buttona nr 1
        Button button1 = findViewById(R.id.button1);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, AddRideActivity.class);
                intent.putExtra("token", token);  // Przekazujemy token
                intent.putExtra("userId", currentUserId);  // Przekazujemy id użytkownika
                if (lastRide != null) {

                    //Log.d("HomeActivity", "Final Counter: " + lastRide.getFinalCounter());
                    //Toast.makeText(HomeActivity.this, "Final Counter: " + lastRide.getFinalCounter(), Toast.LENGTH_LONG).show();

                    intent.putExtra("counter", lastRide.getFinalCounter()); // Przekazanie finalCounter
                }

                startActivity(intent);
            }
        });

//przekierowanie dla buttona nr 2
        Button button2 = findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, RideActivity.class);
                intent.putExtra("token", token);  // Przekazujemy token
                startActivity(intent);
            }
        });

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

                        lastRide = null; // Przechowujemy ostatni przejazd (najwyższe ID)


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
                            textViewCounter.setText("Aktualny stan licznika: \n" + lastRide.getFinalCounter() + " km");

                        } else {
                            textViewCounter.setText("Brak danych o stanie licznika");
                        }

                        // Obliczanie całkowitej przejechanej odległości i przygotowanie tekstu do wyświetlenia
                        int totalDistance = 0;
                        //StringBuilder ridesText = new StringBuilder("Przejazdy:\n");
                        StringBuilder ridesText = new StringBuilder();
                        for (Ride ride : ridesList) {
                            if (ride.getUserId() == currentUserId) {
                                totalDistance += ride.getDistance();
//                                ridesText.append("ID: ").append(ride.getId())
//                                        .append(", Data: ").append(ride.getDate())
//                                        .append(", Km: ").append(ride.getDistance())
//                                        .append(", User ID: ").append(ride.getUserId()) // Dodanie user_id do wyświetlania
//                                        .append("\n");
                            }
                        }
                        ridesText.append("\nŁączny przebyty dystans: ").append(totalDistance).append(" km");
                        textViewTotalRidesById.setText(ridesText.toString());
                    } else {
                        textViewCounter.setText("Brak danych o stanie licznika");

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    textViewCounter.setText("Błąd w odczycie licznika.");

                }
            } else {
                textViewCounter.setText("Nie udało się pobrać danych o liczniku.");

            }
        }
    }
}