package com.pixplicity.cryptogram;

import android.annotation.SuppressLint;

import com.pixplicity.cryptogram.models.Cryptogram;
import com.pixplicity.cryptogram.models.CryptogramProgress;
import com.pixplicity.cryptogram.stringsimilarity.Levenshtein;
import com.pixplicity.cryptogram.utils.CryptogramProvider;

import org.junit.Test;

import java.util.HashMap;
import java.util.Locale;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class CryptogramTest {

    private static final boolean VERBOSE = false;

    @Test
    public void validProvider() throws Exception {
        System.out.println("Total puzzles: " + CryptogramProvider.getInstance(null).getCount());
    }

    @Test
    public void validCryptogramMapping() throws Exception {
        for (long seed = 0L; seed < 100L; seed++) {
            if (VERBOSE) {
                System.out.print("seed " + seed + ":");
            }
            CryptogramProgress.setRandomSeed(seed);
            Cryptogram cryptogram = new Cryptogram.Mock();
            CryptogramProgress progress = new CryptogramProgress();
            HashMap<Character, Character> mapping = progress.getCharMapping(cryptogram);
            for (Character key : mapping.keySet()) {
                Character value = mapping.get(key);
                if (VERBOSE) {
                    System.out.print("  " + key + "/" + value);
                }
                if (key.equals(value)) {
                    throw new AssertionError("Key and value maps to same character for seed " + seed);
                }
            }
            if (VERBOSE) {
                System.out.println();
            }
        }
    }

    @Test
    public void noEmptyOrDuplicateCryptograms() throws Exception {
        Levenshtein levenshtein = new Levenshtein();
        @SuppressLint("UseSparseArrays") HashMap<Integer, Cryptogram> hashes = new HashMap<>();
        for (Cryptogram cryptogram : CryptogramProvider.getInstance(null).getAll()) {
            int id = cryptogram.getId();
            String text = cryptogram.getText();
            String author = cryptogram.getAuthor();
            if (VERBOSE) {
                System.out.println("cryptogram " + cryptogram);
            }
            // Ensure there's content
            if (text.trim().length() == 0) {
                throw new AssertionError("No content: " + cryptogram);
            }
            // Ensure there aren't single quotes (replace with ’)
            if (text.indexOf('\'') >= 0) {
                throw new AssertionError("Contains single quote; replace with '’': " + cryptogram);
            }
            // Ensure there aren't simple hyphens (replace with —)
            if (text.contains(" - ")) {
                throw new AssertionError("Contains simple hyphen; replace with '—': " + cryptogram);
            }
            // Ensure there aren't simple hyphens (replace with —)
            if (text.contains("...")) {
                throw new AssertionError("Contains expanded ellipsis; replace with '…': " + cryptogram);
            }
            if (!cryptogram.isInstruction()) {
                // Ensure there's an author
                if (author == null || author.trim().length() == 0) {
                    throw new AssertionError("No author: " + cryptogram);
                }
            }
            // Ensure there aren't simple hyphens (replace with —)
            String given = cryptogram.getGiven();
            if (given != null && !given.equals(given.toUpperCase(Locale.ENGLISH))) {
                throw new AssertionError("Contains lowercase given characters: " + cryptogram);
            }
            // Ensure there aren't duplicates
            for (Cryptogram otherCryptogram : hashes.values()) {
                double distance = levenshtein.distance(text, otherCryptogram.getText());
                if (distance < 10) {
                    throw new AssertionError("Levenshtein distance of " + cryptogram + " is " + distance + " to " + otherCryptogram);
                }
            }
            hashes.put(id, cryptogram);
        }
    }

}
