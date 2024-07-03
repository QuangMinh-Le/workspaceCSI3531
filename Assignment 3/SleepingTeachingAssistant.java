import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public class SleepingTeachingAssistant {

    // Number of students and chairs
    private static final int NUM_STUDENTS = 5;
    private static final int NUM_CHAIRS = 3;

    // Semaphore for waking up the TA
    private static final Semaphore taSemaphore = new Semaphore(0);

    // Lock for accessing the waiting queue
    private static final Lock queueLock = new ReentrantLock();

    // Queue to keep track of waiting students
    private static final Queue<Integer> waitingStudents = new LinkedList<>();

    public static void main(String[] args) {
        Thread taThread = new Thread(new TA());
        taThread.start();

        for (int i = 0; i < NUM_STUDENTS; i++) {
            Thread studentThread = new Thread(new Student(i));
            studentThread.start();
        }
    }

    static class TA implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    // TA waits to be woken up by a student
                    taSemaphore.acquire();

                    int studentId;
                    queueLock.lock();
                    try {
                        studentId = waitingStudents.poll();
                    } finally {
                        queueLock.unlock();
                    }

                    if (studentId != -1) {
                        System.out.println("TA is helping student " + studentId);
                        // Simulate helping the student
                        Thread.sleep(new Random().nextInt(3000) + 1000);
                        System.out.println("TA finished helping student " + studentId);
                    }

                    queueLock.lock();
                    try {
                        if (!waitingStudents.isEmpty()) {
                            taSemaphore.release();  // Wake up again to help the next student
                        }
                    } finally {
                        queueLock.unlock();
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static class Student implements Runnable {
        private final int studentId;

        Student(int studentId) {
            this.studentId = studentId;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    // Simulate programming
                    Thread.sleep(new Random().nextInt(5000) + 1000);
                    System.out.println("Student " + studentId + " needs help");

                    queueLock.lock();
                    try {
                        if (waitingStudents.size() < NUM_CHAIRS) {
                            System.out.println("Student " + studentId + " is waiting in the hallway");
                            waitingStudents.add(studentId);
                            taSemaphore.release();  // Wake up the TA if necessary
                        } else {
                            System.out.println("No chairs available. Student " + studentId + " will try later");
                        }
                    } finally {
                        queueLock.unlock();
                    }

                    // Wait some time before trying again
                    Thread.sleep(new Random().nextInt(5000) + 1000);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
