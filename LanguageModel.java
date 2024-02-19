import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.ArrayList;

public class LanguageModel {

    // The map of this model.
    // Maps windows to lists of charachter data objects.
    HashMap<String, List> CharDataMap;
    
    // The window length used in this model.
    int windowLength;
    
    // The random number generator used by this model. 
	private Random randomGenerator;

    /** Constructs a language model with the given window length and a given
     *  seed value. Generating texts from this model multiple times with the 
     *  same seed value will produce the same random texts. Good for debugging. */
    public LanguageModel(int windowLength, int seed) {
        this.windowLength = windowLength;
        randomGenerator = new Random(seed);
        CharDataMap = new HashMap<String, List>();
    }

    /** Constructs a language model with the given window length.
     * Generating texts from this model multiple times will produce
     * different random texts. Good for production. */
    public LanguageModel(int windowLength) {
        this.windowLength = windowLength;
        randomGenerator = new Random();
        CharDataMap = new HashMap<String, List>();
    }

    /** Builds a language model from the text in the given file (the corpus). */
	public void train(String fileName) {
        String window = "";
        char character;
        In in = new In(fileName);
        for (int i = 0; i < windowLength; i++) {
            window = window + in.readChar();
        }
        while (!(in.isEmpty())) {
            character = in.readChar();
            if (CharDataMap.containsKey(window)) {
                CharDataMap.get(window).update(character);
            }
            else{
                List lst = new List();
                CharDataMap.put(window, lst);
                lst.update(character);
            }
            window = window.substring(1) + character;
        }
        for (List probs : CharDataMap.values())
            calculateProbabilities(probs);
	}
	public void calculateProbabilities(List probs) {
      Node pointer = probs.getFirst();
      int numOfChars = 0;
      while (pointer != null){
          numOfChars = numOfChars + pointer.cp.count;
          pointer = pointer.next;
      }
      pointer = probs.getFirst();
      pointer.cp.p = (double)(pointer.cp.count) / numOfChars;
      pointer.cp.cp = pointer.cp.p;
      while (pointer.next != null){
          pointer.next.cp.p = (double)(pointer.next.cp.count) / numOfChars;
          pointer.next.cp.cp = pointer.next.cp.p + pointer.cp.cp;
          pointer = pointer.next;
      }
	}

    // Returns a random character from the given probabilities list.
	public char getRandomChar(List probs) {
        Double rnd = randomGenerator.nextDouble();
        Node pointer = probs.getFirst();
        while (pointer != null){
            if (pointer.cp.cp >= rnd){
                return  pointer.cp.chr;
            }
            pointer = pointer.next;
        }
        return  ' ';
	}

    /**
	 * Generates a random text, based on the probabilities that were learned during training.
	 * @param initialText - text to start with. If initialText's last substring of size numberOfLetters
	 * doesn't appear as a key in Map, we generate no text and return only the initial text.
	 * @param numberOfLetters - the size of text to generate
	 * @return the generated text
	 */
	public String generate(String initialText, int textLength) {
	    if (initialText.length() < windowLength || !(CharDataMap.containsKey(initialText.substring(initialText.length() - windowLength)))){
            return  initialText;
        }
        String str = initialText;
        while (str.length() <= textLength + initialText.length() - 1){
            List probs = CharDataMap.get(str.substring(str.length() - windowLength));
            str = str + getRandomChar(probs);
        }
        return str;
	}

    /** Returns a string representing the map of this language model. */
	public String toString() {
		StringBuilder str = new StringBuilder();
		for (String key : CharDataMap.keySet()) {
			List keyProbs = CharDataMap.get(key);
			str.append(key + " : " + keyProbs + "\n");
		}
		return str.toString();
	}

    public static void main(String[] args) {
		// Your code goes here
    }
}
