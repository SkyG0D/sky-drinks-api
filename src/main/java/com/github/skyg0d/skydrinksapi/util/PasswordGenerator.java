package com.github.skyg0d.skydrinksapi.util;

import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Log4j2
public class PasswordGenerator {

    public static void main(String[] args) {
        String toEncode = args.length > 0 ? args[0] : "admin123";

        String password = new BCryptPasswordEncoder().encode(toEncode);

        log.info(password);
    }

}
