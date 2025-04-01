package com.example.carshare;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.example.carshare.Model.Ride;
import com.example.carshare.Model.User;

public class AddRideActivity extends AppCompatActivity {

    private EditText editTextDate, editTextInitialCounter, editTextFinalCounter;
    private Button buttonSubmit;
    private Button buttonCorrect;
    private String token;
    private int userId;
    private int counter;

    private LinearLayout checkboxLoginContainer;

    // Lista do przechowywania ID zaznaczonych użytkowników
    private List<Integer> selectedUserIds;

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



        checkboxLoginContainer = findViewById(R.id.checkboxLoginContainer);

       selectedUserIds = new ArrayList<>();
       selectedUserIds.add(userId);

        // Lista użytkowników (loginów) oraz ich ID
        List<User> users = Arrays.asList(
                new User(4,"Arek"),
                new User(1,"Patryk"),
                new User(2,"Klaudia"),
                new User(3,"Wiktoria")
        );

        // Tworzymy dynamicznie CheckBoxy
        for (User user : users) {

            if (user.getId() == userId) {
                // Jeśli tak, pomijamy tego użytkownika i nie tworzymy dla niego CheckBoxa
                continue;
            }

            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(user.getLogin());  // Ustawiamy tekst jako login

            // Ustawiamy parametr ID jako tag w CheckBoxie
            checkBox.setTag(user.getId());

            // Dodajemy nasłuchiwacz zdarzeń dla zaznaczenia/odznaczenia
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int id = (int) buttonView.getTag();  // Pobieramy ID z tagu

                if (isChecked) {
                    selectedUserIds.add(id);  // Dodajemy ID do listy zaznaczonych
                } else {
                    selectedUserIds.remove(Integer.valueOf(id));  // Usuwamy ID z listy zaznaczonych
                }

                // Wyświetlamy liczbę zaznaczonych CheckBoxów
                //Toast.makeText(this, "Liczba zaznaczonych użytkowników: " + selectedUserIds.size(), Toast.LENGTH_SHORT).show();

                // Można także wyświetlić pełną listę zaznaczonych ID
                //Toast.makeText(this, "Zaznaczone ID: " + selectedUserIds.toString(), Toast.LENGTH_SHORT).show();

            });

            checkBox.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));

            // Dodajemy CheckBox do kontenera
            checkboxLoginContainer.addView(checkBox);
        }
    }


/*
        // Lista użytkowników (loginów)
        List<String> users = Arrays.asList("Arek", "Patryk", "Klaudia", "Wiktoria");

        // Tworzymy dynamicznie CheckBoxy
        for (String user : users) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(user);  // Ustawiamy tekst jako login
            checkBox.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));

            // Dodajemy CheckBox do kontenera
            checkboxLoginContainer.addView(checkBox);
        }
*/



    private void submitRide() {
        String date = editTextDate.getText().toString();
        String initialCounter = editTextInitialCounter.getText().toString();
        String finalCounter = editTextFinalCounter.getText().toString();

        if (date.isEmpty() || initialCounter.isEmpty() || finalCounter.isEmpty()) {
            Toast.makeText(this, "Wypełnij wszystkie pola", Toast.LENGTH_SHORT).show();
            return;
        }

        // zamiana String na int
        int initial = Integer.parseInt(initialCounter);
        int finalVal = Integer.parseInt(finalCounter);

        if (initial > finalVal) {
            Toast.makeText(this, "Stan licznika za niski.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (finalVal - initial > 500) {
            Toast.makeText(this, "Różnica między stanem licznika zbyt duża.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Sprawdzenie, czy finalVal ma 6 cyfr
//        if (finalVal < 100000 || finalVal > 999999) {
//            Toast.makeText(this, "Numer końcowy musi mieć 6 cyfr.", Toast.LENGTH_SHORT).show();
//            return;
//        }

        // Obliczamy różnicę między licznikiem początkowym a końcowym
        int difference = finalVal - initial;

        // Sprawdzamy ile użytkowników będzie miało przypisany ten przejazd
        int userCount = selectedUserIds.size();
        if (userCount > 1) {
            // Dzielimy różnicę między użytkowników
            int perUser = difference / userCount;
            int remainder = difference % userCount; // reszta, którą trzeba dodać do pierwszych użytkowników

            // Przejście po liście selectedUserIds i dodanie przejazdu dla każdego użytkownika
            int currentInitial = initial;
            for (int i = 0; i < userCount; i++) {
                int currentFinal = currentInitial + perUser + (i < remainder ? 1 : 0);  // Dodajemy resztę do pierwszych użytkowników

                // Uruchamiamy zadanie dodawania przejazdu dla danego użytkownika
                new AddRideTask().execute(date, String.valueOf(currentInitial), String.valueOf(currentFinal), String.valueOf(selectedUserIds.get(i)));

                currentInitial = currentFinal;  // Przesuwamy initial_counter dla kolejnego użytkownika
            }
        } else {
            // Jeśli tylko jeden użytkownik, po prostu dodajemy przejazd bez podziału
            new AddRideTask().execute(date, initialCounter, finalCounter, String.valueOf(selectedUserIds.get(0)));
        }
    }

    private class AddRideTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String date = params[0];
            String initialCounter = params[1];
            String finalCounter = params[2];
            String userId = params[3];  // Odbieramy userId z argumentów

            HttpURLConnection connection = null;

                try {
                    URL url = new URL("http://tankujemy.online/rides.php");
                    connection = (HttpURLConnection) url.openConnection();
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

                    connection.disconnect();

                    if (responseCode == 200 || responseCode == 201) {
                        return "success";
                    } else {
                        return "error";
                    }

                } catch (Exception e) {
                    Log.e("AddRideTask", "Error adding ride", e);  // Logowanie błędu

                    e.printStackTrace();
                    return "error";
                } finally {
                    // Wykonanie disconnect w bloku finally, aby upewnić się, że połączenie zostanie zamknięte
                    if (connection != null) {
                        connection.disconnect();
                    }
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
