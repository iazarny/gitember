package com.az.gitember.gitlab.model;

import org.gitlab4j.api.models.AbstractUser;

public class FxAuthor extends FxAbstractUser {
    public FxAuthor(AbstractUser user) {
        super(user);
    }

    public FxAuthor() {
    }
}
