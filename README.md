# EASY-SQLITE
my helper for sqlite and java
using jdk 8

## Usage example 



    @Table(name="cars")
    class Car{

      public Car(){}
      public Car(int id, String owner){
          this.id = new Id(id);
          this.owner = owner;
      }

      @Column(name="id")
      public Id id;
      @Column(name="owner")
      public String owner;

      @Converter(target=Id.class)
      public static final ConverterIntrf string_to_id = from -> new Id(Integer.parseInt(from)-1);

      public static class Id{
          private int _id;
          public Id(int id){
              this._id = id;
          }

          @Override
          public String toString(){
              return String.valueOf(_id);
          }
      }
    }

    Database<Car> db = new Database<>(Car.class, "vehicles.db",
            "CREATE TABLE IF NOT EXISTS cars(id integer PRIMARY KEY, owner text);");
    db.save(new Car(1, "Mr. Sandman"));
    List<Car> car = db.search("id", "Mr. Sandman");

