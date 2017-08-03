package com.example.demo;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.model.PutRecordRequest;
import com.amazonaws.services.kinesis.model.PutRecordResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.task.configuration.EnableTask;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Flux;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

@SpringBootApplication
@EnableTask
@SuppressWarnings("unused")
public class DemoApplication {

    private static final Logger logger = LoggerFactory.getLogger(DemoApplication.class);

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
    public Consumer<Message> sink(AmazonKinesis kinesisClient,
                                  ObjectMapper objectMapper,
                                  @Value("${kinesis.stream:${spring.application.name}}") String streamName) {

        return message -> {
            try {
                PutRecordRequest request = new PutRecordRequest();
                request.setStreamName(streamName);
                request.setPartitionKey(message.getId());
                request.setData(ByteBuffer.wrap(objectMapper.writeValueAsBytes(message)));
                logger.info("Writing to Kinesis: {}", request);

                PutRecordResult result = kinesisClient.putRecord(request);
                logger.info("Successfully wrote to Kinesis: {}", result);
            } catch (Exception e) {
                logger.error("Error writing to Kinesis", e);
                throw new RuntimeException(e);
            }
        };
    }
}
