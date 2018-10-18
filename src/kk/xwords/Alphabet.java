package kk.xwords;

import java.util.Arrays;

public class Alphabet {
    private final char[] alphabetChars;
    private final short[] lookUp = new short[Character.MAX_VALUE];


    public Alphabet(char[] alphabetChars) {
        for (char ch : alphabetChars)
            if (!Character.isLetterOrDigit(ch))
                throw new IllegalArgumentException("invalid char: (int) " + (int) ch);
        this.alphabetChars = alphabetChars;
        int len = length();
        Arrays.fill(lookUp, (short) -1);
        for (int i = 0; i < len; i++)
            lookUp[alphabetChars[i]] = (short) i;
    }

    public char[] getCopy() {
        return alphabetChars.clone();
    }

    public final char get(int n) {
        return alphabetChars[n];
    }

    public final int index(char ch) {
        return lookUp[ch];
    }

    public boolean contains(char ch) {
        return lookUp[ch] != -1;
    }

    public final int length() {
        return alphabetChars.length;
    }
}
