package com.qianlima.demo.es.util;


import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author: Young
 * @Date: 2019/7/15 11:10
 */
public class DateUtils {

    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");
    private static Pattern datePattern = Pattern.compile("(\\d{4})-(\\d{1,2})-(\\d{1,2})");
    private static final String DATE_FORMAT = "%s-%s-%s";

    /**
     * 返回指定日期间隔到当前天的Solr参数格式
     * @param gapDate
     * @return
     */
    public static String[] getSolrSearchDate(int gapDate){
        Calendar curr = Calendar.getInstance();
        String[] res = new String[2];
        res[1] = SDF.format(curr.getTime());
        curr.add(Calendar.DAY_OF_YEAR,0-gapDate);
        res[0] = SDF.format(curr.getTime());
        return res;
    }

    public static String getFormatDate(long epoch){
        return SDF.format(new Date(epoch));
    }

    public static boolean validDateString(String...dateStr){
        if(dateStr==null || dateStr.length==0){
            return false;
        }
        for (String s : dateStr) {
            Matcher m = datePattern.matcher(s);
            if (StringUtils.isEmpty(s) || !m.matches()){
                return false;
            }
            int g1 = Integer.valueOf(m.group(1));
            int g2 = Integer.valueOf(m.group(2));
            int g3 = Integer.valueOf(m.group(3));
            if(g1<2012 || g2==0 || g2 > 12 || g3==0 || g3 > 31){
                return false;
            }
        }
        return true;
    }

    /**
     * 2019-1-1 => 2019-01-02
     * @param dateStr
     * @return
     */
    public static String formatString(String dateStr){
        String[] dates;
        if(StringUtils.isEmpty(dateStr) || (dates = dateStr.split("-")).length < 3){
            return "";
        }
        for(int i = 0; i<dates.length; i++){
            if(dates[i].length() < 2){
                dates[i] = "0" + dates[i];
            }
        }
        return String.format(DATE_FORMAT,dates[0],dates[1],dates[2]);
    }

}
