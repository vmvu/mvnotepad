import java.util.Arrays;
import java.util.Stack;

/**
 * Created by vomin on 12/4/2017.
 */

public class Bu2 {


    public static byte[] binary(int a) {
        byte[] bin = new byte[8];
        Arrays.fill(bin, (byte) 0x00);
        if (a != 0) {
            int N = Math.abs(a);
            int k = 0;
            while (N != 0) {
                int kk = N & 1;
                bin[k++] = Byte.parseByte(String.valueOf(N & 1));
                N >>= 1;
            }
            for (int i = 0; i < 4; i++) {
                byte temp = bin[i];
                bin[i] = bin[7 - i];
                bin[7 - i] = temp;
            }
        }
        return bin;
    }

    public static void bu2(byte[] binArr) {
        boolean flag = false;
        for (int i = 7; i >= 0; i--) {
            if (flag) {
                binArr[i] = binArr[i] == 0 ? (byte) 1 : 0;
            }
            if (!flag && binArr[i] == 1) {
                flag = true;
            }
        }
    }

    public static int bu2To10(byte[] binArr) {
        boolean soAm = false;
        if (binArr[0] == (byte) 1) {
            bu2(binArr);
            soAm = true;
        }
        int n = 0;
        for (int i = 7; i >= 0; i--) {
            n += (int) Math.pow(2, 8 - i  - 1) * binArr[i];
        }
        return soAm ? -n : n;
    }

    public static void main(String[] Args) {
        byte[] bin = binary(128);
        for (byte b : bin) {
            System.out.print(b);
        }
        System.out.println();
        bu2(bin);//so am
        for (byte b : bin) {
            System.out.print(b);
        }
        System.out.println( "\n"+ bu2To10(bin));

    }
}
