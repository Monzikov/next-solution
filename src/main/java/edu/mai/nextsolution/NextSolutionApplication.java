package edu.mai.nextsolution;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(excludeName = {"org.springframework.ai.autoconfigure.transformers.TransformersEmbeddingModelAutoConfiguration"})
public class NextSolutionApplication {

    public static void main(String[] args) {
        SpringApplication.run(NextSolutionApplication.class, args);
    }

}
