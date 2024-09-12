package com.provoly.service.coswin;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.BadRequestException;

import org.apache.hc.client5.http.ContextBuilder;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.auth.CredentialsProviderBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

@ApplicationScoped
public class CoswinClient {

    public static final String SERVICE_ID = "jrjrCode";

    private final Logger logger;
    private final CoswinProperties coswin;
    private final ObjectMapper mapper = new ObjectMapper();

    public CoswinClient(Logger logger, CoswinProperties coswin) {
        this.logger = logger;
        this.coswin = coswin;
    }

    public String sendExternalService(CoswinServiceWriteDto dto) throws IOException {
        try (final CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpHost target = new HttpHost(coswin.scheme(), coswin.host(), coswin.port());

            HttpClientContext localContext = ContextBuilder.create()
                    .useCredentialsProvider(CredentialsProviderBuilder.create()
                            .add(target, new UsernamePasswordCredentials(coswin.username(), coswin.password().toCharArray()))
                            .build())
                    .build();

            var post = new HttpPost(
                    "%s://%s:%s/ws/rest/jobrequests?cwUser=%s&dataSource=%s".formatted(
                            coswin.scheme(),
                            coswin.host(),
                            coswin.port(),
                            dto.jrjrRequester(),
                            coswin.dataSource()));

            var jobRequestCoswin = Map.of(
                    "jobrequestcreate", dto,
                    "jrUserStatusCreateList", Map.of("jrUserStatusCreate", List.of()));
            String body = mapper.writeValueAsString(jobRequestCoswin);

            post.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));

            logger.infof("Send external service to Coswin: %s", dto.jrjrJobDescription());
            var response = httpclient.execute(post, localContext, res -> {
                if (res.getCode() == 200) {
                    return mapper.readTree(res.getEntity().getContent());
                }
                throw new BadRequestException("Error during service creation : Coswin response code " + res.getCode() + ": "
                        + mapper.readTree(res.getEntity().getContent()).get("message").textValue());
            });
            return response.get(SERVICE_ID).textValue();
        }
    }
}
