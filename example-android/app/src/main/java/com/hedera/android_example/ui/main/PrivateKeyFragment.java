package com.hedera.android_example.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.hedera.android_example.R;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;

public class PrivateKeyFragment extends Fragment {
    private TextView privateKey;
    private TextView publicKey;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstance) {
        return inflater.inflate(R.layout.fragment_private_key, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        privateKey = view.findViewById(R.id.privateKey);
        publicKey = view.findViewById(R.id.publicKey);

        view.findViewById(R.id.button).setOnClickListener(v -> {
            final Ed25519PrivateKey key = Ed25519PrivateKey.generate();

            privateKey.setText(key.toString());
            publicKey.setText(key.publicKey.toString());
        });
    }
}
