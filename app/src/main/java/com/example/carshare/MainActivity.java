package com.example.carshare;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.view.Gravity;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Tworzymy TextView programowo
        TextView textView = new TextView(this);
        textView.setText("Hello, World!");
        textView.setTextSize(24);

        // Ustawiamy layout
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.addView(textView);

        // Ustawiamy layout jako główny widok
        setContentView(layout);
    }
}