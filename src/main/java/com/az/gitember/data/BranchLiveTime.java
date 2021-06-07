package com.az.gitember.data;

import com.az.gitember.service.GitemberUtil;
import org.eclipse.jgit.revwalk.RevCommit;

import java.time.ZoneId;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class BranchLiveTime {

    private final RevCommit end;
    private final RevCommit start;
    private int ttlHours = -1;

    public BranchLiveTime(RevCommit end, RevCommit start) {
        this.end = end;
        this.start = start;
    }

    public RevCommit getEnd() {
        return end;
    }

    public RevCommit getStart() {
        return start;
    }


    public int calculateDiff(Date start,  Date end) {
        long diffInMillies = end.getTime() - start.getTime();
        long diffDays = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
        long diffHrs = TimeUnit.HOURS.convert(diffInMillies, TimeUnit.MILLISECONDS);
        java.time.LocalDate startLDate = start.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        java.time.LocalDate endLDate = end.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        int weekends = startLDate.datesUntil(endLDate)
                .collect(Collectors.toList())
                .stream().filter(dt -> {
                    return "SATURDAY".equalsIgnoreCase(dt.getDayOfWeek().toString()) ||
                            "SUNDAY".equalsIgnoreCase(dt.getDayOfWeek().toString());
                })
                .mapToInt(i -> 1)
                .sum();

        diffDays = diffDays - weekends;
        int rez = (int) (diffHrs - 16 * diffDays - 24 * weekends);
        if (rez == 0) {
            rez = 1;
        }
        return rez;
    }

    public void calculateDiff() {
        Date startDate = GitemberUtil.intToDate(start.getCommitTime());
        Date endDate = GitemberUtil.intToDate(end.getCommitTime());
        ttlHours = calculateDiff(startDate, endDate);
    }

    public int getHours() {
        if(ttlHours == -1) {
            Date startDate = GitemberUtil.intToDate(start.getCommitTime());
            Date endDate = GitemberUtil.intToDate(end.getCommitTime());
            long diffInMillies = endDate.getTime() - startDate.getTime();
            return (int)TimeUnit.HOURS.convert(diffInMillies, TimeUnit.MILLISECONDS);
        } else {
            return ttlHours;
        }
    }

}
