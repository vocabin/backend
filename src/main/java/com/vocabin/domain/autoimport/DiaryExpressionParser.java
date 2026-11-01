package com.vocabin.domain.autoimport;

import com.vocabin.domain.upload.ParsedWord;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Parses the "expressions" text of an English-conversation diary, e.g. "1. go on a date — 데이트하러 가다"
public final class DiaryExpressionParser {

    private static final Pattern LINE = Pattern.compile("^\\s*\\d+\\s*[.)]\\s*(.+?)\\s+[—–-]\\s+(.+?)\\s*$");

    private DiaryExpressionParser() {
    }

    public static List<ParsedWord> parse(String expressions) {
        if (expressions == null || expressions.isBlank()) {
            return List.of();
        }
        return expressions.lines()
                .map(LINE::matcher)
                .filter(Matcher::matches)
                .map(matcher -> new ParsedWord(matcher.group(1).trim(), matcher.group(2).trim()))
                .toList();
    }
}
