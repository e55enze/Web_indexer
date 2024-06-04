package Indexer.src;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main
{
    static Map<String, Integer> FilesMap = new HashMap<String, Integer>();
    static int FilesCounter = 0;
    static Map<String, Integer> WordsMap = new HashMap<String, Integer>();
    static int WordsCounter = 0;

    static class LinkEntry
    {
        public Integer EntryIndex;
        public Integer FileIdx;
        public Integer WordIdx;
        public Integer Count;
    }
    static List<LinkEntry> LinksList = new ArrayList<>();
    static int LinksCounter = 0;

    static Integer MyPutIfAbsent(Map<String, Integer> map, String key, Integer newValue)
    {
        Integer v = map.get(key);

        if (v == null)
        {
            map.put(key, newValue);
            v = newValue;
        }

        return v;
    }

    static void AddFileResults(String file, Map<String, Integer> FilewordsMap)
    {
        Integer fileIdx = null;

        synchronized (FilesMap)
        {
            FilesMap.put(file, FilesCounter);
            fileIdx = FilesCounter;
            FilesCounter++;
        }

        Map<String, Integer> WordIndexes = new HashMap<>();

        synchronized (WordsMap)
        {
            for (final var entry : FilewordsMap.entrySet())
            {
                final String word = entry.getKey();
                final int wordIdx = MyPutIfAbsent(WordsMap, word, WordsCounter);

                if(wordIdx == WordsCounter)
                    WordsCounter++;

                WordIndexes.put(word, wordIdx);
            }
        }

        synchronized (LinksList)
        {
            for (final var entry : FilewordsMap.entrySet())
            {
                final String word = entry.getKey();
                final Integer count = entry.getValue();
                final Integer wordIdx = WordIndexes.get(word);

                LinkEntry link = new LinkEntry();
                link.EntryIndex = LinksCounter;
                link.FileIdx = fileIdx;
                link.WordIdx = wordIdx;
                link.Count = count;

                LinksList.add(link);
                LinksCounter++;
            }
        }
    }

    static String ReadFile(String path)
    {
        try
        {
            byte[] encoded = Files.readAllBytes(Paths.get(path));
            return new String(encoded, Charset.forName("windows-1251"));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return new String();
    }

    static String NormalizeWord(String word)
    {
        word = word.toLowerCase();

        for(int i = 0; i < word.length(); i++)
        {
            char ch = word.charAt(i);

            if ((ch >= 'А' && ch <= 'Я') || (ch >= 'а' && ch <= 'я'))
                continue;

            word = word.replace(String.valueOf(ch)  , "");
            i--;
        }

        word = word.trim();
        return word;
    }

    static Map<String, Integer> WordCountsInFile(String path)
    {
        final String text = ReadFile(path);
        final String[] splited = text.split("\\s+"); // by whitespace
        Map<String, Integer> resultMap = new HashMap<>();

        for (String split : splited)
        {
            String word = NormalizeWord(split);

            if(word.isEmpty())
                continue;

            int count = resultMap.getOrDefault(word, 0);
            count++;
            resultMap.put(word, count);
        }

        return resultMap;
    }

    static Queue<String> FilesQueue;

    public static void ThreadProc()
    {
        for(;;)
        {
            String file = FilesQueue.poll();

            if(file == null)
                break;

            AddFileResults(file, WordCountsInFile(file));
        }
    }

    public static Queue<String> FilesInFolder(String dir) {
        return Stream.of(new File(dir).listFiles())
                .filter(file -> !file.isDirectory())
                .map(File::getPath)
                .collect(Collectors.toCollection(ConcurrentLinkedQueue::new));
    }

    public static String GenerateSQL()
    {
        StringBuilder res = new StringBuilder();

        for (final var entry : FilesMap.entrySet())
        {
            res.append("INSERT INTO files (id, name) VALUES(");
            res.append(entry.getValue());
            res.append(", '");
            res.append(entry.getKey());
            res.append("');");
            res.append("\n");
        }

        res.append("\n");

        for (final var entry : WordsMap.entrySet())
        {
            res.append("INSERT INTO words (id, word) VALUES(");
            res.append(entry.getValue());
            res.append(", '");
            res.append(entry.getKey());
            res.append("');");
            res.append("\n");
        }

        res.append("\n");

        for (final var entry : LinksList)
        {
            res.append("INSERT INTO links (id, file, word, cnt) VALUES(");
            res.append(entry.EntryIndex);
            res.append(", ");
            res.append(entry.FileIdx);
            res.append(", ");
            res.append(entry.WordIdx);
            res.append(", ");
            res.append(entry.Count);
            res.append(");");
            res.append("\n");
        }

        return res.toString();
    }

    public static void SaveToFile(String text, String path)
    {
        try
        {
            BufferedWriter writer = new BufferedWriter(new FileWriter(path));
            writer.write(text);
            writer.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void main(String[] args)
    {
        FilesQueue = FilesInFolder("C:\\Idea_Projects\\web");
        final int ThreadsCnt = 4;

        List<Threads> threads = new ArrayList<>();

        for(int i = 0; i < ThreadsCnt; i++)
            threads.add(new Threads());

        for(int i = 0; i < ThreadsCnt; i++)
            threads.get(i).run();

        for(int i = 0; i < ThreadsCnt; i++)
        {
            Thread thread = threads.get(i);

            synchronized (thread)
            {
                try
                {
                    thread.join();
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }

        SaveToFile(GenerateSQL(), "C:\\Idea_Projects\\web\\req.sql");
        System.out.println("done");
    }
}