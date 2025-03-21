package luyen.tradebot.Trade.util;

import java.util.Calendar;
import java.util.Date;

public class DateUtil {
    public static Date plusDate(int dateInt) {
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.DAY_OF_MONTH, dateInt);
        Date newDate = calendar.getTime();
        return newDate;
    }
}
