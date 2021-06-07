package com.az.gitember.service;

import com.az.gitember.controller.TextBrowserContentAdapter;
import com.az.gitember.data.LangDefinition;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import static com.az.gitember.service.GitemberUtil.is;

/**
 * Resolve lang name, which is correspond  to name in configured languages by extension.
 * Keyword definitions taken from here https://github.com/e3b0c442/keywords#visual-basic-2019-217-keywords
 */
public class LangResolver {

    private static LangDefinition[] langDefinitions = null;

    public Optional<LangDefinition> resolveLang(final String ext) {
        return Arrays.stream(readLangDefinition())
                .filter(ld -> is(ext).oneOf(ld.getExtension()))
                .findFirst();

    }


    private synchronized LangDefinition[] readLangDefinition() {

        try {
            String text = IOUtils.toString(TextBrowserContentAdapter.class.getResourceAsStream(
                    "/TextBrowserContentAdapter.json"),
                    "UTF-8");
            langDefinitions = GitemberUtil.getKeywordsDefinition(text);
        } catch (IOException e) {
            e.printStackTrace();
            langDefinitions = new LangDefinition[0];
        }

        return langDefinitions;
    }

}
