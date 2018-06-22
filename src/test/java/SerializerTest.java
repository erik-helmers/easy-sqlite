import easysqlite.serialization.Serializer;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SerializerTest {

    Serializer serializer;


    @Before
    public void setUp() throws Exception {
        serializer = new Serializer();
    }

    @Test
    public void scan_converters() {
        serializer.scan_converters(Car.class);
    }

    @Test
    public void serialize() {
        assertEquals("3", serializer.serialize(new Car.Id(3)));
        assertEquals("3", serializer.serialize(3));
        assertEquals("3.0", serializer.serialize(3f));
        assertEquals("3", serializer.serialize("3"));
    }

    @Test
    public void deserialize() {
        Object x= serializer.deserialize("3", Car.Id.class);
        Log.debug_value("class name", (x.getClass().getName()));
        Log.debug_value("value", x);
        Log.debug_value("_id", ((Car.Id) x)._id);
        assertEquals(new Car.Id(3), x);
    }
}