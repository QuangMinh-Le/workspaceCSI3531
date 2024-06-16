import java.util.Scanner;

class FibGenerator extends Thread {
    private int[] fibNumbers;
    private int n;

    public FibGenerator(int n, int[] fibNumbers) {
        this.n = n;
        this.fibNumbers = fibNumbers;
    }

    @Override
    public void run() {
        if (n > 0) {
            fibNumbers[0] = 0;
        }
        if (n > 1) {
            fibNumbers[1] = 1;
        }
        for (int i = 2; i < n; i++) {
            fibNumbers[i] = fibNumbers[i - 1] + fibNumbers[i - 2];
        }
    }
}

public class FibonacciMultiThreaded {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Punch in the number of Fibonacci numbers: ");
        int n = scanner.nextInt();

        int[] fibNumbers = new int[n];
        FibGenerator generator = new FibGenerator(n, fibNumbers);
        
        generator.start();
        
        try {
            generator.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        System.out.println("Fibonacci sequence:");
        for (int i = 0; i < fibNumbers.length; i++) {
            if (i == fibNumbers.length - 1) {
                System.out.print(fibNumbers[i]);
            } else {
                System.out.print(fibNumbers[i] + ", ");
            }   
        }
    }
}
