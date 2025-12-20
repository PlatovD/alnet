package io.github.platovd.alnet.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.platovd.alnet.authentication.contex.SecurityContextWrapper;
import io.github.platovd.alnet.authentication.token.JWTAuthToken;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.converter.DefaultContentTypeResolver;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;

import static io.github.platovd.alnet.authentication.filter.JWTAuthenticationFilter.*;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Value("${frontend.port}")
    private String port;
    private final ObjectMapper mapper;
    private final AuthenticationManager authManager;
    private final SecurityContextWrapper securityContextWrapper;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // куда будет сервер слать данные по подпискам через STOMP. todo: RabbitMQ
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public boolean configureMessageConverters(List<MessageConverter> messageConverters) {
        DefaultContentTypeResolver contentTypeResolver = new DefaultContentTypeResolver();
        contentTypeResolver.setDefaultMimeType(MimeTypeUtils.APPLICATION_JSON);
        MappingJackson2MessageConverter messageConverter = new MappingJackson2MessageConverter();
        messageConverter.setObjectMapper(mapper);
        messageConverter.setContentTypeResolver(contentTypeResolver);

        messageConverters.add(messageConverter);
        return false;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
//                .setAllowedOrigins("http://localhost:" + port)
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String authHeader = accessor.getFirstNativeHeader(HEADER_NAME);

                    if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
                        try {
                            String jwt = authHeader.substring(BEARER_PREFIX.length());
                            Authentication token = new JWTAuthToken(jwt);
                            Authentication authentication = authManager.authenticate(token);
                            accessor.setUser(authentication);
                            securityContextWrapper.setAuthentication(authentication);
                        } catch (Exception e) {
                            throw new MessageDeliveryException("Invalid token or authentication failed");
                        }
                    } else {
                        throw new MessageDeliveryException("Missing Authorization header");
                    }
                }
                return message;
            }
        });
    }
}
