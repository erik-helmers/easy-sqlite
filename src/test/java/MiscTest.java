import easysqlite.annotations.declarations.IdColumn;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.util.Collection;

public class MiscTest {


    @Test
    public void main() throws Exception{
        Class<X> clss = X.class;
        Annotation annot = clss.getField("x").getAnnotation(IdColumn.class);
        System.out.println(annot.type);
        IdColumn idColumn = (IdColumn) annot;
        System.out.println(idColumn.type);



    }

    public <A> void yo(Collection<A> x, Class b){
        System.out.println(b.equals(x.iterator().next().getClass()));
    }

    public static class X{
        @IdColumn
        public Integer x;
        public X(){};
    }


}
