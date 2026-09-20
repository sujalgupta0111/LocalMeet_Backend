package com.users.dtos;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record AddressDTO(

	    @Size(max = 50)
	    String houseNo,

	    @Size(max = 150)
	    String street,

	    @Size(max = 100)
	    String city,

	    @Size(max = 100)
	    String state,

	    @Size(max = 100)
	    String country,

	    @Pattern(
	        regexp = "^[0-9]{6}$",
	        message = "Pincode must be 6 digits"
	    )
	    String pincode

) {
}