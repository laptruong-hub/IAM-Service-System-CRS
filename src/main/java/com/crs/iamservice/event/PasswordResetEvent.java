package com.crs.iamservice.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event được publish khi user yêu cầu reset mật khẩu
 */
@Getter
public class PasswordResetEvent extends ApplicationEvent {

    private final String email;
    private final String fullName;
    private final String resetCode;

    public PasswordResetEvent(Object source, String email, String fullName, String resetCode) {
        super(source);
        this.email = email;
        this.fullName = fullName;
        this.resetCode = resetCode;
    }
}
