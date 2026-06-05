package edu.udla.integracion.progreso2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;

@SpringBootApplication
public class Progreso2Application {

    public static void main(String[] args) {
        SpringApplication.run(Progreso2Application.class, args);
    }

    // Declarar colas
    @Bean
    public Queue billingQueue() {
        return new Queue("billing.queue", true);
    }

    @Bean
    public Queue notificationsQueue() {
        return new Queue("notifications.queue", true);
    }

    @Bean
    public Queue analyticsQueue() {
        return new Queue("analytics.queue", true);
    }

    // Declarar Exchange Fanout
    @Bean
    public FanoutExchange appointmentsExchange() {
        return new FanoutExchange("appointments.events");
    }

    // Conectar las colas al Exchange Fanout
    @Bean
    public Binding bindingNotifications(Queue notificationsQueue, FanoutExchange appointmentsExchange) {
        return BindingBuilder.bind(notificationsQueue).to(appointmentsExchange);
    }

    @Bean
    public Binding bindingAnalytics(Queue analyticsQueue, FanoutExchange appointmentsExchange) {
        return BindingBuilder.bind(analyticsQueue).to(appointmentsExchange);
    }
}
