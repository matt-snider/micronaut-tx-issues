package com.example;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;

import org.springframework.transaction.annotation.Transactional;

import org.jooq.DSLContext;

/**
 * Run the following:
 *
 *  $ curl localhost:9090/create
 *  $ curl localhost:9090/insert
 *  $ curl localhost:9090/get
 *
 * We see that insert was rolled back, because the last request returns no records.
 *
 * But this request errors because there is no @Transactional annotation:
 *
 *  $ curl localhost:9090/no-tx
 */
@Controller
public class TestController {

    private final DSLContext db;

    public TestController(DSLContext db) {
        this.db = db;
    }

    @Transactional
    @Get("/insert")
    public void insert() {
        db.execute("INSERT INTO TEST VALUES (1)");
    }

    @Transactional
    @Get("/insert-and-rollback")
    public void insertAndRollback() {
        db.execute("INSERT INTO TEST VALUES (2)");
        throw new RuntimeException("Roll back.");
    }

    @Get("/no-tx")
    @Produces(MediaType.TEXT_PLAIN)
    public String noTx() {
        var result = db.fetch("SELECT * FROM TEST");
        return result.formatCSV();
    }
}
