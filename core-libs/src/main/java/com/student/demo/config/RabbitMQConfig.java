package com.student.demo.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE_NAME = "code.analysis.queue";
    public static final String EXCHANGE_NAME = "code.exchange";
    public static final String ROUTING_KEY = "code.routing.key";
    
    public static final String RETRY_QUEUE_NAME = "code.retry.queue";
    public static final String RETRY_EXCHANGE_NAME = "code.retry.exchange";
    
    public static final String FAILED_QUEUE_NAME = "code.failed.queue";

    public static final String EXECUTION_QUEUE_NAME = "code.execution.queue";
    public static final String EXECUTION_ROUTING_KEY = "code.execution.routing.key";

    @Bean
    public TopicExchange mainExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public TopicExchange retryExchange() {
        return new TopicExchange(RETRY_EXCHANGE_NAME);
    }

    @Bean
    public Queue mainQueue() {
        return QueueBuilder.durable(QUEUE_NAME)
                .withArgument("x-dead-letter-exchange", RETRY_EXCHANGE_NAME)
                .withArgument("x-dead-letter-routing-key", ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue retryQueue() {
        return QueueBuilder.durable(RETRY_QUEUE_NAME)
                .withArgument("x-message-ttl", 5000) // 5 seconds delay
                .withArgument("x-dead-letter-exchange", EXCHANGE_NAME)
                .withArgument("x-dead-letter-routing-key", ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue failedQueue() {
        return new Queue(FAILED_QUEUE_NAME, true);
    }

    @Bean
    public Queue executionQueue() {
        return QueueBuilder.durable(EXECUTION_QUEUE_NAME)
                .withArgument("x-dead-letter-exchange", RETRY_EXCHANGE_NAME)
                .withArgument("x-dead-letter-routing-key", EXECUTION_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding executionBinding() {
        return BindingBuilder.bind(executionQueue()).to(mainExchange()).with(EXECUTION_ROUTING_KEY);
    }

    @Bean
    public Binding mainBinding() {
        return BindingBuilder.bind(mainQueue()).to(mainExchange()).with(ROUTING_KEY);
    }

    @Bean
    public Binding retryBinding() {
        return BindingBuilder.bind(retryQueue()).to(retryExchange()).with(ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        final RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        template.setReplyTimeout(30000L);
        return template;
    }
}
