package com.myapps.core;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.widget.Toast;

import java.util.ArrayList;


public class SMSModule{

    private final Activity activity;


    public SMSModule(Activity activity){this.activity = activity;}

    public void sendSMS(String phoneNumber, String message) {
        ArrayList<PendingIntent> sentPendingIntents = new ArrayList<PendingIntent>();
        ArrayList<PendingIntent> deliveredPendingIntents = new ArrayList<PendingIntent>();
        PendingIntent sentPI = PendingIntent.getBroadcast(activity, 0,
                new Intent(activity, SmsSentReceiver.class), 0); // Dodanie intent sprawdzenie czy wysłano sms (klasa zagnieżdżona SmsSentReciver)
        PendingIntent deliveredPI = PendingIntent.getBroadcast(activity, 0,
                new Intent(activity, SmsDeliveredReceiver.class), 0);  // Dodanie INtent , sprawdzenie czy sms został dostarczon czy nie (klasa zagnieżdżona SmsDeliveredReciver)
        try {
            SmsManager sms = SmsManager.getDefault(); // Przypisanie Managera sms
            ArrayList<String> mSMSMessage = sms.divideMessage(message); // dodanie do ArrayList treści sms
            for (int i = 0; i < mSMSMessage.size(); i++) {
                sentPendingIntents.add(i, sentPI); // dodanie intent
                deliveredPendingIntents.add(i, deliveredPI); // dodanie intent
            }
            sms.sendMultipartTextMessage(phoneNumber, null, mSMSMessage,
                    sentPendingIntents, deliveredPendingIntents);  // Wyślij SMS

        } catch (Exception e) {

            e.printStackTrace();
           showToast("sms sending failed ...");
        }

    }

    //Obsłga zdarzeń
    public class SmsDeliveredReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent arg1) {
            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    showToast("sms delivered");
                    break;
                case Activity.RESULT_CANCELED:
                    showToast("sms not delivered");
                    break;
            }
        }
    }

    //Obsłga zdarzeń
        public class SmsSentReceiver extends BroadcastReceiver {
            @Override
            public void onReceive(Context context, Intent arg1) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        showToast("sms Sent");
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        showToast("generic failure");
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        showToast("no service");
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        showToast("null PDU");
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                       showToast("radio Off");
                        break;
                }
            }
        }


    //Wyświetlanie informacji (niebieski pasek na dole ekranu)
    private void showToast(String txt){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, "SMS no service", Toast.LENGTH_SHORT)
                        .show();
            }
        });

    }
}
