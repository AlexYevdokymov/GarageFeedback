package org.example.util;

import okhttp3.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class TrelloService {

    private String api_key;
    private String token;
    private String list_id;
    private static final String BASE_URL = "https://api.trello.com/1";
    private final OkHttpClient client = new OkHttpClient();

    public TrelloService() {
        Properties props = new Properties();
        try (InputStream in = new FileInputStream("config.properties")) {
            props.load(in);
            api_key = props.getProperty("trello.apikey");
            token = props.getProperty("trello.token");
            list_id = props.getProperty("trello.listid");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createCard(String cardName, String description) throws Exception {
        HttpUrl url = HttpUrl.parse(BASE_URL + "/cards").newBuilder()
                .addQueryParameter("idList", list_id)
                .addQueryParameter("key", api_key)
                .addQueryParameter("token", token)
                .addQueryParameter("name", cardName)
                .addQueryParameter("desc", description)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(new byte[0], null))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("Помилка створення картки: " + response);
            }
        }
    }
}

