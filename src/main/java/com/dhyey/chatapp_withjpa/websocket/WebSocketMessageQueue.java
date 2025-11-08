package com.dhyey.chatapp_withjpa.websocket;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

@Service
public class WebSocketMessageQueue {

    private final BlockingQueue<MessageJob> queue = new LinkedBlockingQueue<>();
    private final ExecutorService workers = Executors.newFixedThreadPool(4); // 4 concurrent processors
    private final WsMessageParser parser;

    public WebSocketMessageQueue(WsMessageParser parser) {
        this.parser = parser;
    }

    public void enqueue(WebSocketSession session, TextMessage message) {
        queue.offer(new MessageJob(session, message));
    }

    @Async
    public void processQueue() {
        while (true) {
            try {
                MessageJob job = queue.take();
                workers.submit(() -> parser.parse(job.message(), job.session()));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private record MessageJob(WebSocketSession session, TextMessage message) {}
}
