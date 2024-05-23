package top.ivan.jdrop.framework.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import top.ivan.jdrop.modules.exchange.stable.dto.ExchangeState;

@Data
@AllArgsConstructor
public abstract class ExchangeResult {
    protected ExchangeState state;
}
