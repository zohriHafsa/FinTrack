package com.fintrack.dto.request;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data

public class RegisterRequest {
    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @Size(min = 8, message = "Password must be at least 8 characters")
    @NotBlank(message = "Password is required")
    private String password;

    // Required when registering as a new company owner; ignored when inviteToken is set.
    private String companyName;

    private String sector;

    // When present, this registration joins an existing company as an ACCOUNTANT
    // instead of creating a new company as an OWNER.
    private String inviteToken;
}
