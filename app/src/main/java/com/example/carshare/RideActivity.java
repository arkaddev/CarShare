package com.example.carshare;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.carshare.Model.Ride;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RideActivity extends AppCompatActivity {

    private TextView textViewRides;   // Pole do wyświetlania listy przejazdów
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride);

        // Inicjalizacja pola tekstowego dla przejazdów
        textViewRides = findViewById(R.id.textViewUsers);


        // Odbieramy token przekazany z HomeActivity
        String token = getIntent().getStringExtra("token");
        currentUserId = getIntent().getIntExtra("userId", -1);
        //Toast.makeText(this, "User ID: " + currentUserId, Toast.LENGTH_LONG).show();


        if (token != null) {
            // Pobieramy listę przejazdów, przekazując token
            new RideActivity.RidesDataTask().execute(token);
        } else {
            Toast.makeText(this, "No token found", Toast.LENGTH_SHORT).show();
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
                            int userId = rideObject.getInt("user_id");
                            int archive = rideObject.getInt("archive");
                            int correct = rideObject.getInt("correct");

                            Ride ride = new Ride(id, date, initialCounter, finalCounter, userId, archive, correct);
                            ridesList.add(ride);
                        }

// Sortowanie listy według ID w kolejności malejącej
                        Collections.sort(ridesList, new Comparator<Ride>() {
                            @Override
                            public int compare(Ride r1, Ride r2) {
                                return Integer.compare(r2.getId(), r1.getId()); // Sortowanie malejące
                            }
                        });


                        // Obliczanie całkowitej przejechanej odległości i przygotowanie tekstu do wyświetlenia
//                        int totalDistance = 0;
//                        StringBuilder ridesText = new StringBuilder("Przejazdy:\n");
//                        for (Ride ride : ridesList) {
//                            if (ride.getUserId() == currentUserId && ride.getCorrect() == 0) {
//                                totalDistance += ride.getDistance();
//                                ridesText.append("").append(ride.getId())
//                                        .append(", Data: ").append(ride.getDate())
//                                        .append(", Km: ").append(ride.getDistance())
//                                        //.append(", User ID: ").append(ride.getUserId())
////                                        .append(" A: ").append(ride.getArchive())
////                                        .append(" C: ").append(ride.getCorrect())
//                                        .append(" Koszt: ").append(ride.getTotalCost()).append(" zł")
//                                        .append("\n");
//                            }
//                        }

                        int totalDistance = 0;
                        StringBuilder ridesText = new StringBuilder();

// Nagłówek tabeli
                        ridesText.append(String.format("%-5s %-15s %-10s %-10s\n", "ID", "Data", "Km", "Koszt"));
                        ridesText.append("———————————————————————————————————\n");

                        for (Ride ride : ridesList) {
                            if (ride.getUserId() == currentUserId && ride.getCorrect() == 0) {
                                totalDistance += ride.getDistance();
                                ridesText.append(String.format("%-5d %-15s %-10d %-10.2f\n",
                                        ride.getId(), ride.getDate(), ride.getDistance(), ride.getTotalCost()));
                            }
                        }




                        ridesText.append("\nŁączny przebyty dystans: ").append(totalDistance).append(" km");
                        textViewRides.setText(ridesText.toString());
                    } else {

                        textViewRides.setText("Brak przejazdów do wyświetlenia.");
                    }

                } catch (JSONException e) {
                    e.printStackTrace();

                    textViewRides.setText("Błąd podczas przetwarzania danych.");
                }
            } else {

                textViewRides.setText("Nie udało się pobrać danych o przejazdach.");
            }
        }


    }
}
