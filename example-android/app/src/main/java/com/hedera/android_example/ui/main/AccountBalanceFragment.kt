/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2022 - 2024 Hedera Hashgraph, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.hedera.android_example.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.hedera.android_example.R
import com.hiero.sdk.AccountBalanceQuery
import com.hiero.sdk.AccountId
import com.hiero.sdk.Client
import com.hiero.sdk.PrecheckStatusException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.util.concurrent.TimeoutException

class AccountBalanceFragment : Fragment() {
    private lateinit var accountBalance: TextView
    private lateinit var accountId: EditText
    private lateinit var button: Button
    private lateinit var progressBar: ProgressBar
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstance: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_account_balance, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        accountBalance = view.findViewById(R.id.accountBalance)
        accountId = view.findViewById(R.id.accountId)
        button = view.findViewById(R.id.button)
        progressBar = view.findViewById(R.id.progressBar)
        val operatorId = AccountId.fromString(view.resources.getString(R.string.operator_id))
        accountId.setText(operatorId.toString())
        button.setOnClickListener {

            lifecycleScope.executeAsyncTask(
                onPreExecute = {
                    accountBalance.text = ""
                    button.visibility = View.INVISIBLE
                    progressBar.visibility = View.VISIBLE
                }, doInBackground = {
                    try {
                        val id = AccountId.fromString(accountId.text.toString())
                        val client = Client.forTestnet()
                        val balance = AccountBalanceQuery()
                            .setAccountId(id)
                            .execute(client).hbars
                        balance.toString()
                    } catch (e: TimeoutException) {
                        "Error: " + e.message
                    } catch (e: PrecheckStatusException) {
                        "Error: " + e.message
                    } catch (e: Exception) {
                        "Error: " + e.message
                    }
                }, onPostExecute = {
                    accountBalance.text = it
                    button.visibility = View.VISIBLE
                    progressBar.visibility = View.INVISIBLE
                })


        }
    }

    private fun <R> CoroutineScope.executeAsyncTask(
        onPreExecute: () -> Unit,
        doInBackground: () -> R,
        onPostExecute: (R) -> Unit
    ) = launch {
        onPreExecute() // runs in Main Thread
        val result = withContext(Dispatchers.IO) {
            doInBackground() // runs in background thread without blocking the Main Thread
        }
        onPostExecute(result) // runs in Main Thread
    }
}
