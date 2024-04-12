package top.ivan.jdrop.exchange.swap.application.dto;

public class UploadResult extends ExchangeResult {
    public static final UploadResult SUCCESS = new UploadResult(ExchangeState.SUCCESS);
    public static final UploadResult FAILED = new UploadResult(ExchangeState.FAILED);

    public UploadResult(ExchangeState state) {
        super(state);
    }
}
