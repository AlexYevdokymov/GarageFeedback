package org.example;

import org.example.entity.FeedbackEntity;
import org.example.entity.UserEntity;
import org.example.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.*;

public class TelegramBot extends TelegramLongPollingBot {

    private String botToken;
    private String botUsername;

    private final Map<Long, Integer> levels = new HashMap<>();

    private static final Map<String, String> positions = Map.ofEntries(
            Map.entry("mechanic", "Механік"),
            Map.entry("manager", "Менеджер"),
            Map.entry("electrician", "Електрик")
    );

    private static final Map<String, String> regions = Map.ofEntries(
            Map.entry("kyiv", "Київ"),
            Map.entry("lviv", "Львів"),
            Map.entry("khmelnytskyi", "Хмельницький")
    );

    private static final String CHANGE_SELECTION = "change_selection";

    public TelegramBot() {
        Properties props = new Properties();
        try (InputStream in = new FileInputStream("config.properties")) {
            props.load(in);
            botToken = props.getProperty("bot.token");
            botUsername = props.getProperty("bot.username");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }
    @Override
    public void onUpdateReceived(Update update) {
        Long chatId = getChatId(update);

        if (update.hasMessage() && update.getMessage().hasText()
                && update.getMessage().getText().equals("/start")) {
            askPosition(getChatId(update));
        }

        if (update.hasCallbackQuery()) {
            String data = update.getCallbackQuery().getData();

            if (data.equals(CHANGE_SELECTION) && getLevel(chatId)==3) {
                resetUserSettings(chatId);
                askPosition(chatId);
                return;
            }

            if (positions.containsKey(data) && getLevel(chatId)==1) {
                saveUserPosition(chatId, data);
                askRegion(chatId);
            }


            if (regions.containsKey(data) && getLevel(chatId)==2) {
                saveUserRegion(chatId, data);
                sendFinalMessage(chatId);
            }
        }

        if (update.hasMessage() && update.getMessage().hasText()
                && getLevel(chatId)==3) {
            String text = update.getMessage().getText();
            saveFeedback(chatId,text);

            SendMessage confirmation = new SendMessage();
            confirmation.setChatId(chatId.toString());
            confirmation.setText("Дякуємо за відгук!" +
                    "\nНайближчим часом ми опрацюємо Ваше звернення \n\"" +
                    text + '"');
            executeSafely(confirmation);
            sendFinalMessage(chatId);
        }
    }

    private void askPosition(Long chatId) {
        setLevel(chatId,1);
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("Вітаю! Оберіть свою посаду: ");
        attachButtons(message, positions);
        executeSafely(message);
    }

    private void askRegion(Long chatId) {
        setLevel(chatId,2);
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("Оберіть свою філію: ");
        attachButtons(message, regions);
        executeSafely(message);
    }

    private void sendFinalMessage(Long chatId) {
        setLevel(chatId,3);
        UserEntity user = getUser(chatId);

        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("Чудово! Ви " + user.getPosition() +
                " із філії \"" + user.getRegion() +
                "\".\nНадішліть, будь ласка, Ваш відгук" +
                "\nу повідомлення, або, за потреби," +
                "\nможете змінити позицію та регіон.");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        InlineKeyboardButton changeButton = new InlineKeyboardButton();
        changeButton.setText("Змінити позицію або регіон");
        changeButton.setCallbackData(CHANGE_SELECTION);
        markup.setKeyboard(Collections.singletonList(Collections.singletonList(changeButton)));
        message.setReplyMarkup(markup);

        executeSafely(message);
    }

    private void executeSafely(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public Long getChatId(Update update){
        if (update.hasMessage()){
            return update.getMessage().getFrom().getId();
        }

        if (update.hasCallbackQuery()){
            return update.getCallbackQuery().getFrom().getId();
        }

        return null;
    }

    public void attachButtons(SendMessage message, Map<String, String> buttons) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        for (Map.Entry<String, String> entry : buttons.entrySet()) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(entry.getValue());
            button.setCallbackData(entry.getKey());
            keyboard.add(Collections.singletonList(button));
        }

        markup.setKeyboard(keyboard);
        message.setReplyMarkup(markup);
    }

    public int getLevel(Long chatId){
        return levels.getOrDefault(chatId, 0);
    }

    public void setLevel(Long chatId, int level){
        levels.put(chatId, level);
    }

    private void saveUserPosition(Long chatId, String key) {
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();

        UserEntity user = session.createQuery("FROM UserEntity WHERE chatId = :chatId", UserEntity.class)
                .setParameter("chatId", chatId)
                .uniqueResult();
        if (user == null) {
            user = new UserEntity();
            user.setChatId(chatId);
        }
        user.setPosition(positions.get(key));
        user.setRegion(null);
        session.persist(user);

        tx.commit();
        session.close();
    }
    private void saveUserRegion(Long chatId, String key) {
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();

        UserEntity user = session.createQuery("FROM UserEntity WHERE chatId = :chatId", UserEntity.class)
                .setParameter("chatId", chatId)
                .uniqueResult();
        if (user != null) {
            user.setRegion(regions.get(key));
            session.merge(user);
        }

        tx.commit();
        session.close();
    }
    private void resetUserSettings(Long chatId) {
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();

        UserEntity user = session.createQuery("FROM UserEntity WHERE chatId = :chatId", UserEntity.class)
                .setParameter("chatId", chatId)
                .uniqueResult();
        if (user != null) {
            session.delete(user);
        }

        tx.commit();
        session.close();
    }

private void saveFeedback(Long chatId, String text) {
    Session session = HibernateUtil.getSession();
    Transaction tx = session.beginTransaction();

    UserEntity user = session.createQuery("FROM UserEntity WHERE chatId = :chatId", UserEntity.class)
            .setParameter("chatId", chatId)
            .uniqueResult();

    if (user != null) {
        FeedbackEntity feedback = new FeedbackEntity();
        feedback.setPosition(user.getPosition());
        feedback.setRegion(user.getRegion());
        feedback.setFeedback(text);
        feedback.setUtc(Instant.now());
        session.persist(feedback);
    }

    tx.commit();
    session.close();
}
private UserEntity getUser(Long chatId) {
    Session session = HibernateUtil.getSession();
    UserEntity user = session.createQuery(
                    "FROM UserEntity WHERE chatId = :chatId", UserEntity.class)
            .setParameter("chatId",chatId)
            .uniqueResult();
    session.close();
    return user;
}

}