package com.az.gitember.service;

import com.sshtools.icongenerator.IconBuilder;

public class IconMaker {

    final private int width;
    final private int height;

    private final IconBuilder builder = new IconBuilder();


    public IconMaker(int width, int height) {
        this.width = width;
        this.height = height;
    }
}
