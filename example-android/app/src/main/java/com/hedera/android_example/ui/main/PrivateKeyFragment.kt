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

import android.widget.TextView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.view.View
import com.hedera.android_example.R
import androidx.fragment.app.Fragment
import com.hedera.hashgraph.sdk.PrivateKey

class PrivateKeyFragment : Fragment() {
    private  lateinit var privateKey: TextView
    private lateinit var publicKey: TextView
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstance: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_private_key, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        privateKey = view.findViewById(R.id.privateKey)
        publicKey = view.findViewById(R.id.publicKey)
        view.findViewById<View>(R.id.button).setOnClickListener {
            val key = PrivateKey.generateED25519()
            val pKey = key.publicKey
            privateKey.text = key.toString()
            publicKey.text = pKey.toString()
        }
    }
}
