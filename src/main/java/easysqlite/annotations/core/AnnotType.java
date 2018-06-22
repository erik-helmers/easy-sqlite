package easysqlite.annotations.core;

import easysqlite.annotations.handlers.DefaultColumnHandler;
import easysqlite.annotations.handlers.DefaultTableHandler;

public enum AnnotType {
    TABLE(DefaultTableHandler.class),
    COLUMN(DefaultColumnHandler.class),
    CONVERTER(ConverterHandler.class);

    private Class handler_type;

    AnnotType(Class clss){
            handler_type = clss;
    }

    public Class handler_type(){
            return handler_type;
    }
}
