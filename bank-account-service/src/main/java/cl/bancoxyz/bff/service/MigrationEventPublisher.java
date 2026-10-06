package cl.bancoxyz.bff.service;
import java.time.Instant;
import java.util.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
@Service
public class MigrationEventPublisher {
 private final JmsTemplate jms; private final ObjectMapper json; private final String queue;
 public MigrationEventPublisher(JmsTemplate jms,ObjectMapper json,@Value("${bank.events.queue}") String queue) {
  this.jms=jms;this.json=json;this.queue=queue;
  jms.setExplicitQosEnabled(true);jms.setDeliveryPersistent(true);
 }
 public Map<String,Object> publish(long accountId) throws JsonProcessingException {
  Map<String,Object> event=new LinkedHashMap<>();
  event.put("eventId",UUID.randomUUID().toString());event.put("type","MIGRATION_REVIEW_REQUESTED");
  event.put("accountId",accountId);event.put("occurredAt",Instant.now().toString());
  jms.convertAndSend(queue,json.writeValueAsString(event));
  return event;
 }
}
