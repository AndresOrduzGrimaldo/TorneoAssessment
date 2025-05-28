package com.esport.torneo.domain.ticket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

/**
 * Servicio para generar códigos QR para tickets.
 * 
 * Utiliza la librería ZXing para generar códigos QR que contienen
 * información del ticket en formato JSON.
 * 
 * @author Andrés Orduz Grimaldo
 * @version 1.0.0
 * @since 2024
 */
@Service
public class QrCodeService {

    private static final int QR_CODE_WIDTH = 300;
    private static final int QR_CODE_HEIGHT = 300;
    private static final String IMAGE_FORMAT = "PNG";

    /**
     * Genera un código QR para un ticket.
     * 
     * @param ticket el ticket para el cual generar el QR
     * @return el código QR en formato Base64
     * @throws QrCodeGenerationException si hay error en la generación
     */
    public String generateQrCode(Ticket ticket) {
        try {
            String qrContent = createQrContent(ticket);
            return generateQrCodeFromContent(qrContent);
        } catch (Exception e) {
            throw new QrCodeGenerationException("Error generando código QR para ticket: " + ticket.getTicketCode(), e);
        }
    }

    /**
     * Genera un código QR a partir de contenido específico.
     * 
     * @param content el contenido a codificar
     * @return el código QR en formato Base64
     * @throws QrCodeGenerationException si hay error en la generación
     */
    public String generateQrCodeFromContent(String content) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = createHints();
            
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, 
                                                    QR_CODE_WIDTH, QR_CODE_HEIGHT, hints);
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, IMAGE_FORMAT, outputStream);
            
            byte[] qrCodeBytes = outputStream.toByteArray();
            return Base64.getEncoder().encodeToString(qrCodeBytes);
            
        } catch (WriterException | IOException e) {
            throw new QrCodeGenerationException("Error generando código QR", e);
        }
    }

    /**
     * Crea el contenido del código QR para un ticket.
     * 
     * @param ticket el ticket
     * @return el contenido en formato JSON
     */
    private String createQrContent(Ticket ticket) {
        return String.format(
            "{\"ticketCode\":\"%s\",\"tournamentId\":%d,\"userId\":%d,\"price\":%.2f,\"timestamp\":\"%s\"}",
            ticket.getTicketCode(),
            ticket.getTournament().getId(),
            ticket.getUserId(),
            ticket.getPrice(),
            ticket.getCreatedAt()
        );
    }

    /**
     * Crea las configuraciones para la generación del QR.
     * 
     * @return el mapa de configuraciones
     */
    private Map<EncodeHintType, Object> createHints() {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1);
        return hints;
    }

    /**
     * Valida si un código QR es válido.
     * 
     * @param qrCode el código QR en Base64
     * @return true si es válido
     */
    public boolean isValidQrCode(String qrCode) {
        if (qrCode == null || qrCode.trim().isEmpty()) {
            return false;
        }
        
        try {
            Base64.getDecoder().decode(qrCode);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Excepción personalizada para errores en la generación de códigos QR.
     */
    public static class QrCodeGenerationException extends RuntimeException {
        public QrCodeGenerationException(String message) {
            super(message);
        }
        
        public QrCodeGenerationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
} 