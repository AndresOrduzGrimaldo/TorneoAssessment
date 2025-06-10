package com.esport.torneo.application.mapper;

import com.esport.torneo.application.dto.UserDto;
import com.esport.torneo.domain.user.User;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Mapper para la conversión entre entidades User y DTOs.
 * 
 * Maneja la transformación de datos incluyendo:
 * - Cálculo de campos derivados
 * - Exclusión de información sensible
 * - Formateo de datos para presentación
 * 
 * @author Andrés Orduz
 * @version 1.0
 * @since 1.0
 */
@Component
public class UserMapper {
    
    /**
     * Convierte una entidad User a UserDto completo.
     */
    public UserDto toDto(User user) {
        if (user == null) {
            return null;
        }
        
        UserDto dto = new UserDto();
        
        // Campos básicos
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setPhone(user.getPhone());
        dto.setCountry(user.getCountry());
        dto.setCity(user.getCity());
        dto.setBio(user.getBio());
        dto.setBirthDate(user.getBirthDate());
        dto.setAvatarUrl(user.getAvatarUrl());
        
        // Estados y roles
        dto.setRoles(user.getRoles());
        dto.setStatus(user.getStatus());
        dto.setEmailVerified(user.getEmailVerified());
        dto.setActive(user.getActive());
        
        // Información de seguridad (sin datos sensibles)
        dto.setFailedLoginAttempts(user.getFailedLoginAttempts());
        dto.setLastLoginAt(user.getLastLoginAt());
        dto.setLockedUntil(user.getLockedUntil());
        
        // Campos de auditoría
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        
        // Campos calculados
        dto.setAge(calculateAge(user.getBirthDate()));
        dto.setIsLocked(user.isLocked());
        dto.setCanLogin(canUserLogin(user));
        dto.setDisplayName(getDisplayName(user));
        dto.setDaysSinceLastLogin(calculateDaysSinceLastLogin(user.getLastLoginAt()));
        
        return dto;
    }
    
    /**
     * Convierte una entidad User a UserDto básico (información pública).
     */
    public UserDto toBasicDto(User user) {
        if (user == null) {
            return null;
        }
        
        UserDto dto = new UserDto();
        
        // Solo información pública
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setFullName(user.getFullName());
        dto.setCountry(user.getCountry());
        dto.setCity(user.getCity());
        dto.setBio(user.getBio());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setStatus(user.getStatus());
        dto.setCreatedAt(user.getCreatedAt());
        
        // Campos calculados
        dto.setAge(calculateAge(user.getBirthDate()));
        dto.setDisplayName(getDisplayName(user));
        
        return dto;
    }
    
    /**
     * Convierte una entidad User a UserDto de perfil (información personal).
     */
    public UserDto toProfileDto(User user) {
        if (user == null) {
            return null;
        }
        
        UserDto dto = toDto(user);
        
        // Remover información administrativa
        dto.setFailedLoginAttempts(null);
        dto.setLockedUntil(null);
        
        return dto;
    }
    
    /**
     * Convierte una entidad User a UserDto administrativo (información completa).
     */
    public UserDto toAdminDto(User user) {
        if (user == null) {
            return null;
        }
        
        // DTO completo con toda la información
        return toDto(user);
    }
    
