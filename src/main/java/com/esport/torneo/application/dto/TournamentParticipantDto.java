package com.esport.torneo.application.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO para representar un participante de torneo.
 * 
 * @author Andrés Orduz
 * @version 1.0
 * @since 2024
 */
@Schema(description = "Datos de un participante de torneo")
public class TournamentParticipantDto {

    @Schema(description = "ID único del participante", example = "1")
    private Long id;

    @Schema(description = "ID del usuario participante", example = "123")
    private Long userId;

    @Schema(description = "Nombre de usuario del participante", example = "proGamer2024")
    private String username;

    @Schema(description = "Email del participante", example = "user@example.com")
    private String email;

    @Schema(description = "Estado del participante", example = "CONFIRMED")
    private String status;

    @Schema(description = "Fecha de registro", example = "2024-01-15T10:30:00")
    private LocalDateTime registrationDate;

    @Schema(description = "Posición final en el torneo", example = "3")
    private Integer finalPosition;

    @Schema(description = "Puntuación obtenida", example = "1250")
    private Integer score;

    /**
     * Constructor por defecto.
     */
    public TournamentParticipantDto() {
    }

    /**
     * Constructor completo.
     */
    public TournamentParticipantDto(Long id, Long userId, String username, String email, 
                                   String status, LocalDateTime registrationDate, 
                                   Integer finalPosition, Integer score) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.status = status;
        this.registrationDate = registrationDate;
        this.finalPosition = finalPosition;
        this.score = score;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public Integer getFinalPosition() {
        return finalPosition;
    }

    public void setFinalPosition(Integer finalPosition) {
        this.finalPosition = finalPosition;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "TournamentParticipantDto{" +
                "id=" + id +
                ", userId=" + userId +
                ", username='" + username + '\'' +
                ", status='" + status + '\'' +
                ", registrationDate=" + registrationDate +
                '}';
    }
} 