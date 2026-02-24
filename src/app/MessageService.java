package com.example.emergencyfilter;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.widget.Toast;

import org.json.JSONObject;
import org.tensorflow.lite.Interpreter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

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

        FileInputStream inputStream =
                new FileInputStream(fd.getFileDescriptor());

        FileChannel fileChannel = inputStream.getChannel();

        return fileChannel.map(
                FileChannel.MapMode.READ_ONLY,
                fd.getStartOffset(),
                fd.getDeclaredLength()
        );
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) {

            for (SmsMessage sms :
                    Telephony.Sms.Intents.getMessagesFromIntent(intent)) {

                processText(context, sms.getMessageBody());
            }
        }

        if ("VOICE_NOTE_RECEIVED".equals(intent.getAction())) {

            String path = intent.getStringExtra("audioPath");

            if (path != null) {
                processAudio(context, new File(path));
            }
        }
    }

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

        RequestBody requestBody =
                new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart(
                                "file",
                                audioFile.getName(),
                                RequestBody.create(
                                        audioFile,
                                        MediaType.parse("audio/mpeg")
                                )
                        )
                        .addFormDataPart("model_id", "scribe_v1")
                        .build();

        Request request = new Request.Builder()
                .url("https://api.elevenlabs.io/v1/speech-to-text")
                .addHeader("xi-api-key", API_KEY)
                .addHeader("Accept", "application/json")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response)
                    throws IOException {

                if (!response.isSuccessful()) {
                    return;
                }

                String result = response.body().string();

                try {
                    JSONObject json = new JSONObject(result);
                    String transcript = json.getString("text");
                    processText(context, transcript);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private static void triggerEmergency(Context context, String message) {

        NotificationManager manager =
                (NotificationManager)
                        context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (manager.isNotificationPolicyAccessGranted()) {
            manager.setInterruptionFilter(
                    NotificationManager.INTERRUPTION_FILTER_ALL
            );
        }

        Toast.makeText(
                context,
                "Emergency Detected: " + message,
                Toast.LENGTH_LONG
        ).show();
    }
}
