import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

// spamcity is the average number a word appears in spam mails

final class Dictionary {
    private final List<String[]> data;
    private List<String[]> testSet;
    private List<String[]> trainSet;
    private final float priorSpamProbability;
    private final float priorHamProbability;
    // String: word, Float: spam probability
    private HashMap<String, Float> wordSpamcities;
    // String: word, Float: ham probability
    private HashMap<String, Float> wordHamcities;
    private HashMap<String, Float> generalWordOccurrence;
    // String: word, Float: likelihood ratio of a word
    private HashMap<String, Float> wordLikelihoodRatios;
    private HashMap<String, Integer> generalWordFrequencies;
    // Integer: word, Float: number of times a word is included in a spam mail at least once
    private HashMap<String, Integer> wordSpamFrequencies;
    // Integer: word, Float: number of times a word is included in a ham mail at least once
    private HashMap<String, Integer> wordHamFrequencies;

    Dictionary() {
        // read CSV email dataset
        final String file = "emails.csv";
        data = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                data.add(line.split(","));
            }
        } catch (Exception e) {
        }
        // calculate prior probabilities
        int total_n_of_mail = 0;
        int n_spam_mail = 0;
        for (int i = 1; i < data.size(); i++) {
            total_n_of_mail++;
            if (Objects.equals(data.get(0)[i], "1")) {
                n_spam_mail++;
            }
        }
        int n_ham_mail = total_n_of_mail - n_spam_mail;
        priorSpamProbability = (float) n_spam_mail / total_n_of_mail;
        priorHamProbability = 1 - priorSpamProbability;
        // initialize word hash maps
        for (int i = 1; i < data.get(0).length; i++) {
            wordSpamFrequencies.put(data.get(0)[i], 0);
            wordHamFrequencies.put(data.get(0)[i], 0);
            generalWordFrequencies.put(data.get(0)[i], 0);
        }
        // count word occurrences in spam mail
        for (int i = 1; i < data.size(); i++) {
            for (int j = 1; j < data.get(i).length; j++) {
                // if the mail is spam
                if (Objects.equals(data.get(0)[j], "1")) {
                    // if the the word is used in it more than once
                    if (!Objects.equals(data.get(i)[j], "0")) {
                        // then increment its word count
                        wordSpamFrequencies.put(data.get(0)[j], wordSpamFrequencies.get(data.get(0)[j]) + 1);
                    }
                } else {
                    if (!Objects.equals(data.get(i)[j], "0")) {
                        // then increment its word count
                        wordHamFrequencies.put(data.get(0)[j], wordHamFrequencies.get(data.get(0)[j]) + 1);
                    }
                    generalWordFrequencies.put(data.get(0)[j], generalWordFrequencies.get(data.get(0)[j]) + 1);
                }
            }
        }
        // calculate spam and ham probabilities of each word
        for (int i = 1; i < data.size(); i++) {
            wordSpamcities.put(data.get(0)[i], (float) wordSpamFrequencies.get(data.get(0)[i]) / n_spam_mail);
            wordHamcities.put(data.get(0)[i], (float) wordHamFrequencies.get(data.get(0)[i]) / n_ham_mail);
            generalWordOccurrence.put(data.get(0)[i], (float) generalWordFrequencies.get(data.get(0)[i]) / total_n_of_mail);
        }
        // calculate the likelihood ratio of each word
        for (int i = 1; i < data.size(); i++) {
            wordLikelihoodRatios.put(data.get(0)[i], wordSpamcities.get(data.get(0)[i]) / generalWordOccurrence.get(data.get(0)[i]));
        }
    }

    String getWordByIndex(int index) {
        return data.get(0)[index];
    }

    float calcPosteriorSpamProbability(String mail) {
        List<String> words = Arrays.asList(mail.split(","));
        float numerator = 1.0f;
        float denominator = 1.0f;
        for (int i = 0; i < words.size(); i++) {
            for (String key : wordSpamcities.keySet()) {
                if (Objects.equals(words.get(i), key)) {
                    numerator *= wordSpamcities.get(words.get(i));
                    denominator *= generalWordOccurrence.get(words.get(i));
                }
            }
        }
        numerator *= priorSpamProbability;
        denominator *= priorHamProbability;
        if (numerator == 0) {
            return 0;
        } else {
            return numerator / (numerator + denominator);
        }
    }
}
