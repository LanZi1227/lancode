import java.util.Scanner;

public class TestDemo {
    public static void main(String[] args) {
        int n=1;
        for (;n<=100;n++){
            int i=2;
            for(;i<=Math.sqrt(n);i++){
                if(n%i==0){
                    System.out.println("不是素数");
                    break;
                }
            }
            if(i>Math.sqrt(n)){
                System.out.println("是素数");
            }
        }

    }
}