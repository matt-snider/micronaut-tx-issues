package com.example;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;

import javax.transaction.Transactional;

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
    @Get("/create")
    public void create() {
        db.execute("CREATE TABLE IF NOT EXISTS TEST (id int PRIMARY KEY)");
    }

    @Transactional
    @Get("/insert")
    public void insert() {
        db.execute("INSERT INTO TEST VALUES (1)");
        throw new RuntimeException("Roll back.");
    }

    @Transactional
    @Get("/get")
    @Produces(MediaType.TEXT_PLAIN)
    public String get() {
        var result = db.fetch("SELECT * FROM TEST");
        return result.formatCSV();
    }

    @Get("/no-tx")
    @Produces(MediaType.TEXT_PLAIN)
    public String noTx() {
        var result = db.fetch("SELECT * FROM TEST");
        return result.formatCSV();
    }
}
