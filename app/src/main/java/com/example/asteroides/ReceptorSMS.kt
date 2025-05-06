package com.example.asteroides

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReceptorSMS : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "android.provider.Telephony.SMS_RECEIVED") {
            val i = Intent(context, AcercaDeActivity::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context?.startActivity(i)
        }
    }
}
