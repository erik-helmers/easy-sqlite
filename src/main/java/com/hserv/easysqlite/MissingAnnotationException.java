package easysqlite;

public class MissingAnnotationException extends RuntimeException {
    public MissingAnnotationException() {
        super("Missing annotations !");
    }
}
