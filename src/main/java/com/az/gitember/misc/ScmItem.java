package com.az.gitember.misc;

/**
 * Created by Igor_Azarny on 03 - Dec - 2016
 */
public class ScmItem extends Pair<String, ScmItemAttribute> {

    public ScmItem(String s, ScmItemAttribute attribute) {
        super(s, attribute);
    }

    public String getShortName() {
        return super.getFirst();
    }

    public ScmItemAttribute getAttribute() {
        return super.getSecond();
    }

    @Override
    public String toString() {
        return "ScmItem " + getShortName() + " " + getAttribute();
    }
}
