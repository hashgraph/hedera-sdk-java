package com.hedera.android_example.ui.main;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.hedera.android_example.R;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.HederaStatusException;
import com.hedera.hashgraph.sdk.account.AccountBalanceQuery;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;

public class AccountBalanceFragment extends Fragment {
    private TextView accountBalance;
    private EditText accountId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstance) {
        return inflater.inflate(R.layout.fragment_account_balance, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        accountBalance = view.findViewById(R.id.accountBalance);
        accountId = view.findViewById(R.id.accountId);

        view.findViewById(R.id.button).setOnClickListener(v -> {
            try {
                final AccountId operatorId = AccountId.fromString(view.getResources().getString(R.string.operator_id));
                final Ed25519PrivateKey operatorKey = Ed25519PrivateKey.fromString(view.getResources().getString(R.string.operator_key));
                Client client = Client.forTestnet().setOperator(operatorId, operatorKey);

                final AccountId id = AccountId.fromString(accountId.getText().toString());

                new AccountBalanceAsyncTask(id).execute(client);

            } catch (IllegalArgumentException e) {
                accountBalance.setText("Error: " + e.getMessage());
            }
        });
    }

    private class AccountBalanceAsyncTask extends AsyncTask<Client, Void, String> {
        final AccountId accountId;

        AccountBalanceAsyncTask(AccountId accountId) {
            this.accountId = accountId;
        }

        @Override
        protected String doInBackground(Client... clients) {
            try {
                String balance = new AccountBalanceQuery()
                    .setAccountId(this.accountId)
                    .execute(clients[0])
                    .toString();

                return "Account balance: " + balance;
            } catch (HederaStatusException e) {
                return "Error: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            accountBalance.setText(result);
        }
    }
}
