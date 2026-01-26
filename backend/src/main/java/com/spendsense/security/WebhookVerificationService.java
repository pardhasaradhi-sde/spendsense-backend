package com.spendsense.security;

import com.spendsense.exception.UnauthorizedException;
import com.svix.Webhook;
import com.svix.exceptions.WebhookVerificationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class WebhookVerificationService {

    @Value("${clerk.webhook-secret}")
    private String webhookSecret;

    public void verifyWebHook(String payload, HttpHeaders headers){
        try{
            //extract svix headers
            String svixId=headers.getFirst("svix-id");
            String svixTimeStamp=headers.getFirst("svix-timestamp");
            String svixSignature=headers.getFirst("svix-signature");
            if(svixId==null||svixTimeStamp==null||svixSignature==null){
                throw new RuntimeException("Missing Svix headers");
            }
            //use svix sdk for verification
            Webhook webhook = new Webhook(webhookSecret);
            // Build headers map for verification - Svix expects net.http.HttpHeaders
            java.net.http.HttpHeaders httpHeaders = java.net.http.HttpHeaders.of(
                    java.util.Map.of(
                            "svix-id", java.util.List.of(svixId),
                            "svix-timestamp", java.util.List.of(svixTimeStamp),
                            "svix-signature", java.util.List.of(svixSignature)
                    ),
                    (k, v) -> true  // BiPredicate that allows all headers
            );

            // Verify signature
            webhook.verify(payload, httpHeaders);
            log.info("Webhook verification done successfully");
        } catch (WebhookVerificationException e) {
            log.error("Webhook verification failed: " + e.getMessage());
            throw new UnauthorizedException("Invalid Webhook Signature: " + e.getMessage());
        }
    }
}
