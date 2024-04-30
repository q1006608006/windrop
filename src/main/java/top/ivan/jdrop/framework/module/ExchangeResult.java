package top.ivan.jdrop.framework.module;

import lombok.AllArgsConstructor;
import lombok.Data;
import top.ivan.jdrop.exchange.userinterface.constant.ExchangeState;

@Data
@AllArgsConstructor
public abstract class ExchangeResult {
    protected ExchangeState state;
}
