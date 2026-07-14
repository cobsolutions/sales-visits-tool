package com.sales.visits.app.sales.dto.request;

import com.sales.visits.app.sales.model.enums.PreferredCommunication;
import com.sales.visits.app.sales.utils.AddressPatterns;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record AccountFieldsRequest(
        @NotBlank @Size(max = 255) String organizationName,
        Long parentOrganizationId,

        String organizationType,

        @NotNull Long boroughId,

        @NotNull Long ptocLocationId,

        @NotBlank
        @Pattern(regexp = AddressPatterns.ADDRESS_REGEX,
                message = "Address must be formatted as 'Street, City'")
        String address,

        String floorSuite,

        @NotBlank @Pattern(regexp = "^\\d{5}$", message = "Zip code must be 5 digits")
        String zipCode,

        @NotBlank @Pattern(regexp = "^\\(\\d{3}\\) \\d{3}-\\d{4}$",
                message = "Phone must be formatted as (XXX) XXX-XXXX")
        String phone,

        String fax,

        @Email String email,

        boolean providesTelehealth,

        boolean sameDayWalkins,

        String gatekeeperName,

        String gatekeeperTitle,

        String gatekeeperPhone,

        @Email String gatekeeperEmail,

        PreferredCommunication preferredCommunication
) {}
