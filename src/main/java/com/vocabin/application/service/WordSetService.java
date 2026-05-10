package com.vocabin.application.service;

import com.vocabin.domain.wordset.WordSet;

import java.util.List;

public interface WordSetService {
    List<WordSet> getWordSets();
    WordSet createWordSet(String name);
}
