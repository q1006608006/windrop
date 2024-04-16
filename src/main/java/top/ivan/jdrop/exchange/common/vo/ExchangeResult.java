package top.ivan.jdrop.exchange.common.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import top.ivan.jdrop.exchange.common.constant.ExchangeState;

@Data
@AllArgsConstructor
public abstract class ExchangeResult {
    protected ExchangeState state;
}
