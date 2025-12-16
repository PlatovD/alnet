package io.github.platovd.alnet.authorization;

import io.github.platovd.alnet.service.atomic.MembershipService;
import io.github.platovd.alnet.service.orchestration.UserFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("membershipSecurity")
@RequiredArgsConstructor
public class MembershipSecurity {
    private final UserFacade userFacade;
    private final MembershipService membershipService;

    public boolean isMember(Long chatId) {
        return membershipService.isMemberOfChat(userFacade.getCurrentUser().getUserId(), chatId);
    }
}
