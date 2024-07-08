import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public class SleepingTeachingAssistant {

    // Number of students and chairs
    private static final int NUMBER_STUDENTS = 6;
    private static final int NUMBER_CHAIRS = 3;

    // Semaphore for waking up the TA
    private static final Semaphore taSemaphore = new Semaphore(0);

    // Lock for accessing the waiting queue
    private static final Lock queueLock = new ReentrantLock();

    // Queue to keep track of waiting students
    private static final Queue<Integer> waitingStudents = new LinkedList<>();

    // Counter to track the number of students needing help (assume that each student only ask for help once)
    private static int studentsNeedingHelp = NUMBER_STUDENTS;

    public static void main(String[] args) {
        Thread taThread = new Thread(new TA());
        taThread.start();

        for (int i = 0; i < NUMBER_STUDENTS; i++) {
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

                    Integer studentId = null;
                    queueLock.lock();
                    try {
                        if (!waitingStudents.isEmpty()) {
                            studentId = waitingStudents.poll();
                        }
                    } finally {
                        queueLock.unlock();
                    }

                    if (studentId != null) {
                        System.out.println("TA is helping student " + studentId);
                        // Simulate time helping the student
                        Thread.sleep(new Random().nextInt(2222) + 1000);
                        System.out.println("TA finished helping student " + studentId);

                        queueLock.lock();
                        try {
                            studentsNeedingHelp--;
                            if (studentsNeedingHelp == 0 && waitingStudents.isEmpty()) {
                                System.out.println("TA done helping all students. TA goes back to nap.");
                                break;
                            } else if (!waitingStudents.isEmpty()) {
                                taSemaphore.release();  // Wake up again to help the next student
                            }
                        } finally {
                            queueLock.unlock();
                        }
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
                    Thread.sleep(new Random().nextInt(3333) + 1000);
                    System.out.println("Student " + studentId + " needs help");

                    boolean gotHelp = false;

                    queueLock.lock();
                    try {
                        if (waitingStudents.size() < NUMBER_CHAIRS) {
                            System.out.println("Student " + studentId + " is waiting in the hallway");
                            waitingStudents.add(studentId);
                            taSemaphore.release();  // Wake up the TA if necessary
                            gotHelp = true;
                        } else {
                            System.out.println("No chairs available. Student " + studentId + " will continue programming and will try later");
                        }
                    } finally {
                        queueLock.unlock();
                    }

                    if (gotHelp) {
                        break;
                    }

                    // Wait some time before trying to ask the TA again again if the student didn't get help
                    Thread.sleep(new Random().nextInt(5000) + 1000);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
