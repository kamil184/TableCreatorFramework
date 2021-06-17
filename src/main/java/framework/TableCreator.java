package framework;

import framework.ColumnString;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TableCreator {

    public static void create(Class<?> dbClass, String url, String user, String password) throws ClassNotFoundException {
        framework.DBTable dbTable = dbClass.getAnnotation(framework.DBTable.class);

        if (dbTable == null) {
            System.out.println("No table annotations for class " + dbClass.getName());
        } else {
            String tableName = dbTable.name();
            if (tableName.length() < 1)
                tableName = dbClass.getName().toUpperCase(); // Если поле name не заполнено, то название таблицы совпадает с названием класса
            String columnDef = "";
            StringBuilder createCommand = new StringBuilder(
                    "CREATE TABLE " + tableName + "("
            );
            for (Field field : dbClass.getDeclaredFields()) {
                String columnName = null;
                Annotation[] annotations = field.getDeclaredAnnotations();
                if (annotations.length < 1) {
                    System.out.println("Field is not a db column");
                } else {
                    if (annotations[0] instanceof framework.ColumnInteger) {
                        framework.ColumnInteger columnInteger = (framework.ColumnInteger) annotations[0];
                        if (columnInteger.name().length() < 1)
                            columnName = field.getName().toUpperCase(); // используем имя поля, если оно не предоставлено аннотацией
                        else
                            columnName = columnInteger.name();
                        columnDef = columnName + " INT" + getConstraints(columnInteger.constraints());
                    } else if (annotations[0] instanceof ColumnString) {
                        ColumnString columnString = (ColumnString) annotations[0];
                        if (columnString.name().length() < 1)
                            columnName = field.getName().toUpperCase();
                        else
                            columnName = columnString.name();
                        columnDef = columnName + " VARCHAR(" + columnString.value() + ")" + getConstraints(columnString.constraints());
                    }
                    createCommand.append("\n    ").append(columnDef).append(", ");
                }
            }
            String tableCreate = createCommand.substring(0, createCommand.length() - 2) + ");";
            System.out.println(tableCreate);
            runCommandInDB(tableCreate, url, user, password);
        }

    }

    private static String getConstraints(framework.Constraints con) {
        String constraints = "";
        if (!con.allowNull())
            constraints += " NOT NULL";
        if (con.primaryKey())
            constraints += " PRIMARY KEY";
        if (con.unique())
            constraints += " UNIQUE";

        return constraints;
    }

    private static void runCommandInDB(String command, String url, String user, String password) {
        try(Connection connection = DriverManager.getConnection(url, user, password);) {
            connection.prepareCall(command).execute();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
