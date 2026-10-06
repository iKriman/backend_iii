package cl.bancoxyz.bff.controller;
import cl.bancoxyz.bff.service.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Map;
import org.springframework.http.*;
import org.springframework.jms.JmsException;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/events")
public class MigrationEventController {
 private final LegacyDataService data; private final MigrationEventPublisher publisher;
 public MigrationEventController(LegacyDataService data,MigrationEventPublisher publisher) {this.data=data;this.publisher=publisher;}
 @PostMapping("/migration/{accountId}")
 public ResponseEntity<?> publish(@PathVariable long accountId) throws JsonProcessingException {
  if(data.findAccount(accountId).isEmpty()) return ResponseEntity.notFound().build();
  try {return ResponseEntity.accepted().body(publisher.publish(accountId));}
  catch(JmsException ex) {return ResponseEntity.status(503).body(Map.of("error","Broker no disponible; el evento no fue confirmado. Reintente mas tarde."));}
 }
}
