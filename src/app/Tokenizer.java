package com.example.emergencyfilter;

public class Tokenizer {

    private static final int MAX_LEN = 40;

    public static int[][] tokenize(String text) {

        int[] tokens = new int[MAX_LEN];

        String[] words = text.toLowerCase().split(" ");

        for (int i = 0; i < words.length && i < MAX_LEN; i++) {
            tokens[i] = Math.abs(words[i].hashCode() % 10000);
        }

        return new int[][]{tokens};
    }
}
