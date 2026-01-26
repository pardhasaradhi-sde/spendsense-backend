package com.spendsense.controller;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spendsense.model.webhook.ClerkUserData;
import com.spendsense.security.WebhookVerificationService;
import com.spendsense.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhooks")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Webhooks", description = "Webhook handlers for external services")
public class WebhookController {
    private final UserService userService;
    private final WebhookVerificationService  webhookVerificationService;
    private final ObjectMapper objectMapper;

    @PostMapping("/clerk")
    @Operation(summary = "Handle Clerk webhooks", description = "Processes webhook events from Clerk authentication service")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Webhook processed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid webhook payload or signature")
    })
    public ResponseEntity<Void> handleClerkWebhook(
            @RequestBody String payload,
            @Parameter(hidden = true) @RequestHeader HttpHeaders headers
    ){
        try{
            webhookVerificationService.verifyWebHook(payload, headers);
            //parse event
            ClerkWebhookEvent event=objectMapper.readValue(payload,ClerkWebhookEvent.class);
            log.info("Clerk Webhook event received: " + event.getType());
            //handle event
            switch(event.getType()){
                case "user.created":
                    log.info("processing webhook event : user.created");
                    userService.createUserFromClerk(event.getData());
                    break;
                case "user.updated":
                    log.info("processing webhook event : user.updated");
                    userService.updateUserFromClerk(event.getData());
                    break;
                case "user.deleted":
                    log.info("processing webhook event : user.deleted");
                    userService.deleteUserFromClerk(event.getData().getId());
                    break;
                default:
                    log.warn("Unhandled webhook event:"+event.getType());
            }
            return ResponseEntity.ok().build();
        }
        catch (Exception e){
            log.error("Error processing webhook: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

}
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(hidden = true)
class ClerkWebhookEvent {
    private String type;
    private ClerkUserData data;
}