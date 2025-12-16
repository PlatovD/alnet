package io.github.platovd.alnet.authorization;

import io.github.platovd.alnet.service.AuthenticationService;
import io.github.platovd.alnet.service.MembershipService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("chatSecurity")
@RequiredArgsConstructor
public class MembershipSecurity {
    private final AuthenticationService authenticationService;
    private final MembershipService membershipService;

    public boolean isMember(Long chatId) {
        return membershipService.isMemberOfChat(authenticationService.getCurrentUser().getUserId(), chatId);
    }
}
