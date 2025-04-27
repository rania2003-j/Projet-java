package com.example.livraison.Models;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.ApiContextInitializer;

public class LivraisonExpressBot extends TelegramLongPollingBot {

    private static final String BOT_TOKEN = "8157575665:AAGaI7iPZOpyOLr4a1lEnhSICb6ATXKHweg";
    private static final String BOT_USERNAME = "AutoLivraisonBot_bot";

    @Override
    public String getBotUsername() {
        return BOT_USERNAME;
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }

    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        if (message != null && message.hasText()) {
            String textReponse = "Bienvenue sur **LivraisonExpressBot**! Comment puis-je vous aider ?";
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(message.getChatId().toString());
            sendMessage.setText(textReponse);

            try {
                execute(sendMessage); // Envoi du message
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        // Initialisation de l'API
        ApiContextInitializer.init();

        try {
            // Créer une instance de TelegramBotsApi
            TelegramBotsApi botsApi = new TelegramBotsApi();

            // Enregistrer et démarrer le bot
            botsApi.registerBot(new LivraisonExpressBot());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
