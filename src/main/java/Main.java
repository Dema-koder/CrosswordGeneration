import java.io.*;
import java.util.*;

public class Main {
    private static final int SIZE_OF_POPULATION = 100;
    private static final Random random = new Random();
    private static final char[][] table = new char[20][20];

    public static void main(String[] args) {
        List<String>words = new ArrayList<>();
        try {
            File file = new File("C:\\Users\\User\\IdeaProjects\\crossword\\src\\main\\java\\input.txt");
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                words.add(scanner.next());
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        long start = System.currentTimeMillis();
        var generation = firstGeneration(words);

        int prevAns = -100000, kol = 0;
        while (generation.get(SIZE_OF_POPULATION - 1).getFirst() < 0) {
            generation = crossover(generation);
            if (prevAns == generation.get(SIZE_OF_POPULATION - 1).getFirst())
                kol++;
            else {
                kol = 0;
                prevAns = generation.get(SIZE_OF_POPULATION - 1).getFirst();
            }
            //crosswordPrint(generation.get(SIZE_OF_POPULATION - 1).getSecond());
            //System.out.println(generation.get(SIZE_OF_POPULATION - 1).getFirst());
            if (kol >= 1000)
                generation = firstGeneration(words);
            //System.out.println(generation.get(SIZE_OF_POPULATION - 1).getFirst());
        }
        crosswordPrint(generation.get(SIZE_OF_POPULATION - 1).getSecond());
        for (var word: generation.get(SIZE_OF_POPULATION - 1).getSecond())
            word.printWord();
        System.out.println("Fitness " + generation.get(SIZE_OF_POPULATION - 1).getFirst());
        long end = System.currentTimeMillis();
        System.out.println((end - start) / 1000);
    }

    static List<Pair<Integer, List<Word>>> firstGeneration(List<String>words) {
        List<Pair<Integer, List<Word>>> res = new ArrayList<>();
        for (int i = 0; i < SIZE_OF_POPULATION; i++) {
            List<Word> temp = new ArrayList<>();
            for (var word : words) {
                temp.add(new Word(word));
            }
            Integer fit = calcFitness(temp);
            res.add(new Pair<>(fit, temp));
        }
        Collections.sort(res);
        return res;
    }

    static void crosswordPrint(List<Word>crossword) {
        for (int i = 0; i < 20; i++)
            for (int j = 0; j < 20; j++)
                table[i][j] = '.';
        for (var word: crossword) {
            int x = word.getCoordinates().x();
            int y = word.getCoordinates().y();
            int location = word.getLocation();
            char chars[] = word.getWord().toCharArray();
            for (char c: chars) {
                table[x][y] = c;
                if (location == 0)
                    y++;
                else
                    x++;
            }
        }
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 20; j++)
                System.out.print(table[i][j] + " ");
            System.out.println();
        }
    }

    static Integer calcFitness(List<Word>crossword) {
        Integer fitness = 0;
        for (int i = 0; i < 20; i++)
            for (int j = 0; j < 20; j++)
                table[i][j] = '.';
        for (var word: crossword) {
            int x = word.getCoordinates().x();
            int y = word.getCoordinates().y();
            int location = word.getLocation();
            var chars = word.getWord().toCharArray();
            for (char c: chars) {
                if (table[x][y] != '.' && table[x][y] != c)
                    fitness--;
                table[x][y] = c;
                if (location == 0)
                    y++;
                else
                    x++;
            }
        }
        //System.out.println(fitness);

        int k = 0;
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 20; j++) {
                if (table[i][j] != '.') {
                    dfs(i, j);
                    k++;
                }
            }
        }
        fitness -= ((k - 1) * 30);
        //System.out.println(fitness);

        for (int i = 0; i < crossword.size(); i++) {
            boolean check = false;
            for (int j = 0; j < crossword.size(); j++) {
                if (i != j) {
                    int x1 = crossword.get(i).getCoordinates().x();
                    int y1 = crossword.get(i).getCoordinates().y();
                    int len1 = crossword.get(i).getWord().length();
                    int x2 = crossword.get(j).getCoordinates().x();
                    int y2 = crossword.get(j).getCoordinates().y();
                    int len2 = crossword.get(j).getWord().length();
                    if (crossword.get(i).getLocation() == crossword.get(j).getLocation()) {
                        if (crossword.get(i).getLocation() == 0) { // horizontal
                            if (Math.abs(x1 - x2) <= 1 && ((y1 - len2 - 1 <= y2 && y2 <= y1 + len1) || (y2 - len1 - 1 <= y1 && y1 <= y2 + len2))) {
                                fitness -= 20;
                            }
                        } else {
                            if (Math.abs(y1 - y2) <= 1 && ((x1 - len2 - 1 <= x2 && x2 <= x1 + len1) || (x2 - len1 - 1 <= x1 && x1 <= x2 + len2))) {
                                fitness -= 20;
                            }
                        }
                    } else {
                        if (crossword.get(j).getLocation() == 1) {
                            if ((y1 <= y2 && y2 < y1 + len1) && (x2 <= x1 && x2 + len2 - 1 >= x1))
                                check = true;
                        }
                        if (crossword.get(j).getLocation() == 0) {
                            if ((x1 <= x2 && x2 < x1 + len1) && (y2 <= y1 && y2 + len2 - 1 >= y1))
                                check = true;
                        }

                        if (crossword.get(i).getLocation() == 0) {
                            if ((y2 == y1 - 1 || y2 == y1 + len1) && (x2 <= x1 && x1 <= x2 + len2 - 1))
                                fitness -= 15;
                        } else {
                            if ((x2 == x1 - 1 || x2 == x1 + len1) && (y2 <= y1 && y1 <= y2 + len2 - 1))
                                fitness -= 15;
                        }
                    }
                }
            }
            if (!check)
                fitness -= 7;
        }
        //System.out.println(fitness);

        return fitness;
    }

    static void dfs(int x, int y) {
        table[x][y] = '.';
        if (x - 1 >= 0 && table[x - 1][y] != '.')
            dfs(x - 1, y);
        if (x + 1 <= 19 && table[x + 1][y] != '.')
            dfs(x + 1, y);
        if (y - 1 >= 0 && table[x][y - 1] != '.')
            dfs(x, y - 1);
        if (y + 1 <= 19 && table[x][y + 1] != '.')
            dfs(x, y + 1);
    }

    static List<Pair<Integer, List<Word>>> crossover(List<Pair<Integer, List<Word>>>generation) {
        List<Pair<Integer, List<Word>>> newGeneration = new ArrayList<>(generation.subList(SIZE_OF_POPULATION / 5 * 4, SIZE_OF_POPULATION));
        List<Pair<Integer, List<Word>>> temp = new ArrayList<>();
        for (int i = 0; i < SIZE_OF_POPULATION; i++) {
            for (int j = i + 1; j < SIZE_OF_POPULATION; j++) {
                List<Word>first = generation.get(i).getSecond();
                List<Word>second = generation.get(j).getSecond();
                for (int l = 0; l < first.size(); l++) {
                    double chance = random.nextDouble();
                    if (chance > 0.5) {
                        var tmp = first.get(l);
                        first.set(l, second.get(l));
                        second.set(l, tmp);
                    }
                }
                Integer firstFit = calcFitness(first);
                Integer secondFit = calcFitness(second);
                temp.add(new Pair<>(firstFit, first));
                temp.add(new Pair<>(secondFit, second));
            }
        }
        temp = mutation(temp);
        Collections.sort(temp);
        int i = temp.size() - 1;
        while (newGeneration.size() < SIZE_OF_POPULATION) {
            newGeneration.add(temp.get(i));
            i--;
        }
        Collections.sort(newGeneration);
        return newGeneration;
    }

    static List<Pair<Integer, List<Word>>> mutation(List<Pair<Integer, List<Word>>>generation) {
        List<Pair<Integer, List<Word>>>res = new ArrayList<>();
        for (var integerListPair : generation) {
            List<Word> current = new ArrayList<>(integerListPair.getSecond());
            for (int j = 0; j < current.size(); j++) {
                double chance = random.nextDouble();
                if (chance < 0.25) {
                    String word = current.get(j).getWord();
                    current.set(j, new Word(word));
                }
            }
            Integer fit = calcFitness(current);
            res.add(new Pair<>(fit, current));
        }
        Collections.sort(res);
        return res;
    }
}

