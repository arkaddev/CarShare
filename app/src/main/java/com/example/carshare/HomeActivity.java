package com.example.carshare;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.carshare.Model.Payment;
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

public class HomeActivity extends AppCompatActivity {

    private TextView textViewCounter; // Pole do wyświetlania aktualnego stanu licznika
    private TextView textViewUserInfo; // Pole do wyświetlania informacji o użytkowniku

    private Button buttonAdminRides, buttonAdminPayments, buttonAdminRefuelings;
    private Ride lastRide;
    private int currentUserId;

    private double totalCost;
    private int totalDistance;
    private List<Integer> filteredRideIds;
    private TextView textViewAccountBalance;
    private double totalAmount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Inicjalizacja pola tekstowego dla licznika
        textViewCounter = findViewById(R.id.textViewCounter);

        textViewAccountBalance = findViewById(R.id.textViewAccountBalance);

        // Odbieramy token przekazany z innego ekranu (np. logowania)
        String token = getIntent().getStringExtra("token");

        if (token != null) {
            // Pobieramy listę przejazdów, przekazując token
            new RidesDataTask().execute(token);
            new PaymentsDataTask().execute(token);
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
            textViewUserInfo.setText("Zalogowany: " + username + " ID: " + currentUserId + "token: " + token);
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
                intent.putExtra("userId", currentUserId);  // Przekazujemy id użytkownika
                startActivity(intent);
            }
        });

        //przekierowanie dla buttona nr 3
        Button button3 = findViewById(R.id.button3);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, RefuelingActivity.class);
                intent.putExtra("token", token);  // Przekazujemy token
                intent.putExtra("userId", currentUserId);  // Przekazujemy id użytkownika
                startActivity(intent);
            }
        });

        //przekierowanie dla buttona nr 4
        Button button4 = findViewById(R.id.button4);
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, PaymentActivity.class);
                intent.putExtra("token", token);  // Przekazujemy token
                intent.putExtra("userId", currentUserId);  // Przekazujemy id użytkownika
                startActivity(intent);
            }
        });

        //przekierowanie dla buttona nr 5
        Button button5 = findViewById(R.id.button5);
        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, StatisticActivity.class);
                intent.putExtra("token", token);  // Przekazujemy token
                intent.putExtra("userId", currentUserId);  // Przekazujemy id użytkownika
                startActivity(intent);
            }
        });


        //przekierowanie dla buttona nr 6
        Button button6 = findViewById(R.id.button6);
        button6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
                intent.putExtra("token", token);  // Przekazujemy token
                intent.putExtra("userId", currentUserId);  // Przekazujemy id użytkownika
                startActivity(intent);
            }
        });

        //przekierowanie dla button Admin Ride
        buttonAdminRides = findViewById(R.id.buttonAdminRides);
        buttonAdminRides.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, AdminActivityRide.class);
                intent.putExtra("token", token);  // Przekazujemy token
                startActivity(intent);
            }
        });
