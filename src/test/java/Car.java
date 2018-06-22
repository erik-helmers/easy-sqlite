import easysqlite.annotations.declarations.AllFieldsTable;
import easysqlite.annotations.declarations.IdColumn;
import lombok.Builder;
import lombok.ToString;

@ToString
@AllFieldsTable("cars")
public class Car extends Vehicle{

    @IdColumn
    public int id;
    public String proprietary;
    public int seats;
    public int price;


    @Builder
    public Car(int id, String proprietary, int seats, int price) {
        this.id = id;
        this.proprietary = proprietary;
        this.seats = seats;
        this.price = price;
        System.out.println("e");
    }

    public Car(){};

    public static class Id extends Vehicle.Id{

        public Id(int x) {
            super(x);
        }

        public static Object deserialize(String from, Class target) {
            return new Id(Integer.parseInt(from));
        }
    }

}