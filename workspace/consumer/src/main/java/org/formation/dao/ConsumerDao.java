package org.formation.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ConsumerDao {

    String jdbcUrl = "jdbc:postgresql:consumer";
    String username = "postgres";
    String password = "postgres";

    public ConsumerDao() throws ClassNotFoundException {
        Class.forName("org.postgresql.Driver");

    }
    public void insert(String courierId, long offset) throws SQLException {

        Connection connection = DriverManager.getConnection(jdbcUrl, username, password);

        String sql = "INSERT INTO courier (courierId, kafkaOffset) VALUES (?, ?)";

        // Préparer la déclaration
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, courierId); // Remplacez 123 par la valeur de courierId
        preparedStatement.setLong(2, offset); // Remplacez 456 par la valeur de offset

        try {
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            connection.close();
        }
    }
}
