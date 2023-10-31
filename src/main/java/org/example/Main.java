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

        // Создание и запуск потоков
        Thread[] threads = new Thread[numThreads];
        for (int i = 0; i < numThreads; i++) {
            threads[i] = new Thread(new RouteAnalyzerTask());
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

        // Анализируем результаты
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

        // Выводим результаты
        System.out.println("Самое частое количество повторений " + mostFrequentSize + " (встретилось " + mostFrequentCount + " раз)");
        System.out.println("Другие размеры:");
        for (Map.Entry<Integer, Integer> entry : sizeToFreq.entrySet()) {
            if (entry.getKey() != mostFrequentSize) {
                System.out.println("- " + entry.getKey() + " (" + entry.getValue() + " раз)");
            }
        }
    }

    static class RouteAnalyzerTask implements Runnable {
        @Override
        public void run() {
            String route = generateRoute(instructions, routeLength);
            int rightTurnCount = countRightTurns(route);

            // Синхронизируем потоки
            synchronized (sizeToFreq) {
                int currentCount = sizeToFreq.getOrDefault(rightTurnCount, 0);
                sizeToFreq.put(rightTurnCount, currentCount + 1); // увеличим значение для получения частоты в ней на 1
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
}