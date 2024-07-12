import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;

import javax.swing.*;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Map;

@Slf4j
public class Main {
    public static void main(String[] args) {
        EmbeddedKafkaBroker embeddedKafkaBroker = new EmbeddedKafkaBroker(1).kafkaPorts(9092);

        embeddedKafkaBroker.afterPropertiesSet();

        SwingUtilities.invokeLater(()->new Chat("Michał", "chat"));
        SwingUtilities.invokeLater(()->new Chat("Andrzej", "chat"));
        SwingUtilities.invokeLater(()->new Chat("Agnieszka", "chat"));
        SwingUtilities.invokeLater(()->new Chat("Robert", "chat"));
   }
}