    /**
     * Actualiza una entidad User con datos de UserDto (solo campos permitidos).
     */
    public void updateUserFromDto(User user, UserDto dto) {
        if (user == null || dto == null) {
            return;
        }
        
        // Solo actualizar campos que no son sensibles o calculados
        if (dto.getFullName() != null) {
            user.setFullName(dto.getFullName());
        }
        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone());
        }
        if (dto.getCountry() != null) {
            user.setCountry(dto.getCountry());
        }
        if (dto.getCity() != null) {
            user.setCity(dto.getCity());
        }
        if (dto.getBio() != null) {
            user.setBio(dto.getBio());
        }
        if (dto.getBirthDate() != null) {
            user.setBirthDate(dto.getBirthDate());
        }
        if (dto.getAvatarUrl() != null) {
            user.setAvatarUrl(dto.getAvatarUrl());
        }
    }
    
    /**
     * Calcula la edad basándose en la fecha de nacimiento.
     */
    private Integer calculateAge(LocalDate birthDate) {
        if (birthDate == null) {
            return null;
        }
        
        LocalDate now = LocalDate.now();
        if (birthDate.isAfter(now)) {
            return null; // Fecha inválida
        }
        
        return (int) ChronoUnit.YEARS.between(birthDate, now);
    }
    
    /**
     * Genera el nombre de visualización del usuario.
     */
    private String getDisplayName(User user) {
        if (user.getFullName() != null && !user.getFullName().trim().isEmpty()) {
            return user.getFullName();
        }
        return user.getUsername();
    }
    
    /**
     * Calcula los días transcurridos desde el último login.
     */
    private Long calculateDaysSinceLastLogin(LocalDateTime lastLoginAt) {
        if (lastLoginAt == null) {
            return null;
        }
        
        return ChronoUnit.DAYS.between(lastLoginAt, LocalDateTime.now());
    }
    
    /**
     * Determina si el usuario puede hacer login.
     */
    private Boolean canUserLogin(User user) {
        return user.getActive() && 
               !user.isLocked() && 
               user.getStatus().name().equals("ACTIVE");
    }
    
    /**
     * Convierte una entidad User para lista/búsqueda (información resumida).
     */
    public UserDto toListDto(User user) {
        if (user == null) {
            return null;
        }
        
        UserDto dto = new UserDto();
        
        // Información esencial para listas
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setRoles(user.getRoles());
        dto.setStatus(user.getStatus());
        dto.setEmailVerified(user.getEmailVerified());
        dto.setLastLoginAt(user.getLastLoginAt());
        dto.setCreatedAt(user.getCreatedAt());
        
        // Campos calculados
        dto.setDisplayName(getDisplayName(user));
        dto.setIsLocked(user.isLocked());
        dto.setCanLogin(canUserLogin(user));
        dto.setDaysSinceLastLogin(calculateDaysSinceLastLogin(user.getLastLoginAt()));
        
        return dto;
    }
    
    /**
     * Convierte múltiples entidades User a DTOs básicos.
     */
    public java.util.List<UserDto> toBasicDtoList(java.util.List<User> users) {
        if (users == null) {
            return null;
        }
        
        return users.stream()
                .map(this::toBasicDto)
                .toList();
    }
    
    /**
     * Convierte múltiples entidades User a DTOs de lista.
     */
    public java.util.List<UserDto> toListDtoList(java.util.List<User> users) {
        if (users == null) {
            return null;
        }
        
        return users.stream()
                .map(this::toListDto)
                .toList();
    }
    
    /**
     * Convierte múltiples entidades User a DTOs completos.
     */
    public java.util.List<UserDto> toDtoList(java.util.List<User> users) {
        if (users == null) {
            return null;
        }
        
        return users.stream()
                .map(this::toDto)
                .toList();
    }
    
    /**
     * Crea un DTO resumido para referencias en otras entidades.
     */
    public UserDto toReferenceDto(User user) {
        if (user == null) {
            return null;
        }
        
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setFullName(user.getFullName());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setDisplayName(getDisplayName(user));
        
        return dto;
    }
    
    /**
     * Valida si un DTO contiene datos válidos para creación.
     */
    public boolean isValidForCreation(UserDto dto) {
        return dto != null &&
               dto.getUsername() != null && !dto.getUsername().trim().isEmpty() &&
               dto.getEmail() != null && !dto.getEmail().trim().isEmpty() &&
               dto.getFullName() != null && !dto.getFullName().trim().isEmpty();
    }
    
    /**
     * Valida si un DTO contiene datos válidos para actualización.
     */
    public boolean isValidForUpdate(UserDto dto) {
        return dto != null && dto.getId() != null;
    }
} 