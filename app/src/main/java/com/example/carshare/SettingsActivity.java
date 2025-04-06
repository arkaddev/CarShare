package com.example.carshare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {


    Button buttonLogout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        buttonLogout = findViewById(R.id.buttonLogout);


        // Odbieramy token przekazany z innego ekranu (np. logowania)
        String token = getIntent().getStringExtra("token");

        if (token != null) {

            buttonLogout.setOnClickListener(view -> {
                // Usunięcie danych logowania
                SharedPreferences preferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
//            editor.remove("token");
//            editor.remove("userId");
//            editor.remove("username");
                editor.clear();
                editor.apply();

                Toast.makeText(SettingsActivity.this, "Wylogowano", Toast.LENGTH_SHORT).show();

                // Przejście do ekranu logowania
                Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            });



        } else {
            Toast.makeText(this, "No token found", Toast.LENGTH_SHORT).show();
        }




    }
}
