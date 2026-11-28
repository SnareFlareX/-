package com.mcfriend.customizer.util;

import com.mcfriend.customizer.config.CustomizerConfig;

import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class NameFormatter {
    // Placeholders that survive Lines.java's '(' filter and are restored right before display in BubbleMixin
    public static final char LPAREN_ENC = '\u2768';
    public static final char RPAREN_ENC = '\u2769';

    // Word-boundary-safe patterns for Russian "Боря" (and diminutive "Борька") cases and English "Borya"
    private static final Pattern BORYA_PATTERN = Pattern.compile(
            "(?<![\\p{L}\\p{Nd}_])(бор(?:ей|ею|я|и|е|ю)|борьк(?:ой|ою|а|и|е|у)|borya)(?![\\p{L}\\p{Nd}_])",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
    );

    private static final Pattern NAME_TOKEN_PATTERN = Pattern.compile(
            "\\{(name|имя|name_gen|кого|name_dat|кому|name_acc|винит|name_ins|кем|name_pre|оком)\\}",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
    );

    private NameFormatter() {
    }

    /**
     * Formats any UI/dialogue/bubble string with custom friend name, placeholders, and global word replacements.
     */
    public static String format(String input) {
        return formatInternal(input, true);
    }

    /**
     * Formats a single phrase body inside a .md lines file without corrupting [conditions/tags]
     * and encodes literal parentheses so mcfriend's Lines parser never skips user lines.
     */
    public static String formatPhraseLineForLines(String lineBody) {
        if (lineBody == null || lineBody.isEmpty()) {
            return lineBody;
        }
        // Split around [bracketed metadata] so tags like [весело], [взгляд: удивление], [привет] stay intact
        StringBuilder out = new StringBuilder(lineBody.length() + 16);
        int i = 0;
        while (i < lineBody.length()) {
            int open = lineBody.indexOf('[', i);
            if (open < 0) {
                out.append(encodeParens(formatInternal(lineBody.substring(i), false)));
                break;
            }
            if (open > i) {
                out.append(encodeParens(formatInternal(lineBody.substring(i, open), false)));
            }
            int close = lineBody.indexOf(']', open);
            if (close < 0) {
                out.append(encodeParens(formatInternal(lineBody.substring(open), false)));
                break;
            }
            // Keep bracket tag untouched
            out.append(lineBody, open, close + 1);
            i = close + 1;
        }
        return out.toString();
    }

    public static String encodeParens(String s) {
        if (s == null || (s.indexOf('(') < 0 && s.indexOf(')') < 0)) {
            return s;
        }
        return s.replace('(', LPAREN_ENC).replace(')', RPAREN_ENC);
    }

    public static String restoreParens(String s) {
        if (s == null || (s.indexOf(LPAREN_ENC) < 0 && s.indexOf(RPAREN_ENC) < 0)) {
            return s;
        }
        return s.replace(LPAREN_ENC, '(').replace(RPAREN_ENC, ')');
    }

    private static String formatInternal(String input, boolean restoreEncodedParens) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        String text = restoreEncodedParens ? restoreParens(input) : input;
        CustomizerConfig cfg = CustomizerConfig.get();
        RussianDecliner.Declensions d = cfg.resolveDeclensions();

        // 1. Replace explicit {name} / {имя} / case tokens
        if (text.indexOf('{') >= 0) {
            Matcher tm = NAME_TOKEN_PATTERN.matcher(text);
            StringBuilder sb = new StringBuilder();
            while (tm.find()) {
                String tok = tm.group(1).toLowerCase(Locale.ROOT);
                String rep = switch (tok) {
                    case "name_gen", "кого" -> d.genitive();
                    case "name_dat", "кому" -> d.dative();
                    case "name_acc", "винит" -> d.accusative();
                    case "name_ins", "кем" -> d.instrumental();
                    case "name_pre", "оком" -> d.prepositional();
                    default -> d.nominative();
                };
                tm.appendReplacement(sb, Matcher.quoteReplacement(rep));
            }
            tm.appendTail(sb);
            text = sb.toString();
        }

        // 2. Replace Russian/English forms of "Боря" when custom name differs from default
        if (!cfg.isDefaultName()) {
            Matcher bm = BORYA_PATTERN.matcher(text);
            if (bm.find()) {
                StringBuilder sb = new StringBuilder();
                do {
                    String matched = bm.group(1);
                    String lower = matched.toLowerCase(Locale.ROOT);
                    String target = switch (lower) {
                        case "бори", "борьки" -> d.genitive();
                        case "боре", "борьке" -> d.dative();
                        case "борю", "борьку" -> d.accusative();
                        case "борей", "борею", "борькой", "борькою" -> d.instrumental();
                        default -> d.nominative();
                    };
                    String cased = isAllUpper(matched) ? target.toUpperCase(Locale.ROOT) : target;
                    bm.appendReplacement(sb, Matcher.quoteReplacement(cased));
                } while (bm.find());
                bm.appendTail(sb);
                text = sb.toString();
            }
        }

        // 3. Apply user global word replacements
        Map<String, String> replacements = cfg.globalReplacements;
        if (replacements != null && !replacements.isEmpty()) {
            for (Map.Entry<String, String> entry : replacements.entrySet()) {
                String from = entry.getKey();
                String to = entry.getValue();
                if (from == null || from.isEmpty() || to == null) continue;
                text = replaceIgnoreCasePreserving(text, from, to);
            }
        }

        return text;
    }

    private static boolean isAllUpper(String s) {
        if (s == null || s.length() < 2) return false;
        boolean hasLetter = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isLetter(c)) {
                hasLetter = true;
                if (!Character.isUpperCase(c)) return false;
            }
        }
        return hasLetter;
    }

    private static String replaceIgnoreCasePreserving(String text, String from, String to) {
        String lowerText = text.toLowerCase(Locale.ROOT);
        String lowerFrom = from.toLowerCase(Locale.ROOT);
        int idx = lowerText.indexOf(lowerFrom);
        if (idx < 0) return text;

        StringBuilder sb = new StringBuilder(text.length());
        int start = 0;
        while (idx >= 0) {
            sb.append(text, start, idx);
            String matched = text.substring(idx, idx + from.length());
            sb.append(RussianDecliner.matchCase(matched, to));
            start = idx + from.length();
            idx = lowerText.indexOf(lowerFrom, start);
        }
        sb.append(text, start, text.length());
        return sb.toString();
    }
}
