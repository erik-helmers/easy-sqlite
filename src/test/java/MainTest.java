import easysqlite.Database;
import easysqlite.annotations.Column;
import easysqlite.annotations.Table;
import lombok.Builder;
import lombok.ToString;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Random;
import java.util.UUID;

public class MainTest {

    Database<Vehicle> main;

    @Before
    public void setUp() throws Exception {
        create();
    }

    @Test
    public void create(){
        Path file = Paths.get(getClass().getClassLoader().getResource("vehicles_create.sql").getFile());
        main = new Database<>(Vehicle.class, "vehicles.db", file.toFile());
    }

    @Test
    public void write() throws SQLException {

        int y = new Random().nextInt()%100000;
        String x = String.valueOf(new Random().nextInt()%100);

        Vehicle vehicle = new Vehicle(UUID.randomUUID().toString(), "Agent "+x, y%4, y);
        main.save(vehicle);
    }

    @Test
    public void search() throws Exception{
        System.out.println(main.search("price", "-9180"));
    }

    @Test
    public void big_op() throws Exception{

        System.out.print("creating a new vehicle...");

        int y = new Random().nextInt()%100000;
        String x = String.valueOf(new Random().nextInt()%100);
        Vehicle vehicle = new Vehicle(UUID.randomUUID().toString(), "Agent "+x, y%4, y);

        System.out.println(vehicle.toString());

        main.save(vehicle);

        System.out.print("Searching by id...");
        System.out.println(
                main.search("id", vehicle.id).get(0).id.equals(vehicle.id) ?
                        "OK" : "FAIL");
        System.out.print("Searching by proprietary...");
        System.out.println(
                main.search("proprietary", vehicle.proprietary).get(0).proprietary.equals(vehicle.proprietary) ?
                        "OK" : "FAIL");



    }

    @ToString
    @Table(name="cars")
    private static class Vehicle{

        @Column(name="id")
        public String id;
        @Column(name="proprietary")
        public String proprietary;
        @Column(name="seats")
        public int seats;
        @Column(name="price")
        public int price;


        @Builder
        public Vehicle(String id, String proprietary, int seats, int price) {
            this.id = id;
            this.proprietary = proprietary;
            this.seats = seats;
            this.price = price;
        }

        public Vehicle(){};


    }

}