package serviceManager;

import config.ApplicationContext;
import domain.*;
import repository.CrudRepository;
import repository.sql.GradePostgreSQLRepository;
import repository.sql.HomeworkPostgreSQLRepository;
import repository.sql.ProfessorPostgreSQLRepository;
import repository.sql.StudentPostgreSQLRepository;
import service.*;
import validation.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Vector;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ServiceManager {
    private HomeworkService homeworkServo;
    private GradeService gradeServo;
    private StudentService studentServo;
    private ProfessorService professorServo;
    private final UniversityYearStructure year;
    private Connection c;
    private final MessageSender messageSender;

    public ServiceManager() {
        year = yearSetUp();
        String filePath = ApplicationContext.getPROPERTIES().getProperty("filePath");
        String url = ApplicationContext.getPROPERTIES().getProperty("url");
        String user = ApplicationContext.getPROPERTIES().getProperty("user");
        String password = ApplicationContext.getPROPERTIES().getProperty("password");
        try {
            c = DriverManager.getConnection(url, user, password);
            Validator<Homework> homeworkVali = new HomeworkValidator();
            CrudRepository<Integer, Homework> homeworkRepo = new HomeworkPostgreSQLRepository(homeworkVali, c);
            //CrudRepository<Integer, Homework> homeworkRepo = new HomeworkJsonFileRepository(homeworkVali, filePath + "Homework.json");
            homeworkServo = new HomeworkService(homeworkRepo, homeworkVali, year);

            Validator<Grade> gradeVali = new GradeValidator();
            CrudRepository<GradeId, Grade> gradeRepo = new GradePostgreSQLRepository(gradeVali, c);
            //CrudRepository<GradeId, Grade> gradeRepo = new GradeXmlFileRepository(gradeVali, filePath + "Grade.xml");
            gradeServo = new GradeService(gradeRepo, gradeVali, year);

            Validator<Student> studentVali = new StudentValidator();
            CrudRepository<Integer, Student> studentRepo = new StudentPostgreSQLRepository(studentVali, c);
            //CrudRepository<Integer, Student> studentRepo = new StudentJsonFileRepository(studentVali, filePath + "Student.json");
            studentServo = new StudentService(studentRepo, studentVali, year);

            Validator<Professor> professorVali = new ProfessorValidator();
            CrudRepository<Integer, Professor> professorRepo = new ProfessorPostgreSQLRepository(professorVali, c);
            //CrudRepository<Integer, Professor> professorRepo = new ProfessorJsonFileRepository(professorVali, filePath + "Professor.json");
            professorServo = new ProfessorService(professorRepo, professorVali, year);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String emailUsername = ApplicationContext.getPROPERTIES().getProperty("emailUsername");
        String emailPassword = ApplicationContext.getPROPERTIES().getProperty("emailPassword");
        messageSender = new MessageSender(emailUsername, emailPassword);
    }

    public void closeConnection() throws SQLException {
        c.close();
    }

    /**
     * @return current week of the semester
     */
    public Integer getWeek() {
        return year.getWeek(LocalDate.now());
    }

    public Integer getWeek(LocalDate date) {
        return year.getWeek(date);
    }

    /**
     * creates a year structure for 2019
     *
     * @return the 2019 university year structure
     */
    private UniversityYearStructure yearSetUp() {
        Vector<Holiday> v1 = new Vector<>();
        v1.add(new Holiday(LocalDate.of(2019, 12, 23), 2));
        v1.add(new Holiday(LocalDate.of(2020, 2, 10), 1));
        Vector<Holiday> v2 = new Vector<>();
        v2.add(new Holiday(LocalDate.of(2020, 4, 20), 1));
        v2.add(new Holiday(LocalDate.of(2020, 6, 29), 1));
        UniversitySemesterStructure s1 = new UniversitySemesterStructure(1, LocalDate.of(2019, 10, 1), v1);
        UniversitySemesterStructure s2 = new UniversitySemesterStructure(1, LocalDate.of(2020, 2, 24), v2);
        return new UniversityYearStructure(1, 2019, s1, s2);
    }

    /**
     * @return returns the next Homework id that would be used to create a new Homework entity
     */
    public Integer getNextHomeworkId() {
        return homeworkServo.getNextId();
    }

    public Integer getNextProfessorId() {
        return professorServo.getNextId();
    }

    public Integer getNextStudentId() {
        return studentServo.getNextId();
    }

    /**
     * creates a new Homework entity and saves it
     *
     * @param h Homework with the description and deadlineWeek fields filled
     */
    public void saveHomework(Homework h) {
        if (h.getId() == null)
            h = homeworkServo.createHomework(h.getDescription(), h.getDeadlineWeek());
        homeworkServo.save(h);
        String message = "The new homework has the starting week: " + h.getStartWeek() + " and the deadline: " + h.getDeadlineWeek() + ".\n" + h.getDescription();
        messageSender.send(h + " was added.", message,
                StreamSupport.stream(studentServo.findAll().spliterator(), false).map(Student::getEmail).collect(Collectors.toList()));
    }

    /**
     * creates a new Student entity and saves it
     *
     * @param s Student with the familyName, firstName, group, email, labProfessorId fields filled in
     */
    public void saveStudent(Student s) {
        if (s.getId() == null)
            s = studentServo.createStudent(s.getFamilyName(), s.getFirstName(), s.getGroup(), s.getEmail(), s.getLabProfessorId());
        studentServo.save(s);
    }

    /**
     * tests if the provided id's are found in the database, if everything is found
     * it creates a new Grade entity and saves it in the provided repository and
     * in a report file
     *
     * @param g             Grade with the id, professorId, givenGrade, feedback fields filled in
     * @param lateProfessor the number of weeks a professor was late to save a grade
     * @throws ValidationException if any of the provided id's aren't found in the database
     */
    public void saveGrade(Grade g, Integer lateProfessor) throws ValidationException {
        String errors = "";
        Homework homework = homeworkServo.findOne(g.getId().getHomeworkId());
        if (homework == null)
            errors += "Tema inexistenta";
        Student student = studentServo.findOne(g.getId().getStudentId());
        if (student == null)
            errors += "Student inexistent";
        Professor professor = professorServo.findOne(g.getProfessorId());
        if (professor == null)
            errors += "Profesor inexistent";
        if (homework == null || student == null || professor == null)
            throw new ValidationException(errors);
        if (g.getId() == null)
            g = gradeServo.createGrade(student, professor, g.getGivenGrade(), homework, g.getFeedback(), lateProfessor);
        gradeServo.save(g);
        gradeServo.saveInStudentNameFile(g, homework, student);
        String message = "The grade is " + g.getGivenGrade() + ", added on " + g.getHandOverDate() + " with the mentions: " + g.getFeedback();
        messageSender.send(professor + " added a new grade.", message, student.getEmail());
    }

    public void saveGrade(Grade g, Homework homework, Professor professor, Student student) throws ValidationException {
        gradeServo.save(g);
        gradeServo.saveInStudentNameFile(g, homework, student);
        String message = "The grade is " + g.getGivenGrade() + ", added on " + g.getHandOverDate() + " with the mentions: " + g.getFeedback();
        messageSender.send(professor + " added a new grade.", message, student.getEmail());
    }

    public Double getFinalGrade(Double givenGrade, Integer deadlineWeek, Integer lateProfessor) {
        return gradeServo.calculateFinalGrade(givenGrade, deadlineWeek, lateProfessor);
    }

    /**
     * creates a new Professor entity and saves it
     *
     * @param p Professor with the familyName, firstName, email fields filler in
     */
    public void saveProfessor(Professor p) {
        if (p.getId() == null)
            p = professorServo.createProfessor(p.getFamilyName(), p.getFirstName(), p.getEmail());
        professorServo.save(p);
    }

    /**
     * deletes the Homework with the given id
     *
     * @param id of the Homework to be deleted
     */
    public void deleteHomework(int id) {
        Homework h = homeworkServo.delete(id);
//        String message = "The homework with the starting week: " + h.getStartWeek() + " and the deadline: " + h.getDeadlineWeek() + ".\n" + h.getDescription();
//        messageSender.send(h + " was deleted.", message,
//                StreamSupport.stream(studentServo.findAll().spliterator(), false).map(Student::getEmail).collect(Collectors.toList()));
    }

    /**
     * deletes the Student with the given id
     *
     * @param id of the Student to be deleted
     */
    public void deleteStudent(int id) {
        studentServo.delete(id);
    }

    /**
     * deletes the Grade with the given id
     *
     * @param id of the Grade to be deleted
     */
    public void deleteGrade(GradeId id) {
        gradeServo.delete(id);
    }

    /**
     * deletes the professor with the given id
     *
     * @param id of the Professor to be deleted
     */
    public void deleteProfessor(int id) {
        professorServo.delete(id);
    }

    /**
     * updates the fields of the Homework entity found at the given id with the new Homework
     *
     * @param id of the Homework to be updated
     * @param h  Homework to be saved at the given id
     */
    public void updateHomework(Integer id, Homework h) {
        if (h.getId() == null) {
            h = homeworkServo.createHomework(h.getDescription(), h.getDeadlineWeek());
            h.setId(id);
        }
        Homework oldH = homeworkServo.findOne(id);
        homeworkServo.update(h);
        String message = "The homework with the starting week: " + oldH.getStartWeek() + " and the deadline: " + oldH.getDeadlineWeek() + ".\n" + oldH.getDescription() + "\n"
                + "Was changed with the homework with the starting week: " + h.getStartWeek() + " and the deadline: " + h.getDeadlineWeek() + ".\n" + h.getDescription();
        messageSender.send(oldH + " was changed with " + h + ".", message,
                StreamSupport.stream(studentServo.findAll().spliterator(), false).map(Student::getEmail).collect(Collectors.toList()));
    }

    /**
     * updates the fields of the Student entity found at the given id with the new Student
     *
     * @param id of the Student to be updated
     * @param s  Student to be saved at the given id
     */
    public void updateStudent(Integer id, Student s) {
        if (s.getId() == null) {
            s = studentServo.createStudent(s.getFamilyName(), s.getFirstName(), s.getGroup(), s.getEmail(), s.getLabProfessorId());
            s.setId(id);
        }
        studentServo.update(s);
    }

    /**
     * tests if the provided id's are found in the database, if everything is found
     * it creates a new Grade entity and updates it in the provided repository
     *
     * @param g             Grade with the id, professorId, givenGrade, feedback fields filled in
     * @param lateProfessor the number of weeks a professor was late to save a grade
     * @throws ValidationException if any of the provided id's aren't found in the database
     */
    public void updateGrade(GradeId id, Grade g, Integer lateProfessor) throws ValidationException {
        String errors = "";
        Homework homework = homeworkServo.findOne(g.getId().getHomeworkId());
        if (homework == null)
            errors += "Tema inexistenta";
        Student student = studentServo.findOne(g.getId().getStudentId());
        if (student == null)
            errors += "Student inexistent";
        Professor professor = professorServo.findOne(g.getProfessorId());
        if (professor == null)
            errors += "Profesor inexistent";
        if (homework == null || student == null || professor == null)
            throw new ValidationException(errors);
        if (g.getId() == null) {
            g = gradeServo.createGrade(student, professor, g.getGivenGrade(), homework, g.getFeedback(), lateProfessor);
            g.setId(id);
        }
        gradeServo.update(g);
    }

    public void updateGrade(GradeId id, Grade g) throws ValidationException {
        gradeServo.update(g);

    }

    /**
     * updates the fields of the Professor entity found at the given id with the new Professor
     *
     * @param id of the Professor to be updated
     * @param p  Professor to be saved at the given id
     */
    public void updateProfessor(Integer id, Professor p) {
        if (p.getId() == null) {
            p = professorServo.createProfessor(p.getFamilyName(), p.getFirstName(), p.getEmail());
            p.setId(id);
        }
        professorServo.update(p);
    }

    /**
     * @return all Homework entities found in the repository
     */
    public Iterable<Homework> findAllHomework() {
        return homeworkServo.findAll();
    }

    public Homework findOneHomework(Integer id) {
        return homeworkServo.findOne(id);
    }

    /**
     * @return all Student entities found in the repository
     */
    public Iterable<Student> findAllStudent() {
        return studentServo.findAll();
    }

    public Student findOneStudent(Integer id) {
        return studentServo.findOne(id);
    }

    /**
     * @return all Grade entities found in the repository
     */
    public Iterable<Grade> findAllGrade() {
        return gradeServo.findAll();
    }

    public Grade findOneGrade(GradeId id) {
        return gradeServo.findOne(id);
    }

    /**
     * @return all Professor entities found in the repository
     */
    public Iterable<Professor> findAllProfessor() {
        return professorServo.findAll();
    }

    public Professor findOneProfessor(Integer id) {
        return professorServo.findOne(id);
    }

    /**
     * saves all the changes made in memory to file
     */
    public void saveAll() {
        homeworkServo.saveAll();
        gradeServo.saveAll();
        studentServo.saveAll();
        professorServo.saveAll();
    }

    /**
     * @param group Student field
     * @return an iterable with all the students from a given group
     */
    public Iterable<Student> filterByStudentGroup(Integer group) {
        return StreamSupport.stream(studentServo.findAll().spliterator(), false)
                .filter(s -> s.getGroup().equals(group))
                .collect(Collectors.toList());
    }

    /**
     * @param homeworkId Grade field
     * @return an iterable with all the Students that handed over a given homework
     */
    public Iterable<Student> filterByHandOverHomework(Integer homeworkId) {
        return StreamSupport.stream(gradeServo.findAll().spliterator(), false)
                .filter(g -> g.getId().getHomeworkId().equals(homeworkId))
                .map(g -> studentServo.findOne(g.getId().getStudentId()))
                .collect(Collectors.toList());
    }

    /**
     * @param homeworkId  Grade field
     * @param professorId Grade field
     * @return an iterable with all the Students that handed over a given homework to a given professor
     */
    public Iterable<Student> filterByHandOverHomeworkAndProfessor(Integer homeworkId, Integer professorId) {
        return StreamSupport.stream(gradeServo.findAll().spliterator(), false)
                .filter(g -> g.getId().getHomeworkId().equals(homeworkId) && g.getProfessorId().equals(professorId))
                .map(g -> studentServo.findOne(g.getId().getStudentId()))
                .collect(Collectors.toList());
    }

    /**
     * @param homeworkId   Grade field
     * @param handOverWeek Grade field
     * @return an iterable with all the Grades from a Homework handed oven in a given week
     */
    public Iterable<Grade> filterByHomeworkAndHandOverWeek(Integer homeworkId, Integer handOverWeek) {
        return StreamSupport.stream(gradeServo.findAll().spliterator(), false)
                .filter(g -> g.getId().getHomeworkId().equals(homeworkId) && year.getWeek(g.getHandOverDate()).equals(handOverWeek))
                .collect(Collectors.toList());
    }
}
