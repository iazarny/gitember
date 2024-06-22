package com.az.gitember.os;

import javafx.scene.image.Image;

public interface IconFactory {

    enum Theme {
        DARK,
        WHITE;
    }

    enum WinIconType {
        CLOSE,
        MINIMIZE,
        MAXIMIZE
    }

    enum WinIconMode {
        NORMAL,
        HOVER,
        INACTIVE
    }

    Image createImage(IconFactory.WinIconType type,
                      IconFactory.WinIconMode mode,
                      IconFactory.Theme theme,
                      boolean isAppMaximised
    );




}

