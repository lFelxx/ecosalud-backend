package com.demo.ecosalud.util;

import com.demo.ecosalud.enums.RolUser;
import com.demo.ecosalud.enums.UserStatus;
import com.demo.ecosalud.model.entities.User;
import com.demo.ecosalud.service.impl.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

class JwtUtilsTest {

    private JwtUtils jwtUtils;

    // Misma clave que usa la aplicación
    private static final String SECRET = "ZWNvc2FsdWQtc2VjcmV0LWtleS1zdXBlci1zZWd1cmEtMjAyNC1hY2FkZW1pY28=";
    private static final long EXPIRATION = 86400000L;

    private UserDetailsImpl userDetails;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", SECRET);
        ReflectionTestUtils.setField(jwtUtils, "jwtExpiration", EXPIRATION);

        User user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@ecosalud.com");
        user.setRole(RolUser.USER);
        user.setStatus(UserStatus.ACTIVO);
        user.setPassword("$2a$encoded");

        userDetails = new UserDetailsImpl(user);
    }

    @Test
    void generateToken_retornaTokenNoNulo() {
        String token = jwtUtils.generateToken(userDetails);

        assertThat(token).isNotNull().isNotBlank();
    }

    @Test
    void extractUsername_retornaEmailCorrecto() {
        String token = jwtUtils.generateToken(userDetails);

        String username = jwtUtils.extractUsername(token);

        assertThat(username).isEqualTo("test@ecosalud.com");
    }

    @Test
    void validateToken_tokenValido_retornaTrue() {
        String token = jwtUtils.generateToken(userDetails);

        Boolean valid = jwtUtils.validateToken(token, userDetails);

        assertThat(valid).isTrue();
    }

    @Test
    void validateToken_usuarioDiferente_retornaFalse() {
        String token = jwtUtils.generateToken(userDetails);

        User otroUser = new User();
        otroUser.setId(2L);
        otroUser.setName("Otro");
        otroUser.setEmail("otro@ecosalud.com");
        otroUser.setRole(RolUser.USER);
        otroUser.setStatus(UserStatus.ACTIVO);
        otroUser.setPassword("$2a$encoded");

        UserDetailsImpl otroDetails = new UserDetailsImpl(otroUser);

        Boolean valid = jwtUtils.validateToken(token, otroDetails);

        assertThat(valid).isFalse();
    }

    @Test
    void extractExpiration_retornaFechaFutura() {
        String token = jwtUtils.generateToken(userDetails);

        java.util.Date expiration = jwtUtils.extractExpiration(token);

        assertThat(expiration).isAfter(new java.util.Date());
    }
}
