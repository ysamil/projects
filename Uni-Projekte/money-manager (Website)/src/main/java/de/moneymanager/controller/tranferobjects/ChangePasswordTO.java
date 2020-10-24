package de.moneymanager.controller.tranferobjects;

import com.sun.istack.NotNull;
import lombok.Data;

@Data
public class ChangePasswordTO {

    @NotNull
    private String currentPassword;
    @NotNull
    private String newPassword;
    @NotNull
    private String newPasswordConfirmed;

}
