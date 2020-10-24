package de.moneymanager.controller.tranferobjects;

import com.sun.istack.NotNull;
import lombok.Data;

@Data
public class NewBankAccountTO {

    @NotNull
    private String name;

}
