package top.ivan.jdrop.modules.exchange.stable.dto;

import top.ivan.jdrop.framework.model.ExchangeResult;

public class UploadResult extends ExchangeResult {
    public static final UploadResult SUCCESS = new UploadResult(ExchangeState.SUCCESS);
    public static final UploadResult FAILED = new UploadResult(ExchangeState.FAILED);

    public UploadResult(ExchangeState state) {
        super(state);
    }
}
