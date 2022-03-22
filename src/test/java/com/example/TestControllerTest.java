package com.example;

import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.runtime.EmbeddedApplication;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;
import org.jooq.DSLContext;
import org.junit.jupiter.api.Assertions;

import jakarta.inject.Inject;

@MicronautTest
class TestControllerTest {

    @Inject
    EmbeddedApplication<?> application;

    @Inject
    @Client("/")
    private HttpClient client;

    @Inject
    private DSLContext db;

    @Test
    void testTransactional() {
        Assertions.assertTrue(application.isRunning());

        // Create database table
        db.execute("CREATE TABLE IF NOT EXISTS TEST (id int PRIMARY KEY)");

        // Insert should succeed
        client.toBlocking().exchange("/insert");
        var count1 = db.fetch("SELECT * FROM TEST").getValues(0, Long.class).size();
        Assertions.assertEquals(1, count1);

        // Insert and rollback should fail and no additional records should be inserted
        try {
            client.toBlocking().retrieve("/insert-and-rollback");
            Assertions.fail("GET on /insert-and-rollback should throw");
        } catch (HttpClientResponseException e) {
        }
        var count2 = db.fetch("SELECT * FROM TEST").getValues(0, Long.class).size();
        Assertions.assertEquals(1, count2);

        // This shouldn't fail but does because of:
        // CannotGetJdbcConnectionException: No current JDBC Connection found. Consider wrapping this call in transactional boundaries.
        try {
            var response = client.toBlocking().retrieve("/no-tx", String.class);
            System.out.println(response);
            Assertions.assertEquals("Hello world", response);
        } catch (HttpClientResponseException e) {
            Assertions.fail("GET on /no-tx should not throw an exception");
        }
    }

}

