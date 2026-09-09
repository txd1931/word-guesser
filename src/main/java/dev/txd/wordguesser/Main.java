package dev.txd.wordguesser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

public class Main {
    private static final String DICTIONARY = "palavras.txt";
    private static final String HELP = "help.txt";

    private static final String COLOR_RESET = "\u001B[0m";
    private static final String GREEN_BG = "\u001B[42m\u001B[30m";
    private static final String YELLOW_BG = "\u001B[43m\u001B[30m";
    private static final String GRAY_BG = "\u001B[100m\u001B[37m";
    private static final String RED = "\u001B[31m";
    
    private static String[] attempts = null;
    private static int attemptCount = 0;
    private static long startTime = 0;
    private static String answer = "";
    private static int chosenWordLength = 5;
    private static long seed;
    
    private static Set<String> words = new HashSet<>();
    public static void main(String[] args) {
        PrintStream out = System.out;
        clearOutputStream(out);
        if (args.length == 1) {
            try { 
                if (args[0].toLowerCase().equals("-help")) outputHelpMessage(HELP, out);
            } catch (IOException e) { out.println("No help message found");}
            return;
        } 
        try {
            words = fetchDictionary(DICTIONARY, chosenWordLength, out);
        } catch (IOException e) {
            System.err.println("Could not load internal dictionary: " + e.getMessage());
            System.exit(1);    
        }


        attempts = new String[6];
        seed = new Random().nextLong(Long.MAX_VALUE);
        selectAnswer(seed);

        gameLoop(out);
        ending(out);
    }

    private static void gameLoop(PrintStream out) {
        startTime = System.currentTimeMillis();
        String attempt = "";
        boolean validAttempt = false;
        Scanner scanner = new Scanner(System.in);
        while (attempt != answer && attemptCount < attempts.length) {
            while (!validAttempt && !attempt.equals("-")) {
                clearOutputStream(out);
                displayGame(out);
                attempt = scanner.nextLine();
                validAttempt = validateAsAttempt(attempt);
            }
            attempts[attemptCount] = attempt;
            attemptCount++;
        }
        scanner.close();
    }

    private static void ending(PrintStream out) {
        out.println("Answer: " + answer);
        out.println("seed: " + seed);
        Duration duration = Duration.ofMillis(System.currentTimeMillis() - startTime);
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();
        out.println("Time: " + String.format("%02d:%02d:%02d", hours, minutes, seconds));
    }

    private static void displayGame(PrintStream out) {
        for (int i = 0; i < attempts.length; i++) {
            for (int j = 0; j < chosenWordLength; j++) {
                if (attempts[i] != null) { 
                    out.print(attempts[i].charAt(j));
                } else {
                    out.print(GRAY_BG + " " + COLOR_RESET);
                }
            }
            out.print("\n");
        }
    }

    private static boolean validateAsAttempt(String attempt) {
        return true;
    }

    private static void selectAnswer(long seed) {
        List<String> wordList = new ArrayList<>(words);
        answer = wordList.get(new Random(seed).nextInt(wordList.size()));
    }

    private static String sanitizeWord(String word) {
        if (word == null) return "";
        String normalizedWord = Normalizer.normalize(word.trim(), Normalizer.Form.NFD).replaceAll("\\p{M}", "").toUpperCase();
        return normalizedWord;        
    }

    private static Set<String> fetchDictionary(String source, int wordLenth, PrintStream out) throws IOException {
        InputStream inputStream = Main.class.getClassLoader().getResourceAsStream(source);
        if (inputStream == null) throw new IOException("InputStream could not be instantiated");

        ProgressBar bar = new ProgressBar.Builder()
            .label("Fetching dictionary:")
            .width(50)
            .hasBorders(false)
            .totalValue(inputStream.available())
            .out(out)
            .build();

        Set<String> dictionary = new HashSet<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        String line;
        
        System.out.print("\033[s");
        while ((line = reader.readLine()) != null) {
            bar.advanceBy(line.getBytes(StandardCharsets.UTF_8).length + 1);
            String cleanWord = sanitizeWord(line);
            if (cleanWord.isBlank() || !cleanWord.matches("[A-Z]+") || cleanWord.length() != chosenWordLength) continue;
            dictionary.add(cleanWord);
        }
        try { Thread.sleep(1000);
        } catch (InterruptedException e) { e.printStackTrace(); }
        clearOutputStream(out);
        return dictionary;
    }

    private static void outputHelpMessage(String source, PrintStream out) throws IOException {
        InputStream inputStream = Main.class.getClassLoader().getResourceAsStream(source);
        if (inputStream == null) throw new IOException("InputStream could not be instantiated");
        out.println(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));
    }

    private static void clearOutputStream(PrintStream out) {
        out.print("\033[H\033[2J");
        out.flush();    
    }
}