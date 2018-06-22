package easysqlite.serialization;

public interface Serializable {
    static Object deserialize(String from, Class target){
        throw new Error("this shouldn't be called !");
    }

    String serialize();
}