class Coordinates {
    private int x;
    private int y;
    public Coordinates() {
        Random random = new Random();
        this.x = Math.abs(random.nextInt()) % 20;
        this.y = Math.abs(random.nextInt()) % 20;
    }

    public Coordinates(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }
}

class Word {
    private String word;
    private Coordinates coordinates;
    private int location;

    public Word(String curWord) {
        this.word = curWord;
        Random random = new Random();
        do {
            this.coordinates = new Coordinates();
            this.location = Math.abs(random.nextInt()) % 2;
        } while (!this.isValidLocation());
    }

    public Word(String word, Coordinates coordinates, int location) {
        this.word = word;
        this.coordinates = coordinates;
        this.location = location;
    }

    public boolean isValidLocation() {
        if ((coordinates.x() >= 0 && coordinates.x() < 20 && coordinates.y() >= 0 && coordinates.y() < 20)) {
            if (location == 1) {
                return (word.length() + coordinates.x() < 20);
            } else {
                return (word.length() + coordinates.y() < 20);
            }
        } else
            return false;
    }

    public void printWord() {
        System.out.println(word + " " + coordinates.x() + " " + coordinates.y() + " " + location);
    }

    public String getWord() {
        return word;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public int getLocation() {
        return location;
    }
}

class Pair<T, U> implements Comparable<Pair<Integer, U>> {
    private Integer first;
    private U second;

    public Pair(Integer first, U second) {
        this.first = first;
        this.second = second;
    }

    public Integer getFirst() {
        return first;
    }

    public U getSecond() {
        return second;
    }

    public void setFirst(java.lang.Integer first) {
        this.first = first;
    }

    public void setSecond(U second) {
        this.second = second;
    }

    @Override
    public int compareTo(Pair<Integer, U> o) {
        return Integer.compare(first, o.getFirst());
    }
}