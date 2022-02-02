package com.chat.backend.controller;

import com.chat.backend.model.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.Date;
import java.util.Random;

@Controller
public class ChatController {

    private String[] colors = {"red", "green", "blue", "purple", "orange", "yellow", "black"};

    @MessageMapping("/message") // recibe
    @SendTo("/chat/message") // Notificamos a todos los usuarios el nuevo mensaje y a todos los clientes suscritos
    public Message receiveMessage(Message message){
        message.setDate(new Date().getTime());
        if(message.getType().equals("NEW_USER"))
        {
            message.setColor(colors[new Random().nextInt(colors.length)]);
            message.setText(" - New user connected");
        }
        //message.setText("Received by the broker: " + message.getText());
        System.out.println(message.getText());
        return message;
    }

    @MessageMapping("/writing")
    @SendTo("/chat/writing")
    public String isUserWriting(String username)
    {
        return username.concat("is writing ...");
    }
}
