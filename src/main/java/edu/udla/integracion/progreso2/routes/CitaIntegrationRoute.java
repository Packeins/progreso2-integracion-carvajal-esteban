package edu.udla.integracion.progreso2.routes;

import edu.udla.integracion.progreso2.model.CitaRequest;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class CitaIntegrationRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        // --- Configuración de Error Handling Centralizado (Opcional, también se maneja en el Controller) ---
        onException(Exception.class)
            .handled(true)
            .process(exchange -> {
                Exception cause = exchange.getProperty(org.apache.camel.Exchange.EXCEPTION_CAUGHT, Exception.class);
                CitaRequest body = exchange.getIn().getBody(CitaRequest.class);
                
                Map<String, Object> errorInfo = new HashMap<>();
                errorInfo.put("fechaHora", java.time.LocalDateTime.now().toString());
                errorInfo.put("idCita", body != null ? body.getIdCita() : "N/A");
                errorInfo.put("motivo", "Error en integración: " + cause.getMessage());
                errorInfo.put("payload", body);
                
                exchange.getIn().setBody(errorInfo);
            })
            .to("direct:registrarError");

        // --- Ruta de Manejo de Errores (RF5) ---
        from("direct:registrarError")
            .routeId("errorHandlingRoute")
            .marshal().json(JsonLibrary.Jackson)
            // Añadir un salto de línea al final del JSON para el log
            .setBody(simple("${body}\n"))
            .to("file:data/errors?fileName=citas-rechazadas.log&fileExist=Append");


        // --- Ruta Principal de Procesamiento (Multicast) ---
        from("direct:procesarCita")
            .routeId("mainProcessingRoute")
            .multicast().parallelProcessing()
                .to("direct:facturacion")
                .to("direct:notificacionesYAnalitica")
                .to("direct:auditoriaCSV")
            .end();

        // --- Ruta Facturación: Point-to-Point (RF2) ---
        from("direct:facturacion")
            .routeId("billingRoute")
            .process(exchange -> {
                CitaRequest cita = exchange.getIn().getBody(CitaRequest.class);
                Map<String, Object> billingMsg = new HashMap<>();
                billingMsg.put("idCita", cita.getIdCita());
                billingMsg.put("paciente", cita.getPaciente());
                billingMsg.put("especialidad", cita.getEspecialidad());
                billingMsg.put("valor", cita.getValor());
                billingMsg.put("tipoMensaje", "COMANDO_FACTURAR_CITA");
                exchange.getIn().setBody(billingMsg);
            })
            .marshal().json(JsonLibrary.Jackson)
            // Enviar a cola específica (Point-to-Point)
            // spring-rabbitmq usa exchange y routing key. Al mandar directo a la cola podemos usar el default exchange
            .to("spring-rabbitmq:default?routingKey=billing.queue&queues=billing.queue");

        // --- Ruta Eventos: Publish/Subscribe (RF3) ---
        from("direct:notificacionesYAnalitica")
            .routeId("eventsRoute")
            .process(exchange -> {
                CitaRequest cita = exchange.getIn().getBody(CitaRequest.class);
                Map<String, Object> eventMsg = new HashMap<>();
                eventMsg.put("idCita", cita.getIdCita());
                eventMsg.put("paciente", cita.getPaciente());
                eventMsg.put("correo", cita.getCorreo());
                eventMsg.put("especialidad", cita.getEspecialidad());
                eventMsg.put("fechaCita", cita.getFechaCita());
                eventMsg.put("sede", cita.getSede());
                eventMsg.put("tipoEvento", "CITA_CONFIRMADA");
                exchange.getIn().setBody(eventMsg);
            })
            .marshal().json(JsonLibrary.Jackson)
            // Enviar a un Fanout Exchange (Publish/Subscribe)
            // Notar que la configuración de colas la hace idealmente el broker, pero podemos declararlas aquí
            .to("spring-rabbitmq:appointments.events?exchangeType=fanout&queues=notifications.queue,analytics.queue");

        // --- Ruta Archivo CSV (RF4) ---
        from("direct:auditoriaCSV")
            .routeId("csvAuditRoute")
            .process(exchange -> {
                CitaRequest cita = exchange.getIn().getBody(CitaRequest.class);
                // idCita,paciente,correo,especialidad,fechaCita,sede,valor
                String csvLine = String.format("%s,%s,%s,%s,%s,%s,%.2f\n",
                        cita.getIdCita(),
                        cita.getPaciente(),
                        cita.getCorreo(),
                        cita.getEspecialidad(),
                        cita.getFechaCita(),
                        cita.getSede(),
                        cita.getValor()
                );
                exchange.getIn().setBody(csvLine);
            })
            .to("file:data/outbox?fileName=auditoria-citas.csv&fileExist=Append");

    }
}
