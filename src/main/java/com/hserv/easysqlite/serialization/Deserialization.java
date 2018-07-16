package easysqlite.serialization;

public  interface Deserialization {
    Object deserialize(String from, Class target, Class source);
}
