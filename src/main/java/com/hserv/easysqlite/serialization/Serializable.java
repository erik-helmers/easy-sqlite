package easysqlite.serialization;

public interface Serializable {
    static Object deserialize(String from, Class target, Class source){
        throw new Error("this shouldn't be called !");
    }
    public String serialize();
    public static boolean propagate(){return false;};
}
