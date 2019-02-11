package no.ks.fiks.streaming.klient.authentication;

import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;

import java.util.UUID;

public class PersonIntegrasjonAuthenticationStrategy implements AuthenticationStrategy {

    private final UUID integrasjonId;
    private final String integrasjonPassord;
    private String personToken;

    public PersonIntegrasjonAuthenticationStrategy(String personToken, UUID integrasjonId, String integrasjonPassord) {
        this.personToken = personToken;
        this.integrasjonId = integrasjonId;
        this.integrasjonPassord = integrasjonPassord;
    }

    public void setAuthenticationHeaders(Request request) {
        request.header(HttpHeader.AUTHORIZATION, "Bearer " + personToken)
                .header("IntegrasjonId", integrasjonId.toString())
                .header("IntegrasjonPassord", integrasjonPassord);
    }
}
