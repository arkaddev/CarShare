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

import com.example.carshare.Model.Refueling;
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

public class RefuelingActivity extends AppCompatActivity {

    private TextView textViewRefuelings;
    private TextView buttonAddRefueling;
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refueling);


        // Inicjalizacja pola tekstowego dla przejazdów
        textViewRefuelings = findViewById(R.id.textViewRefuelings);


        // Odbieramy token przekazany z HomeActivity
        String token = getIntent().getStringExtra("token");
        currentUserId = getIntent().getIntExtra("userId", -1);
        //Toast.makeText(this, "User ID: " + currentUserId, Toast.LENGTH_LONG).show();


        if (token != null) {
            // Pobieramy listę przejazdów, przekazując token
            new RefuelingActivity.RefuelingsDataTask().execute(token);
        } else {
            Toast.makeText(this, "No token found", Toast.LENGTH_SHORT).show();
        }


        //przekierowanie dla buttona nr 1
        buttonAddRefueling = findViewById(R.id.buttonAddRefueling);
        buttonAddRefueling.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RefuelingActivity.this, AddRefuelingActivity.class);
                intent.putExtra("token", token);  // Przekazujemy token
                intent.putExtra("userId", currentUserId);  // Przekazujemy id użytkownika
                startActivity(intent);
            }
        });


    }


    // Klasa do pobierania danych o przejazdach w tle
    private class RefuelingsDataTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String token = params[0];

            try {
                // Tworzymy połączenie z API
                URL url = new URL("http://tankujemy.online/refuelings.php"); // Adres API
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

                Log.d("RefuelingsDataTask", "Response: " + response.toString()); // Logowanie odpowiedzi dla debugowania

                connection.disconnect();

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

                    if (response.has("refuelings")) {
                        JSONArray refuelingArray = response.getJSONArray("refuelings");
                        List<Refueling> refuelingList = new ArrayList<>();


                        for (int i = 0; i < refuelingArray.length(); i++) {
                            JSONObject refuelingObject = refuelingArray.getJSONObject(i);

                            int id = refuelingObject.getInt("id");
                            String dateRefueling = refuelingObject.getString("date_refueling");
                            double pricePerLiter = refuelingObject.getDouble("price_per_liter");
                            double amountRefueling = refuelingObject.getDouble("amount_refueling");
                            int userId = refuelingObject.getInt("userId");


                            Refueling refueling = new Refueling(id, dateRefueling, pricePerLiter, amountRefueling, userId);
                            refuelingList.add(refueling);

                        }

// Sortowanie listy według ID w kolejności malejącej
                        Collections.sort(refuelingList, new Comparator<Refueling>() {
                            @Override
                            public int compare(Refueling r1, Refueling r2) {
                                return Integer.compare(r2.getId(), r1.getId()); // Sortowanie malejące
                            }
                        });


                        // Obliczanie całkowitej przejechanej odległości i przygotowanie tekstu do wyświetlenia

//                        StringBuilder refuelingText = new StringBuilder("Tankowania:\n");
//                        for (Refueling refueling : refuelingList) {
//
//
//                            refuelingText.append("ID: ").append(refueling.getId())
//                                    .append(", Data: ").append(refueling.getDateRefueling())
//                                    .append(", Cena za l: ").append(refueling.getPricePerLiter())
//                                    .append(", Zapłacono: ").append(refueling.getAmountRefueling())
//                                    .append(" User: ").append(refueling.getUserId())
//                                    .append("\n");
//
//                        }


                        // z userId

                        StringBuilder refuelingText = new StringBuilder();
                        refuelingText.append(String.format("%-5s %-15s %-12s %-12s %-8s\n", "ID", "Data", "Cena za l", "Zapłacono", "User")); // Nagłówki
                        refuelingText.append("——————————————————————————————————————————————————\n"); // Linia oddzielająca

                        for (Refueling refueling : refuelingList) {
                            refuelingText.append(String.format("%-5d %-15s %-12.2f %-12.2f %-8d\n",
                                    refueling.getId(), refueling.getDateRefueling(), refueling.getPricePerLiter(), refueling.getAmountRefueling(), refueling.getUserId()));
                        }

// bez userId

//                        StringBuilder refuelingText = new StringBuilder();
//                        refuelingText.append(String.format("%-5s %-15s %-12s %-12s\n", "ID", "Data", "Cena za l", "Zapłacono")); // Nagłówki
//                        refuelingText.append("—————————————————————————————————————————————\n"); // Linia oddzielająca
//
//                        for (Refueling refueling : refuelingList) {
//                            refuelingText.append(String.format("%-5d %-15s %-12.2f %-12.2f\n",
//                                    refueling.getId(), refueling.getDateRefueling(), refueling.getPricePerLiter(), refueling.getAmountRefueling()));
//                        }

                        textViewRefuelings.setText(refuelingText.toString());
                    } else {

                        textViewRefuelings.setText("Brak tankowań do wyświetlenia.");
                    }

                } catch (JSONException e) {
                    e.printStackTrace();

                    textViewRefuelings.setText("Błąd podczas przetwarzania danych.");
                }
            } else {

                textViewRefuelings.setText("Nie udało się pobrać danych o tankowaniach.");
            }
        }


    }
}