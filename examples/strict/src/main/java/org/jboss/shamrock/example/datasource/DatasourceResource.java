package org.jboss.shamrock.example.datasource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.sql.DataSource;
import javax.transaction.UserTransaction;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/datasource")
public class DatasourceResource {

    @Inject
    private DataSource dataSource;

    @Inject
    private UserTransaction userTransaction;

    @Inject
    private DatasourceSetup datasourceSetup;

    @PostConstruct
    public void postConstruct() throws Exception {
        datasourceSetup.doInit();
    }

    @GET
    public String simpleTest() throws Exception {
        try (Connection con = dataSource.getConnection()) {
            try (Statement statement = con.createStatement()) {
                statement.execute("insert into a values (10)");
            }
            try (Statement statement = con.createStatement()) {
                try (ResultSet rs = statement.executeQuery("select b from a")) {
                    if (rs.next()) {
                        return rs.getString(1);
                    }
                    return "FAILED";
                }
            }
        }
    }

    @GET
    @Path("/txn")
    public String transactionTest() throws Exception {
        userTransaction.begin();
        try (Connection con = dataSource.getConnection()) {
            try (Statement statement = con.createStatement()) {
                statement.execute("insert into tx values (10)");
            }
        }
        userTransaction.rollback();
        try (Connection con = dataSource.getConnection()) {
            try (Statement statement = con.createStatement()) {
                try (ResultSet rs = statement.executeQuery("select b from a")) {
                    if (rs.next()) {
                        return "FAILED";
                    }
                    return "PASSED";
                }
            }
        }
    }

}