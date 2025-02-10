package com.tenant.form.setting;

import lombok.Data;

import java.util.Arrays;
import java.util.List;

@Data
public class FindByKeyNameForm {
    private String[] keyNames;

    public List<String> getKeyNames(){
        return Arrays.asList(keyNames);
    }
}
