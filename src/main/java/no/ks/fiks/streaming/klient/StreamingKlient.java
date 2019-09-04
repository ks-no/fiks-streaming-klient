package no.ks.fiks.streaming.klient;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.ks.fiks.streaming.klient.authentication.AuthenticationStrategy;
import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.util.InputStreamResponseListener;
import org.eclipse.jetty.client.util.MultiPartContentProvider;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class StreamingKlient {

    private final HttpClient client = new HttpClient(new SslContextFactory.Client());
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AuthenticationStrategy authenticationStrategy;
    private Long listenerTimeout;
    private TimeUnit listenerTimeUnit;

    public StreamingKlient(AuthenticationStrategy authenticationStrategy) {
        this(authenticationStrategy, 5L, TimeUnit.MINUTES);
    }

    public StreamingKlient(AuthenticationStrategy authenticationStrategy, Long listenerTimeout, TimeUnit listenerTimeUnit) {
        this.authenticationStrategy = authenticationStrategy;
        this.listenerTimeout = listenerTimeout;
        this.listenerTimeUnit = listenerTimeUnit;
        try {
            this.client.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <T> KlientResponse<T> sendRequest(MultiPartContentProvider contentProvider, HttpMethod httpMethod, String baseUrl, String path, List<HttpHeader> headers, TypeReference returnType) {
        InputStreamResponseListener listener = sendRequestReturnResponseListener(contentProvider, httpMethod, baseUrl, path, headers);

        try {
            Response response = listener.get(listenerTimeout, listenerTimeUnit);
            int status = response.getStatus();
            if (isError(status)) {
                String content = IOUtils.toString(listener.getInputStream(), StandardCharsets.UTF_8);
                throw new KlientHttpException(String.format("HTTP-feil (%d): %s", status, content), status, content);
            }
            return buildResponse(response, returnType != null ? objectMapper.readValue(listener.getInputStream(), returnType) : null);
        } catch (InterruptedException | TimeoutException | ExecutionException | IOException e) {
            throw new RuntimeException("Feil under invokering av api", e);
        }
    }

    public KlientResponse<InputStream> sendDownloadRequest(MultiPartContentProvider contentProvider, HttpMethod httpMethod, String baseUrl, String path, List<HttpHeader> headers) {
        InputStreamResponseListener listener = sendRequestReturnResponseListener(contentProvider, httpMethod, baseUrl, path, headers);

        try {
            Response response = listener.get(listenerTimeout, listenerTimeUnit);
            int status = response.getStatus();
            if (isError(status)) {
                String content = IOUtils.toString(listener.getInputStream(), StandardCharsets.UTF_8);
                throw new KlientHttpException(String.format("HTTP-feil (%d): %s", status, content), status, content);
            }
            return buildResponse(response, listener.getInputStream());
        } catch (InterruptedException | TimeoutException | ExecutionException | IOException e) {
            throw new RuntimeException("Feil under invokering av api", e);
        }
    }

    public KlientResponse<byte[]> sendGetRawContentRequest(HttpMethod httpMethod, String baseUrl, String path, List<HttpHeader> headers) {
        Request request = client.newRequest(baseUrl);
        authenticationStrategy.setAuthenticationHeaders(request);

        if (headers != null) {
            headers.forEach(header -> request.header(header.getHeaderName(), header.getHeaderValue()));
        }

        try {
            ContentResponse response = request
                    .method(httpMethod)
                    .path(path)
                    .send();

            int status = response.getStatus();
            if (isError(status)) {
                String content = response.getContentAsString();
                throw new KlientHttpException(String.format("HTTP-feil (%d): %s", status, content), status, content);
            }

            return buildResponse(response, response.getContent());
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new RuntimeException("Feil under invokering av api", e);
        }
    }

    private InputStreamResponseListener sendRequestReturnResponseListener(MultiPartContentProvider contentProvider, HttpMethod httpMethod, String baseUrl, String path, List<HttpHeader> headers) {
        InputStreamResponseListener listener = new InputStreamResponseListener();

        Request request = client.newRequest(baseUrl);
        authenticationStrategy.setAuthenticationHeaders(request);

        if (headers != null) {
            headers.forEach(header -> request.header(header.getHeaderName(), header.getHeaderValue()));
        }
        request
                .method(httpMethod)
                .path(path)
                .content(contentProvider)
                .send(listener);
        return listener;
    }

    private <T> KlientResponse<T> buildResponse(Response response, T result) {
        return KlientResponse.<T>builder()
                .result(result)
                .httpStatus(response.getStatus())
                .httpHeaders(response.getHeaders().stream().collect(Collectors.toMap(HttpField::getName, HttpField::getValue, (prev, next) -> next, HashMap::new)))
                .build();

    }

    private boolean isError(int httpStatus) {
        return !HttpStatus.isSuccess(httpStatus);
    }
}