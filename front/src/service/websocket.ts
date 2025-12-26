import {Client, Message} from '@stomp/stompjs';
import SockJS from 'sockjs-client';

export interface MessageResponse {
    id: number;
    content: string;
    dateTime: string;
    username: string;
    chatId: number;
}

export interface MessageRequest {
    content: string;
}

class WebSocketService {
    private client: Client | null = null;

    connect(token: string, onConnect: () => void, onError: () => void) {
        this.client = new Client({
            webSocketFactory: () => new SockJS("/ws"),
            connectHeaders: {
                Authorization: `Bearer ${token}`
            },
            onConnect: () => {
                console.log('Connected');
                onConnect();
            },
            onStompError: (frame) => {
                console.error('Broker error: ' + frame.headers['message']);
                onError();
            },
            reconnectDelay: 5000,
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000,
        });

        this.client.activate();
    }

    subscribe(topic: string, callback: (message: Message) => void) {
        if (!this.client || !this.client.connected) {
            console.error("Cannot subscribe, client not connected");
            return;
        }
        return this.client.subscribe(topic, callback);
    }

    sendMessage(topic: string, content: string): void {
        if (this.client && this.client.connected) {
            let message: MessageRequest = {content};
            this.client.publish({
                destination: topic,
                body: JSON.stringify(message)
            });
        } else {
            console.error("STOMP client is not connected!");
        }
    }

    disconnect(): void {
        this.client?.deactivate();
    }
}

export default new WebSocketService();
