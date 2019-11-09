package repository;

import domain.Entity;
import validation.Validator;

public abstract class AbstractInFileRepository<ID, E extends Entity<ID>> extends InMemoryRepository<ID, E> {
    String fileName;

    AbstractInFileRepository(Validator<E> validator, String fileName) {
        super(validator);
        this.fileName = fileName;
        readEntities();
    }

    /**
     * writes all entities from memory to file
     */
    abstract void writeEntities();

    /**
     * reads all entities from file and stores them in memory
     */
    abstract void readEntities();

    /**
     * deletes all entities from memory and file
     */
    @Override
    public void deleteAll() {
        super.deleteAll();
    }

    /**
     * saves all the entities from memory to file
     */
    public void saveAll() {
        writeEntities();
    }
}
