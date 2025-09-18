package org.example.util;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.docs.v1.Docs;
import com.google.api.services.docs.v1.DocsScopes;
import com.google.api.services.docs.v1.model.BatchUpdateDocumentRequest;
import com.google.api.services.docs.v1.model.InsertTextRequest;
import com.google.api.services.docs.v1.model.Location;
import com.google.api.services.docs.v1.model.Request;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

public class GoogleDocs {

    private static final String APPLICATION_NAME = "Garage Feedbacks";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String DOCUMENT_ID = "1MyyAQ_ADtrd1bkDUxs7LpnzuxtweKDY1AP-h4hrEXkk";
    private static final String SERVICE_ACCOUNT_KEY_PATH = "src/main/resources/credentials.json";


    public void sendMessage(String text) throws IOException, GeneralSecurityException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        GoogleCredentials credentials = GoogleCredentials.fromStream(
                        new FileInputStream(SERVICE_ACCOUNT_KEY_PATH))
                .createScoped(Collections.singleton(DocsScopes.DOCUMENTS));

        Docs service = new Docs.Builder(HTTP_TRANSPORT, JSON_FACTORY, new HttpCredentialsAdapter(credentials))
                .setApplicationName(APPLICATION_NAME)
                .build();

        Request insertTextRequest = new Request().setInsertText(
                new InsertTextRequest()
                        .setText(text + "\n")
                        .setLocation(new Location().setIndex(1))
        );

        BatchUpdateDocumentRequest body = new BatchUpdateDocumentRequest()
                .setRequests(Collections.singletonList(insertTextRequest));

        service.documents().batchUpdate(DOCUMENT_ID, body).execute();
    }
}
