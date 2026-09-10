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
    private static final String UNDERLINE = "\u001B[4m";
    private static final String DARK_GRAY = "\u001B[90m";
    
    private static String[] attempts = null;
    private static int attemptCount = 0;
    private static long startTime = 0;
    private static String answer = "";
    private static int chosenWordLength = 5;
    private static long seed;
    
    public static void main(String[] args) {
        PrintStream out = System.out;
        Set<String> words = new HashSet<>();
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


        attempts = new String[20];
        seed = new Random().nextLong(Long.MIN_VALUE, Long.MAX_VALUE);
        //seed = 1;
        answer = selectAnswer(seed, words);

        gameLoop(out, words);
        ending(out);
    }

    private static void gameLoop(PrintStream out, Set<String> words) {
        startTime = System.currentTimeMillis();
        String attempt = "";
        Scanner scanner = new Scanner(System.in);
        while (!attempt.equals(answer) && attemptCount < attempts.length) {
            boolean validAttempt = false;
            while (!validAttempt && !attempt.contains(" ")) {
                clearOutputStream(out);
                displayGame(out, words);
                attempt = scanner.nextLine().toUpperCase();
                validAttempt = validateAsAttempt(attempt, words);
            }
            if (!validAttempt) {
                break;   
            }
            attempts[attemptCount] = attempt;
            attemptCount++;
        }
        clearOutputStream(out);
        displayGame(out, words);
        scanner.close();
    }

    private static void ending(PrintStream out) {
        out.println("Answer: " + answer);
        out.println("Seed: " + seed);
        out.println("Guesses: " + attemptCount);
        Duration duration = Duration.ofMillis(System.currentTimeMillis() - startTime);
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();
        out.println("Time: " + String.format("%02d:%02d:%02d", hours, minutes, seconds));
        out.println("\n");
    }

    private static void displayGame(PrintStream out, Set<String> words) {
        char letter = 0;
        for (int i = 0; i < attempts.length; i++) {
            for (int j = 0; j < chosenWordLength; j++) {
                out.print(COLOR_RESET + " " + UNDERLINE);
                if (attempts[i] != null) { 
                    letter = attempts[i].charAt(j);

                    out.print(getLetterColor(letter, j, answer));

                    out.print(letter + COLOR_RESET);
                } else {
                    out.print(DARK_GRAY + " " + COLOR_RESET);
                }
            }
            out.print("\n\n");
        }
    }

    private static String getLetterColor(char letter, int position, String answer) {
        final String GREEN_BG = "\u001B[42m\u001B[30m";
        final String YELLOW_BG = "\u001B[43m\u001B[30m";

        if (letter == answer.charAt(position)) return GREEN_BG;
        if (answer.contains(String.valueOf(letter))) return YELLOW_BG;
        return "";
    }

    private static boolean validateAsAttempt(String attempt, Set<String> words) {
        return words.contains(attempt);
    }

    private static String selectAnswer(long seed, Set<String> words) {
        List<String> wordList = new ArrayList<>(words);
        return wordList.get(new Random(seed).nextInt(wordList.size()));
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
            .width(20)
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
        try { Thread.sleep(0);
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