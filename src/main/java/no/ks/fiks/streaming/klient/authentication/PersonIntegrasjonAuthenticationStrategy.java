package no.ks.fiks.streaming.klient.authentication;

import org.eclipse.jetty.client.Request;
import org.eclipse.jetty.http.HttpHeader;

import java.util.UUID;

public class PersonIntegrasjonAuthenticationStrategy implements AuthenticationStrategy {

    private final UUID integrasjonId;
    private final String integrasjonPassord;
    private final String personToken;

    public PersonIntegrasjonAuthenticationStrategy(String personToken, UUID integrasjonId, String integrasjonPassord) {
        this.personToken = personToken;
        this.integrasjonId = integrasjonId;
        this.integrasjonPassord = integrasjonPassord;
    }

    public void setAuthenticationHeaders(Request request) {
        request.headers(headers -> {
                    headers.put(HttpHeader.AUTHORIZATION, "Bearer " + personToken);
                    headers.put("IntegrasjonId", integrasjonId.toString());
                    headers.put("IntegrasjonPassord", integrasjonPassord);
                }
        );
    }
}
