package easysqlite.annotations.handlers;

import easysqlite.annotations.declarations.Exclude;

import java.lang.annotation.Annotation;

public class ExcludeHandler extends DefaultColumnHandler{

    private Wrapper annotation;

    public ExcludeHandler(Annotation annotation) {
        super(annotation);
    }

    public static class Wrapper extends DefaultColumnHandler.Wrapper {
        Exclude annotation;
        @Override
        public void setAnnotation(Annotation annotation) {
            super.setAnnotation(annotation);
            this.annotation = (Exclude)annotation;
        }

        @Override
        public String name() {
            return "";
        }
    }
