package com.pingyougou.redis.trans;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class Temptest {

    /**
     * sun.misc.Version 类会在JDK类库的初始化过程中被加载并初始化，
     * 而在初始化时它需要对静态常量字段根据指定的常量值（ConstantValue）做默认初始化，
     * 此时被 sun.misc.Version.launcher 静态常量字段所引用的"java"字符串
     * 字面量就被intern到HotSpot VM的字符串常量池——StringTable里了。
     */
    @Test
    public void test1() {
        String s1 = new StringBuilder("go")
                .append("od").toString();
        String intern1 = s1.intern();
        System.out.println("s1.intern()="+intern1);
        System.out.println(s1.intern() == s1);
        String s2 = new StringBuilder("ja")
                .append("va").toString();
        String intern = s2.intern();
        System.out.println("s2.intern()="+intern);
        System.out.println(s2.intern() == s2);
    }

    @Test
    public void test2 () {
        String s1 = "Programming";
        String s2 = new String("Programming");
        String s3 = "Program";
        String s4 = "ming";
        String s5 = "Program" + "ming";
        String s6 = s3 + s4;
        System.out.println(s1 == s2);
        System.out.println(s1 == s5);
        System.out.println(s1 == s6);
        System.out.println(s1 == s6.intern());
        System.out.println(s2 == s2.intern());
    }

    @Test
    public void test3() {
        System.out.println(reverse("abdcef"));
    }

    public static String reverse(String originStr) {
        if(originStr == null || originStr.length() <= 1)
            return originStr;
        System.out.println(originStr.substring(1));
        return reverse(originStr.substring(1)) + originStr.charAt(0);
    }

    @Test
    public void test4 () throws Exception {
            try {
                try {
                    throw new Sneeze();
                }
                catch ( Annoyance a ) {
                    System.out.println("Caught Annoyance");
                    throw a;
                }
            }
            catch ( Sneeze s ) {
                System.out.println("Caught Sneeze");
                return ;
            }
            finally {
                System.out.println("Hello World!");
            }
    }

    @Test
    public void test5() {
        System.out.println(countWordInFile("G:/test.txt", "abc"));
    }

    /**
     * 要求列出当前文件夹下的文件
     */
    @Test
    public void test6() {
        File f = new File("G:/");
        for(File temp : f.listFiles()) {
            if(temp.isFile()) {
                System.out.println(temp.getName());
            }
        }
    }

    /**
     * 统计给定文件中给定字符串的出现次数
     *
     * @param filename  文件名
     * @param word 字符串
     * @return 字符串在文件中出现的次数
     */
    public static int countWordInFile(String filename, String word) {
        int counter = 0;
        try (FileReader fr = new FileReader(filename)) {
            try (BufferedReader br = new BufferedReader(fr)) {
                String line = null;
                while ((line = br.readLine()) != null) {
                    int index = -1;
                    while (line.length() >= word.length() && (index = line.indexOf(word)) >= 0) {
                        counter++;
                        line = line.substring(index + word.length());
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return counter;
    }


}

class Annoyance extends Exception {}
class Sneeze extends Annoyance {}
