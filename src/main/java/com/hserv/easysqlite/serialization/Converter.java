package easysqlite.serialization;

public class Converter {

    public Deserialization deserializater;
    public Serialization serializer;
    public boolean propagate;

    public Converter(Serialization serializater, Deserialization deserializater){
        this.serializer = serializater;
        this.deserializater = deserializater;
        this.propagate = false;
    }

    public Converter(Serialization serializater, Deserialization deserializater, boolean propagate){
        this.deserializater = deserializater;
        this.serializer = serializater;
        this.propagate = propagate;
    }
}
