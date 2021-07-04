package top.ivan.windrop.ex;

public class UnCatchableException extends RuntimeException {

    public UnCatchableException() {
        super();
    }

    public UnCatchableException(String s) {
        super(s);
    }

    public UnCatchableException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public UnCatchableException(Throwable throwable) {
        super(throwable);
    }

    protected UnCatchableException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1);
    }
}
