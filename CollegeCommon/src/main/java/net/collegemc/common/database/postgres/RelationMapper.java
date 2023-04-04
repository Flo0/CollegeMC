package net.collegemc.common.database.postgres;


import net.collegemc.common.database.postgres.annotations.Id;
import net.collegemc.common.database.postgres.annotations.TableName;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RelationMapper {

  private final TypeMapperRegistry registry;
  private final Map<Class<?>, DBClassView<?>> classViewMap = new HashMap<>();

  public RelationMapper(TypeMapperRegistry registry) {
    this.registry = registry;
  }

  public <T> PreparedStatement constructTableQuery(Class<T> tClass, Connection connection) throws SQLException {
    DBClassView<?> view = classViewMap.computeIfAbsent(tClass, this::createClassView);
    StringBuilder sb = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
    sb.append(getTableName(tClass)).append(" (");

    if (view.idField.getAnnotation(Id.class).autoIncrement()) {
      sb.append(view.idField.getName()).append(" ").append(" SERIAL PRIMARY KEY , ");
    } else {
      sb.append(view.idField.getName()).append(" ").append(registry.getDBType(view.idField.getType())).append(" PRIMARY KEY, ");
    }

    for (Field field : view.dataFields) {
      sb.append(field.getName()).append(" ").append(registry.getDBType(field.getType())).append(", ");
    }

    sb.delete(sb.length() - 2, sb.length());
    sb.append(");");

    return connection.prepareStatement(sb.toString());
  }

  @SuppressWarnings("unchecked")
  public <T> PreparedStatement constructInsertion(T object, Connection connection) {
    DBClassView<T> view = (DBClassView<T>) classViewMap.computeIfAbsent(object.getClass(), this::createClassView);
    boolean autoIncremented = view.idField.getAnnotation(Id.class).autoIncrement();

    StringBuilder sb;
    if (!autoIncremented) {
      sb = new StringBuilder("INSERT INTO " + view.tableName + " VALUES (");
    } else {
      sb = new StringBuilder("INSERT INTO " + view.tableName + " (" + view.dataFields.stream().map(Field::getName).reduce((s, s2) -> s + ", " + s2).orElse("") + ") VALUES (");
    }


    sb.append("?, ".repeat(view.dataFields.size() + (autoIncremented ? 0 : 1)));
    sb.delete(sb.length() - 2, sb.length());
    sb.append(");");

    try {
      PreparedStatement statement = connection.prepareStatement(sb.toString());

      if (!autoIncremented) {
        Class<?> idType = view.idField.getType();
        registry.getPreparation(idType).applyTo(1, view.idField.get(object), statement);
      }
      for (int i = 0; i < view.dataFields.size(); i++) {
        Field dataField = view.dataFields.get(i);
        SQLAccess.IndexedPreparation<Object> preparation = registry.getPreparation(dataField.getType());
        if (preparation == null) {
          throw new IllegalStateException("No preparation registered for " + dataField.getType());
        }
        int indexOffset = autoIncremented ? 1 : 2;
        registry.getPreparation(dataField.getType()).applyTo(i + indexOffset, dataField.get(object), statement);
      }
      return statement;
    } catch (SQLException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  private <T> DBClassView<T> createClassView(Class<T> clazz) {
    var fields = clazz.getDeclaredFields();
    Field idField = null;
    List<Field> dataFields = new ArrayList<>(fields.length - 1);

    for (Field field : fields) {
      if ((field.getModifiers() & Modifier.TRANSIENT) != 0) {
        continue;
      }
      if (field.isAnnotationPresent(Id.class)) {
        if (idField != null) {
          throw new IllegalStateException("Duplicate ID found!");
        }
        idField = field;
      } else {
        dataFields.add(field);
      }
    }

    if (idField == null) {
      throw new IllegalStateException("No ID found!");
    }

    return new DBClassView<>(clazz, getTableName(clazz), idField, dataFields);
  }

  private String getTableName(Class<?> tClass) {
    if (tClass.isAnnotationPresent(TableName.class)) {
      return tClass.getAnnotation(TableName.class).name();
    } else {
      return tClass.getSimpleName();
    }
  }

  private record DBClassView<T>(Class<T> clazz, String tableName, Field idField, List<Field> dataFields) {
  }

}
