package edu.udla.integracion.progreso2.service;

import edu.udla.integracion.progreso2.model.CitaRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CitaValidationService {

    public void validarCita(CitaRequest cita) {
        List<String> errores = new ArrayList<>();

        if (cita.getIdCita() == null || cita.getIdCita().trim().isEmpty()) {
            errores.add("idCita es obligatorio");
        }
        if (cita.getPaciente() == null || cita.getPaciente().trim().isEmpty()) {
            errores.add("paciente es obligatorio");
        }
        if (cita.getCorreo() == null || cita.getCorreo().trim().isEmpty()) {
            errores.add("correo es obligatorio");
        }
        if (cita.getEspecialidad() == null || cita.getEspecialidad().trim().isEmpty()) {
            errores.add("especialidad es obligatoria");
        }
        if (cita.getFechaCita() == null || cita.getFechaCita().trim().isEmpty()) {
            errores.add("fechaCita es obligatoria");
        }
        if (cita.getSede() == null || cita.getSede().trim().isEmpty()) {
            errores.add("sede es obligatoria");
        }
        if (cita.getValor() == null || cita.getValor() <= 0) {
            errores.add("valor debe ser mayor a 0");
        }

        if (!errores.isEmpty()) {
            throw new IllegalArgumentException("Errores de validación: " + String.join(", ", errores));
        }
    }
}
