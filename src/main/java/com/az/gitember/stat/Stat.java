package com.az.gitember.stat;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.text.DateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Stat {

    public static void main(String[] args) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        StatDTO[] dtos = new StatDTO[0];
        String repoName = "c:\\work\\mec\\stat2\\ios.html";
        for (int i = 1; i < 10; i++) {
            StatDTO[] tmp = objectMapper.readValue(new File("C:\\work\\mec\\stat2\\ios-" + i + ".json"), StatDTO[].class);
            dtos = ArrayUtils.addAll(dtos, tmp);
        }
        StringBuilder repo = new StringBuilder();
        repo.append("<table>")
                .append("<tr style=\"background-color : lightgray\">")
                .append("<td>#</td>")
                .append("<td>Name</td>")
                .append("<td>Open</td>")
                .append("<td>Merged</td>")
                .append("<td>&#x394; raw</td>")
                .append("<td>&#x394; WT. &gt;= 1h</td>")
                .append("</tr>");
        int month = dtos[0].getCreated_at().getMonth();
        int idx = 1;

        long sumMonth = 0;
        long qtyMonth = 0;
        long sumAll= 0;
        long qtyAll= 0;


        for (StatDTO dto : dtos) {


            long diffInMillies = 0;
            long diffHrs = 0;
            long diffMin = 0;
            long diffDays = 0;
            long diffHrsWorking = 0;


            if (month != dto.getCreated_at().getMonth()) {

                java.time.LocalDate lastTd = dto.getCreated_at().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
                lastTd = lastTd.plus(Period.ofMonths(1));


if (qtyMonth == 0 ) {
    qtyMonth = 1;
}
                repo.append("<tr style=\"background-color : lightgray\">")
                        .append("<td></td>")
                        .append("<td> Month  " + lastTd.getMonth().getDisplayName(TextStyle.FULL, Locale.ROOT)  +"   </td>")
                        .append("<td></td>")
                        .append("<td></td>")
                        .append("<td></td>")
                        .append("<td> <b>Sum " + sumMonth  + " h <br>" )
                        .append(" Avg " + (sumMonth / qtyMonth) + " h </b>" + "</td>")
                        .append("</tr>");

                month = dto.getCreated_at().getMonth();
                sumMonth =0;
                qtyMonth =0;
            }
            if (dto.getMerged_at() != null && dto.getCreated_at() != null) {
                diffInMillies = Math.abs(dto.getMerged_at().getTime() - dto.getCreated_at().getTime());
                diffDays = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
                diffHrs = TimeUnit.HOURS.convert(diffInMillies, TimeUnit.MILLISECONDS);
                diffMin = TimeUnit.MINUTES.convert(diffInMillies, TimeUnit.MILLISECONDS);

                java.time.LocalDate startDate = dto.getCreated_at().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
                java.time.LocalDate endDate = dto.getMerged_at().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();



                int weekends = startDate.datesUntil(endDate)
                        .collect(Collectors.toList())
                        .stream().filter(dt -> {
                            return "SATURDAY".equalsIgnoreCase(dt.getDayOfWeek().toString()) ||
                                    "SUNDAY".equalsIgnoreCase(dt.getDayOfWeek().toString());
                        })
                        .mapToInt(i -> 1)
                        .sum();


                diffDays = diffDays - weekends;
                diffHrsWorking = (diffHrs - 16 * diffDays - 24 * weekends);

                if (diffHrsWorking > 0) {
                    sumMonth += diffHrsWorking;
                    qtyMonth ++;
                    sumAll += diffHrsWorking;
                    qtyAll ++;

                }

                repo.append("<tr>")
                        .append("<td>" + idx + "</td>")
                        .append("<td>" + dto.getTitle() + "</td>")
                        .append("<td>" + dto.getCreated_at() + "</td>")
                        .append("<td>" + dto.getMerged_at() + "</td>")
                        .append("<td>" + (diffHrs == 0 ? diffMin + " m" : diffHrs + " h") + "</td>")
                        .append("<td style=\"background-color : lightgray\">" + (diffHrsWorking > 0 ? diffHrsWorking : "") + " " + "</td>")
                        .append("</tr>");

                idx++;


            }


        }

        repo.append("<tr style=\"background-color : gray\">")
                .append("<td></td>")
                .append("<td>   </td>")
                .append("<td></td>")
                .append("<td></td>")
                .append("<td></td>")
                .append("<td> <b>Sum " + sumAll  + " h <br>" )
                .append(" Avg " + (sumAll / qtyAll) + " h </b>" + "</td>")

                .append("</tr>");

        repo.append("</table>");

        Files.write(
                Path.of(repoName),
                repo.toString().getBytes()
        );


    }

}
