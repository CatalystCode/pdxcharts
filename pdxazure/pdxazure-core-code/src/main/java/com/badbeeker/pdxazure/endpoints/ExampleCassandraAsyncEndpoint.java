package com.badbeeker.pdxazure.endpoints;

import com.nike.backstopper.apierror.sample.SampleCoreApiError;
import com.nike.backstopper.exception.ApiException;
import com.nike.riposte.server.http.RequestInfo;
import com.nike.riposte.server.http.ResponseInfo;
import com.nike.riposte.server.http.StandardEndpoint;
import com.nike.riposte.util.Matcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.netty.channel.ChannelHandlerContext;

/**
 * Endpoint that shows how to do Cassandra calls in an async way using the async driver utilities, without creating
 * extra threads to monitor futures/etc. This maximizes the async nonblocking functionality.
 *
 * <p>NOTE: Don't let the volume of code in here throw you - a large portion of this class is for embedded cassandra
 * which wouldn't be necessary for a non-example project.
 *
 * <p>TODO: EXAMPLE CLEANUP - Delete this class.
 *
 * @author Nic Munroe
 */
@SuppressWarnings("WeakerAccess")
public class ExampleCassandraAsyncEndpoint extends StandardEndpoint<Void, String> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    public static final String MATCHING_ENDPOINT_PATH = "/exampleSQLAsync";
    private String connectionString =
        "jdbc:sqlserver://pdxazure.database.windows.net:1433;database=pdxazure;user=nikeadmin@pdxazure;password=nike123!@;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;";

    @Override
    public CompletableFuture<ResponseInfo<String>> execute(RequestInfo<Void> request, Executor longRunningTaskExecutor,
                                                           ChannelHandlerContext ctx) {
        String statement = "select SalesOrderID from SalesLT.SalesOrderHeader;";

        CompletableFuture<ResponseInfo<String>> promise = new CompletableFuture<>();
        try (Connection conn = DriverManager.getConnection(connectionString)) {
            try (ResultSet resultSet = conn.createStatement().executeQuery(statement)) {
                Stream.Builder<String> stream = Stream.builder();
                while (resultSet.next()) {
                    stream.add(resultSet.getString("SalesOrderID"));
                }
                ResponseInfo<String> response = ResponseInfo.newBuilder(stream.build().collect(Collectors.joining(",")))
                                                            .withDesiredContentWriterMimeType("text/text")
                                                            .build();
                promise.complete(response);
            }
        } catch (SQLException e) {
            logger.error("Failure to connect to SQL Server", e);
            throw ApiException.newBuilder()
                              .withApiErrors(SampleCoreApiError.GENERIC_SERVICE_ERROR)
                              .withExceptionMessage("Unable to connect to SQL Database")
                              .build();
        }
        return promise;
    }

    @Override
    public Matcher requestMatcher() {
        return Matcher.match(MATCHING_ENDPOINT_PATH);
    }
}
