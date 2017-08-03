package com.example.demo;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.task.configuration.EnableTask;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Flux;

import java.util.function.Consumer;

@SpringBootApplication
@EnableTask
@SuppressWarnings("unused")
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

    @Bean
    public <T> CommandLineRunner processorTask(Flux<T> source, Consumer<T> sink) {
        return args -> source.subscribe(sink);
    }

    @Bean
    public Flux<Message> source(DynamoDB dynamoDB,
                                @Value("${dynamodb.table:${spring.application.name}}") String tableName) {

        return Flux.fromIterable(dynamoDB.getTable(tableName).scan())
                .map(outcome -> {
                    String id = outcome.getString("Id");
                    String text = outcome.getString("Text");
                    return new Message(id, text);
                });
	}

    @Bean
    public Consumer<Message> sink() {
	    return System.out::println;
    }
}
