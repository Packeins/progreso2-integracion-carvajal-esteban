package edu.udla.integracion.progreso2.model;

import lombok.Data;

@Data
public class CitaRequest {
    private String idCita;
    private String paciente;
    private String correo;
    private String especialidad;
    private String fechaCita;
    private String sede;
    private Double valor;
}
