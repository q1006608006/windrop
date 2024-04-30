package top.ivan.jdrop.exchange.application.dto;

import top.ivan.jdrop.exchange.userinterface.constant.ExchangeState;
import top.ivan.jdrop.framework.module.ExchangeResult;

public class UploadResult extends ExchangeResult {
    public static final UploadResult SUCCESS = new UploadResult(ExchangeState.SUCCESS);
    public static final UploadResult FAILED = new UploadResult(ExchangeState.FAILED);

    public UploadResult(ExchangeState state) {
        super(state);
    }
}
