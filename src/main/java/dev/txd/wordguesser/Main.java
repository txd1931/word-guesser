package dev.txd.wordguesser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.HashSet;
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
    private static String answer = "";
    private static int chosenWordLength = 5;
    
    private static Set<String> words = new HashSet<>();
    public static void main(String[] args) {
        PrintStream out = System.out;
        if (args.length == 1) {
            if (args[0] == "help" || args[0] == "-help") outputHelpMessage(HELP, out);
            return;
        } 
        try {
            words = fetchDictionary(DICTIONARY, chosenWordLength);
        } catch (IOException e) {
            System.err.println("Could not load internal dictionary: " + e.getMessage());
            System.exit(1);    
        }

        gameLoop(out);
    }

    private static void gameLoop(PrintStream out) {
        for (String word : words) {
            out.println(word);
        }
        out.println(words.size());
    }

    private static String sanitizeWord(String word) {
        if (word == null) return "";
        String normalizedWord = Normalizer.normalize(word.trim(), Normalizer.Form.NFD).replaceAll("\\p{M}", "").toUpperCase();
        return normalizedWord;
        
    }

    private static Set<String> fetchDictionary(String source, int wordLenth) throws IOException{
        InputStream is = Main.class.getClassLoader().getResourceAsStream(source);
        if (is == null) throw new IOException("InputStream could not be instantiated");
        
        Set<String> dictionary = new HashSet<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        String line;
        while ((line = reader.readLine()) != null) {
            String cleanWord = sanitizeWord(line);
            System.out.println(cleanWord);
            if (cleanWord.isBlank() || !cleanWord.matches("[A-Z]+") || cleanWord.length() != chosenWordLength) continue;
            dictionary.add(cleanWord);
        }
        return dictionary;
    }

    private static void outputHelpMessage(String source, PrintStream out) {

    }
}