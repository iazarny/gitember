package com.az.gitember.gitlab.model;

import org.gitlab4j.api.models.AbstractUser;

public class FxAssignee extends FxAbstractUser {

    public FxAssignee(AbstractUser user) {
        super(user);
    }

    public FxAssignee() {
        super();
    }
}
