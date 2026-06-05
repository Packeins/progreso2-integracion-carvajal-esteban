package edu.udla.integracion.progreso2.controller;

import edu.udla.integracion.progreso2.model.CitaRequest;
import edu.udla.integracion.progreso2.service.CitaValidationService;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/citas")
public class CitaController {

    private final CitaValidationService validationService;
    private final ProducerTemplate producerTemplate;

    @Autowired
    public CitaController(CitaValidationService validationService, ProducerTemplate producerTemplate) {
        this.validationService = validationService;
        this.producerTemplate = producerTemplate;
    }

    @PostMapping
    public ResponseEntity<?> registrarCita(@RequestBody CitaRequest cita) {
        try {
            // Validar la cita recibida (RF1)
            validationService.validarCita(cita);

            // Enviar la cita válida al flujo de integración de Camel
            producerTemplate.sendBody("direct:procesarCita", cita);

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Cita registrada y procesada correctamente.");
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);

        } catch (IllegalArgumentException e) {
            // Error de validación, enviar a log de errores
            enviarError(cita, e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);

        } catch (Exception e) {
            // Error inesperado
            enviarError(cita, "Error interno del servidor: " + e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Error interno al procesar la cita.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    private void enviarError(CitaRequest cita, String motivo) {
        Map<String, Object> errorInfo = new HashMap<>();
        errorInfo.put("fechaHora", java.time.LocalDateTime.now().toString());
        errorInfo.put("idCita", cita != null ? cita.getIdCita() : "N/A");
        errorInfo.put("motivo", motivo);
        errorInfo.put("payload", cita);
        
        producerTemplate.sendBody("direct:registrarError", errorInfo);
    }
}
