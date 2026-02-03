package com.crs.iamservice.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event được publish khi user đăng ký tài khoản thành công
 */
@Getter
public class UserRegistrationEvent extends ApplicationEvent {
    
    private final String email;
    private final String fullName;
    
    public UserRegistrationEvent(Object source, String email, String fullName) {
        super(source);
        this.email = email;
        this.fullName = fullName;
    }
}
