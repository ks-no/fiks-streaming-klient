package no.ks.fiks.streaming.klient.authentication;

import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;

public class PersonAuthenticationStrategy implements AuthenticationStrategy {

    private final String bearerToken;

    public PersonAuthenticationStrategy(String bearerToken) {
        this.bearerToken = bearerToken;
    }

    public void setAuthenticationHeaders(Request request) {
        request.headers(headers -> headers.put(HttpHeader.AUTHORIZATION, "Bearer " + bearerToken));
    }

}
