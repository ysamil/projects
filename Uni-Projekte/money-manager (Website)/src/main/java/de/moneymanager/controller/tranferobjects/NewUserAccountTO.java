package de.moneymanager.controller.tranferobjects;

import com.sun.istack.NotNull;
import lombok.Data;

@Data

public class NewUserAccountTO {

    @NotNull
    private String name;
    @NotNull
    private String email;
    @NotNull
    private String iban;

}
