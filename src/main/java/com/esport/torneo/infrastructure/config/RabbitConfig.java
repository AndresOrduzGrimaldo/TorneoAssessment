package com.esport.torneo.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for event-driven messaging.
 * Configures exchanges, queues, bindings, and message converters.
 * 
 * @author Andrés Orduz
 * @version 1.0
 */
@Configuration
public class RabbitConfig {

    // Exchange names
    public static final String TOURNAMENT_EXCHANGE = "tournament.exchange";
    public static final String TICKET_EXCHANGE = "ticket.exchange";
    public static final String NOTIFICATION_EXCHANGE = "notification.exchange";
    public static final String DLX_EXCHANGE = "dlx.exchange";

    // Queue names
    public static final String TOURNAMENT_CREATED_QUEUE = "tournament.created.queue";
    public static final String TOURNAMENT_UPDATED_QUEUE = "tournament.updated.queue";
    public static final String TOURNAMENT_STARTED_QUEUE = "tournament.started.queue";
    public static final String TOURNAMENT_FINISHED_QUEUE = "tournament.finished.queue";
    
    public static final String TICKET_CREATED_QUEUE = "ticket.created.queue";
    public static final String TICKET_PAID_QUEUE = "ticket.paid.queue";
    public static final String TICKET_USED_QUEUE = "ticket.used.queue";
    public static final String TICKET_EXPIRED_QUEUE = "ticket.expired.queue";
    
    public static final String EMAIL_NOTIFICATION_QUEUE = "email.notification.queue";
    public static final String PUSH_NOTIFICATION_QUEUE = "push.notification.queue";
    public static final String SMS_NOTIFICATION_QUEUE = "sms.notification.queue";
    
    public static final String DLQ_SUFFIX = ".dlq";

    // Routing keys
    public static final String TOURNAMENT_CREATED_KEY = "tournament.created";
    public static final String TOURNAMENT_UPDATED_KEY = "tournament.updated";
    public static final String TOURNAMENT_STARTED_KEY = "tournament.started";
    public static final String TOURNAMENT_FINISHED_KEY = "tournament.finished";
    
    public static final String TICKET_CREATED_KEY = "ticket.created";
    public static final String TICKET_PAID_KEY = "ticket.paid";
    public static final String TICKET_USED_KEY = "ticket.used";
    public static final String TICKET_EXPIRED_KEY = "ticket.expired";
    
    public static final String EMAIL_NOTIFICATION_KEY = "notification.email";
    public static final String PUSH_NOTIFICATION_KEY = "notification.push";
    public static final String SMS_NOTIFICATION_KEY = "notification.sms";

    /**
     * Configures JSON message converter.
     * 
     * @return MessageConverter
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * Configures RabbitTemplate with JSON converter.
     * 
     * @param connectionFactory RabbitMQ connection factory
     * @return RabbitTemplate
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    /**
     * Configures listener container factory.
     * 
     * @param connectionFactory RabbitMQ connection factory
     * @return SimpleRabbitListenerContainerFactory
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setConcurrentConsumers(3);
        factory.setMaxConcurrentConsumers(10);
        factory.setDefaultRequeueRejected(false);
        return factory;
    }

    // ========== EXCHANGES ==========

    /**
     * Tournament events exchange.
     */
    @Bean
    public TopicExchange tournamentExchange() {
        return new TopicExchange(TOURNAMENT_EXCHANGE, true, false);
    }

    /**
     * Ticket events exchange.
     */
    @Bean
    public TopicExchange ticketExchange() {
        return new TopicExchange(TICKET_EXCHANGE, true, false);
    }

