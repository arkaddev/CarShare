package com.example.carshare;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Home extends AppCompatActivity {

    private TextView textViewWelcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        textViewWelcome = findViewById(R.id.textViewWelcome);

        // Pobierz nazwę użytkownika z Intent
        String username = getIntent().getStringExtra("username");

        // Wyświetl powitanie
        textViewWelcome.setText("Witaj " + username);
    }
}