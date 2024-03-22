import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

// spamcity is the average number a word appears in spam mails

final class Dictionary {
    private final List<String[]> data;
    private final float priorSpamProbability;
    private final float priorHamProbability;
    // String: word, Float: spam probability
    private final HashMap<String, Float> wordSpamcities;
    // String: word, Float: ham probability
    private final HashMap<String, Float> wordHamcities;
    private final HashMap<String, Float> generalWordOccurrence;
    // String: word, Float: likelihood ratio of a word
    private final HashMap<String, Float> wordLikelihoodRatios;
    private final HashMap<String, Integer> generalWordFrequencies;
    // Integer: word, Float: number of times a word is included in a spam mail at least once
    private final HashMap<String, Integer> wordSpamFrequencies;
    // Integer: word, Float: number of times a word is included in a ham mail at least once
    private final HashMap<String, Integer> wordHamFrequencies;
    private List<String[]> testSet;
    private List<String[]> trainSet;

    Dictionary() {
        // read CSV email dataset
        final String file = "emails.csv";
        data = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = br.readLine();
            while (line != null) {
                data.add(line.split(","));
                line = br.readLine();
            }
        } catch (Exception e) {
        }
        // calculate prior probabilities
        int total_n_of_mail = 0;
        int n_spam_mail = 0;
        for (int i = 1; i < data.size(); i++) {
            total_n_of_mail++;
            if (Objects.equals(data.get(i)[0], "1")) {
                n_spam_mail++;
            }
        }
        int n_ham_mail = total_n_of_mail - n_spam_mail;
        priorSpamProbability = (float) n_spam_mail / total_n_of_mail;
        priorHamProbability = 1 - priorSpamProbability;
        // initialize word hash maps
        wordSpamFrequencies = new HashMap<>();
        wordHamFrequencies = new HashMap<>();
        generalWordFrequencies = new HashMap<>();
        for (int i = 1; i < data.get(0).length; i++) {
            wordSpamFrequencies.put(data.get(0)[i], 0);
            wordHamFrequencies.put(data.get(0)[i], 0);
            generalWordFrequencies.put(data.get(0)[i], 0);
        }
        // count word occurrences in spam mail
        for (int i = 1; i < data.get(0).length; i++) {
            for (int j = 1; j < data.size(); j++) {
                // if the mail is spam
                if (Objects.equals(data.get(j)[0], "1")) {
                    // if the the word is used in it more than once
                    if (!Objects.equals(data.get(j)[i], "0")) {
                        // then increment its word count
                        wordSpamFrequencies.put(data.get(0)[i], wordSpamFrequencies.get(data.get(0)[i]) + 1);
                    }
                } else {
                    if (!Objects.equals(data.get(j)[i], "0")) {
                        // then increment its word count
                        wordHamFrequencies.put(data.get(0)[i], wordHamFrequencies.get(data.get(0)[i]) + 1);
                    }
                }
                if (!Objects.equals(data.get(j)[i], "0")) {
                    // then increment its word count
                    generalWordFrequencies.put(data.get(0)[i], generalWordFrequencies.get(data.get(0)[i]) + 1);
                }
            }
        }
        // calculate spam and ham probabilities of each word
        wordSpamcities = new HashMap<>();
        wordHamcities = new HashMap<>();
        generalWordOccurrence = new HashMap<>();
        for (int i = 1; i < data.get(0).length; i++) {
            wordSpamcities.put(data.get(0)[i], (float) wordSpamFrequencies.get(data.get(0)[i]) / n_spam_mail);
            wordHamcities.put(data.get(0)[i], (float) wordHamFrequencies.get(data.get(0)[i]) / n_ham_mail);
            generalWordOccurrence.put(data.get(0)[i], (float) generalWordFrequencies.get(data.get(0)[i]) / total_n_of_mail);
        }
        // calculate the likelihood ratio of each word
        wordLikelihoodRatios = new HashMap<>();
        for (int i = 1; i < data.get(0).length; i++) {
            wordLikelihoodRatios.put(data.get(0)[i], wordSpamcities.get(data.get(0)[i]) / generalWordOccurrence.get(data.get(0)[i]));
        }
    }

    List<String> sanitize(String mail) {
        List<String> fillerWords = Arrays.asList("i", "me", "my", "myself", "we", "our", "ours", "ourselves", "you", "you're", "you've", "you'll", "you'd", "your", "yours", "yourself", "yourselves", "he", "him", "his", "himself", "she", "she's", "her", "hers", "herself", "it", "it's", "its", "itself", "they", "them", "their", "theirs", "themselves", "what", "which", "who", "whom", "this", "that", "that'll", "these", "those", "am", "is", "are", "was", "were", "be", "been", "being", "have", "has", "had", "having", "do", "does", "did", "doing", "a", "an", "the", "and", "but", "if", "or", "because", "as", "until", "while", "of", "at", "by", "for", "with", "about", "against", "between", "into", "through", "during", "before", "after", "above", "below", "to", "from", "up", "down", "in", "out", "on", "off", "over", "under", "again", "further", "then", "once", "here", "there", "when", "where", "why", "how", "all", "any", "both", "each", "few", "more", "most", "other", "some", "such", "no", "nor", "not", "only", "own", "same", "so", "than", "too", "very", "s", "t", "can", "will", "just", "don", "don't", "should", "should've", "now", "d", "ll", "m", "o", "re", "ve", "y", "ain", "aren", "aren't", "couldn", "couldn't", "didn", "didn't", "doesn", "doesn't", "hadn", "hadn't", "hasn", "hasn't", "haven", "haven't", "isn", "isn't", "ma", "mightn", "mightn't", "mustn", "mustn't", "needn", "needn't", "shan", "shan't", "shouldn", "shouldn't", "wasn", "wasn't", "weren", "weren't", "won", "won't", "wouldn", "wouldn't");
        String s1 = mail.toLowerCase();
        String s2 = s1;
        for (String s : fillerWords) {
            s2 = s2.replaceAll(s, "");
        }
        s2 = s2.replaceAll("\\(\\).,\\[]", "");
        return Arrays.asList(s2.split(" "));
    }

    float calcPosteriorSpamProbability(String mail) {
        if (Objects.equals(mail, "")) {
            return 0.0f;
        }
        List<String> words = sanitize(mail);
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
