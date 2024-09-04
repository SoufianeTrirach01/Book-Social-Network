package com.soufianeTr.book_network.auth;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Data
@Builder
public class AuthenticationResponse {
    private String token;
}
