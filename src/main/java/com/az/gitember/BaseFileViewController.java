package com.az.gitember;

import com.az.gitember.misc.HighlightProvider;
import org.fxmisc.richtext.CodeArea;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Created by Igor_Azarny on 08.12.2016.
 */
public class BaseFileViewController {

    protected void fillCodeArea(CodeArea area, String fileName) throws IOException {
        area.richChanges()
                .filter(ch -> !ch.getInserted().equals(ch.getRemoved())) // XXX
                .subscribe(change -> {
                            area.setStyleSpans(
                                    0,
                                    HighlightProvider.computeHighlighting(
                                            area.getText(),
                                            HighlightProvider.resolvePattern(fileName)
                                    )
                            );
                        }
                );

        area.replaceText(0, 0, new String(
                        Files.readAllBytes((new File(fileName)).toPath()),
                        "UTF-8"
                )
        );

    }

}
