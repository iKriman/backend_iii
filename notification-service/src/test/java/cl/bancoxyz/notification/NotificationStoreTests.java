package cl.bancoxyz.notification;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class NotificationStoreTests {
 @Test void consumesAndDeduplicates() throws Exception {
  var store=new NotificationStore(new ObjectMapper());String msg="{\"eventId\":\"id-1\",\"accountId\":106}";
  store.receive(msg);store.receive(msg);assertEquals(1,store.all().size());assertEquals(106,store.all().get(0).get("accountId").asInt());
 }
 @Test void rejectsMalformedEvent() {assertThrows(IllegalArgumentException.class,()->new NotificationStore(new ObjectMapper()).receive("{}"));}
}
