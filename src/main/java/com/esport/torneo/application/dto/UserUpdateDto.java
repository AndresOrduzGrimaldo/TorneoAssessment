package com.esport.torneo.application.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO para la actualización de datos de usuario.
 * 
 * Permite la actualización parcial de información de perfil
 * con validaciones específicas para cada campo.
 * 
 * @author Andrés Orduz
 * @version 1.0
 * @since 1.0
 */
public class UserUpdateDto {
    
    @Email(message = "El formato del email no es válido")
    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    private String email;
    
    @Size(min = 2, max = 100, message = "El nombre completo debe tener entre 2 y 100 caracteres")
    private String fullName;
    
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "El formato del teléfono no es válido")
    private String phone;
    
    @Size(max = 100, message = "El país no puede exceder 100 caracteres")
    private String country;
    
    @Size(max = 100, message = "La ciudad no puede exceder 100 caracteres")
    private String city;
    
    @Size(max = 500, message = "La biografía no puede exceder 500 caracteres")
    private String bio;
    
    @Past(message = "La fecha de nacimiento debe ser en el pasado")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;
    
    @Size(max = 500, message = "La URL del avatar no puede exceder 500 caracteres")
    @Pattern(regexp = "^https?://.*\\.(jpg|jpeg|png|gif|webp)$", 
             message = "La URL del avatar debe ser una imagen válida (jpg, jpeg, png, gif, webp)")
    private String avatarUrl;
    
    // Preferencias de notificación
    private Boolean emailNotifications;
    private Boolean pushNotifications;
    private Boolean marketingEmails;
    
    // Constructores
    public UserUpdateDto() {}
    
    // Getters y Setters
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
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
    
    public String getBio() {
        return bio;
    }
    
    public void setBio(String bio) {
        this.bio = bio;
    }
    
    public LocalDate getBirthDate() {
        return birthDate;
    }
    
    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }
    
    public String getAvatarUrl() {
        return avatarUrl;
    }
    
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
    
    public Boolean getEmailNotifications() {
        return emailNotifications;
    }
    
    public void setEmailNotifications(Boolean emailNotifications) {
        this.emailNotifications = emailNotifications;
    }
    
    public Boolean getPushNotifications() {
        return pushNotifications;
    }
    
    public void setPushNotifications(Boolean pushNotifications) {
        this.pushNotifications = pushNotifications;
    }
    
    public Boolean getMarketingEmails() {
        return marketingEmails;
    }
    
    public void setMarketingEmails(Boolean marketingEmails) {
        this.marketingEmails = marketingEmails;
    }
    
    // Métodos de validación
    @AssertTrue(message = "Debe ser mayor de 13 años")
    public boolean isAgeValid() {
        if (birthDate == null) return true; // Campo opcional
        return birthDate.plusYears(13).isBefore(LocalDate.now());
    }
    
    // Método para verificar si hay cambios
    public boolean hasChanges() {
        return email != null || fullName != null || phone != null || 
               country != null || city != null || bio != null || 
               birthDate != null || avatarUrl != null ||
               emailNotifications != null || pushNotifications != null || 
               marketingEmails != null;
    }
    
    @Override
    public String toString() {
        return "UserUpdateDto{" +
                "email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", phone='" + phone + '\'' +
                ", country='" + country + '\'' +
                ", city='" + city + '\'' +
                ", bio='" + bio + '\'' +
                ", birthDate=" + birthDate +
                ", avatarUrl='" + avatarUrl + '\'' +
                ", emailNotifications=" + emailNotifications +
                ", pushNotifications=" + pushNotifications +
                ", marketingEmails=" + marketingEmails +
                '}';
    }
} 