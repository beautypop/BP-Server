package common.utils;

import java.util.Random;

/**
 * 
 * @author keithlei
 *
 */
public class ShortCodeGenerator {

    private static final String CODES = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int CODES_LEN = CODES.length();
    private static final Random rand = new Random();

    /**
     * @param len
     * @return
     */
    public static String genRandomCode(int len)  {
       StringBuilder sb = new StringBuilder(len);
       for (int i = 0; i < len; i++) {
          sb.append(CODES.charAt(rand.nextInt(CODES_LEN)));
       }
       return sb.toString();
    }

    /**
     * @return
     */
    public static String genPromoCode() {
        return genRandomCode(6);
    }
}