    /**
     * Notification events exchange.
     */
    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(NOTIFICATION_EXCHANGE, true, false);
    }

    /**
     * Dead letter exchange.
     */
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX_EXCHANGE, true, false);
    }

    // ========== TOURNAMENT QUEUES ==========

    /**
     * Tournament created events queue.
     */
    @Bean
    public Queue tournamentCreatedQueue() {
        return QueueBuilder.durable(TOURNAMENT_CREATED_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", TOURNAMENT_CREATED_QUEUE + DLQ_SUFFIX)
                .build();
    }

    /**
     * Tournament updated events queue.
     */
    @Bean
    public Queue tournamentUpdatedQueue() {
        return QueueBuilder.durable(TOURNAMENT_UPDATED_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", TOURNAMENT_UPDATED_QUEUE + DLQ_SUFFIX)
                .build();
    }

    /**
     * Tournament started events queue.
     */
    @Bean
    public Queue tournamentStartedQueue() {
        return QueueBuilder.durable(TOURNAMENT_STARTED_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", TOURNAMENT_STARTED_QUEUE + DLQ_SUFFIX)
                .build();
    }

    /**
     * Tournament finished events queue.
     */
    @Bean
    public Queue tournamentFinishedQueue() {
        return QueueBuilder.durable(TOURNAMENT_FINISHED_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", TOURNAMENT_FINISHED_QUEUE + DLQ_SUFFIX)
                .build();
    }

    // ========== TICKET QUEUES ==========

    /**
     * Ticket created events queue.
     */
    @Bean
    public Queue ticketCreatedQueue() {
        return QueueBuilder.durable(TICKET_CREATED_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", TICKET_CREATED_QUEUE + DLQ_SUFFIX)
                .build();
    }

    /**
     * Ticket paid events queue.
     */
    @Bean
    public Queue ticketPaidQueue() {
        return QueueBuilder.durable(TICKET_PAID_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", TICKET_PAID_QUEUE + DLQ_SUFFIX)
                .build();
    }

    /**
     * Ticket used events queue.
     */
    @Bean
    public Queue ticketUsedQueue() {
        return QueueBuilder.durable(TICKET_USED_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", TICKET_USED_QUEUE + DLQ_SUFFIX)
                .build();
    }

    /**
     * Ticket expired events queue.
     */
    @Bean
    public Queue ticketExpiredQueue() {
        return QueueBuilder.durable(TICKET_EXPIRED_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", TICKET_EXPIRED_QUEUE + DLQ_SUFFIX)
                .build();
    }

    // ========== NOTIFICATION QUEUES ==========

    /**
     * Email notification queue.
     */
    @Bean
    public Queue emailNotificationQueue() {
        return QueueBuilder.durable(EMAIL_NOTIFICATION_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", EMAIL_NOTIFICATION_QUEUE + DLQ_SUFFIX)
                .build();
    }

    /**
     * Push notification queue.
     */
    @Bean
    public Queue pushNotificationQueue() {
        return QueueBuilder.durable(PUSH_NOTIFICATION_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", PUSH_NOTIFICATION_QUEUE + DLQ_SUFFIX)
                .build();
    }

    /**
     * SMS notification queue.
     */
    @Bean
    public Queue smsNotificationQueue() {
        return QueueBuilder.durable(SMS_NOTIFICATION_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", SMS_NOTIFICATION_QUEUE + DLQ_SUFFIX)
                .build();
    }

    // ========== DEAD LETTER QUEUES ==========

    /**
     * Dead letter queue for tournament events.
     */
    @Bean
    public Queue tournamentDeadLetterQueue() {
        return QueueBuilder.durable(TOURNAMENT_CREATED_QUEUE + DLQ_SUFFIX).build();
    }

    /**
     * Dead letter queue for ticket events.
     */
    @Bean
    public Queue ticketDeadLetterQueue() {
        return QueueBuilder.durable(TICKET_CREATED_QUEUE + DLQ_SUFFIX).build();
    }

    /**
     * Dead letter queue for notification events.
     */
    @Bean
    public Queue notificationDeadLetterQueue() {
        return QueueBuilder.durable(EMAIL_NOTIFICATION_QUEUE + DLQ_SUFFIX).build();
    }

    // ========== BINDINGS ==========

    /**
     * Tournament event bindings.
     */
    @Bean
    public Binding tournamentCreatedBinding() {
        return BindingBuilder.bind(tournamentCreatedQueue())
                .to(tournamentExchange())
                .with(TOURNAMENT_CREATED_KEY);
    }

    @Bean
    public Binding tournamentUpdatedBinding() {
        return BindingBuilder.bind(tournamentUpdatedQueue())
                .to(tournamentExchange())
                .with(TOURNAMENT_UPDATED_KEY);
    }

    @Bean
    public Binding tournamentStartedBinding() {
        return BindingBuilder.bind(tournamentStartedQueue())
                .to(tournamentExchange())
                .with(TOURNAMENT_STARTED_KEY);
    }

    @Bean
    public Binding tournamentFinishedBinding() {
        return BindingBuilder.bind(tournamentFinishedQueue())
                .to(tournamentExchange())
                .with(TOURNAMENT_FINISHED_KEY);
    }

    /**
     * Ticket event bindings.
     */
    @Bean
    public Binding ticketCreatedBinding() {
        return BindingBuilder.bind(ticketCreatedQueue())
                .to(ticketExchange())
                .with(TICKET_CREATED_KEY);
    }

    @Bean
    public Binding ticketPaidBinding() {
        return BindingBuilder.bind(ticketPaidQueue())
                .to(ticketExchange())
                .with(TICKET_PAID_KEY);
    }

    @Bean
    public Binding ticketUsedBinding() {
        return BindingBuilder.bind(ticketUsedQueue())
                .to(ticketExchange())
                .with(TICKET_USED_KEY);
    }

    @Bean
    public Binding ticketExpiredBinding() {
        return BindingBuilder.bind(ticketExpiredQueue())
                .to(ticketExchange())
                .with(TICKET_EXPIRED_KEY);
    }

    /**
     * Notification event bindings.
     */
    @Bean
    public Binding emailNotificationBinding() {
        return BindingBuilder.bind(emailNotificationQueue())
                .to(notificationExchange())
                .with(EMAIL_NOTIFICATION_KEY);
    }

    @Bean
    public Binding pushNotificationBinding() {
        return BindingBuilder.bind(pushNotificationQueue())
                .to(notificationExchange())
                .with(PUSH_NOTIFICATION_KEY);
    }

    @Bean
    public Binding smsNotificationBinding() {
        return BindingBuilder.bind(smsNotificationQueue())
                .to(notificationExchange())
                .with(SMS_NOTIFICATION_KEY);
    }
} 