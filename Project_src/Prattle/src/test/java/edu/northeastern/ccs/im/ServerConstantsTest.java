package edu.northeastern.ccs.im;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import static edu.northeastern.ccs.im.server.ServerConstants.getBroadcastResponses;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 *
 * a test case for ServerConstants
 */
class ServerConstantsTest {

    @Test
    /**
     * get Broadcast Responses Test
     */
    void getBroadcastResponsesTest() {

            //testing date
            //get  a Broadcast Response
            String input = "What is the date?";
            ArrayList<Message> list = getBroadcastResponses(input);
            String r1=list.get(0).toString();
            GregorianCalendar cal = new GregorianCalendar();

            //make a message
            Message dateMessage = Message.makeBroadcastMessage("NIST",
                    (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.DATE) + "/" + cal.get(Calendar.YEAR));
            String r2=dateMessage.toString();

            assertEquals(r1 ,r2);





            //testing time
            //get  a Broadcast Response
            input= "What time is it?";
            list = getBroadcastResponses(input);
            r1=list.get(0).toString();
            cal = new GregorianCalendar();

            //make a message
            Message timeMessage = Message.makeBroadcastMessage("NIST",
                cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE));
             String r3=timeMessage.toString();

             assertEquals(r1 ,r3);







             // testing fox
             //get  a Broadcast Response
             input= "What time is it Mr. Fox?";
             list = getBroadcastResponses(input);
             String text ="The time is now";
             ArrayList<Message> result = new ArrayList<>();
             result.add(Message.makeBroadcastMessage("BBC", text));
             result.add(Message.makeBroadcastMessage("Mr. Fox",
                cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE)));
              String r4=list.get(0).toString();
              String r5=result.get(0).toString();
              assertEquals(r4 ,r5);






              //testing server name
              //get  a Broadcast Response
              String SERVER_NAME = "Prattle";
              input= "WTF";
              list = getBroadcastResponses(input);
              text="OMG ROFL TTYL";
              result.add(Message.makeBroadcastMessage(SERVER_NAME, text));
              String r6=list.get(0).toString();
              String r7=result.get(2).toString();
              assertEquals(r6 ,r7);

              //testing null
              input = "";
              list = getBroadcastResponses(input);
              assertNull(list);


        }
}