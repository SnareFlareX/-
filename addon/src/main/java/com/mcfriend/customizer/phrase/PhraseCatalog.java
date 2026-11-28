package com.mcfriend.customizer.phrase;

import com.mcfriend.customizer.McFriendCustomizer;
import net.donila.friend.talk.Lines;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class PhraseCatalog {
    public static final Pattern NUMBERED_LINE = Pattern.compile("^\\d+\\.\\s+(.*)$");
    private static final String PREFIX = "assets/mcfriend/lines/";

    public record Entry(
            String id,
            String title,
            String group,
            List<String> defaultPhrases,
            List<String> rawLines
    ) {}

    private static volatile List<Entry> entries = Collections.emptyList();
    private static volatile Map<String, Entry> byId = Collections.emptyMap();

    private PhraseCatalog() {
    }

    public static synchronized void init() {
        Map<String, Entry> map = new LinkedHashMap<>();
        try {
            URL probe = Lines.class.getResource("/" + PREFIX + "first_meet.md");
            if (probe != null) {
                String proto = probe.getProtocol();
                if ("jar".equalsIgnoreCase(proto)) {
                    JarURLConnection conn = (JarURLConnection) probe.openConnection();
                    JarFile jar = conn.getJarFile();
                    Enumeration<JarEntry> en = jar.entries();
                    while (en.hasMoreElements()) {
                        JarEntry je = en.nextElement();
                        String name = je.getName();
                        if (!je.isDirectory() && name.startsWith(PREFIX) && name.endsWith(".md")) {
                            String id = name.substring(PREFIX.length(), name.length() - 3);
                            if (id.indexOf('/') >= 0) continue;
                            try (InputStream in = jar.getInputStream(je)) {
                                List<String> lines = readAllLines(in);
                                Entry e = parseEntry(id, lines);
                                map.put(id, e);
                            }
                        }
                    }
                } else if ("file".equalsIgnoreCase(proto)) {
                    Path dir = Path.of(probe.toURI()).getParent();
                    if (dir != null && Files.isDirectory(dir)) {
                        try (Stream<Path> s = Files.list(dir)) {
                            s.filter(Files::isRegularFile).forEach(p -> {
                                String fn = p.getFileName().toString();
                                if (fn.endsWith(".md")) {
                                    String id = fn.substring(0, fn.length() - 3);
                                    try {
                                        List<String> lines = Files.readAllLines(p, StandardCharsets.UTF_8);
                                        map.put(id, parseEntry(id, lines));
                                    } catch (Exception ignored) {
                                    }
                                }
                            });
                        }
                    }
                }
            }
        } catch (Exception e) {
            McFriendCustomizer.LOGGER.warn("Не удалось просканировать каталог фраз mcfriend: {}", e.getMessage());
        }

        List<Entry> list = new ArrayList<>(map.values());
        list.sort(Comparator.comparingInt((Entry e) -> priorityBucket(e.id()))
                .thenComparing(Entry::title, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(Entry::id));

        Map<String, Entry> sortedMap = new LinkedHashMap<>();
        for (Entry e : list) {
            sortedMap.put(e.id(), e);
        }
        entries = Collections.unmodifiableList(list);
        byId = Collections.unmodifiableMap(sortedMap);
        McFriendCustomizer.LOGGER.info("MCFriend Customizer: проиндексировано {} категорий фраз", entries.size());
    }

    public static List<Entry> all() {
        if (entries.isEmpty()) {
            init();
        }
        return entries;
    }

    public static Entry get(String id) {
        if (byId.isEmpty()) {
            init();
        }
        return id == null ? null : byId.get(id);
    }

    public static List<Entry> search(String query) {
        List<Entry> all = all();
        if (query == null || query.isBlank()) {
            return all;
        }
        String q = query.trim().toLowerCase(Locale.ROOT);
        List<Entry> exact = new ArrayList<>();
        List<Entry> partial = new ArrayList<>();
        for (Entry e : all) {
            String idLow = e.id().toLowerCase(Locale.ROOT);
            String titleLow = e.title().toLowerCase(Locale.ROOT);
            String groupLow = e.group().toLowerCase(Locale.ROOT);
            if (idLow.contains(q) || titleLow.contains(q) || groupLow.contains(q)) {
                exact.add(e);
            } else {
                for (String p : e.defaultPhrases()) {
                    if (p.toLowerCase(Locale.ROOT).contains(q)) {
                        partial.add(e);
                        break;
                    }
                }
            }
        }
        exact.addAll(partial);
        return exact;
    }

    private static List<String> readAllLines(InputStream in) throws Exception {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            return br.lines().toList();
        }
    }

    private static Entry parseEntry(String id, List<String> raw) {
        String title = id;
        List<String> phrases = new ArrayList<>();
        for (String line : raw) {
            String t = line.trim();
            if (t.startsWith("# ")) {
                title = t.substring(2).trim();
                continue;
            }
            Matcher m = NUMBERED_LINE.matcher(t);
            if (m.matches()) {
                String body = m.group(1).trim();
                if (!body.isEmpty() && !body.contains("(")) {
                    phrases.add(body);
                }
            }
        }
        return new Entry(id, title, inferGroup(id), Collections.unmodifiableList(phrases), Collections.unmodifiableList(raw));
    }

    private static int priorityBucket(String id) {
        // Put most popular everyday categories at the very top for instant access!
        return switch (id) {
            case "first_meet", "thought", "morning", "goodnight", "follow_me", "follow_release",
                 "rain_start", "rain_end", "stare", "boom", "fight_start", "fight_won",
                 "mine_ore_seen", "wood_start", "hunger_growl" -> 0;
            default -> 1;
        };
    }

    private static String inferGroup(String id) {
        if (id.startsWith("fight_") || id.startsWith("foe_") || id.startsWith("ask_fight")) return "Бой";
        if (id.startsWith("mine_") || id.startsWith("ask_mine") || id.startsWith("stone_")) return "Шахта";
        if (id.startsWith("wood_") || id.startsWith("sapling_") || id.startsWith("grove_")) return "Дерево";
        if (id.startsWith("bed_") || id.startsWith("sleep_") || id.startsWith("home_") || id.equals("morning") || id.equals("goodnight")) return "Дом и сон";
        if (id.startsWith("eat_") || id.startsWith("food_") || id.startsWith("hunger_") || id.startsWith("gift_")) return "Еда и подарки";
        if (id.startsWith("rain_") || id.equals("snow") || id.equals("thunder") || id.equals("sun") || id.equals("moon")) return "Погода";
        if (id.startsWith("follow_") || id.equals("first_meet") || id.equals("thought") || id.equals("stare") || id.startsWith("angry")) return "Общение";
        if (id.startsWith("furnace_") || id.startsWith("iron_") || id.startsWith("tools_") || id.startsWith("table_") || id.startsWith("blocks_") || id.startsWith("inv_")) return "Дела и крафт";
        return "Мобы и мир";
    }
}
