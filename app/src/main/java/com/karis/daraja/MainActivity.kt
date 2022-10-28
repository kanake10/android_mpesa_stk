package com.karis.daraja

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.karis.daraja.driver.DarajaDriver
import com.karis.daraja.model.requests.STKPushRequest
import com.karis.daraja.ui.theme.DarajaTheme
import com.karis.daraja.utils.getPassword
import com.karis.daraja.utils.sanitizePhoneNumber
import com.karis.daraja.utils.timestamp
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DarajaTheme {
                Column(
                    Modifier.padding(16.dp)
                        .fillMaxWidth()
                        .fillMaxHeight(.4F),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    val phoneState = remember { mutableStateOf(TextFieldValue("0710102720")) }
                    TextField(
                        placeholder = { Text(text = "Enter phone number") },
                        value = phoneState.value,
                        onValueChange = { phoneState.value = it }
                    )

                    val amount = remember { mutableStateOf(TextFieldValue("1")) }
                    TextField(
                        placeholder = { Text(text = "Enter amount") },
                        value = amount.value,
                        onValueChange = { amount.value = it }
                    )

                    Button(onClick = {
                        sendStkPush(amount.value.text, phoneState.value.text)
                    }) {
                        Text(text = "Initiate Payment")
                    }
                }
            }
        }
    }

    private fun sendStkPush(amount: String, phoneNumber: String) {
        val stkPushRequest = STKPushRequest(
            businessShortCode = Constants.BUSINESS_SHORT_CODE,
            password = getPassword(Constants.BUSINESS_SHORT_CODE, Constants.PASSKEY, timestamp),
            timestamp = timestamp,
            transactionType = "CustomerPayBillOnline",
            amount = amount,
            partyA = sanitizePhoneNumber(phoneNumber),
            partyB = Constants.PARTYB,
            phoneNumber = sanitizePhoneNumber(phoneNumber),
            callBackURL = Constants.CALLBACKURL,
            accountReference = "Dlight", // Account reference
            transactionDesc = "Dlight STK PUSH " // Transaction description
        )

        val darajaDriver = DarajaDriver(
            consumerKey = BuildConfig.CONSUMER_KEY,
            consumerSecret = BuildConfig.CONSUMER_SECRET
        )

        lifecycleScope.launch {
            darajaDriver.performStkPush(stkPushRequest).collect()
        }
    }
}
