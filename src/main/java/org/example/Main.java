package org.example;

import java.util.Random;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static final Map<Integer, Integer> sizeToFreq = new HashMap<>();
    public static final String instructions = "RLRFR";
    public static final int routeLength = 100;
    public static final int numThreads = 1000;

    public static void main(String[] args) {

        Thread leaderThread = new Thread(new LeaderPrinter()); // Создаем отдельный поток для вывода на экран лидера в мапу sizeToFreq
        leaderThread.start();

        // Создание и запуск потоков
        Thread[] threads = new Thread[numThreads];
        for (int i = 0; i < numThreads; i++) {
            threads[i] = new Thread(new RouteAnalyzerTask(leaderThread));
            threads[i].start();
        }

        // Ждем завершения всех потоков
        try {
            for (int i = 0; i < numThreads; i++) {
                threads[i].join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Прерываем поток печати лидера
        leaderThread.interrupt();

    }

    static class RouteAnalyzerTask implements Runnable {
        private Thread leaderThread;

        public RouteAnalyzerTask(Thread leaderThread) {
            this.leaderThread = leaderThread;
        }
        @Override
        public void run() {

            String route = generateRoute(instructions, routeLength);
            int rightTurnCount = countRightTurns(route);

            // Синхронизируем потоки
            synchronized (sizeToFreq) {
                int currentCount = sizeToFreq.getOrDefault(rightTurnCount, 0);
                sizeToFreq.put(rightTurnCount, currentCount + 1); // увеличим значение для получения частоты в ней на 1

                sizeToFreq.notify();// Отправляем сигнал печатающему максимумы потоку
            }
        }

        public String generateRoute(String letters, int length) {
            Random random = new Random();
            StringBuilder route = new StringBuilder();
            for (int i = 0; i < length; i++) {
                route.append(letters.charAt(random.nextInt(letters.length())));
            }
            return route.toString();
        }

        public int countRightTurns(String route) {
            return route.length() - route.replace("R", "").length();
        }
    }
    static class LeaderPrinter implements Runnable {
        @Override
        public void run() {
            while (!Thread.interrupted()) {
                synchronized (sizeToFreq) {
                    try {
                        // Ждем сигнала от других потоков
                        sizeToFreq.wait();
                    } catch (InterruptedException e) {
                        // Поток прерван, выходим из цикла
                        break;
                    }

                    // Находим лидера
                    int mostFrequentSize = 0;
                    int mostFrequentCount = 0;
                    for (Map.Entry<Integer, Integer> entry : sizeToFreq.entrySet()) {
                        int size = entry.getKey();
                        int count = entry.getValue();
                        if (count > mostFrequentCount) {
                            mostFrequentSize = size;
                            mostFrequentCount = count;
                        }
                    }

                    // Выводим лидера
                    System.out.println("Текущий лидер: " + mostFrequentSize + " (встретился " + mostFrequentCount + " раз)");
                }
            }
        }
    }
}
