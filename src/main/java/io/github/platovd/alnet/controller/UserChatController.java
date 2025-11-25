package io.github.platovd.alnet.controller;

import io.github.platovd.alnet.service.UserChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/membership")
@RequiredArgsConstructor
public class UserChatController {
    private final UserChatService userChatService;

    @GetMapping("/chat")
    public
}
