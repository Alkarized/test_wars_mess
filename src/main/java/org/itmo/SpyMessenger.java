package org.itmo;

import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class SpyMessenger {
    private static class Message {
        String sender;
        String receiver;
        String text;
        String passcode;
        long timestamp;

        Message(String sender, String receiver, String text, String passcode) {
            this.sender = sender;
            this.receiver = receiver;
            this.text = text;
            this.passcode = passcode;
            this.timestamp = System.currentTimeMillis();
        }
    }

    private final Map<String, LinkedList<Message>> userMessages = new ConcurrentHashMap<>();
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(10);

    public SpyMessenger() {
        executorService.scheduleAtFixedRate(this::removeExpiredMessages, 0, 500, TimeUnit.MILLISECONDS);
    }

    public void sendMessage(String sender, String receiver, String message, String passcode) {
        userMessages.putIfAbsent(receiver, new LinkedList<>());
        LinkedList<Message> messages = userMessages.get(receiver);

        if (messages.size() >= 5) {
            messages.removeFirst();
        }

        messages.add(new Message(sender, receiver, message, passcode));
    }

    public String readMessage(String user, String passcode) {
        LinkedList<Message> messages = null;
        try {
            messages = userMessages.get(user);
        } catch (Exception e) {
            return null;
        }
        if (messages == null || messages.isEmpty()) {
            return null;
        }

        int j = -1;
        for (int i = 0; i < messages.size(); i++) {
            Message message = messages.get(i);
            if (message.passcode.equals(passcode)) {
                j = i;
                break;
            }
        }

        if (j != -1) {
            var message= messages.get(j);
            messages.remove(j);
            return message.text;
        }

        return null;
    }

    private void removeExpiredMessages() {
        long currentTime = System.currentTimeMillis();

        for (Map.Entry<String, LinkedList<Message>> entry : userMessages.entrySet()) {
            LinkedList<Message> messages = entry.getValue();

            while (!messages.isEmpty()) {
                Message message = messages.peekFirst();
                if (currentTime - message.timestamp > 1500) {
                    messages.removeFirst();
                } else {
                    break;
                }
            }
        }
    }
}