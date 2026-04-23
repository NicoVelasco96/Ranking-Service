package com.tournament.ranking.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String TOURNAMENT_EXCHANGE = "tournament.exchange";
    public static final String TOURNAMENT_STARTED_QUEUE = "tournament.started.queue";
    public static final String TOURNAMENT_FINISHED_QUEUE = "tournament.finished.queue";
    public static final String ROUTING_KEY_STARTED = "tournament.started";
    public static final String ROUTING_KEY_FINISHED = "tournament.finished";
    public static final String MATCH_FINISHED_QUEUE = "match.finished.queue";
    public static final String ROUTING_KEY_MATCH_FINISHED = "match.finished";


    @Bean
    public TopicExchange tournamentExchange() {
        return new TopicExchange(TOURNAMENT_EXCHANGE);
    }

    @Bean
    public Queue tournamentStartedQueue() {
        return new Queue(TOURNAMENT_STARTED_QUEUE, true);
    }

    @Bean
    public Queue tournamentFinishedQueue() {
        return new Queue(TOURNAMENT_FINISHED_QUEUE, true);
    }

    @Bean
    public Binding bindingStarted(Queue tournamentStartedQueue, TopicExchange tournamentExchange) {
        return BindingBuilder
                .bind(tournamentStartedQueue)
                .to(tournamentExchange)
                .with(ROUTING_KEY_STARTED);
    }

    @Bean
    public Binding bindingFinished(Queue tournamentFinishedQueue, TopicExchange tournamentExchange) {
        return BindingBuilder
                .bind(tournamentFinishedQueue)
                .to(tournamentExchange)
                .with(ROUTING_KEY_FINISHED);
    }

    @Bean
    public JacksonJsonMessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }

    @Bean
    public Queue matchFinishedQueue() {
        return new Queue(MATCH_FINISHED_QUEUE, true);
    }

    @Bean
    public Binding bindingMatchFinished(Queue matchFinishedQueue, TopicExchange tournamentExchange) {
        return BindingBuilder
                .bind(matchFinishedQueue)
                .to(tournamentExchange)
                .with(ROUTING_KEY_MATCH_FINISHED);
    }
}