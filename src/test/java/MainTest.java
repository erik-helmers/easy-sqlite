import easysqlite.core.Repository;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class MainTest {

    Repository<Car> main;

    @Before
    public void setUp() throws Exception {
        Path file = Paths.get(getClass().getClassLoader().getResource("vehicles_create.sql").getFile());
        main = new Repository<>(Car.class, "vehicles.db", file.toFile());
    }


    @Test
    public void create(){

    }

    @Test
    public void write() throws SQLException {

        int y = new Random().nextInt()%100000;
        String x = String.valueOf(new Random().nextInt()%100);

        Car vehicle = new Car(y, "Agent "+x, 3, y);
        main.save(vehicle);
    }

    @Test
    public void write_2() throws SQLException{
        Car car = new Car(0, "007", 2, 10000000);
        main.save(car);
        System.out.println(car);
    }

    @Test
    public void write_3() throws SQLException {
        List<Car> cars = new ArrayList<>();
        for (int i=0; i<20; i++){
            cars.add(new Car(0, "Mr Number "+String.valueOf(i), i%4, i*354%120+3));
        }
        main.save(cars);
    }

    @Test
    public void search() throws Exception{
        System.out.println(main.search("price", "-9180"));
    }

    @Test
    public void big_op() throws Exception{


    }





}