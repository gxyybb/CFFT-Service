package com.example.cfft;

import java.util.HashSet;
import java.util.Set;

public class Test {
    public static void main(String[] args) {
//        Set set = new HashSet<>();
//        for (short i = 0; i<100; i++){
//            set.add(i);
//            set.remove(i-1);
//        }
//        System.out.println(set.size());
            int x = 4;
            System.out.println("value is "+ ((x>4)?new Base():9));

    }

}
class NULL {

    public static void print(){
        System.out.println("MTDP");
    }
    public static void main(String[] args) {
        try{
            ((NULL)null).print();
        }catch(NullPointerException e){
            System.out.println("NullPointerException");
        }
    }
}
class Base{
    private void test(){
        String aStr = "?One?";
        String bStr = aStr;
        aStr.toUpperCase();
        aStr.trim();
    }
}

class  X implements Runnable{
    private int x;
    private int y;

    public static void main(String[] args) {
        X that = new X();
        (new Thread(that)).start();
        (new Thread(that)).start();
    }

    @Override
    public synchronized void run() {
        for (;;){
            x++;
            y++;
            System.out.println("x="+x+",y="+y);
        }
    }
}
class TryCatchExample {
    public static void main(String[] args) {
        try {

            int result = 10 / 0;  // 这行代码会抛出一个 ArithmeticException
        } catch (ArithmeticException e) {
            System.out.println("Caught ArithmeticException: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Caught Exception: " + e.getMessage());
        } finally {
            System.out.println("In finally block");
        }
        System.out.println("After try-catch-finally");
    }
}