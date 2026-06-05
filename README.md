# Integración de Sistemas - Salud360

1. **Nombre del estudiante:** [Tu Apellido y Nombre aquí]
2. **Descripción breve:** Solución mínima de integración para automatizar el flujo de registro de citas médicas confirmadas, comunicando sistemas a través de API REST, RabbitMQ y archivos CSV.
3. **Tecnologías utilizadas:** 
   - Java 17
   - Spring Boot
   - Apache Camel
   - RabbitMQ
   - Docker / Docker Compose
4. **Instrucciones para levantar RabbitMQ:**
   ```bash
   docker-compose up -d
   ```
5. **Instrucciones para ejecutar la aplicación:**
   ```bash
   mvn spring-boot:run
   ```
6. **Endpoint disponible:**
   - `POST http://localhost:8080/api/citas`

7. **Ejemplo de request válido:**
   ```json
   {
     "idCita": "CITA-1001",
     "paciente": "Ana Torres",
     "correo": "ana.torres@email.com",
     "especialidad": "Cardiología",
     "fechaCita": "2026-06-15",
     "sede": "Centro Norte",
     "valor": 45.50
   }
   ```

8. **Ejemplo de request inválido:**
   ```json
   {
     "idCita": "",
     "paciente": "Ana Torres",
     "correo": "ana.torres@email.com",
     "especialidad": "Cardiología",
     "fechaCita": "2026-06-15",
     "sede": "Centro Norte",
     "valor": -10.00
   }
   ```

9. **Explicación breve:**
   - **Point-to-Point:** Se aplica en la integración con el Sistema de Facturación (`billing.queue`). Se usa este patrón porque la solicitud de facturación de una cita específica debe ser procesada por un **único** consumidor (el sistema de facturación) para evitar cobros duplicados.
   - **Publish/Subscribe:** Se aplica para distribuir el evento de cita confirmada (`appointments.events` a `notifications.queue` y `analytics.queue`). Se utiliza porque el mismo evento interesa a **múltiples** sistemas de manera independiente.
   - **Transferencia de archivos:** Se aplica para el Sistema Legado de Auditoría. Se utiliza porque este sistema no cuenta con API ni conexión a mensajería, por lo que la forma de intercambiar información es dejando un archivo CSV en un directorio compartido (`data/outbox/`).
   - **Manejo de errores:** Se centraliza mediante un bloque `onException` en Apache Camel y validaciones previas en la API. Los errores generan un registro detallado en el archivo `data/errors/citas-rechazadas.log`.

10. **Evidencia:** 
    - Ver carpeta `docs/capturas/` para la evidencia de ejecución.
