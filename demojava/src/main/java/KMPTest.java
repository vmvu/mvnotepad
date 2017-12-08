/**
 * Created by vomin on 9/24/2017.
 */

public class KMPTest {
    static int[] bangNext_MP(String pattern){
        int []MP_map = new int[pattern.length() + 1];
        MP_map[0] = -1;
        int i = 0;
        int j = MP_map[i];
        while(i < pattern.length()){
            while(j >= 0 && (pattern.charAt(i) != pattern.charAt(j))) j=  MP_map[j];
            i++;
            j++;
            MP_map[i] = j;
        }
        return MP_map;
    }

    static int[] bangNext_KMP(String pattern){
        int m = pattern.length();
        int []KMP_map = new int[m + 1];
        int []MP_map = bangNext_MP(pattern);
        KMP_map[0] =-1;
        KMP_map[m] = MP_map[m];
        for(int i = 1; i < m; i++){
            int j=  MP_map[i];
            if(pattern.charAt(i) != pattern.charAt(j))
                KMP_map[i] = j;
            else
                KMP_map[i] = MP_map[j];
        }
        return KMP_map;
    }

    public static void test(String []Args){
        String s=  "abcadaaadbcadaa";
        String pattern  ="cadaa?";
        int []next = bangNext_KMP(pattern);
        int n = s.length();
        int m = pattern.length();
        int i = 0;
        String res="";
        for(int j = 0; j < n; j++){
            while((i >= 0 ) && (pattern.charAt(i) != s.charAt(j)))
                i = next[i];
            i++;
            if(i == m){
                res += String.valueOf((j - m + 1)) + " ";
                i = next[i];
            }
        }
        System.out.println(res);
    }
}
