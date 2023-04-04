package net.collegemc.common.database.postgres;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SQLAccess {

  public static SQLAccessBuilder builder() {
    return new SQLAccessBuilder();
  }

  private final RelationMapper relationMapper;
  private final TypeMapperRegistry registry;
  private final Set<Class<?>> initializedTables = new HashSet<>();
  private final HikariDataSource dataSource;

  public SQLAccess(HikariConfig config) {
    this.registry = new TypeMapperRegistry();
    this.relationMapper = new RelationMapper(registry);

    dataSource = new HikariDataSource(config);
  }

  public <T> void persist(T object) {
    if (!initializedTables.contains(object.getClass())) {
      createTable(object.getClass());
    }

    try (Connection connection = dataSource.getConnection()) {
      relationMapper.constructInsertion(object, connection).executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private <T> void createTable(Class<T> tClass) {
    try (Connection connection = dataSource.getConnection()) {
      relationMapper.constructTableQuery(tClass, connection).executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    initializedTables.add(tClass);
  }

  public <T> void registerType(SQLTypeResolver<T> typeResolver) {
    registry.registerType(typeResolver.type, typeResolver.sqlType);
    registry.registerPreparation(typeResolver.type, typeResolver.preparation);
  }

  public interface IndexedPreparation<T> {
    void applyTo(int index, T object, PreparedStatement statement);
  }

  public record SQLTypeResolver<T>(Class<T> type, String sqlType, IndexedPreparation<T> preparation) {
  }

  public static class SQLAccessBuilder {

    private final HikariConfig config;
    private final List<SQLTypeResolver<?>> typeResolvers = new ArrayList<>();

    private SQLAccessBuilder() {
      this.config = new HikariConfig();
    }

    public SQLAccessBuilder withJdbcUrl(String url) {
      config.setJdbcUrl(url);
      return this;
    }

    public SQLAccessBuilder withUsername(String username) {
      config.setUsername(username);
      return this;
    }

    public SQLAccessBuilder withPassword(String password) {
      config.setPassword(password);
      return this;
    }

    public SQLAccessBuilder withDataSourceProperty(String key, String value) {
      config.addDataSourceProperty(key, value);
      return this;
    }

    public SQLAccessBuilder withTypeResolver(SQLTypeResolver<?> typeResolver) {
      typeResolvers.add(typeResolver);
      return this;
    }

    public SQLAccess build() {
      if (config.getJdbcUrl() == null) {
        throw new IllegalStateException("JDBC URL must be set");
      }
      SQLAccess sqlAccess = new SQLAccess(config);
      typeResolvers.forEach(sqlAccess::registerType);
      return sqlAccess;
    }

  }

}