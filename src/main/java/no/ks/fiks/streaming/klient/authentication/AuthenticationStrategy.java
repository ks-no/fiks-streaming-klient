package no.ks.fiks.streaming.klient.authentication;


import org.eclipse.jetty.client.Request;

public interface AuthenticationStrategy {
    void setAuthenticationHeaders(Request request);
}
