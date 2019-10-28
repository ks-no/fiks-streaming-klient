package no.ks.fiks.streaming.klient.authentication;

import no.ks.fiks.maskinporten.Maskinportenklient;
import org.eclipse.jetty.client.api.Request;
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
        request.header(HttpHeader.AUTHORIZATION, "Bearer " + getAccessToken())
                .header("IntegrasjonId", integrasjonId.toString())
                .header("IntegrasjonPassord", integrasjonPassord);
    }

    private String getAccessToken() {
        return maskinportenklient.getAccessToken("ks:fiks");
    }
}
