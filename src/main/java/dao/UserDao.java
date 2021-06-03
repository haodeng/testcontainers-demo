package dao;

import model.User;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.function.Consumer;

public interface UserDao {
    @SqlUpdate("create table users (id int primary key, name varchar(100), email varchar(100))")
    void createTable();

    @SqlUpdate("insert into users (id, name) values (?, ?)")
    int insert(long id, String name);

    /**
     * As a special case, you may provide a Consumer<T> argument in addition to other bound parameters.
     * The provided consumer is executed once for each row in the result set.
     * The static type of parameter T determines the row type.
     */
    @SqlQuery("select id, name from users")
    @RegisterBeanMapper(User.class)
    void forEachUser(Consumer<User> consumer);
}
