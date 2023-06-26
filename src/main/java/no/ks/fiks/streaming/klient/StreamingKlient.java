package no.ks.fiks.streaming.klient;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.ks.fiks.streaming.klient.authentication.AuthenticationStrategy;
import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.dynamic.HttpClientTransportDynamic;
import org.eclipse.jetty.client.util.InputStreamResponseListener;
import org.eclipse.jetty.client.util.MultiPartRequestContent;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.io.ClientConnector;
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

    private final HttpClient client = buildClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AuthenticationStrategy authenticationStrategy;
    private final Long listenerTimeout;
    private final TimeUnit listenerTimeUnit;

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

    private HttpClient buildClient() {
        ClientConnector clientConnector = new ClientConnector();
        clientConnector.setSslContextFactory(new SslContextFactory.Client());
        return new HttpClient(new HttpClientTransportDynamic(clientConnector));
    }

    public <T> KlientResponse<T> sendRequest(MultiPartRequestContent content, HttpMethod httpMethod, String baseUrl, String path, List<HttpHeader> headers, TypeReference<T> returnType) {
        InputStreamResponseListener listener = sendRequestReturnResponseListener(content, httpMethod, baseUrl, path, headers);

        try {
            final Response response = awaitResponse(listener);
            int status = response.getStatus();
            if (isError(status)) {
                String errorContent = IOUtils.toString(listener.getInputStream(), StandardCharsets.UTF_8);
                throw new KlientHttpException(String.format("HTTP-feil (%d): %s", status, errorContent), status, errorContent);
            }
            try (final InputStream input = listener.getInputStream()) {
                return buildResponse(response, returnType != null ? objectMapper.readValue(input, returnType) : null);
            }
        } catch (IOException e) {
            throw new RuntimeException("Feil under lesing av datastrøm", e);
        }
    }

    public KlientResponse<InputStream> sendDownloadRequest(MultiPartRequestContent content, HttpMethod httpMethod, String baseUrl, String path, List<HttpHeader> headers) {
        InputStreamResponseListener listener = sendRequestReturnResponseListener(content, httpMethod, baseUrl, path, headers);

        try {
            Response response = awaitResponse(listener);
            int status = response.getStatus();
            if (isError(status)) {
                String errorContent = IOUtils.toString(listener.getInputStream(), StandardCharsets.UTF_8);
                throw new KlientHttpException(String.format("HTTP-feil (%d): %s", status, errorContent), status, errorContent);
            }
            return buildResponse(response, listener.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException("Feil under lesing av datastrøm", e);
        }
    }

    private Response awaitResponse(InputStreamResponseListener listener) {
        try {
            return listener.get(listenerTimeout, listenerTimeUnit);
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new RuntimeException("Feil under invokering av api", e);
        }
    }

    public KlientResponse<byte[]> sendGetRawContentRequest(HttpMethod httpMethod, String baseUrl, String path, List<HttpHeader> headers) {
        Request request = client.newRequest(baseUrl);
        authenticationStrategy.setAuthenticationHeaders(request);

        if (headers != null) {
            request.headers(requestHeaders -> headers.forEach(header -> requestHeaders.put(header.name(), header.value())));
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

    private InputStreamResponseListener sendRequestReturnResponseListener(MultiPartRequestContent content, HttpMethod httpMethod, String baseUrl, String path, List<HttpHeader> headers) {
        InputStreamResponseListener listener = new InputStreamResponseListener();

        Request request = client.newRequest(baseUrl);
        authenticationStrategy.setAuthenticationHeaders(request);

        if (headers != null) {
            request.headers(requestHeaders -> headers.forEach(header -> requestHeaders.put(header.name(), header.value())));
        }
        request
                .method(httpMethod)
                .path(path)
                .body(content)
                .send(listener);
        return listener;
    }

    private <T> KlientResponse<T> buildResponse(Response response, T result) {
        return new KlientResponse<>(
                result,
                response.getStatus(),
                response.getHeaders().stream().collect(Collectors.toMap(HttpField::getName, HttpField::getValue, (prev, next) -> next, HashMap::new))
        );
    }

    private boolean isError(int httpStatus) {
        return !HttpStatus.isSuccess(httpStatus);
    }
}