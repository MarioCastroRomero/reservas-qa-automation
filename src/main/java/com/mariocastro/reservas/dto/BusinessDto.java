package com.mariocastro.reservas.dto;

import com.mariocastro.reservas.model.Business;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BusinessDto {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private UUID uuid;

    @NotBlank(message = "Name must not be blank")
    @Size(max = 100, message = "Name must be at most 100 characters")
    private String name;

    @NotBlank(message = "CIF must not be blank")
    @Pattern(regexp = "^[ABCDEFGHJNPQRSUVW][0-9]{7}[0-9A-J]$", message = "CIF format is not valid (example: B12345678 or A1234567J)")
    private String cif;

    @NotBlank(message = "Address must not be blank")
    @Pattern(regexp = "^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑüÜ\\s,.-/ºª#()]{5,150}$", message = "Address must be between 5 and 150 characters and only contain valid characters")
    private String address;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private UUID ownerId;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Boolean active;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String ownerName;

    public static BusinessDto fromBusiness(Business business) {
        BusinessDto businessDto = new BusinessDto();
        businessDto.setUuid(business.getUuid());
        businessDto.setName(business.getName());
        businessDto.setCif(business.getCif());
        businessDto.setAddress(business.getAddress());
        businessDto.setOwnerId(business.getOwner().getUuid());
        businessDto.setActive(business.isActive());
        businessDto.setOwnerName(fullName(business.getOwner().getName(), business.getOwner().getLastName()));
        return businessDto;
    }

    static String fullName(String name, String lastName) {
        return lastName == null || lastName.isBlank() ? name : name + " " + lastName;
    }

    public Business toBusiness() {
        Business business = new Business();
        business.setName(this.getName());
        business.setCif(this.getCif());
        business.setAddress(this.getAddress());
        return business;
    }
}
