package com.az.gitember.os;

import com.az.gitember.service.Context;
import javafx.scene.image.Image;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Just cache.
 */
public class ProxyIconFactory implements IconFactory {

    private IconFactory delegate;
    private Map<String, Image> cache = new ConcurrentHashMap<>();

    public ProxyIconFactory() {
        if(Context.isMac()) {
            delegate = new com.az.gitember.os.mac.IconFactoryImpl();
        }
    }

    @Override
    public Image createImage(WinIconType type, WinIconMode mode, Theme theme) {
        String key = "" + type + mode + theme;
        return cache.computeIfAbsent(
                key,
                k -> delegate.createImage(type, mode, theme)
        );
    }
}
