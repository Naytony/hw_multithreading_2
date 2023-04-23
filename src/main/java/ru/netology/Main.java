package ru.netology;

import java.util.*;

public class Main {
    private static final Map<Integer, Integer> sizeToFreq = new HashMap<>();
    private static final int AMOUNT_ROUTES = 1000;
    private static final int ROUTE_LENGTH = 100;
    private static final String ROUTE_GENERATION_TEXT = "RLRFR";
    private static final char CHARACTER_TO_SEARCH = 'R';

    public static void main(String[] args) throws InterruptedException {
        long startTs = System.currentTimeMillis();
        List<Thread> threads = new ArrayList<>();

        Runnable taskCurrentMaximum = () -> {
            int maximumFrequency = 0;
            int currentMaximum = 0;
            while (!Thread.interrupted()) {
                synchronized (sizeToFreq) {
                    try {
                        sizeToFreq.wait();
                    } catch (InterruptedException e) {
                        return;
                    }

                    for (Map.Entry<Integer, Integer> entry : sizeToFreq.entrySet()) {
                        if (entry.getValue() > currentMaximum) {
                            currentMaximum = entry.getValue();
                            maximumFrequency = entry.getKey();
                        }
                    }
                    System.out.println("Текущий лидер частот: " + maximumFrequency);
                }
            }
        };
        Thread threadCurrentMaximum = new Thread(taskCurrentMaximum);
        threadCurrentMaximum.start();

        for (int i = 0; i < AMOUNT_ROUTES; i++) {
            Runnable task = () -> {
                // Генерация маршрута
                String route = generateRoute(ROUTE_GENERATION_TEXT, ROUTE_LENGTH);
                // Подсчёт поворотов направо в маршруте
                int numberCoincidences = (int) route.chars()
                        .filter(c -> (c == CHARACTER_TO_SEARCH))
                        .count();
                // Заполняем Map
                synchronized (sizeToFreq) {
                    if (sizeToFreq.containsKey(numberCoincidences)) {
                        int lineCounter = sizeToFreq.get(numberCoincidences) + 1;
                        sizeToFreq.put(numberCoincidences, lineCounter);
                    } else {
                        sizeToFreq.put(numberCoincidences, 1);
                    }
                    System.out.println(route + " -> " + numberCoincidences);
                    sizeToFreq.notify();
                }
            };
            // Создаём новый поток для каждой строки, сохраняем его в список и стартуем
            Thread thread = new Thread(task);
            threads.add(thread);
            thread.start();
            thread.join();
        }

        // Зависаем, ждём когда поток, объект которого лежит в thread, завершится
        for (Thread thread : threads) {
            thread.join();
        }
        // Прерываем поток
        threadCurrentMaximum.interrupt();

        // Находим наиболее частое количество повторов и сколько маршрутов содержит данное число повторов
        int meetingFrequency = 0;
        int maximumNumberRepetitions = 0;
        for (Map.Entry<Integer, Integer> entry : sizeToFreq.entrySet()) {
            int key = entry.getKey();
            int value = entry.getValue();
            if (entry.getValue() > maximumNumberRepetitions) {
                meetingFrequency = key;
                maximumNumberRepetitions = value;
            }
        }

        // Убираем найденную пару из Map для дальнейшего корректного вывода остальных повторов в цикле
        sizeToFreq.remove(meetingFrequency);

        System.out.println("\nСамое частое количество повторений " + meetingFrequency +
                " (встретилось " + maximumNumberRepetitions + " раз)" +
                "\nДругие размеры:");
        for (Map.Entry<Integer, Integer> entry : sizeToFreq.entrySet()) {
            System.out.println("- " + entry.getKey() + " (" + entry.getValue() + " раз)");
        }

        // Время работы программы
        long endTs = System.currentTimeMillis();
        System.out.println("Time: " + (endTs - startTs) + "ms");
    }

    public static String generateRoute(String letters, int length) {
        Random random = new Random();
        StringBuilder route = new StringBuilder();
        for (int i = 0; i < length; i++) {
            route.append(letters.charAt(random.nextInt(letters.length())));
        }
        return route.toString();
    }
}