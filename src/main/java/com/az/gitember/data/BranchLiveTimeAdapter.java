package com.az.gitember.data;

import com.az.gitember.service.GitemberUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class BranchLiveTimeAdapter {


    public List<AverageLiveTime> adapt(List<BranchLiveTime> branchLiveTimes) {

        if (branchLiveTimes.isEmpty()) {
            return Collections.EMPTY_LIST;
        } else {
            int qtyPerMonth = 0 ;
            int hrsPerMonth = 0;
            final List<AverageLiveTime> avg = new ArrayList<>();
            final SimpleDateFormat sdf = new SimpleDateFormat("yyyy MMM");
            Date dt = GitemberUtil.intToDate(branchLiveTimes.get(0).getEnd().getCommitTime());


            for (BranchLiveTime blt : branchLiveTimes) {
                final Date currentDt = GitemberUtil.intToDate(blt.getEnd().getCommitTime());
                if (currentDt.getMonth() != dt.getMonth()) {
                    String yyyyMMM = sdf.format(dt);
                    avg.add(new AverageLiveTime(yyyyMMM, qtyPerMonth, hrsPerMonth));
                    qtyPerMonth = 0 ;
                    hrsPerMonth = 0;
                    dt = currentDt;
                }
                qtyPerMonth++;
                hrsPerMonth += blt.getHours();
            }
            avg.add(new AverageLiveTime(sdf.format(dt), qtyPerMonth, hrsPerMonth));

            Collections.reverse(avg);

            return avg;
        }

    }


}
