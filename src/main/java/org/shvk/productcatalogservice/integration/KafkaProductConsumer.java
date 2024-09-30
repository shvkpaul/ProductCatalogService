package org.shvk.productcatalogservice.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaProductConsumer {

    @KafkaListener(topics = "product-topic", groupId = "product-group")
    public void listen(String message) {
        log.info("Received product creation message: {}", message);
    }

}