package pl.seba.simplychat.models;

import lombok.Data;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.time.LocalTime;

@Data
public class UserModel {
    private String username;
    private WebSocketSession session;


    private LocalTime lastWriteMinute;
    private int messageCounterInLastMinute;

    private LocalTime banTime;



    public UserModel(WebSocketSession session){
        this.lastWriteMinute = LocalTime.now();
        this.session = session;
    }

    public void sendMessage(String text) throws IOException {
        session.sendMessage(new TextMessage(text));
    }
}
