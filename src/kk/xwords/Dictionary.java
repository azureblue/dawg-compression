package kk.xwords;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.Consumer;

public class Dictionary {


    public static void main(String[] args) throws IOException {
        final Alphabet alphabet = new Alphabet("aąbcćdeęfghijklłmnńoópqrsśtuvwyzźż".toCharArray());
//        Dictionary dict = DictionaryUtils.linesWordsFromURL("https://raw.githubusercontent.com/" +
//                        "first20hours/google-10000-english/master/google-10000-english-usa.txt",
//                new Alphabet("abcdefghijklmnopqrstuvwxyz".toCharArray()));
        Node<?> root = DictionaryUtils.loadWordsAndCompress("/media/ram/sjp-20210731/slowa.txt",
                alphabet);
//        Dictionary dict = Dictionary.loadFromAutomaton(Files.lines(Paths.get("/media/docs_ram/out.txt")).iterator());
//        new DictionaryCompressor().compress(dict);
//        DictionaryUtils.writeDictionaryAutomatonToFile(root,  alphabet, Paths.get("/media/ram/out.txt"));
    }


}
