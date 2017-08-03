package com.example.demo;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.task.configuration.EnableTask;
import org.springframework.context.annotation.Bean;

import java.util.Collections;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@SpringBootApplication
@EnableTask
@SuppressWarnings("unused")
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

    @Bean
    public CommandLineRunner processorTask(Processor processor) {
	    return args -> processor.process();
    }

    @Bean
    public <T> Processor processor(Source<Stream<T>> source, Sink<T> sink) {
	    return () -> source.get().forEach(sink::accept);
    }

    @Bean
    public Source<Stream<Message>> source(DynamoDB dynamoDB,
                                          @Value("${dynamodb.table:${spring.application.name}}") String tableName) {

        return () ->
                StreamSupport.stream(dynamoDB.getTable(tableName).scan().spliterator(), false)
                        .map(outcome -> {
                            String id = outcome.getString("Id");
                            String text = outcome.getString("Text");
                            return new Message(id, text);
                        });
	}

    @Bean
    public Sink<Message> sink() {
	    return System.out::println;
    }
}
