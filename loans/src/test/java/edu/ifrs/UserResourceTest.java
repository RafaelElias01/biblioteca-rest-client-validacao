package edu.ifrs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.ifrs.model.User;
import jakarta.ws.rs.core.Response;

/**
 * Testes unitários de {@link UserResource}, sem subir o Quarkus.
 */
class UserResourceTest {

    private static final String KEY_PROPERTY = "smallrye.jwt.sign.key.location";

    private UserResource resource;

    @BeforeAll
    static void configureSignKey() {
        System.setProperty(KEY_PROPERTY, "privateKey.pem");
    }

    @AfterAll
    static void clearSignKey() {
        System.clearProperty(KEY_PROPERTY);
    }

    @BeforeEach
    void setUp() {
        resource = new UserResource();
    }

    @Test
    void shouldCreateUserWithGeneratedId() {
        Response response = resource.addUser(new User(null, "Ana", "ana@email.com", "123"));
        User created = (User) response.getEntity();

        assertEquals(201, response.getStatus());
        assertNotNull(created.id());
        assertEquals("Ana", created.name());
        assertEquals("ana@email.com", created.email());
    }

    @Test
    void shouldReturnTokenWhenCredentialsAreValid() {
        resource.addUser(new User(null, "Ana", "ana@email.com", "123"));

        Response response = resource.login(new User(null, null, "ana@email.com", "123"));

        assertEquals(200, response.getStatus());
        assertEquals(3, response.getEntity().toString().split("\\.").length);
    }

    @Test
    void shouldReturn401WhenPasswordIsWrong() {
        resource.addUser(new User(null, "Ana", "ana@email.com", "123"));

        Response response = resource.login(new User(null, null, "ana@email.com", "errada"));

        assertEquals(401, response.getStatus());
    }

    @Test
    void shouldReturn401WhenUserDoesNotExist() {
        Response response = resource.login(new User(null, null, "ninguem@email.com", "123"));

        assertEquals(401, response.getStatus());
    }
}
