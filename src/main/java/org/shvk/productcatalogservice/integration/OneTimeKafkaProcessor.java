package org.shvk.productcatalogservice.integration;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.springframework.kafka.core.ConsumerFactory;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
public abstract class OneTimeKafkaProcessor<K, T> {
    private final ConsumerFactory<K, T> consumerFactory;
    private final String topic;
    private final Duration pollDuration = Duration.ofSeconds(10);

    public OneTimeKafkaProcessor(ConsumerFactory<K, T> consumerFactory, String topic) {
        this.consumerFactory = consumerFactory;
        this.topic = topic;
    }

    public abstract void processRecord(ConsumerRecord<K, T> record) throws Exception;

    public void onStart(Map<TopicPartition, Long> fromOffset, Map<TopicPartition, Long> untilOffset) {
        // overloadable
    }

    public void onEnd(long processed, long errors) {
        // overloadable
    }

    public void onError(Throwable exception, ConsumerRecord<K, T> record) {
        log.error("Unhandled exception while one time processing messages of topic " + topic +
                " at partition " + record.partition() + " and offset " + record.offset(), exception);
    }

    public void processOnce(Instant fromDate, Instant untilDate) throws Exception {
        long fromMillis = fromDate.toEpochMilli();
        long untilMillis = untilDate.toEpochMilli();

        if (fromMillis > untilMillis) {
            return;
        }

        try (Consumer<K, T> consumer = consumerFactory.createConsumer()) {
            List<TopicPartition> topicPartitions = consumer.partitionsFor(topic).stream()
                    .map(partitionInfo -> new TopicPartition(partitionInfo.topic(), partitionInfo.partition()))
                    .collect(Collectors.toList());

            Map<TopicPartition, Long> lastOffsets = consumer.endOffsets(topicPartitions);

            Map<TopicPartition, Long> untilOffsets = consumer.offsetsForTimes(
                    topicPartitions.stream().collect(Collectors.toMap(tp -> tp, tp -> untilMillis))
            ).entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue() != null ? e.getValue().offset() : null));

            Map<TopicPartition, Long> fromOffsets = consumer.offsetsForTimes(
                    topicPartitions.stream().collect(Collectors.toMap(tp -> tp, tp -> fromMillis))
            ).entrySet().stream()
                    .filter(e -> e.getValue() != null)
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().offset()));

            Map<TopicPartition, Long> startOffsets = fromOffsets.entrySet().stream()
                    .filter(e -> {
                        Long untilOffset = untilOffsets.get(e.getKey());
                        return untilOffset == null || untilOffset > e.getValue();
                    })
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            Map<TopicPartition, Long> endOffsets = startOffsets.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> untilOffsets.getOrDefault(e.getKey(), lastOffsets.get(e.getKey()))));

            seekTo(consumer, startOffsets);
            onStart(startOffsets, endOffsets);

            if (consumer.assignment().isEmpty()) {
                onEnd(0, 0);
            } else {
                processUntil(consumer, endOffsets);
            }
        }
    }

    private void processUntil(Consumer<K, T> consumer, Map<TopicPartition, Long> untilOffsets) throws Exception {
        Set<TopicPartition> topicPartitions = untilOffsets.keySet();
        Map<Integer, Long> untilOffsetByPartition = untilOffsets.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().partition(), Map.Entry::getValue));

        long processed = 0;
        AtomicLong errors = new AtomicLong();

        while (!topicPartitions.isEmpty()) {
            List<ConsumerRecord<K, T>> records = (List<ConsumerRecord<K, T>>) consumer.poll(pollDuration).records(topic);
            log.debug("Polled " + records.size() + " records");

            if (records.isEmpty()) break;

            List<CompletableFuture<Void>> futures = records.stream()
                    .filter(record -> untilOffsetByPartition.get(record.partition()) > record.offset())
                    .map(record -> CompletableFuture.runAsync(() -> {
                        try {
                            processRecord(record);
                        } catch (Exception ex) {
                            errors.getAndIncrement();
                            onError(ex, record);
                        }
                    }))
                    .collect(Collectors.toList());

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            processed += futures.size();

            Set<TopicPartition> finishedPartitions = topicPartitions.stream()
                    .filter(tp -> {
                        long position = consumer.position(tp);
                        long target = untilOffsets.get(tp);
                        log.debug("Topic offset " + tp + " is at offset " + position + ", end offset is " + target);
                        return position >= target;
                    })
                    .collect(Collectors.toSet());

            if (!finishedPartitions.isEmpty()) {
                consumer.pause(finishedPartitions);
            }

            finishedPartitions.forEach(tp -> log.debug("Finished processing partition " + tp));
            topicPartitions.removeAll(finishedPartitions);
        }

        onEnd(processed, errors.get());
    }

    private void seekTo(Consumer<K, T> consumer, Map<TopicPartition, Long> fromOffset) {
        consumer.assign(fromOffset.keySet());
        fromOffset.forEach(consumer::seek);
    }
}
