package com.mcfriend.customizer.util;

import java.util.Locale;

public final class RussianDecliner {
    public record Declensions(
            String nominative,
            String genitive,
            String dative,
            String accusative,
            String instrumental,
            String prepositional
    ) {}

    private RussianDecliner() {
    }

    public static Declensions decline(String rawName) {
        if (rawName == null || rawName.isBlank()) {
            return new Declensions("Боря", "Бори", "Боре", "Борю", "Борей", "Боре");
        }
        String name = rawName.trim();
        // Multi-word names: decline only the last word if it is Cyrillic, keeping prefix intact
        int lastSpace = name.lastIndexOf(' ');
        if (lastSpace > 0 && lastSpace < name.length() - 1) {
            String prefix = name.substring(0, lastSpace + 1);
            Declensions tail = declineSingleWord(name.substring(lastSpace + 1));
            return new Declensions(
                    prefix + tail.nominative(),
                    prefix + tail.genitive(),
                    prefix + tail.dative(),
                    prefix + tail.accusative(),
                    prefix + tail.instrumental(),
                    prefix + tail.prepositional()
            );
        }
        return declineSingleWord(name);
    }

    private static Declensions declineSingleWord(String word) {
        if (word.length() < 2 || !containsCyrillic(word)) {
            return unchanged(word);
        }
        String lower = word.toLowerCase(Locale.ROOT);
        if ("боря".equals(lower)) {
            return applyCase(word, "боря", "бори", "боре", "борю", "борей", "боре");
        }
        // Common indeclinable endings
        char last = lower.charAt(lower.length() - 1);
        if (last == 'о' || last == 'е' || last == 'ё' || last == 'и' || last == 'у' || last == 'ю' || last == 'э' || last == 'ы') {
            return unchanged(word);
        }

        String stem = word.substring(0, word.length() - 1);
        char prev = lower.charAt(lower.length() - 2);

        if (last == 'я') {
            if (prev == 'и') {
                // Мария -> Марии / Марии / Марию / Марией / Марии
                return new Declensions(word, stem + "и", stem + "и", stem + "ю", stem + "ей", stem + "и");
            }
            // Боря, Ваня, Петя, Аня, Катя, Соня
            return new Declensions(word, stem + "и", stem + "е", stem + "ю", stem + "ей", stem + "е");
        }

        if (last == 'а') {
            // г, к, х, ж, ш, ч, щ -> genitive -и; else -ы
            String genEnd = isVelarOrSibilant(prev) ? "и" : "ы";
            // ж, ш, ч, щ, ц -> instrumental -ей; else -ой
            String insEnd = isSibilantOrTs(prev) ? "ей" : "ой";
            return new Declensions(word, stem + genEnd, stem + "е", stem + "у", stem + insEnd, stem + "е");
        }

        if (last == 'й') {
            if (prev == 'и' && lower.length() >= 4) {
                // Дмитрий -> Дмитрия / Дмитрию / Дмитрия / Дмитрием / Дмитрии
                return new Declensions(word, stem + "я", stem + "ю", stem + "я", stem + "ем", stem + "и");
            }
            // Андрей, Алексей, Сергей, Тимофей
            return new Declensions(word, stem + "я", stem + "ю", stem + "я", stem + "ем", stem + "е");
        }

        if (last == 'ь') {
            // Игорь, Василий-like soft masculine default (note: users can always customize in GUI)
            return new Declensions(word, stem + "я", stem + "ю", stem + "я", stem + "ем", stem + "е");
        }

        if (isCyrillicConsonant(last)) {
            // Special fleeting vowel -ек / -ец / -ок? Keep simple & predictable unless very common
            if (lower.endsWith("ик") || lower.endsWith("ок") && lower.length() <= 3) {
                // Барсик -> Барсика, Коток -> Котока (no false fleeting deletion)
                String ins = isSibilantOrTs(last) ? "ем" : "ом";
                return new Declensions(word, word + "а", word + "у", word + "а", word + ins, word + "е");
            }
            String ins = isSibilantOrTs(last) ? "ем" : "ом";
            return new Declensions(word, word + "а", word + "у", word + "а", word + ins, word + "е");
        }

        return unchanged(word);
    }

    private static Declensions unchanged(String word) {
        return new Declensions(word, word, word, word, word, word);
    }

    private static Declensions applyCase(String template, String nom, String gen, String dat, String acc, String ins, String pre) {
        return new Declensions(
                matchCase(template, nom),
                matchCase(template, gen),
                matchCase(template, dat),
                matchCase(template, acc),
                matchCase(template, ins),
                matchCase(template, pre)
        );
    }

    public static String matchCase(String source, String replacement) {
        if (source == null || source.isEmpty() || replacement == null || replacement.isEmpty()) {
            return replacement == null ? "" : replacement;
        }
        boolean allUpper = true;
        boolean hasLetter = false;
        for (int i = 0; i < source.length(); i++) {
            char c = source.charAt(i);
            if (Character.isLetter(c)) {
                hasLetter = true;
                if (!Character.isUpperCase(c)) {
                    allUpper = false;
                    break;
                }
            }
        }
        if (hasLetter && allUpper && source.length() > 1) {
            return replacement.toUpperCase(Locale.ROOT);
        }
        if (Character.isUpperCase(source.charAt(0))) {
            return Character.toUpperCase(replacement.charAt(0)) + replacement.substring(1);
        }
        return replacement.toLowerCase(Locale.ROOT);
    }

    private static boolean containsCyrillic(String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if ((c >= 'А' && c <= 'я') || c == 'Ё' || c == 'ё') {
                return true;
            }
        }
        return false;
    }

    private static boolean isVelarOrSibilant(char c) {
        return c == 'г' || c == 'к' || c == 'х' || c == 'ж' || c == 'ш' || c == 'ч' || c == 'щ';
    }

    private static boolean isSibilantOrTs(char c) {
        return c == 'ж' || c == 'ш' || c == 'ч' || c == 'щ' || c == 'ц';
    }

    private static boolean isCyrillicConsonant(char c) {
        return "бвгджзклмнпрстфхцчшщ".indexOf(c) >= 0;
    }
}
