package com.vocabin.domain.listening;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Caption lines are cut every 2-3 seconds, often mid-sentence. This regroups them into sentences so
// a learner can pick (and the AI can read) one whole sentence.
public final class TranscriptSentences {

    // Captions without punctuation (auto-generated) are cut at pauses or after this many words
    static final int MAX_WORDS_UNPUNCTUATED = 20;
    // Guard against a run-on sentence swallowing a whole paragraph
    static final int MAX_WORDS_PUNCTUATED = 45;
    static final double PAUSE_SECONDS = 1.5;

    // Sentence end: . ? ! (optionally followed by a closing quote/bracket) and then whitespace
    private static final Pattern BOUNDARY = Pattern.compile("[.?!][\"')\\]]*(?=\\s)");
    private static final Pattern ENDS_SENTENCE = Pattern.compile("[.?!][\"')\\]]*$");
    private static final Set<String> ABBREVIATIONS = Set.of(
            "mr", "mrs", "ms", "dr", "st", "jr", "sr", "vs", "etc", "e.g", "i.e", "u.s", "u.k", "a.m", "p.m", "no");

    private TranscriptSentences() {
    }

    public static List<TranscriptSentence> from(List<TranscriptLine> lines) {
        boolean punctuated = isPunctuated(lines);
        int maxWords = punctuated ? MAX_WORDS_PUNCTUATED : MAX_WORDS_UNPUNCTUATED;

        List<TranscriptSentence> sentences = new ArrayList<>();
        StringBuilder text = new StringBuilder();
        double start = 0;
        double end = 0;
        double previousLineEnd = -1;

        for (TranscriptLine line : lines) {
            if (text.length() > 0 && line.start() - previousLineEnd > PAUSE_SECONDS) {
                sentences.add(new TranscriptSentence(start, end, text.toString()));
                text.setLength(0);
            }
            for (Piece piece : split(line)) {
                if (text.length() == 0) start = piece.start();
                else text.append(' ');
                text.append(piece.text());
                end = piece.end();
                if (piece.endsSentence() || wordCount(text) >= maxWords) {
                    sentences.add(new TranscriptSentence(start, end, text.toString()));
                    text.setLength(0);
                }
            }
            previousLineEnd = line.start() + line.duration();
        }
        if (text.length() > 0) {
            sentences.add(new TranscriptSentence(start, end, text.toString()));
        }
        return sentences;
    }

    private record Piece(String text, double start, double end, boolean endsSentence) {}

    // Splits one caption line at sentence ends; times inside the line are interpolated by character position
    private static List<Piece> split(TranscriptLine line) {
        String text = line.text().trim();
        List<Piece> pieces = new ArrayList<>();
        if (text.isEmpty()) return pieces;

        int from = 0;
        Matcher matcher = BOUNDARY.matcher(text);
        while (matcher.find()) {
            int to = matcher.end();
            if (isAbbreviation(text, matcher.start())) continue;
            addPiece(pieces, line, text, from, to, true);
            from = to;
        }
        if (from < text.length()) {
            addPiece(pieces, line, text, from, text.length(), endsWithSentenceEnd(text));
        }
        return pieces;
    }

    private static void addPiece(List<Piece> pieces, TranscriptLine line, String text, int from, int to, boolean endsSentence) {
        String pieceText = text.substring(from, to).trim();
        if (pieceText.isEmpty()) return;
        double length = text.length();
        pieces.add(new Piece(
                pieceText,
                line.start() + line.duration() * (from / length),
                line.start() + line.duration() * (to / length),
                endsSentence));
    }

    private static boolean endsWithSentenceEnd(String text) {
        Matcher end = ENDS_SENTENCE.matcher(text);
        if (!end.find()) return false;
        return text.charAt(end.start()) != '.' || !isAbbreviation(text, end.start());
    }

    // "Mr." / "U.S." / single initials like "J." do not end a sentence
    private static boolean isAbbreviation(String text, int dotIndex) {
        int wordStart = dotIndex;
        while (wordStart > 0 && !Character.isWhitespace(text.charAt(wordStart - 1))) wordStart--;
        String word = text.substring(wordStart, dotIndex).toLowerCase(Locale.ROOT).replaceAll("^[\"'(\\[]+", "");
        if (word.length() == 1 && Character.isLetter(word.charAt(0)) && Character.isUpperCase(text.charAt(dotIndex - 1))) {
            return true;
        }
        return ABBREVIATIONS.contains(word);
    }

    private static boolean isPunctuated(List<TranscriptLine> lines) {
        if (lines.isEmpty()) return false;
        long withPunctuation = lines.stream().filter(l -> l.text().matches(".*[.?!].*")).count();
        return withPunctuation * 5 >= lines.size(); // at least 20% of lines carry sentence punctuation
    }

    private static int wordCount(CharSequence text) {
        String trimmed = text.toString().trim();
        return trimmed.isEmpty() ? 0 : trimmed.split("\\s+").length;
    }
}
