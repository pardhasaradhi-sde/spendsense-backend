package com.spendsense.model.webhook;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(hidden = true)
public class ClerkUserData {

    private String id;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    @JsonProperty("image_url")
    private String imageUrl;

    @JsonProperty("email_addresses")
    private List<EmailAddress> emailAddresses;

    @JsonProperty("primary_email_address_id")
    private String primaryEmailAddressId;

    // Helper method to get primary email
    public String getEmail() {
        if (emailAddresses == null || emailAddresses.isEmpty()) {
            return null;
        }

        // Find primary email
        if (primaryEmailAddressId != null) {
            return emailAddresses.stream()
                    .filter(e -> primaryEmailAddressId.equals(e.getId()))
                    .map(EmailAddress::getEmailAddress)
                    .findFirst()
                    .orElse(emailAddresses.get(0).getEmailAddress());
        }

        // Fallback to first email
        return emailAddresses.get(0).getEmailAddress();
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EmailAddress {
        private String id;

        @JsonProperty("email_address")
        private String emailAddress;
    }
}