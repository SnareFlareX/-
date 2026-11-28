package com.mcfriend.customizer.command;

import com.mcfriend.customizer.McFriendCustomizer;
import com.mcfriend.customizer.config.CustomizerConfig;
import com.mcfriend.customizer.network.CustomizerPayloads;
import com.mcfriend.customizer.phrase.PhraseCatalog;
import com.mcfriend.customizer.phrase.PhraseEngine;
import com.mcfriend.customizer.util.RussianDecliner;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.class_124;
import net.minecraft.class_2168;
import net.minecraft.class_2170;
import net.minecraft.class_2561;
import net.minecraft.class_3222;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public final class CustomizerCommands {
    private static final SuggestionProvider<class_2168> SKIN_SUGGESTIONS = (ctx, builder) -> {
        String rem = builder.getRemainingLowerCase();
        if ("default".startsWith(rem)) builder.suggest("default");
        if ("сброс".startsWith(rem)) builder.suggest("сброс");
        try (Stream<Path> s = Files.list(CustomizerConfig.getSkinsDir())) {
            s.filter(Files::isRegularFile)
                    .map(p -> p.getFileName().toString())
                    .filter(n -> n.toLowerCase(Locale.ROOT).endsWith(".png"))
                    .sorted()
                    .forEach(n -> {
                        if (n.toLowerCase(Locale.ROOT).startsWith(rem)) {
                            builder.suggest(n);
                        }
                    });
        } catch (Exception ignored) {
        }
        return builder.buildFuture();
    };

    private static final SuggestionProvider<class_2168> CATEGORY_SUGGESTIONS = (ctx, builder) -> {
        String rem = builder.getRemainingLowerCase();
        int count = 0;
        for (PhraseCatalog.Entry e : PhraseCatalog.all()) {
            if (e.id().toLowerCase(Locale.ROOT).startsWith(rem) || e.title().toLowerCase(Locale.ROOT).contains(rem)) {
                builder.suggest(e.id());
                if (++count >= 60) break;
            }
        }
        return builder.buildFuture();
    };

    private static final SuggestionProvider<class_2168> PROFILE_SUGGESTIONS = (ctx, builder) -> {
        String rem = builder.getRemainingLowerCase();
        for (String p : CustomizerConfig.listProfiles()) {
            if (p.toLowerCase(Locale.ROOT).startsWith(rem)) {
                builder.suggest(p);
            }
        }
        return builder.buildFuture();
    };

    private CustomizerCommands() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            registerRoot(dispatcher, "друг", true);
            registerRoot(dispatcher, "friend", false);
        });
    }

    private static void registerRoot(CommandDispatcher<class_2168> dispatcher, String rootName, boolean ru) {
        LiteralArgumentBuilder<class_2168> root = class_2170.method_9247(rootName)
                .executes(ctx -> openGui(ctx, "name"))
                .then(class_2170.method_9247(ru ? "помощь" : "help").executes(CustomizerCommands::sendHelp))
                .then(class_2170.method_9247(ru ? "сброс" : "reset").executes(CustomizerCommands::resetAll))
                .then(class_2170.method_9247(ru ? "перезагрузить" : "reload").executes(CustomizerCommands::reloadAll))
                .then(class_2170.method_9247(ru ? "сказать" : "say")
                        .then(class_2170.method_9244("text", StringArgumentType.greedyString())
                                .executes(CustomizerCommands::sayPhrase)))
                .then(buildNameBranch(ru))
                .then(buildSkinBranch(ru))
                .then(buildPhraseBranch(ru))
                .then(buildProfileBranch(ru));

        dispatcher.register(root);
    }

    private static LiteralArgumentBuilder<class_2168> buildNameBranch(boolean ru) {
        return class_2170.method_9247(ru ? "имя" : "name")
                .executes(ctx -> openGui(ctx, "name"))
                .then(class_2170.method_9247(ru ? "сброс" : "reset").executes(ctx -> setName(ctx, CustomizerConfig.DEFAULT_NAME)))
                .then(class_2170.method_9247(ru ? "тег" : "tag")
                        .then(class_2170.method_9244("visible", BoolArgumentType.bool())
                                .executes(ctx -> {
                                    boolean vis = BoolArgumentType.getBool(ctx, "visible");
                                    CustomizerConfig cfg = CustomizerConfig.get().copy();
                                    cfg.showNametag = vis;
                                    McFriendCustomizer.applyAndBroadcast(ctx.getSource().method_9211(), cfg);
                                    ok(ctx, "Отображение имени над головой: " + (vis ? "ВКЛ" : "ВЫКЛ"));
                                    return 1;
                                })))
                .then(class_2170.method_9247(ru ? "падежи" : "cases")
                        .then(class_2170.method_9244("gen", StringArgumentType.string())
                                .then(class_2170.method_9244("dat", StringArgumentType.string())
                                        .then(class_2170.method_9244("acc", StringArgumentType.string())
                                                .then(class_2170.method_9244("ins", StringArgumentType.string())
                                                        .then(class_2170.method_9244("pre", StringArgumentType.string())
                                                                .executes(ctx -> {
                                                                    CustomizerConfig cfg = CustomizerConfig.get().copy();
                                                                    cfg.autoDecline = false;
                                                                    cfg.genitive = StringArgumentType.getString(ctx, "gen");
                                                                    cfg.dative = StringArgumentType.getString(ctx, "dat");
                                                                    cfg.accusative = StringArgumentType.getString(ctx, "acc");
                                                                    cfg.instrumental = StringArgumentType.getString(ctx, "ins");
                                                                    cfg.prepositional = StringArgumentType.getString(ctx, "pre");
                                                                    McFriendCustomizer.applyAndBroadcast(ctx.getSource().method_9211(), cfg);
                                                                    ok(ctx, "Падежи для «" + cfg.getEffectiveName() + "» сохранены!");
                                                                    return 1;
                                                                })))))))
                .then(class_2170.method_9244("newName", StringArgumentType.greedyString())
                        .executes(ctx -> setName(ctx, StringArgumentType.getString(ctx, "newName"))));
    }

    private static LiteralArgumentBuilder<class_2168> buildSkinBranch(boolean ru) {
        return class_2170.method_9247(ru ? "скин" : "skin")
                .executes(ctx -> openGui(ctx, "skin"))
                .then(class_2170.method_9247(ru ? "список" : "list").executes(CustomizerCommands::listSkins))
                .then(class_2170.method_9247(ru ? "модель" : "model")
                        .then(class_2170.method_9247("slim").executes(ctx -> setModel(ctx, "slim")))
                        .then(class_2170.method_9247("classic").executes(ctx -> setModel(ctx, "classic"))))
                .then(class_2170.method_9247(ru ? "глаза" : "eyes")
                        .then(class_2170.method_9244("enabled", BoolArgumentType.bool())
                                .executes(ctx -> {
                                    boolean on = BoolArgumentType.getBool(ctx, "enabled");
                                    CustomizerConfig cfg = CustomizerConfig.get().copy();
                                    cfg.showCatEyes = on;
                                    McFriendCustomizer.applyAndBroadcast(ctx.getSource().method_9211(), cfg);
                                    ok(ctx, "Кошачьи глаза: " + (on ? "ВКЛ" : "ВЫКЛ"));
                                    return 1;
                                })))
                .then(class_2170.method_9244("skinArg", StringArgumentType.greedyString())
                        .suggests(SKIN_SUGGESTIONS)
                        .executes(ctx -> setSkin(ctx, StringArgumentType.getString(ctx, "skinArg"))));
    }

    private static LiteralArgumentBuilder<class_2168> buildPhraseBranch(boolean ru) {
        return class_2170.method_9247(ru ? "фраза" : "phrase")
                .executes(ctx -> openGui(ctx, "phrases"))
                .then(class_2170.method_9247(ru ? "сказать" : "say")
                        .then(class_2170.method_9244("text", StringArgumentType.greedyString())
                                .executes(CustomizerCommands::sayPhrase)))
                .then(class_2170.method_9247(ru ? "добавить" : "add")
                        .then(class_2170.method_9244("category", StringArgumentType.word())
                                .suggests(CATEGORY_SUGGESTIONS)
                                .then(class_2170.method_9244("text", StringArgumentType.greedyString())
                                        .executes(ctx -> editPhrase(ctx, false)))))
                .then(class_2170.method_9247(ru ? "заменить" : "set")
                        .then(class_2170.method_9244("category", StringArgumentType.word())
                                .suggests(CATEGORY_SUGGESTIONS)
                                .then(class_2170.method_9244("text", StringArgumentType.greedyString())
                                        .executes(ctx -> editPhrase(ctx, true)))))
                .then(class_2170.method_9247(ru ? "сброс" : "reset")
                        .then(class_2170.method_9244("category", StringArgumentType.word())
                                .suggests(CATEGORY_SUGGESTIONS)
                                .executes(ctx -> {
                                    String cat = StringArgumentType.getString(ctx, "category");
                                    CustomizerConfig cfg = CustomizerConfig.get().copy();
                                    cfg.phraseOverrides.remove(cat);
                                    McFriendCustomizer.applyAndBroadcast(ctx.getSource().method_9211(), cfg);
                                    ok(ctx, "Фразы категории «" + cat + "» сброшены к стандартным.");
                                    return 1;
                                })))
                .then(class_2170.method_9247(ru ? "замена" : "replace")
                        .then(class_2170.method_9244("from", StringArgumentType.string())
                                .then(class_2170.method_9244("to", StringArgumentType.greedyString())
                                        .executes(ctx -> {
                                            String from = StringArgumentType.getString(ctx, "from");
                                            String to = StringArgumentType.getString(ctx, "to");
                                            CustomizerConfig cfg = CustomizerConfig.get().copy();
                                            cfg.globalReplacements.put(from, to);
                                            McFriendCustomizer.applyAndBroadcast(ctx.getSource().method_9211(), cfg);
                                            ok(ctx, "Глобальная замена добавлена: «" + from + "» → «" + to + "»");
                                            return 1;
                                        }))))
                .then(class_2170.method_9247(ru ? "экспорт" : "export")
                        .then(class_2170.method_9244("category", StringArgumentType.word())
                                .suggests(CATEGORY_SUGGESTIONS)
                                .executes(ctx -> {
                                    String cat = StringArgumentType.getString(ctx, "category");
                                    try {
                                        Path out = PhraseEngine.exportCategoryToDisk(cat);
                                        ok(ctx, "Экспортировано в " + out.getFileName() + " (папка config/mcfriend-customizer/lines/)");
                                        return 1;
                                    } catch (Exception e) {
                                        err(ctx, "Ошибка экспорта: " + e.getMessage());
                                        return 0;
                                    }
                                })))
                .then(class_2170.method_9247(ru ? "перезагрузить" : "reload").executes(CustomizerCommands::reloadAll));
    }

    private static LiteralArgumentBuilder<class_2168> buildProfileBranch(boolean ru) {
        return class_2170.method_9247(ru ? "профиль" : "profile")
                .executes(ctx -> openGui(ctx, "profiles"))
                .then(class_2170.method_9247(ru ? "список" : "list").executes(ctx -> {
                    List<String> list = CustomizerConfig.listProfiles();
                    if (list.isEmpty()) {
                        info(ctx, "Сохранённых профилей пока нет. Создайте через /друг профиль сохранить <название>.");
                    } else {
                        ok(ctx, "Профили (" + list.size() + "): " + String.join(", ", list));
                    }
                    return 1;
                }))
                .then(class_2170.method_9247(ru ? "сохранить" : "save")
                        .then(class_2170.method_9244("profileName", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    String pName = StringArgumentType.getString(ctx, "profileName");
                                    if (CustomizerConfig.saveProfile(pName, CustomizerConfig.get())) {
                                        ok(ctx, "Профиль «" + pName + "» сохранён!");
                                        return 1;
                                    }
                                    err(ctx, "Некорректное имя профиля.");
                                    return 0;
                                })))
                .then(class_2170.method_9247(ru ? "загрузить" : "load")
                        .then(class_2170.method_9244("profileName", StringArgumentType.greedyString())
                                .suggests(PROFILE_SUGGESTIONS)
                                .executes(ctx -> {
                                    String pName = StringArgumentType.getString(ctx, "profileName");
                                    CustomizerConfig loaded = CustomizerConfig.loadProfile(pName);
                                    if (loaded == null) {
                                        err(ctx, "Профиль «" + pName + "» не найден.");
                                        return 0;
                                    }
                                    McFriendCustomizer.applyAndBroadcast(ctx.getSource().method_9211(), loaded);
                                    ok(ctx, "Профиль «" + pName + "» загружен! Друг: " + loaded.getEffectiveName());
                                    return 1;
                                })))
                .then(class_2170.method_9247(ru ? "удалить" : "delete")
                        .then(class_2170.method_9244("profileName", StringArgumentType.greedyString())
                                .suggests(PROFILE_SUGGESTIONS)
                                .executes(ctx -> {
                                    String pName = StringArgumentType.getString(ctx, "profileName");
                                    if (CustomizerConfig.deleteProfile(pName)) {
                                        ok(ctx, "Профиль «" + pName + "» удалён.");
                                        return 1;
                                    }
                                    err(ctx, "Профиль «" + pName + "» не найден.");
                                    return 0;
                                })));
    }

    private static int openGui(CommandContext<class_2168> ctx, String tab) {
        class_3222 player = ctx.getSource().method_44023();
        if (player == null) {
            info(ctx, "Используйте подкоманды: имя, скин, фраза, профиль.");
            return 1;
        }
        if (ServerPlayNetworking.canSend(player, CustomizerPayloads.OpenGuiS2C.ID)) {
            ServerPlayNetworking.send(player, new CustomizerPayloads.OpenGuiS2C(tab));
            return 1;
        }
        err(ctx, "Для открытия меню установите аддон MCFriend Customizer на клиенте.");
        return 0;
    }

    private static int setName(CommandContext<class_2168> ctx, String newName) {
        CustomizerConfig cfg = CustomizerConfig.get().copy();
        cfg.name = newName;
        cfg.autoDecline = true;
        McFriendCustomizer.applyAndBroadcast(ctx.getSource().method_9211(), cfg);
        RussianDecliner.Declensions d = cfg.resolveDeclensions();
        ok(ctx, "Имя друга изменено на «" + d.nominative() + "» (род. " + d.genitive() + ", дат. " + d.dative() + ", вин. " + d.accusative() + ", твор. " + d.instrumental() + ")");
        return 1;
    }

    private static int setModel(CommandContext<class_2168> ctx, String model) {
        CustomizerConfig cfg = CustomizerConfig.get().copy();
        cfg.skinModel = model;
        McFriendCustomizer.applyAndBroadcast(ctx.getSource().method_9211(), cfg);
        ok(ctx, "Модель рук: " + ("classic".equals(model) ? "Обычные (Steve, 4px)" : "Тонкие (Alex, 3px)"));
        return 1;
    }

    private static int setSkin(CommandContext<class_2168> ctx, String arg) {
        String trimmed = arg.trim();
        if ("default".equalsIgnoreCase(trimmed) || "сброс".equalsIgnoreCase(trimmed) || "боря".equalsIgnoreCase(trimmed)) {
            CustomizerConfig cfg = CustomizerConfig.get().copy();
            cfg.skinFile = "";
            cfg.skinModel = "slim";
            cfg.showCatEyes = true;
            McFriendCustomizer.applyAndBroadcast(ctx.getSource().method_9211(), cfg);
            ok(ctx, "Возвращён стандартный скин Бори!");
            return 1;
        }
        // Check if local PNG exists in config/mcfriend-customizer/skins/
        String candidate = trimmed.toLowerCase(Locale.ROOT).endsWith(".png") ? trimmed : trimmed + ".png";
        Path localFile = CustomizerConfig.getSkinsDir().resolve(candidate);
        if (Files.isRegularFile(localFile)) {
            CustomizerConfig cfg = CustomizerConfig.get().copy();
            cfg.skinFile = localFile.getFileName().toString();
            cfg.showCatEyes = false;
            McFriendCustomizer.applyAndBroadcast(ctx.getSource().method_9211(), cfg);
            ok(ctx, "Установлен скин «" + cfg.skinFile + "» (кошачьи глаза выключены, можно вернуть через /друг скин глаза true).");
            return 1;
        }
        // Otherwise ask client to download from Mojang username or URL
        class_3222 player = ctx.getSource().method_44023();
        if (player != null && ServerPlayNetworking.canSend(player, CustomizerPayloads.DownloadSkinS2C.ID)) {
            ServerPlayNetworking.send(player, new CustomizerPayloads.DownloadSkinS2C(trimmed));
            info(ctx, "Загружаю скин «" + trimmed + "»...");
            return 1;
        }
        err(ctx, "Файл скина «" + candidate + "» не найден в папке config/mcfriend-customizer/skins/.");
        return 0;
    }

    private static int listSkins(CommandContext<class_2168> ctx) {
        List<String> files = new ArrayList<>();
        try (Stream<Path> s = Files.list(CustomizerConfig.getSkinsDir())) {
            s.filter(Files::isRegularFile)
                    .map(p -> p.getFileName().toString())
                    .filter(n -> n.toLowerCase(Locale.ROOT).endsWith(".png"))
                    .sorted()
                    .forEach(files::add);
        } catch (Exception ignored) {
        }
        if (files.isEmpty()) {
            info(ctx, "В папке config/mcfriend-customizer/skins/ пока нет .png скинов. Положите .png или введите /друг скин <ник_Mojang>!");
        } else {
            ok(ctx, "Скины (" + files.size() + "): " + String.join(", ", files));
        }
        return 1;
    }

    private static int editPhrase(CommandContext<class_2168> ctx, boolean replaceMode) {
        String cat = StringArgumentType.getString(ctx, "category");
        String text = StringArgumentType.getString(ctx, "text").trim();
        if (PhraseCatalog.get(cat) == null) {
            err(ctx, "Категория «" + cat + "» не найдена (используйте Tab для автодополнения или откройте меню клавишей G).");
            return 0;
        }
        CustomizerConfig cfg = CustomizerConfig.get().copy();
        if (replaceMode) {
            List<String> lines = new ArrayList<>();
            lines.add(text);
            cfg.phraseOverrides.put(cat, new CustomizerConfig.PhraseOverride("REPLACE", lines));
            McFriendCustomizer.applyAndBroadcast(ctx.getSource().method_9211(), cfg);
            ok(ctx, "Категория «" + cat + "» заменена на: «" + text + "»");
        } else {
            CustomizerConfig.PhraseOverride ov = cfg.phraseOverrides.get(cat);
            if (ov == null) {
                ov = new CustomizerConfig.PhraseOverride("APPEND", new ArrayList<>());
                cfg.phraseOverrides.put(cat, ov);
            }
            ov.lines.add(text);
            McFriendCustomizer.applyAndBroadcast(ctx.getSource().method_9211(), cfg);
            ok(ctx, "Добавлена фраза в «" + cat + "»: «" + text + "»");
        }
        return 1;
    }

    private static int sayPhrase(CommandContext<class_2168> ctx) {
        class_3222 player = ctx.getSource().method_44023();
        String text = StringArgumentType.getString(ctx, "text");
        if (player == null) {
            err(ctx, "Команда доступна только игроку.");
            return 0;
        }
        return McFriendCustomizer.speakTestPhrase(player, text) ? 1 : 0;
    }

    private static int reloadAll(CommandContext<class_2168> ctx) {
        CustomizerConfig.load();
        McFriendCustomizer.applyAndBroadcast(ctx.getSource().method_9211(), CustomizerConfig.get());
        ok(ctx, "Настройки и все фразы (.md) перезагружены!");
        return 1;
    }

    private static int resetAll(CommandContext<class_2168> ctx) {
        McFriendCustomizer.applyAndBroadcast(ctx.getSource().method_9211(), new CustomizerConfig());
        ok(ctx, "Все настройки сброшены к стандартному Боре.");
        return 1;
    }

    private static int sendHelp(CommandContext<class_2168> ctx) {
        info(ctx, "=== MCFriend Customizer (клавиша G или /друг, /friend) ===");
        info(ctx, "/друг — открыть удобное меню настройки");
        info(ctx, "/друг имя <имя> — сменить имя (автопадежи!) | /друг имя тег <true|false>");
        info(ctx, "/друг скин <файл.png | ник_Mojang | URL | default>");
        info(ctx, "/друг скин модель <slim|classic> | /друг скин глаза <true|false>");
        info(ctx, "/друг фраза добавить|заменить|сброс <категория> <текст>");
        info(ctx, "/друг сказать <текст> — друг сразу скажет эту фразу!");
        info(ctx, "/друг профиль сохранить|загрузить|удалить|список <имя>");
        return 1;
    }

    private static void ok(CommandContext<class_2168> ctx, String msg) {
        ctx.getSource().method_9226(() -> class_2561.method_43470("[Друг] " + msg).method_27692(class_124.method_544('a')), false);
    }

    private static void info(CommandContext<class_2168> ctx, String msg) {
        ctx.getSource().method_9226(() -> class_2561.method_43470(msg).method_27692(class_124.method_544('e')), false);
    }

    private static void err(CommandContext<class_2168> ctx, String msg) {
        ctx.getSource().method_9213(class_2561.method_43470("[Друг] " + msg));
    }
}
