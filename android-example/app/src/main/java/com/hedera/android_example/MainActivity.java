package com.hedera.android_example;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText privateKey = findViewById(R.id.private_key);
        Button genPrivateKey = findViewById(R.id.gen_private_key);

        genPrivateKey.setOnClickListener(_view -> {
            privateKey.setText(Ed25519PrivateKey.generate().toString());
        });
    }
}
