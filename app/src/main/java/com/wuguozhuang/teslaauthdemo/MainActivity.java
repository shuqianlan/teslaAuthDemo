package com.wuguozhuang.teslaauthdemo;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.gson.Gson;

import java.util.Arrays;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final String CLIENT_ID = "ownerapi";
    private static final String CLIENT_SECRET = "c7257eb71a564034f9419ee651c7d0e5f7aa6bfbd18bafb5c5c033b093bb2fa3";

    private static final String authTokenUrl_CHINA = "https://auth.tesla.cn/oauth2/v3/token";
    private static final String authTokenUrl_GLOBAL = "https://auth.tesla.com/oauth2/v3/token";

    private static final String authorizeUrl_CHINA = "https://auth.tesla.cn/oauth2/v3/authorize";
    private static final String authorizeUrl_GLOBAL = "https://auth.tesla.com/oauth2/v3/authorize";

    public static String redirectUrl = "https://auth.tesla.com/void/callback";
    private AuthorizationCodeFlow flow;
    ActivityResultLauncher launcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        flow = new AuthorizationCodeFlow.Builder(
                BearerToken.authorizationHeaderAccessMethod(),
                new NetHttpTransport(),
                new GsonFactory(),
                new GenericUrl(authTokenUrl_CHINA),
                new ClientParametersAuthentication(CLIENT_ID, CLIENT_SECRET),
                CLIENT_ID,
                authorizeUrl_CHINA
        ).setScopes(Arrays.asList("openid", "email", "offline_access")).enablePKCE().build();

        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    String code = result.getData().getStringExtra("code");
                    exchangeCode(code);
                }
            }
        });
    }

    public void onClickAuth(View v) {
        String url = authorizeUrl();

        Intent i = new Intent(this, AuthActivity.class);
        i.putExtra("url", url);
        i.putExtra("redirectUrl", redirectUrl);

        launcher.launch(i);
    }

    private String authorizeUrl() {
        return flow.newAuthorizationUrl().setRedirectUri(redirectUrl).setState(getRandomString(6)).build();
    }

    private void exchangeCode(final String code) {
        AsyncTask.THREAD_POOL_EXECUTOR.execute(() -> {
            runOnUiThread(() -> {
                ((TextView)findViewById(R.id.innerContent)).setText("获取中...");
            });
            boolean succ = false;
            String jsonStr = "failed";
            try {
                TokenResponse response = flow.newTokenRequest(code).setGrantType("authorization_code").setRedirectUri(redirectUrl).execute();
                succ = response != null;

                if (succ) {
                    jsonStr = new Gson().toJson(response);
                }
            } catch (Exception ex) {
                jsonStr = ex.getMessage();
            } finally {
                String finalJsonStr = jsonStr;
                runOnUiThread(() -> {
                    String content = new StringBuilder("code:").append(code).append("\n").append("jsonStr:").append(finalJsonStr).toString();
                    ((TextView)findViewById(R.id.innerContent)).setText(content);
                });
            }
        });
    }

    public static String getRandomString(int length) {
        String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(62);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }

}