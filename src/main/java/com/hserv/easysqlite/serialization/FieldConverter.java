package easysqlite.serialization;

public abstract class FieldConverter {

    abstract String serialize(Object object);
    abstract Object deserialize(String from, Class target, Class source);

}
