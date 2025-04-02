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

public class PaymentActivity extends AppCompatActivity {

    private TextView textViewPayments;   // Pole do wyświetlania platnosci
    private int currentUserId;

    private Button buttonPay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        textViewPayments = findViewById(R.id.textViewPayments); // Inicjalizacja pola tekstowego dla przejazdów

        // Odbieramy token przekazany z innego ekranu (np. logowania)
        String token = getIntent().getStringExtra("token");
        currentUserId = getIntent().getIntExtra("userId", -1);
        if (token != null) {
            // Pobieramy listę przejazdów, przekazując token
            new PaymentActivity.PaymentsDataTask().execute(token);
        } else {
            Toast.makeText(this, "No token found", Toast.LENGTH_SHORT).show();
        }




        //przekierowanie dla buttona pay
        buttonPay = findViewById(R.id.buttonPay);
        buttonPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PaymentActivity.this, AddPaymentActivity.class);
                intent.putExtra("token", token);  // Przekazujemy token
                intent.putExtra("userId", currentUserId);  // Przekazujemy id użytkownika
                startActivity(intent);
            }
        });
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

                Log.d("PaymentsDataTask", "Response: " + response.toString()); // Logowanie odpowiedzi dla debugowania

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

                        // Sortowanie listy według ID w kolejności malejącej
                        Collections.sort(paymentList, new Comparator<Payment>() {
                            @Override
                            public int compare(Payment r1, Payment r2) {
                                return Integer.compare(r2.getId(), r1.getId()); // Sortowanie malejące
                            }
                        });

                        // Przygotowanie tekstu do wyświetlenia
//                        StringBuilder paymentText = new StringBuilder();
//                        for (Payment payment : paymentList) {
//                            if (payment.getUserId() == currentUserId){
//                            paymentText.append("").append(payment.getId())
//                                    .append(", Data: ").append(payment.getDate())
//                                    .append(", Kwota: ").append(payment.getAmount()).append(" zł ")
//                                    .append(", Km: ").append(payment.getDistance())
//                                    .append(", User ID: ").append(payment.getUserId())
//                                    .append("\n");
//                        }
//                    }

                        StringBuilder paymentText = new StringBuilder();
                        paymentText.append(String.format("%-5s %-15s %-10s\n", "ID", "Data", "Kwota")); // Nagłówki
                        paymentText.append("———————————————————————————\n"); // Linia oddzielająca

                        for (Payment payment : paymentList) {
                            if (payment.getUserId() == currentUserId) {
                                paymentText.append(String.format("%-5d %-15s %-10.2f\n",
                                        payment.getId(), payment.getDate(), payment.getAmount()));
                            }
                        }

                        textViewPayments.setText(paymentText.toString());
                    } else {
                        textViewPayments.setText("Brak płatności do wyświetlenia.");
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    textViewPayments.setText("Błąd podczas przetwarzania danych.");
                }
            } else {
                textViewPayments.setText("Nie udało się pobrać danych o płatnościach.");
            }
        }
    }
}