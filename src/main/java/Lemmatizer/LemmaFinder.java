package Lemmatizer;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import javax.persistence.criteria.CriteriaBuilder;
import java.io.IOException;
import java.util.*;

public class LemmaFinder {
    private final LuceneMorphology luceneMorphology = new RussianLuceneMorphology();
    private static final String[] particlesNames = new String[]{"МЕЖД", "ПРЕДЛ", "СОЮЗ"};
    private static final HashMap<String, Integer> lemmas = new HashMap<>();

    public LemmaFinder() throws IOException {
    }

    public static void main(String[] args) throws IOException {
       LemmaFinder lemma = new LemmaFinder();
       lemma.collectLemmas("Брат брата не выдаст");
    }
    public void collectLemmas(String text) {
        String[] words = text.toLowerCase().replaceAll("([^а-я\\s])", " ").split("\\s+");

        for (String word : words) {

            List<String> wordBaseForms = luceneMorphology.getMorphInfo(word);
            if (anyWordBaseBelongToParticle(wordBaseForms)) {
                continue;
            }

            List<String> normalForms = luceneMorphology.getNormalForms(word);

            String normalWord = normalForms.get(0);

            if (lemmas.containsKey(normalWord)) {
                lemmas.put(normalWord, lemmas.get(normalWord) + 1);
            } else {
                lemmas.put(normalWord, 1);
            }
        }

        printAllLemmas();
    }


    private boolean anyWordBaseBelongToParticle(List<String> wordBaseForms) {
        return wordBaseForms.stream().anyMatch(this::hasParticleProperty);
    }

    private boolean hasParticleProperty(String wordBase) {
        for (String property : particlesNames) {
            if (wordBase.toUpperCase().contains(property)) {
                return true;
            }
        }
        return false;
    }

    private void printAllLemmas() {
        for (Map.Entry<String, Integer> w : lemmas.entrySet()) {
            System.out.println(w.getKey() + " - " + w.getValue());
        }
    }
}