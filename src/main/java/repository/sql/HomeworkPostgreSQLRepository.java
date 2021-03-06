package repository.sql;

import domain.Homework;
import validation.Validator;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class HomeworkPostgreSQLRepository extends AbstractPostgreSQLRepository<Integer, Homework> {
    public HomeworkPostgreSQLRepository(Validator<Homework> validator, Connection c) {
        super(validator, c);
    }

    @Override
    protected Homework readEntity(ResultSet result) throws SQLException {
        Integer id = result.getInt("id");
        String description = result.getString("description");
        Integer startWeek = result.getInt("startWeek");
        Integer deadlineWeek = result.getInt("deadlineWeek");
        return new Homework(id, description, startWeek, deadlineWeek);
    }

    @Override
    protected String findOneString(Integer id) {
        return "SELECT * from HOMEWORK where ID = " + id + ";";
    }

    @Override
    protected String findAllString() {
        return "SELECT * from HOMEWORK;";
    }

    @Override
    protected String insertString(Homework homework) {
        return "INSERT INTO HOMEWORK (id, description, startWeek, deadlineWeek) " +
                "VALUES (" + homework.getId()
                + ",'" + homework.getDescription()
                + "'," + homework.getStartWeek()
                + "," + homework.getDeadlineWeek()
                + ");";
    }

    @Override
    protected String deleteString(Integer id) {
        return "DELETE from HOMEWORK where ID = " + id + ";";
    }

    @Override
    protected String updateString(Homework homework) {
        return "UPDATE HOMEWORK SET "
                + "id= " + homework.getId()
                + ", description= '" + homework.getDescription()
                + "', startWeek= " + homework.getStartWeek()
                + ", deadlineWeek= " + homework.getDeadlineWeek()
                + " where ID = " + homework.getId() + ";";
    }
}
