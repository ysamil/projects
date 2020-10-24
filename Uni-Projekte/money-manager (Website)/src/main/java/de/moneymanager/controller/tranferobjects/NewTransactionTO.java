package de.moneymanager.controller.tranferobjects;

import com.sun.istack.NotNull;
import lombok.Data;

@Data
public class NewTransactionTO {

    @NotNull
    private String recipentIban;
    private String usage;
    private Double amount;

}
