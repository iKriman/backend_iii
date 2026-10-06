package cl.bancoxyz.notification;
import java.util.*;
import com.fasterxml.jackson.databind.*;
import org.slf4j.*;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
@Service
public class NotificationStore {
 private static final Logger log=LoggerFactory.getLogger(NotificationStore.class);
 private final ObjectMapper mapper;
 private final LinkedHashMap<String,JsonNode> events=new LinkedHashMap<>();
 public NotificationStore(ObjectMapper mapper) {this.mapper=mapper;}
 @JmsListener(destination="${bank.events.queue}")
 public synchronized void receive(String message) throws Exception {
  JsonNode event=mapper.readTree(message);
  if(!event.hasNonNull("eventId") || !event.hasNonNull("accountId")) throw new IllegalArgumentException("Evento invalido");
  String id=event.get("eventId").asText();
  if(events.containsKey(id)) return;
  events.put(id,event);
  if(events.size()>100) events.remove(events.keySet().iterator().next());
  log.info("EVENTO_RECIBIDO eventId={} accountId={} type={}",id,event.get("accountId"),event.get("type"));
 }
 public synchronized List<JsonNode> all(){return List.copyOf(events.values());}
}
@RestController
@RequestMapping("/api/notifications")
class NotificationController {
 private final NotificationStore store;
 NotificationController(NotificationStore store){this.store=store;}
 @GetMapping public List<JsonNode> all(){return store.all();}
}
