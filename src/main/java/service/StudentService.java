package service;

import domain.Student;
import domain.UniversityYearStructure;
import repository.CrudRepository;
import validation.ValidationException;
import validation.Validator;

public class StudentService extends Service<Integer, Student> {
    private Integer lastId;

    public StudentService(CrudRepository<Integer, Student> repo, Validator<Student> vali, UniversityYearStructure year) {
        super(repo, vali, year);
        this.lastId = idSetUp();
    }

    /**
     * @return the last id used for a student when the service is created
     */
    private Integer idSetUp() {
        Integer max = 0;
        for(var i : super.findAll())
            max = i.getId() > max ? i.getId() : max;
        return max;
    }

    public Integer getNextId() {
        return lastId + 1;
    }

    /**
     * creates a new Student object, auto-generating the id
     *
     * @param familyName  Student field
     * @param firstName   Student field
     * @param group       Student field
     * @param email       Student field
     * @param professorId Student field
     * @return a new Student object
     */
    public Student createStudent(String familyName, String firstName, Integer group, String email, Integer professorId) {
        Student student = new Student(getNextId(), familyName, firstName, group, email, professorId);
        vali.validate(student);
        return student;
    }

    @Override
    public void save(Student entity) throws ValidationException {
        super.save(entity);
        lastId++;
    }
}
