package net.runtime.role.inconsistency;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Created by nguonly on 4/2/16.
 */
public class LOG {
    public static void println(String msg){
        long threadId = Thread.currentThread().getId();
        LocalDateTime time = LocalDateTime.now();
        String m = String.format("%s [%d] %s", time, threadId, msg);
        System.out.println(m);

    }
}
