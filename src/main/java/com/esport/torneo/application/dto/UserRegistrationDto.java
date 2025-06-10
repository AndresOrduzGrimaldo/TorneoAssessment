package com.esport.torneo.application.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO para el registro de nuevos usuarios.
 * 
 * Incluye validaciones específicas para el proceso de registro
 * y campos opcionales para información adicional del perfil.
 * 
 * @author Andrés Orduz
 * @version 1.0
 * @since 1.0
 */
public class UserRegistrationDto {
    
    @NotBlank(message = "El username es obligatorio")
    @Size(min = 3, max = 50, message = "El username debe tener entre 3 y 50 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "El username solo puede contener letras, números, guiones y guiones bajos")
    private String username;
    
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    private String email;
    
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", 
             message = "La contraseña debe contener al menos: 1 minúscula, 1 mayúscula, 1 número y 1 carácter especial")
    private String password;
    
    @NotBlank(message = "La confirmación de contraseña es obligatoria")
    private String confirmPassword;
    
    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre completo debe tener entre 2 y 100 caracteres")
    private String fullName;
    
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "El formato del teléfono no es válido")
    private String phone;
    
    @Size(max = 100, message = "El país no puede exceder 100 caracteres")
    private String country;
    
    @Size(max = 100, message = "La ciudad no puede exceder 100 caracteres")
    private String city;
    
    @Past(message = "La fecha de nacimiento debe ser en el pasado")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;
    
    @AssertTrue(message = "Debe aceptar los términos y condiciones")
    private Boolean acceptTerms;
    
    @AssertTrue(message = "Debe aceptar la política de privacidad")
    private Boolean acceptPrivacy;
    
    // Campos opcionales para marketing
    private Boolean acceptNewsletters = false;
    private Boolean acceptPromotions = false;
    
    // Constructores
    public UserRegistrationDto() {}
    
    public UserRegistrationDto(String username, String email, String password, String fullName) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
    }
    
    // Getters y Setters
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getConfirmPassword() {
        return confirmPassword;
    }
    
    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getCountry() {
        return country;
    }
    
    public void setCountry(String country) {
        this.country = country;
    }
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public LocalDate getBirthDate() {
        return birthDate;
    }
    
    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }
    
    public Boolean getAcceptTerms() {
        return acceptTerms;
    }
    
    public void setAcceptTerms(Boolean acceptTerms) {
        this.acceptTerms = acceptTerms;
    }
    
    public Boolean getAcceptPrivacy() {
        return acceptPrivacy;
    }
    
    public void setAcceptPrivacy(Boolean acceptPrivacy) {
        this.acceptPrivacy = acceptPrivacy;
    }
    
    public Boolean getAcceptNewsletters() {
        return acceptNewsletters;
    }
    
    public void setAcceptNewsletters(Boolean acceptNewsletters) {
        this.acceptNewsletters = acceptNewsletters;
    }
    
    public Boolean getAcceptPromotions() {
        return acceptPromotions;
    }
    
    public void setAcceptPromotions(Boolean acceptPromotions) {
        this.acceptPromotions = acceptPromotions;
    }
    
    // Métodos de validación
    @AssertTrue(message = "Las contraseñas no coinciden")
    public boolean isPasswordsMatch() {
        return password != null && password.equals(confirmPassword);
    }
    
    @AssertTrue(message = "Debe ser mayor de 13 años")
    public boolean isAgeValid() {
        if (birthDate == null) return true; // Campo opcional
        return birthDate.plusYears(13).isBefore(LocalDate.now());
    }
    
    @Override
    public String toString() {
        return "UserRegistrationDto{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", phone='" + phone + '\'' +
                ", country='" + country + '\'' +
                ", city='" + city + '\'' +
                ", birthDate=" + birthDate +
                ", acceptTerms=" + acceptTerms +
                ", acceptPrivacy=" + acceptPrivacy +
                '}';
    }
} 