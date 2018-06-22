import easysqlite.serialization.Serializable;

public abstract class Vehicle {

    public static abstract class Id implements Serializable {
        public final int _id;

        public Id(int x){
            _id = x;
        }

        public static Object deserialize(String from,Class target){
            throw new Error("This should be overided by children");
        }

        @Override
        public String serialize() {
            return String.valueOf(_id);
        }

        @Override
        public boolean equals(Object o) {
            if (this.getClass().equals(o.getClass()))
                return _id == ((Id)o)._id;
            return false;
        }
    }

}