//przekierowanie dla buttona Admin Payment
        buttonAdminPayments = findViewById(R.id.buttonAdminPayments);
        buttonAdminPayments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, AdminPaymentActivity.class);
                intent.putExtra("token", token);  // Przekazujemy token
                startActivity(intent);
            }
        });

        //przekierowanie dla buttona Admin Refuelings
        buttonAdminRefuelings = findViewById(R.id.buttonAdminRefuelings);
        buttonAdminRefuelings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, AdminRefuelingActivity.class);
                intent.putExtra("token", token);  // Przekazujemy token
                startActivity(intent);
            }
        });

    }


    // AUTOMATYCZNE ODŚWIEŻANIE DANYCH PO POWROCIE DO AKTYWNOŚCI
    @Override
    protected void onResume() {
        super.onResume();
        refreshData(); // Pobiera aktualne dane o przejazdach
    }

    // Metoda do pobrania nowych danych
    private void refreshData() {
        String token = getIntent().getStringExtra("token");
        if (token != null) {
            new RidesDataTask().execute(token);
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

                    if (response.has("rides")) {
                        JSONArray ridesArray = response.getJSONArray("rides");
                        List<Ride> ridesList = new ArrayList<>();
                        filteredRideIds = new ArrayList<>();

                        lastRide = null; // Przechowujemy ostatni przejazd (najwyższe ID)


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

                            // Znajdujemy przejazd z najwyższym ID
                            if (lastRide == null || ride.getId() > lastRide.getId()) {
                                lastRide = ride;
                            }
                        }

                        // Ustawienie aktualnego stanu licznika
                        if (lastRide != null) {
                            //textViewCounter.setText("Aktualny stan licznika: \n" + lastRide.getFinalCounter() + " km");
                            textViewCounter.setText(lastRide.getFinalCounter() + " km");

                        } else {
                            textViewCounter.setText("Brak danych o stanie licznika");
                        }

                        // Obliczanie całkowitej przejechanej odległości i przygotowanie tekstu do wyświetlenia
                        totalDistance = 0;
                        //StringBuilder ridesText = new StringBuilder("Przejazdy:\n");
                        StringBuilder ridesText = new StringBuilder();
                        for (Ride ride : ridesList) {
                            //if (ride.getUserId() == currentUserId && ride.getCorrect() == 0 && ride.getArchive() == 0) {
                            if (ride.getUserId() == currentUserId && ride.getCorrect() == 0) {

                                totalDistance += ride.getDistance();
                                filteredRideIds.add(ride.getId()); // Dodajemy ID do listy
//                                ridesText.append("ID: ").append(ride.getId())
//                                        .append(", Data: ").append(ride.getDate())
//                                        .append(", Km: ").append(ride.getDistance())
//                                        .append(", User ID: ").append(ride.getUserId()) // Dodanie user_id do wyświetlania
//                                        .append("\n");
                            }
                        }
                        //ridesText.append("Łączny przebyty dystans: ").append(totalDistance).append(" km");
                        //textViewTotalRidesById.setText(ridesText.toString());


                        // Obliczenie kwoty do zaplaty
                        StringBuilder paymentText = new StringBuilder();
                        double fuelPrice = 3;
                        double petrolConsumption = 10;
                        totalCost = ((petrolConsumption * totalDistance) / 100) * fuelPrice;
                        totalCost = Math.round(totalCost * 100.0) / 100.0;
                        paymentText.append("Do zapłaty: ").append(totalCost).append(" zł");
                        //textViewPaymentById.setText(paymentText);

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


    // Klasa do pobierania danych o przejazdach w tle
    private class PaymentsDataTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String token = params[0];

            try {
                // Tworzymy połączenie z API
                URL url = new URL("http://tankujemy.online/payments.php"); // Adres API
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

                    if (response.has("payments")) {
                        JSONArray paymentsArray = response.getJSONArray("payments");
                        List<Payment> paymentList = new ArrayList<>();

                        // Iteracja po danych i dodanie do listy
                        for (int i = 0; i < paymentsArray.length(); i++) {
                            JSONObject rideObject = paymentsArray.getJSONObject(i);

                            int id = rideObject.getInt("id");
                            String date = rideObject.getString("date");
                            double amount = rideObject.getDouble("amount");
                            double distance = rideObject.getDouble("distance");
                            int userId = rideObject.getInt("userId");

                            Payment payment = new Payment(id, amount, distance, date, userId);
                            paymentList.add(payment);  // Dodajemy do listy
                        }

                        // Inicjalizacja zmiennej do sumowania kwot
                        totalAmount = 0.0;

                        // Przygotowanie tekstu do wyświetlenia
                        StringBuilder paymentText = new StringBuilder();
                        for (Payment payment : paymentList) {
                            if (payment.getUserId() == currentUserId) {
                                // Sumowanie kwot
                                totalAmount += payment.getAmount();
                            }
                        }


                        // wyswietlenie stanu konta
                        double accountBalance = totalAmount - totalCost;
                        accountBalance = Math.round(accountBalance * 100.0) / 100.0;

                        double dist = (accountBalance * 100) / (3 * 10);
                        dist = Math.round(dist * 100.0) / 100.0;


                        paymentText.append("Stan konta: ").append(accountBalance).append(" zł");
                        paymentText.append(" - (").append(dist).append(" km )");

                        // ustawienie koloru tla w zaleznosci od stanu konta
                        CardView cardView = findViewById(R.id.cardViewAccountBalance);
                        if (accountBalance > 10) {
                            cardView.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#82e0aa")));
                        } else if (accountBalance < 10 && accountBalance >= 0) {
                            cardView.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#f8c471")));
                            paymentText.append("\n\nDoładuj swoje konto!");
                        } else if (accountBalance < 0) {
                            cardView.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#ec7063")));
                            paymentText.append("\n\nKoniecznie doładuj swoje konto!");
                        }
                        textViewAccountBalance.setText(paymentText.toString());

                    } else {
                        textViewAccountBalance.setText("Brak płatności do wyświetlenia.");
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    textViewAccountBalance.setText("Błąd podczas przetwarzania danych.");
                }
            } else {
                textViewAccountBalance.setText("Nie udało się pobrać danych o płatnościach.");
            }
        }
    }


}