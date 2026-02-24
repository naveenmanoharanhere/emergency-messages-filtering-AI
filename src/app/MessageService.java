package com.example.emergencyfilter;

import android.app.NotificationManager;
import android.content.*;
import android.content.res.AssetFileDescriptor;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.widget.Toast;

import org.tensorflow.lite.Interpreter;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import okhttp3.*;

public class MessageService extends BroadcastReceiver {

    private static Interpreter interpreter;
    private static final String API_KEY = "YOUR_ELEVENLABS_API_KEY";


    public static void initialize(Context context) {
        try {
            interpreter = new Interpreter(loadModel(context));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static MappedByteBuffer loadModel(Context context) throws Exception {

        AssetFileDescriptor fd =
                context.getAssets().openFd("emergency_model.tflite");

        FileInputStream input = new FileInputStream(fd.getFileDescriptor());
        FileChannel channel = input.getChannel();

        return channel.map(
                FileChannel.MapMode.READ_ONLY,
                fd.getStartOffset(),
                fd.getDeclaredLength());
    }


    @Override
    public void onReceive(Context context, Intent intent) {

        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) {

            for (SmsMessage sms :
                    Telephony.Sms.Intents.getMessagesFromIntent(intent)) {

                processText(context, sms.getMessageBody());
            }
        }
    }

    /* ================= TEXT CLASSIFICATION ================= */

    private static void processText(Context context, String text) {

        int[][] input = Tokenizer.tokenize(text);
        float[][] output = new float[1][1];

        interpreter.run(input, output);

        if (output[0][0] > 0.5f) {
            triggerEmergency(context, text);
        }
    }


    public static void processAudio(Context context, File audioFile) {

        OkHttpClient client = new OkHttpClient();

        RequestBody body =
                new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart(
                                "file",
                                audioFile.getName(),
                                RequestBody.create(
                                        audioFile,
                                        MediaType.parse("audio/wav")))
                        .build();

        Request request = new Request.Builder()
                .url("https://api.elevenlabs.io/v1/speech-to-text")
                .addHeader("xi-api-key", API_KEY)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response)
                    throws IOException {

                String result = response.body().string();

                String transcript =
                        result.replaceAll(".*\"text\":\"", "")
                              .replaceAll("\".*", "");

                processText(context, transcript);
            }
        });
    }


    private static void triggerEmergency(Context context, String msg) {

        NotificationManager manager =
                (NotificationManager)
                        context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (manager.isNotificationPolicyAccessGranted()) {
            manager.setInterruptionFilter(
                    NotificationManager.INTERRUPTION_FILTER_ALL);
        }

        Toast.makeText(
                context,
                "🚨 Emergency Detected:\n" + msg,
                Toast.LENGTH_LONG
        ).show();
    }
}
