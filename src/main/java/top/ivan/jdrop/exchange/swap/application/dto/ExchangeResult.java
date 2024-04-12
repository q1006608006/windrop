package top.ivan.jdrop.exchange.swap.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public abstract class ExchangeResult {
    protected ExchangeState state;
}
