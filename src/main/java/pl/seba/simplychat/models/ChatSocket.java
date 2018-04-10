package pl.seba.simplychat.models;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@EnableWebSocket
@Component
public class ChatSocket extends TextWebSocketHandler implements WebSocketConfigurer {

    private List<UserModel> sessionList = new ArrayList<>();


    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry webSocketHandlerRegistry) {
        webSocketHandlerRegistry.addHandler(this, "/chat").setAllowedOrigins("*");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        UserModel userModel = findUserBySessionId(session.getId());
        String messageString = message.getPayload();

        userPostMessage(userModel);
        if(isUserBanned(userModel)){
            userModel.sendMessage("server:Jestes zbanowany ");
            userModel.sendMessage("server: Jeszcze: " + (userModel.getBanTime().until(LocalTime.now(), ChronoUnit.SECONDS)) + "s.");
            return;
        }

        if(messageString.trim().isEmpty()){
            return;
        }

        if(userModel.getUsername() == null){
            if(isNickBusy(messageString)){
                userModel.sendMessage("server:Nick jest zajęty, podaj inny");
                return;
            }
            userModel.setUsername(messageString);
            userModel.sendMessage("server:Ustawiłem nick");
            return;
        }
/*
if (messageString.startsWith("/kick")){
            String data[] = messageString.split(" ");
            if (data.length < 2){
                userModel.sendMessage("Za mało argumentów");
                return;
            }
            Optional<UserModel> userToKick = findUserByUsername(data[1]);
            if (!userToKick.isPresent()){
                userModel.sendMessage("Taki user nie istnieje");
                return;
            }
            userToKick.get().sendMessage("Zostałeś wyrzucony");
            userToKick.get().getSession().close();
} */


        sendMessageToAll("log:" + userModel.getUsername() + ": " + message.getPayload());
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        UserModel userModel = new UserModel(session);
        sessionList.add(userModel);



        userModel.sendMessage("server:Witaj na naszym chacie!");
        userModel.sendMessage("server:Twoja pierwsza wiadomość, zostanie Twoim nickiem");

        sendMessageToAll("connected:" + sessionList.size());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessionList.remove(findUserBySessionId(session.getId()));

        sendMessageToAll("connected:" + sessionList.size());
    }

    private void sendMessageToAll(String message) throws IOException {
        for (UserModel userModel : sessionList) {
            userModel.sendMessage(message);
        }
    }

    private UserModel findUserBySessionId(String sessionId){
        return sessionList.stream()
                .filter(s -> s.getSession().getId().equals(sessionId))
                .findAny()
                .orElseThrow(IllegalStateException::new);
    }

  /*  private Optional findUserByUsername(String username){
        return sessionList.stream()
                .filter(s -> s.getUsername().equals(username))
                .findAny()
                .orElseThrow(IllegalStateException::new);
    } */

    private boolean isNickBusy(String nickname){
        return sessionList.stream().anyMatch(s -> s.getUsername() != null && s.getUsername().equals(nickname));
    }

    private void userPostMessage(UserModel userModel){
        if(userModel.getBanTime() != null){
            return;
        }

        if(Math.abs(userModel.getLastWriteMinute().until(LocalTime.now(), ChronoUnit.MINUTES)) > 1){
            userModel.setLastWriteMinute(LocalTime.now());
            userModel.setMessageCounterInLastMinute(0);
        }else {
            userModel.setMessageCounterInLastMinute(userModel.getMessageCounterInLastMinute() + 1);
            if (userModel.getMessageCounterInLastMinute() >= 30) {
                userModel.setBanTime(LocalTime.now().plusMinutes(1));
                userModel.setMessageCounterInLastMinute(0);
            }
        }
    }

    private boolean isUserBanned(UserModel userModel){
        if(userModel.getBanTime() != null ){
            if(userModel.getBanTime().isAfter(LocalTime.now())){
                return true;
            }else{
                userModel.setBanTime(null);
            }
        }
        return false;
    }
}