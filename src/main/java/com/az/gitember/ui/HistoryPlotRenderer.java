package com.az.gitember.ui;

import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revplot.AbstractPlotRenderer;
import org.eclipse.jgit.revplot.PlotCommit;
import org.eclipse.jgit.revplot.PlotCommitList;
import org.eclipse.jgit.revplot.PlotLane;

/**
 * Created by Igor_Azarny on 03 - Dec - 2016
 */
public class HistoryPlotRenderer extends AbstractPlotRenderer {


    @Override
    protected int drawLabel(int x, int y, Ref ref) {
        return 0;
    }

    @Override
    protected Object laneColor(PlotLane myLane) {
        return null;
    }

    @Override
    protected void drawLine(Object o, int x1, int y1, int x2, int y2, int width) {

    }

    @Override
    protected void drawCommitDot(int x, int y, int w, int h) {

    }

    @Override
    protected void drawBoundaryDot(int x, int y, int w, int h) {

    }

    @Override
    protected void drawText(String msg, int x, int y) {

    }

    public void render(PlotCommit pc, int height) {

        this.paintCommit(pc, height);

    }


}
