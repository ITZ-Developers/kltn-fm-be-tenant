package com.tenant.form.setting;

import lombok.Data;

import java.util.Arrays;
import java.util.List;

@Data
public class FindByGroupNameForm {
    private String[] groupNames;

    public List<String> getGroupNames(){
        return Arrays.asList(groupNames);
    }
}
