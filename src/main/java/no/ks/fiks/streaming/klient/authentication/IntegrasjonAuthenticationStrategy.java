package no.ks.fiks.streaming.klient.authentication;

import no.ks.fiks.maskinporten.AccessTokenRequest;
import no.ks.fiks.maskinporten.Maskinportenklient;
import org.eclipse.jetty.client.Request;
import org.eclipse.jetty.http.HttpHeader;

import java.util.UUID;

public class IntegrasjonAuthenticationStrategy implements AuthenticationStrategy {

    private final Maskinportenklient maskinportenklient;
    private final UUID integrasjonId;
    private final String integrasjonPassord;

    public IntegrasjonAuthenticationStrategy(Maskinportenklient maskinportenklient, UUID integrasjonId, String integrasjonPassord) {
        this.maskinportenklient = maskinportenklient;
        this.integrasjonId = integrasjonId;
        this.integrasjonPassord = integrasjonPassord;
    }

    public void setAuthenticationHeaders(Request request) {
        request.headers(headers -> {
                    headers.put(HttpHeader.AUTHORIZATION, "Bearer " + getAccessToken());
                    headers.put("IntegrasjonId", integrasjonId.toString());
                    headers.put("IntegrasjonPassord", integrasjonPassord);
                }
        );
    }

    private String getAccessToken() {
        return maskinportenklient.getAccessToken(AccessTokenRequest.builder().scope("ks:fiks").build());
    }
}
