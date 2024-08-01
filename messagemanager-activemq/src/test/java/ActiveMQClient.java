import org.apache.activemq.ActiveMQConnectionFactory;
   import javax.jms.*;

   public class ActiveMQClient {
       public static void main(String[] args) {
           try {
               System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
               System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
               System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.activemq", "DEBUG");
               System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.activemq.transport", "TRACE");

               ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("tcp://host.docker.internal:61616");
               factory.setUserName("admin");
               factory.setPassword("admin");
               Connection connection = factory.createConnection();
               connection.start();
               Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
               
               // Use the session to create producers, consumers, etc.
               
               connection.close();
           } catch (Exception e) {
               e.printStackTrace();
           }
       }
   }
   