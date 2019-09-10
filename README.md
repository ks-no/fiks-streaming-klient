# Streaming for multipart requests
![Maven Central](https://img.shields.io/maven-central/v/no.ks.fiks/streaming-klient)
![GitHub](https://img.shields.io/github/license/ks-no/fiks-streaming-klient)

Bruker Fiks-Maskinporten for autorisering og Jetty HttpClient for sending av multipart requests.

Eksempel på bruk av fiks-streaming-klient ved opplasting av json-data og en fil med tilhørende metadata:
```
package no.ks.fiks.streaming.klient;

import com.fasterxml.jackson.core.type.TypeReference;
import no.ks.fiks.streaming.klient.authentication.AuthenticationStrategy;
import org.eclipse.jetty.client.util.MultiPartContentProvider;
import org.eclipse.jetty.http.HttpMethod;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import static java.util.concurrent.TimeUnit.MINUTES;

public class Eksempel {

  private StreamingKlient streamingKlient;

  private String baseUrl = "https://example.com:443";
  private String path = "/api/endepunkt";

  public class MinMetadata {
    String filnavn = "filnavn.pdf";
    String mimetype = "application/pdf";
    String ekstraInfo = "ekstra";
  }

  public Eksempel(AuthenticationStrategy authenticationStrategy) {
    streamingKlient = new StreamingKlient(authenticationStrategy, 15L, MINUTES);
  }

  public KlientResponse<String> eksempelRequest(String jsonData, InputStream inputStream, String filnavn, MinMetadata minMetadata) {
    FilForOpplasting<Object> minFil = FilForOpplasting.builder()
            .filnavn(filnavn)
            .metadata(minMetadata)
            .data(inputStream)
            .build();

    MultipartContentProviderBuilder multipartBuilder = new MultipartContentProviderBuilder();
    multipartBuilder.addFieldPart(jsonData, "jsonData");
    multipartBuilder.addFileData(minFil);
    MultiPartContentProvider multiPartContentProvider = multipartBuilder.build();

    List<HttpHeader> httpHeaders = Collections.singletonList(HttpHeader.builder().headerName("MinHttpHeader").headerValue("value").build());

    TypeReference<String> returtypeApiResponse = new TypeReference<String>() {};

    return streamingKlient.sendRequest(multiPartContentProvider, HttpMethod.POST, baseUrl, path, httpHeaders, returtypeApiResponse);
  }
}
```
