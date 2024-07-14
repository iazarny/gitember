package com.az.gitember.controller.common;

import com.az.gitember.data.ScmRevisionInformation;
import com.az.gitember.service.GitemberUtil;
import javafx.util.StringConverter;

public class ScmRevisionInformationConverter extends StringConverter<ScmRevisionInformation> {
    @Override
    public String toString(ScmRevisionInformation scmRevisionInformation) {

        return GitemberUtil.formatRev(scmRevisionInformation);
    }

    @Override
    public ScmRevisionInformation fromString(String s) {
        return new ScmRevisionInformation();
    }
}
