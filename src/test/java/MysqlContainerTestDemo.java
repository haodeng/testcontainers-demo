import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dao.UserDao;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@Testcontainers
public class MysqlContainerTestDemo {
    private static final Logger logger = LoggerFactory.getLogger(MysqlContainerTestDemo.class);
    private static final DockerImageName MYSQL_80_IMAGE = DockerImageName.parse("mysql:8.0.24");


    // Add MYSQL_ROOT_HOST environment so that we can root login from anywhere for testing purposes
    @Container
    public static MySQLContainer<?> mysql = new MySQLContainer<>(MYSQL_80_IMAGE)
            .withDatabaseName("foo")
            .withUsername("root")
            .withPassword("")
            .withEnv("MYSQL_ROOT_HOST", "%")
            .withLogConsumer(new Slf4jLogConsumer(logger));

    private DataSource testDataSource;

    @BeforeAll
    public static void startMysql() {
        mysql.start();
    }

    @AfterAll
    public static void shutDownMysql() {
        mysql.stop();
    }

    @Test
    public void testConnection() throws SQLException {
        ResultSet resultSet = performQuery(mysql, "SELECT 1");
        int resultSetInt = resultSet.getInt(1);

        Assertions.assertEquals(1, resultSetInt);
    }

    @Test
    public void testInsertAndSelect() {
        Jdbi jdbi = Jdbi.create(getDataSource(mysql));

        jdbi.installPlugin(new SqlObjectPlugin());

        try (Handle handle = jdbi.open()) {
            UserDao userDao = handle.attach(UserDao.class);

            userDao.createTable();
            userDao.insert(1, "Bob");
            userDao.insert(2, "Jan");

            userDao.forEachUser(user -> System.out.println(user));
        }
    }

    protected ResultSet performQuery(JdbcDatabaseContainer<?> container, String sql) throws SQLException {
        DataSource ds = getDataSource(container);
        Statement statement = ds.getConnection().createStatement();
        statement.execute(sql);
        ResultSet resultSet = statement.getResultSet();

        resultSet.next();
        return resultSet;
    }

    protected DataSource getDataSource(JdbcDatabaseContainer<?> container) {
        if (testDataSource == null) {
            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setJdbcUrl(container.getJdbcUrl());
            hikariConfig.setUsername(container.getUsername());
            hikariConfig.setPassword(container.getPassword());
            hikariConfig.setDriverClassName(container.getDriverClassName());
            testDataSource = new HikariDataSource(hikariConfig);
        }
        return testDataSource;
    }
}
