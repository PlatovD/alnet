package io.github.platovd.alnet.authorization;

import io.github.platovd.alnet.service.atomic.MembershipService;
import io.github.platovd.alnet.service.atomic.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("membershipSecurity")
@RequiredArgsConstructor
public class MembershipSecurity {
    private final UserService userService;
    private final MembershipService membershipService;

    public boolean isMember(Long chatId) {
        return membershipService.isMemberOfChat(userService.getCurrentUser().getUserId(), chatId);
    }
}
