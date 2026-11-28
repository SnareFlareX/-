package com.mcfriend.customizer.client;

import com.mcfriend.customizer.config.CustomizerConfig;
import com.mcfriend.customizer.phrase.PhraseCatalog;
import com.mcfriend.customizer.phrase.PhraseEngine;
import com.mcfriend.customizer.util.RussianDecliner;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_156;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.minecraft.class_342;
import net.minecraft.class_4185;
import net.minecraft.class_437;
import net.minecraft.class_7529;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Environment(EnvType.CLIENT)
public final class CustomizerScreen extends class_437 {
    private static final int COL_BG = 0xE8161922;
    private static final int COL_PANEL_DARK = 0xCC0E1017;
    private static final int COL_BORDER_GOLD = 0xFFD4A847;
    private static final int COL_BORDER_SUB = 0xFF3A4154;
    private static final int COL_ROW_SEL = 0xAA3A4D6E;
    private static final int COL_ROW_HOV = 0x662A3448;
    private static final int COL_TEXT_MAIN = 0xFFF2F2F2;
    private static final int COL_TEXT_GOLD = 0xFFFFD86B;
    private static final int COL_TEXT_MUTED = 0xFF9EA8BC;
    private static final int COL_STATUS_OK = 0xFF7CF08A;
    private static final int COL_STATUS_INFO = 0xFFFFE27A;
    private static final int COL_STATUS_ERR = 0xFFFF7A7A;

    private String currentTab;
    private int panelX, panelY, panelW, panelH;
    private int contentTop, contentBottom;

    private String statusText = "";
    private int statusColor = COL_STATUS_INFO;

    // Tab 1: Name widgets
    private class_342 nameField;
    private class_342 genField, datField, accField, insField, preField;

    // Tab 2: Skin state & widgets
    private List<String> skinFiles = new ArrayList<>();
    private int skinScroll = 0;
    private int skinListX, skinListY, skinListW, skinListH;
    private class_342 skinDownloadField;
    private class_4185 skinDownloadBtn;
    private boolean downloadingSkin = false;

    // Tab 3: Phrases state & widgets
    private String phraseQuery = "";
    private List<PhraseCatalog.Entry> filteredCategories = new ArrayList<>();
    private String selectedCategoryId = "first_meet";
    private String editorMode = "REPLACE";
    private int catScroll = 0;
    private int catListX, catListY, catListW, catListH;
    private class_342 phraseSearchField;
    private class_7529 phraseEditor;
    private class_4185 phraseModeBtn;
    private class_342 replFromField;
    private class_342 replToField;

    // Tab 4: Profiles state & widgets
    private List<String> profileList = new ArrayList<>();
    private String selectedProfile = "";
    private int profileScroll = 0;
    private int profListX, profListY, profListW, profListH;
    private class_342 profileNameField;
    private boolean confirmResetAll = false;

    public CustomizerScreen(String initialTab) {
        super(class_2561.method_43470("Настройка друга"));
        this.currentTab = normalizeTab(initialTab);
    }

    private static String normalizeTab(String tab) {
        if (tab == null) return "name";
        return switch (tab.toLowerCase()) {
            case "skin", "скин" -> "skin";
            case "phrases", "phrase", "фразы" -> "phrases";
            case "profiles", "profile", "профили" -> "profiles";
            default -> "name";
        };
    }

    @Override
    public boolean method_25421() {
        return false; // Don't pause world so live changes to friend can be seen immediately!
    }

    @Override
    protected void method_25426() {
        this.panelW = Math.min(448, Math.max(340, this.field_22789 - 16));
        this.panelH = Math.min(262, Math.max(216, this.field_22790 - 16));
        this.panelX = (this.field_22789 - this.panelW) / 2;
        this.panelY = (this.field_22790 - this.panelH) / 2;
        this.contentTop = this.panelY + 46;
        this.contentBottom = this.panelY + this.panelH - 26;

        rebuildWidgets();
    }

    private void rebuildWidgets() {
        this.method_37067();
        this.confirmResetAll = false;

        // Top 4 tab buttons
        int tabGap = 4;
        int tabW = (this.panelW - 20 - tabGap * 3) / 4;
        int tabY = this.panelY + 22;
        addTabButton(0, "name", "Имя", this.panelX + 10, tabY, tabW, tabGap);
        addTabButton(1, "skin", "Скин", this.panelX + 10, tabY, tabW, tabGap);
        addTabButton(2, "phrases", "Фразы", this.panelX + 10, tabY, tabW, tabGap);
        addTabButton(3, "profiles", "Профили", this.panelX + 10, tabY, tabW, tabGap);

        // Footer close button
        this.method_37063(class_4185.method_46430(class_2561.method_43470("Закрыть"), b -> this.method_25419())
                .method_46434(this.panelX + this.panelW - 74, this.panelY + this.panelH - 22, 64, 18)
                .method_46431());

        switch (this.currentTab) {
            case "skin" -> initSkinTab();
            case "phrases" -> initPhrasesTab();
            case "profiles" -> initProfilesTab();
            default -> initNameTab();
        }
    }

    private void addTabButton(int index, String id, String label, int startX, int y, int w, int gap) {
        int x = startX + index * (w + gap);
        class_4185 btn = class_4185.method_46430(class_2561.method_43470(label), b -> {
            if (!id.equals(this.currentTab)) {
                this.currentTab = id;
                this.statusText = "";
                rebuildWidgets();
            }
        }).method_46434(x, y, w, 18).method_46431();
        btn.field_22763 = !id.equals(this.currentTab);
        this.method_37063(btn);
    }

    // ==================== TAB 1: NAME ====================
    private void initNameTab() {
        CustomizerConfig cfg = CustomizerConfig.get();
        int left = this.panelX + 14;
        int innerW = this.panelW - 28;
        int y = this.contentTop + 14;

        int btnSaveW = 86;
        int btnResetW = 64;
        int fieldW = Math.max(100, innerW - btnSaveW - btnResetW - 10);

        this.nameField = new class_342(this.field_22793, left, y, fieldW, 18, class_2561.method_43470("Имя"));
        this.nameField.method_1880(32);
        this.nameField.method_1852(cfg.getEffectiveName());
        this.nameField.method_47404(class_2561.method_43470("Введите имя друга..."));
        this.method_37063(this.nameField);

        this.method_37063(class_4185.method_46430(class_2561.method_43470("Применить"), b -> applyNameSettings())
                .method_46434(left + fieldW + 5, y, btnSaveW, 18).method_46431());

        this.method_37063(class_4185.method_46430(class_2561.method_43470("Боря"), b -> {
            CustomizerConfig updated = CustomizerConfig.get().copy();
            updated.name = CustomizerConfig.DEFAULT_NAME;
            updated.autoDecline = true;
            updated.genitive = updated.dative = updated.accusative = updated.instrumental = updated.prepositional = "";
            McFriendCustomizerClient.applyFromClient(updated);
            setStatus("Имя сброшено на «Боря»!", COL_STATUS_OK);
            rebuildWidgets();
        }).method_46434(left + fieldW + btnSaveW + 10, y, btnResetW, 18).method_46431());

        int toggleY = y + 25;
        int halfW = (innerW - 6) / 2;
        this.method_37063(class_4185.method_46430(
                class_2561.method_43470("Авто-падежи: " + (cfg.autoDecline ? "ВКЛ" : "РУЧНЫЕ")),
                b -> {
                    CustomizerConfig updated = CustomizerConfig.get().copy();
                    updated.name = this.nameField != null ? this.nameField.method_1882() : updated.name;
                    updated.autoDecline = !updated.autoDecline;
                    if (!updated.autoDecline) {
                        RussianDecliner.Declensions d = RussianDecliner.decline(updated.getEffectiveName());
                        if (updated.genitive.isBlank()) updated.genitive = d.genitive();
                        if (updated.dative.isBlank()) updated.dative = d.dative();
                        if (updated.accusative.isBlank()) updated.accusative = d.accusative();
                        if (updated.instrumental.isBlank()) updated.instrumental = d.instrumental();
                        if (updated.prepositional.isBlank()) updated.prepositional = d.prepositional();
                    }
                    McFriendCustomizerClient.applyFromClient(updated);
                    rebuildWidgets();
                }).method_46434(left, toggleY, halfW, 18).method_46431());

        this.method_37063(class_4185.method_46430(
                class_2561.method_43470("Имя над головой: " + (cfg.showNametag ? "ВКЛ" : "ВЫКЛ")),
                b -> {
                    CustomizerConfig updated = CustomizerConfig.get().copy();
                    updated.showNametag = !updated.showNametag;
                    McFriendCustomizerClient.applyFromClient(updated);
                    setStatus("Имя над головой: " + (updated.showNametag ? "ВКЛ" : "ВЫКЛ"), COL_STATUS_OK);
                    rebuildWidgets();
                }).method_46434(left + halfW + 6, toggleY, halfW, 18).method_46431());

        this.genField = this.datField = this.accField = this.insField = this.preField = null;
        if (!cfg.autoDecline) {
            RussianDecliner.Declensions d = cfg.resolveDeclensions();
            int caseY = toggleY + 28;
            int colW = (innerW - 12) / 3;
            this.genField = makeCaseField(left, caseY + 11, colW, d.genitive());
            this.datField = makeCaseField(left + colW + 6, caseY + 11, colW, d.dative());
            this.accField = makeCaseField(left + (colW + 6) * 2, caseY + 11, colW, d.accusative());
            this.insField = makeCaseField(left, caseY + 44, colW, d.instrumental());
            this.preField = makeCaseField(left + colW + 6, caseY + 44, colW, d.prepositional());
            this.method_37063(class_4185.method_46430(class_2561.method_43470("Сохранить падежи"), b -> applyNameSettings())
                    .method_46434(left + (colW + 6) * 2, caseY + 44, colW, 18).method_46431());
        }
    }

    private class_342 makeCaseField(int x, int y, int w, String val) {
        class_342 f = new class_342(this.field_22793, x, y, w, 18, class_2561.method_43470(""));
        f.method_1880(32);
        f.method_1852(val == null ? "" : val);
        this.method_37063(f);
        return f;
    }

    private void applyNameSettings() {
        CustomizerConfig updated = CustomizerConfig.get().copy();
        if (this.nameField != null) {
            String typed = this.nameField.method_1882().trim();
            updated.name = typed.isEmpty() ? CustomizerConfig.DEFAULT_NAME : typed;
        }
        if (!updated.autoDecline && this.genField != null) {
            updated.genitive = this.genField.method_1882().trim();
            updated.dative = this.datField.method_1882().trim();
            updated.accusative = this.accField.method_1882().trim();
            updated.instrumental = this.insField.method_1882().trim();
            updated.prepositional = this.preField.method_1882().trim();
        }
        McFriendCustomizerClient.applyFromClient(updated);
        setStatus("Имя применено: «" + updated.getEffectiveName() + "»!", COL_STATUS_OK);
    }

    // ==================== TAB 2: SKIN ====================
    private void initSkinTab() {
        this.skinFiles = ClientSkinManager.listLocalSkins();
        CustomizerConfig cfg = CustomizerConfig.get();

        int left = this.panelX + 12;
        int innerW = this.panelW - 24;
        this.skinListX = left;
        this.skinListY = this.contentTop + 12;
        this.skinListW = (int) (innerW * 0.50);
        this.skinListH = this.contentBottom - this.skinListY - 24;

        int rightX = this.skinListX + this.skinListW + 10;
        int rightW = innerW - this.skinListW - 10;

        // Bottom left utilities: open skins folder + refresh
        int halfLeft = (this.skinListW - 4) / 2;
        int utilY = this.skinListY + this.skinListH + 4;
        this.method_37063(class_4185.method_46430(class_2561.method_43470("Папка скинов"), b -> {
            try {
                class_156.method_668().method_60932(CustomizerConfig.getSkinsDir());
                setStatus("Открыта папка config/mcfriend-customizer/skins/", COL_STATUS_INFO);
            } catch (Exception e) {
                setStatus("Папка: config/mcfriend-customizer/skins/", COL_STATUS_INFO);
            }
        }).method_46434(this.skinListX, utilY, halfLeft, 18).method_46431());

        this.method_37063(class_4185.method_46430(class_2561.method_43470("Обновить"), b -> {
            this.skinFiles = ClientSkinManager.listLocalSkins();
            ClientSkinManager.forceReloadOnRenderThread();
            setStatus("Список скинов обновлён (" + this.skinFiles.size() + " шт.)", COL_STATUS_OK);
        }).method_46434(this.skinListX + halfLeft + 4, utilY, this.skinListW - halfLeft - 4, 18).method_46431());

        // Right controls: Model toggle + Cat eyes toggle (placed next to/below 48x48 head preview)
        int btnY = this.skinListY + 56;
        boolean classic = "classic".equalsIgnoreCase(cfg.skinModel);
        this.method_37063(class_4185.method_46430(
                class_2561.method_43470("Руки: " + (classic ? "Steve (4px)" : "Alex (3px)")),
                b -> {
                    CustomizerConfig updated = CustomizerConfig.get().copy();
                    updated.skinModel = "classic".equalsIgnoreCase(updated.skinModel) ? "slim" : "classic";
                    McFriendCustomizerClient.applyFromClient(updated);
                    setStatus("Модель рук: " + ("classic".equals(updated.skinModel) ? "Steve (обычные)" : "Alex (тонкие)"), COL_STATUS_OK);
                    rebuildWidgets();
                }).method_46434(rightX, btnY, rightW, 18).method_46431());

        this.method_37063(class_4185.method_46430(
                class_2561.method_43470("Кошачьи глаза: " + (cfg.showCatEyes ? "ВКЛ" : "ВЫКЛ")),
                b -> {
                    CustomizerConfig updated = CustomizerConfig.get().copy();
                    updated.showCatEyes = !updated.showCatEyes;
                    McFriendCustomizerClient.applyFromClient(updated);
                    setStatus("Кошачьи глаза: " + (updated.showCatEyes ? "ВКЛ" : "ВЫКЛ"), COL_STATUS_OK);
                    rebuildWidgets();
                }).method_46434(rightX, btnY + 22, rightW, 18).method_46431());

        // Skin download by Mojang nick or PNG URL
        int dlY = btnY + 56;
        this.skinDownloadField = new class_342(this.field_22793, rightX, dlY, rightW, 18, class_2561.method_43470(""));
        this.skinDownloadField.method_1880(256);
        this.skinDownloadField.method_47404(class_2561.method_43470("Ник Mojang или URL .png"));
        this.method_37063(this.skinDownloadField);

        this.skinDownloadBtn = class_4185.method_46430(
                class_2561.method_43470(this.downloadingSkin ? "Загрузка..." : "Скачать и применить"),
                b -> startSkinDownload()
        ).method_46434(rightX, dlY + 22, rightW, 18).method_46431();
        this.skinDownloadBtn.field_22763 = !this.downloadingSkin;
        this.method_37063(this.skinDownloadBtn);
    }

    private void startSkinDownload() {
        if (this.downloadingSkin || this.skinDownloadField == null) return;
        String input = this.skinDownloadField.method_1882().trim();
        if (input.isEmpty()) {
            setStatus("Введите ник игрока Minecraft или прямую ссылку на .png!", COL_STATUS_ERR);
            return;
        }
        this.downloadingSkin = true;
        if (this.skinDownloadBtn != null) {
            this.skinDownloadBtn.field_22763 = false;
            this.skinDownloadBtn.method_25355(class_2561.method_43470("Загрузка..."));
        }
        setStatus("Скачиваю скин «" + input + "»...", COL_STATUS_INFO);
        ClientSkinManager.downloadSkinAsync(input, res -> {
            this.downloadingSkin = false;
            if (res.ok()) {
                CustomizerConfig updated = CustomizerConfig.get().copy();
                updated.skinFile = res.fileName();
                if (res.detectedModel() != null) {
                    updated.skinModel = res.detectedModel();
                }
                updated.showCatEyes = false;
                McFriendCustomizerClient.applyFromClient(updated);
                setStatus(res.message(), COL_STATUS_OK);
            } else {
                setStatus(res.message(), COL_STATUS_ERR);
            }
            if ("skin".equals(this.currentTab)) {
                rebuildWidgets();
            }
        });
    }

    // ==================== TAB 3: PHRASES ====================
    private void initPhrasesTab() {
        this.filteredCategories = PhraseCatalog.search(this.phraseQuery);
        if (PhraseCatalog.get(this.selectedCategoryId) == null && !this.filteredCategories.isEmpty()) {
            this.selectedCategoryId = this.filteredCategories.get(0).id();
        }
        CustomizerConfig.PhraseOverride ov = CustomizerConfig.get().phraseOverrides.get(this.selectedCategoryId);
        this.editorMode = (ov != null && "APPEND".equalsIgnoreCase(ov.mode)) ? "APPEND" : "REPLACE";

        int left = this.panelX + 10;
        int innerW = this.panelW - 20;
        int leftW = (int) (innerW * 0.37);
        int rightX = left + leftW + 8;
        int rightW = innerW - leftW - 8;

        // Left: search input + scrollable category list
        this.phraseSearchField = new class_342(this.field_22793, left, this.contentTop + 2, leftW, 16, class_2561.method_43470(""));
        this.phraseSearchField.method_1880(64);
        this.phraseSearchField.method_1852(this.phraseQuery);
        this.phraseSearchField.method_47404(class_2561.method_43470("Поиск (465 тем)..."));
        this.phraseSearchField.method_1863(q -> {
            this.phraseQuery = q;
            this.filteredCategories = PhraseCatalog.search(q);
            this.catScroll = 0;
        });
        this.method_37063(this.phraseSearchField);

        this.catListX = left;
        this.catListY = this.contentTop + 21;
        this.catListW = leftW;
        this.catListH = this.contentBottom - this.catListY - 22;

        // Left bottom: open lines dir + reload .md
        int lh = (leftW - 3) / 2;
        this.method_37063(class_4185.method_46430(class_2561.method_43470("Папка .md"), b -> {
            try {
                class_156.method_668().method_60932(CustomizerConfig.getLinesDir());
                setStatus("Открыта папка config/mcfriend-customizer/lines/", COL_STATUS_INFO);
            } catch (Exception e) {
                setStatus("Папка: config/mcfriend-customizer/lines/", COL_STATUS_INFO);
            }
        }).method_46434(left, this.catListY + this.catListH + 3, lh, 17).method_46431());

        this.method_37063(class_4185.method_46430(class_2561.method_43470("Релоад"), b -> {
            PhraseEngine.reloadLines();
            setStatus("Все фразы и .md файлы перезагружены!", COL_STATUS_OK);
            loadCategoryIntoEditor();
        }).method_46434(left + lh + 3, this.catListY + this.catListH + 3, leftW - lh - 3, 17).method_46431());

        // Right: multi-line EditBoxWidget + action row + global word replacement row
        int editorY = this.contentTop + 14;
        int editorH = Math.max(68, this.contentBottom - editorY - 46);
        this.phraseEditor = new class_7529(
                this.field_22793,
                rightX,
                editorY,
                rightW,
                editorH,
                class_2561.method_43470("По одной фразе на строку..."),
                class_2561.method_43470("Фразы")
        );
        this.phraseEditor.method_44402(16000);
        loadCategoryIntoEditor();
        this.method_37063(this.phraseEditor);

        // Action row 1: Mode, Save, Reset, Say, .md
        int actY = editorY + editorH + 3;
        int gap = 2;
        int modeW = 66;
        int saveW = 58;
        int rstW = 42;
        int sayW = 50;
        int mdW = Math.max(28, rightW - modeW - saveW - rstW - sayW - gap * 4);
        int cx = rightX;

        this.phraseModeBtn = class_4185.method_46430(
                class_2561.method_43470("APPEND".equals(this.editorMode) ? "+Добав." : "Замена"),
                b -> {
                    this.editorMode = "APPEND".equals(this.editorMode) ? "REPLACE" : "APPEND";
                    b.method_25355(class_2561.method_43470("APPEND".equals(this.editorMode) ? "+Добав." : "Замена"));
                    setStatus("Режим: " + ("APPEND".equals(this.editorMode) ? "добавить к стандартным" : "заменить стандартные"), COL_STATUS_INFO);
                }).method_46434(cx, actY, modeW, 17).method_46431();
        this.method_37063(this.phraseModeBtn);
        cx += modeW + gap;

        this.method_37063(class_4185.method_46430(class_2561.method_43470("Сохранить"), b -> saveCurrentCategoryPhrases())
                .method_46434(cx, actY, saveW, 17).method_46431());
        cx += saveW + gap;

        this.method_37063(class_4185.method_46430(class_2561.method_43470("Сброс"), b -> {
            CustomizerConfig updated = CustomizerConfig.get().copy();
            updated.phraseOverrides.remove(this.selectedCategoryId);
            McFriendCustomizerClient.applyFromClient(updated);
            this.editorMode = "REPLACE";
            setStatus("Категория «" + this.selectedCategoryId + "» сброшена!", COL_STATUS_OK);
            loadCategoryIntoEditor();
        }).method_46434(cx, actY, rstW, 17).method_46431());
        cx += rstW + gap;

        this.method_37063(class_4185.method_46430(class_2561.method_43470("Сказать"), b -> {
            String firstLine = getFirstNonEmptyLine();
            if (firstLine.isEmpty()) {
                setStatus("Введите фразу, чтобы друг её сказал!", COL_STATUS_ERR);
            } else {
                McFriendCustomizerClient.sendTestPhrase(firstLine);
                setStatus("Друг говорит: «" + trimShort(firstLine, 34) + "»", COL_STATUS_OK);
            }
        }).method_46434(cx, actY, sayW, 17).method_46431());
        cx += sayW + gap;

        this.method_37063(class_4185.method_46430(class_2561.method_43470(".md"), b -> {
            try {
                Path out = PhraseEngine.exportCategoryToDisk(this.selectedCategoryId);
                setStatus("Сохранено: lines/" + out.getFileName(), COL_STATUS_OK);
            } catch (Exception e) {
                setStatus("Ошибка: " + e.getMessage(), COL_STATUS_ERR);
            }
        }).method_46434(cx, actY, mdW, 17).method_46431());

        // Action row 2: Global word replacement (find -> replace + add + clear)
        int repY = actY + 21;
        int addW = 24;
        int clrW = 54;
        int inpW = Math.max(40, (rightW - addW - clrW - 8) / 2);

        this.replFromField = new class_342(this.field_22793, rightX, repY, inpW, 16, class_2561.method_43470(""));
        this.replFromField.method_1880(48);
        this.replFromField.method_47404(class_2561.method_43470("Слово..."));
        this.method_37063(this.replFromField);

        this.replToField = new class_342(this.field_22793, rightX + inpW + 3, repY, inpW, 16, class_2561.method_43470(""));
        this.replToField.method_1880(48);
        this.replToField.method_47404(class_2561.method_43470("Заменить на..."));
        this.method_37063(this.replToField);

        this.method_37063(class_4185.method_46430(class_2561.method_43470("+"), b -> {
            String f = this.replFromField.method_1882().trim();
            String t = this.replToField.method_1882().trim();
            if (f.isEmpty()) {
                setStatus("Введите слово для автозамены во всех фразах!", COL_STATUS_ERR);
                return;
            }
            CustomizerConfig updated = CustomizerConfig.get().copy();
            updated.globalReplacements.put(f, t);
            McFriendCustomizerClient.applyFromClient(updated);
            this.replFromField.method_1852("");
            this.replToField.method_1852("");
            setStatus("Автозамена добавлена: «" + f + "» → «" + t + "»", COL_STATUS_OK);
            rebuildWidgets();
        }).method_46434(rightX + inpW * 2 + 6, repY, addW, 16).method_46431());

        int rCount = CustomizerConfig.get().globalReplacements.size();
        this.method_37063(class_4185.method_46430(class_2561.method_43470("Замен:" + rCount), b -> {
            if (CustomizerConfig.get().globalReplacements.isEmpty()) {
                setStatus("Глобальных автозамен пока нет.", COL_STATUS_INFO);
                return;
            }
            CustomizerConfig updated = CustomizerConfig.get().copy();
            updated.globalReplacements.clear();
            McFriendCustomizerClient.applyFromClient(updated);
            setStatus("Все глобальные автозамены очищены.", COL_STATUS_OK);
            rebuildWidgets();
        }).method_46434(rightX + inpW * 2 + 6 + addW + 2, repY, rightW - (inpW * 2 + 8 + addW), 16).method_46431());
    }

    private void loadCategoryIntoEditor() {
        if (this.phraseEditor == null) return;
        List<String> lines = PhraseEngine.getEffectiveEditablePhrases(this.selectedCategoryId);
        this.phraseEditor.method_44400(String.join("\n", lines));
    }

    private void saveCurrentCategoryPhrases() {
        if (this.phraseEditor == null || this.selectedCategoryId == null) return;
        String raw = this.phraseEditor.method_44405();
        List<String> lines = new ArrayList<>();
        if (raw != null) {
            for (String line : raw.split("\\r?\\n")) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty()) {
                    lines.add(PhraseEngine.stripLeadingNumber(trimmed));
                }
            }
        }
        CustomizerConfig updated = CustomizerConfig.get().copy();
        if (lines.isEmpty()) {
            updated.phraseOverrides.remove(this.selectedCategoryId);
            setStatus("Пустой список — категория «" + this.selectedCategoryId + "» сброшена.", COL_STATUS_INFO);
        } else {
            updated.phraseOverrides.put(this.selectedCategoryId, new CustomizerConfig.PhraseOverride(this.editorMode, lines));
            setStatus("Сохранено " + lines.size() + " фраз для «" + this.selectedCategoryId + "»!", COL_STATUS_OK);
        }
        McFriendCustomizerClient.applyFromClient(updated);
    }

    private String getFirstNonEmptyLine() {
        if (this.phraseEditor == null) return "";
        String raw = this.phraseEditor.method_44405();
        if (raw == null) return "";
        for (String line : raw.split("\\r?\\n")) {
            String t = PhraseEngine.stripLeadingNumber(line.trim());
            // Strip [tags] for test preview bubble
            t = t.replaceAll("\\[[^\\]]*\\]", "").trim();
            if (!t.isEmpty()) return t;
        }
        return "";
    }

    // ==================== TAB 4: PROFILES ====================
    private void initProfilesTab() {
        this.profileList = CustomizerConfig.listProfiles();
        if (!this.profileList.contains(this.selectedProfile)) {
            this.selectedProfile = this.profileList.isEmpty() ? "" : this.profileList.get(0);
        }

        int left = this.panelX + 12;
        int innerW = this.panelW - 24;
        int topY = this.contentTop + 12;

        int saveW = 112;
        int nameW = innerW - saveW - 6;
        this.profileNameField = new class_342(this.field_22793, left, topY, nameW, 18, class_2561.method_43470(""));
        this.profileNameField.method_1880(32);
        this.profileNameField.method_47404(class_2561.method_43470("Название профиля (напр. Барсик)..."));
        this.method_37063(this.profileNameField);

        this.method_37063(class_4185.method_46430(class_2561.method_43470("Сохранить профиль"), b -> {
            String pName = this.profileNameField.method_1882().trim();
            if (pName.isEmpty()) {
                pName = CustomizerConfig.get().getEffectiveName();
            }
            if (CustomizerConfig.saveProfile(pName, CustomizerConfig.get())) {
                this.selectedProfile = CustomizerConfig.sanitizeFileName(pName);
                setStatus("Профиль «" + this.selectedProfile + "» сохранён!", COL_STATUS_OK);
                rebuildWidgets();
            } else {
                setStatus("Некорректное название профиля!", COL_STATUS_ERR);
            }
        }).method_46434(left + nameW + 6, topY, saveW, 18).method_46431());

        this.profListX = left;
        this.profListY = topY + 25;
        this.profListW = (int) (innerW * 0.50);
        this.profListH = this.contentBottom - this.profListY - 24;

        int rightX = this.profListX + this.profListW + 10;
        int rightW = innerW - this.profListW - 10;

        int actY = this.profListY + this.profListH + 4;
        int halfL = (this.profListW - 4) / 2;

        class_4185 loadBtn = class_4185.method_46430(class_2561.method_43470("Загрузить"), b -> {
            if (this.selectedProfile.isEmpty()) return;
            CustomizerConfig loaded = CustomizerConfig.loadProfile(this.selectedProfile);
            if (loaded != null) {
                McFriendCustomizerClient.applyFromClient(loaded);
                setStatus("Профиль «" + this.selectedProfile + "» загружен!", COL_STATUS_OK);
                rebuildWidgets();
            }
        }).method_46434(this.profListX, actY, halfL, 18).method_46431();
        loadBtn.field_22763 = !this.selectedProfile.isEmpty();
        this.method_37063(loadBtn);

        class_4185 delBtn = class_4185.method_46430(class_2561.method_43470("Удалить"), b -> {
            if (this.selectedProfile.isEmpty()) return;
            if (CustomizerConfig.deleteProfile(this.selectedProfile)) {
                setStatus("Профиль «" + this.selectedProfile + "» удалён.", COL_STATUS_INFO);
                this.selectedProfile = "";
                rebuildWidgets();
            }
        }).method_46434(this.profListX + halfL + 4, actY, this.profListW - halfL - 4, 18).method_46431();
        delBtn.field_22763 = !this.selectedProfile.isEmpty();
        this.method_37063(delBtn);

        // Right bottom: reset everything safely
        this.method_37063(class_4185.method_46430(class_2561.method_43470("Сбросить всё к Боре"), b -> {
            if (!this.confirmResetAll) {
                this.confirmResetAll = true;
                b.method_25355(class_2561.method_43470("Точно сбросить?"));
                setStatus("Нажмите ещё раз для полного сброса настроек.", COL_STATUS_ERR);
                return;
            }
            McFriendCustomizerClient.applyFromClient(new CustomizerConfig());
            setStatus("Все настройки сброшены к стандартному Боре!", COL_STATUS_OK);
            rebuildWidgets();
        }).method_46434(rightX, actY, rightW, 18).method_46431());
    }

    // ==================== RENDERING ====================
    @Override
    public void method_25394(class_332 g, int mouseX, int mouseY, float delta) {
        // Subtle dim backdrop
        g.method_25294(0, 0, this.field_22789, this.field_22790, 0x88000000);

        // Main panel frame + golden border
        g.method_25294(this.panelX, this.panelY, this.panelX + this.panelW, this.panelY + this.panelH, COL_BG);
        g.method_49601(this.panelX, this.panelY, this.panelW, this.panelH, COL_BORDER_GOLD);
        g.method_25294(this.panelX + 1, this.panelY + 42, this.panelX + this.panelW - 1, this.panelY + 43, COL_BORDER_SUB);
        g.method_25294(this.panelX + 1, this.panelY + this.panelH - 25, this.panelX + this.panelW - 1, this.panelY + this.panelH - 24, COL_BORDER_SUB);

        // Header title & live friend summary
        CustomizerConfig cfg = CustomizerConfig.get();
        g.method_25303(this.field_22793, "Настройка друга", this.panelX + 12, this.panelY + 8, COL_TEXT_GOLD);
        String summary = cfg.getEffectiveName() + "  •  " + (cfg.skinFile.isEmpty() ? "скин Бори" : trimShort(cfg.skinFile, 22));
        int sumW = this.field_22793.method_1727(summary);
        g.method_25303(this.field_22793, summary, this.panelX + this.panelW - sumW - 12, this.panelY + 8, COL_TEXT_MUTED);

        // Render tab-specific custom panels & previews BEFORE widgets
        switch (this.currentTab) {
            case "skin" -> renderSkinTabContent(g, mouseX, mouseY);
            case "phrases" -> renderPhrasesTabContent(g, mouseX, mouseY);
            case "profiles" -> renderProfilesTabContent(g, mouseX, mouseY);
            default -> renderNameTabContent(g);
        }

        // Render child widgets (buttons, text fields, edit box)
        super.method_25394(g, mouseX, mouseY, delta);

        // Status message in footer
        if (this.statusText != null && !this.statusText.isEmpty()) {
            int maxW = this.panelW - 92;
            String trimmed = this.field_22793.method_27523(this.statusText, maxW);
            g.method_25303(this.field_22793, trimmed, this.panelX + 12, this.panelY + this.panelH - 17, this.statusColor);
        }
    }

    private void renderNameTabContent(class_332 g) {
        CustomizerConfig cfg = CustomizerConfig.get();
        int left = this.panelX + 14;
        int innerW = this.panelW - 28;
        g.method_25303(this.field_22793, "Имя друга (меняется в окнах, диалогах и фразах):", left, this.contentTop + 3, COL_TEXT_MUTED);

        String previewName = (this.nameField != null && !this.nameField.method_1882().isBlank())
                ? this.nameField.method_1882().trim()
                : cfg.getEffectiveName();

        if (cfg.autoDecline) {
            RussianDecliner.Declensions d = RussianDecliner.decline(previewName);
            int boxY = this.contentTop + 68;
            int boxH = Math.max(76, this.contentBottom - boxY - 4);
            g.method_25294(left, boxY, left + innerW, boxY + boxH, COL_PANEL_DARK);
            g.method_49601(left, boxY, innerW, boxH, COL_BORDER_SUB);

            int ty = boxY + 8;
            g.method_25303(this.field_22793, "Предпросмотр склонений:", left + 8, ty, COL_TEXT_GOLD);
            ty += 14;
            g.method_25303(this.field_22793, "Кто: " + d.nominative() + "    •    Мешок " + d.genitive() + "    •    Дать " + d.dative(), left + 8, ty, COL_TEXT_MAIN);
            ty += 13;
            g.method_25303(this.field_22793, "Позвать " + d.accusative() + "    •    Идти с " + d.instrumental() + "    •    О " + d.prepositional(), left + 8, ty, COL_TEXT_MAIN);
            ty += 17;
            g.method_25303(this.field_22793, "Совет: в своих фразах можно писать {имя}, {кого}, {кому}, {кем}!", left + 8, ty, COL_TEXT_MUTED);
            if (ty + 13 < boxY + boxH - 4) {
                g.method_25303(this.field_22793, "Пример: «Привет! Я " + d.nominative() + ", давай дружить!»", left + 8, ty + 13, COL_STATUS_OK);
            }
        } else {
            int caseY = this.contentTop + 67;
            int colW = (innerW - 12) / 3;
            g.method_25303(this.field_22793, "Род. (мешок кого?):", left, caseY, COL_TEXT_MUTED);
            g.method_25303(this.field_22793, "Дат. (дать кому?):", left + colW + 6, caseY, COL_TEXT_MUTED);
            g.method_25303(this.field_22793, "Вин. (позвать кого?):", left + (colW + 6) * 2, caseY, COL_TEXT_MUTED);
            g.method_25303(this.field_22793, "Твор. (идти с кем?):", left, caseY + 33, COL_TEXT_MUTED);
            g.method_25303(this.field_22793, "Пред. (о ком?):", left + colW + 6, caseY + 33, COL_TEXT_MUTED);
        }
    }

    private void renderSkinTabContent(class_332 g, int mouseX, int mouseY) {
        CustomizerConfig cfg = CustomizerConfig.get();
        g.method_25303(this.field_22793, "Список скинов (" + this.skinFiles.size() + " PNG):", this.skinListX, this.contentTop + 2, COL_TEXT_MUTED);

        // Skin list box
        g.method_25294(this.skinListX, this.skinListY, this.skinListX + this.skinListW, this.skinListY + this.skinListH, COL_PANEL_DARK);
        g.method_49601(this.skinListX, this.skinListY, this.skinListW, this.skinListH, COL_BORDER_SUB);

        int rowH = 16;
        int totalRows = 1 + this.skinFiles.size();
        int visibleRows = Math.max(1, (this.skinListH - 4) / rowH);
        int maxScroll = Math.max(0, totalRows - visibleRows);
        this.skinScroll = Math.max(0, Math.min(this.skinScroll, maxScroll));

        g.method_44379(this.skinListX + 1, this.skinListY + 1, this.skinListX + this.skinListW - 1, this.skinListY + this.skinListH - 1);
        for (int i = 0; i < visibleRows && (this.skinScroll + i) < totalRows; i++) {
            int idx = this.skinScroll + i;
            int ry = this.skinListY + 2 + i * rowH;
            String file = (idx == 0) ? "" : this.skinFiles.get(idx - 1);
            boolean selected = file.equals(cfg.skinFile);
            boolean hovered = mouseX >= this.skinListX + 2 && mouseX <= this.skinListX + this.skinListW - 2
                    && mouseY >= ry && mouseY < ry + rowH;
            if (selected) {
                g.method_25294(this.skinListX + 2, ry, this.skinListX + this.skinListW - 2, ry + rowH - 1, COL_ROW_SEL);
            } else if (hovered) {
                g.method_25294(this.skinListX + 2, ry, this.skinListX + this.skinListW - 2, ry + rowH - 1, COL_ROW_HOV);
            }
            String label = (idx == 0) ? "★ Стандартный скин Бори" : file;
            String fitted = this.field_22793.method_27523(label, this.skinListW - 10);
            g.method_25303(this.field_22793, fitted, this.skinListX + 5, ry + 4, selected ? COL_TEXT_GOLD : COL_TEXT_MAIN);
        }
        g.method_44380();
        drawScrollbar(g, this.skinListX + this.skinListW - 4, this.skinListY + 2, this.skinListH - 4, this.skinScroll, totalRows, visibleRows);

        // Right side: Live 48x48 head preview (base face + overlay hat)
        int rightX = this.skinListX + this.skinListW + 10;
        int rightW = (this.panelX + this.panelW - 12) - rightX;
        int prevSize = 48;
        g.method_25294(rightX, this.skinListY, rightX + prevSize + 4, this.skinListY + prevSize + 4, COL_PANEL_DARK);
        g.method_49601(rightX, this.skinListY, prevSize + 4, prevSize + 4, COL_BORDER_GOLD);

        class_2960 tex = ClientSkinManager.getPreviewTexture();
        if (tex != null) {
            // Base face (u=8,v=8, 8x8 in 64x64 UV space)
            g.method_25293(tex, rightX + 2, this.skinListY + 2, prevSize, prevSize, 8.0f, 8.0f, 8, 8, 64, 64);
            // Hat overlay (u=40,v=8, 8x8 in 64x64 UV space)
            g.method_25293(tex, rightX + 2, this.skinListY + 2, prevSize, prevSize, 40.0f, 8.0f, 8, 8, 64, 64);
        }

        int infoX = rightX + prevSize + 10;
        int infoW = Math.max(40, rightW - prevSize - 10);
        g.method_25303(this.field_22793, "Активный скин:", infoX, this.skinListY + 4, COL_TEXT_MUTED);
        String activeName = cfg.skinFile.isEmpty() ? "Боря (станд.)" : cfg.skinFile;
        g.method_25303(this.field_22793, this.field_22793.method_27523(activeName, infoW), infoX, this.skinListY + 16, COL_TEXT_GOLD);
        g.method_25303(this.field_22793, "64x64 / 64x32", infoX, this.skinListY + 30, COL_TEXT_MUTED);
        g.method_25303(this.field_22793, "PNG / HD PNG", infoX, this.skinListY + 40, COL_TEXT_MUTED);

        // Download heading
        g.method_25303(this.field_22793, "Скачать по нику / ссылке:", rightX, this.skinListY + 101, COL_TEXT_MUTED);
    }

    private void renderPhrasesTabContent(class_332 g, int mouseX, int mouseY) {
        CustomizerConfig cfg = CustomizerConfig.get();

        // Left category list
        g.method_25294(this.catListX, this.catListY, this.catListX + this.catListW, this.catListY + this.catListH, COL_PANEL_DARK);
        g.method_49601(this.catListX, this.catListY, this.catListW, this.catListH, COL_BORDER_SUB);

        int rowH = 15;
        int total = this.filteredCategories.size();
        int visible = Math.max(1, (this.catListH - 4) / rowH);
        int maxScroll = Math.max(0, total - visible);
        this.catScroll = Math.max(0, Math.min(this.catScroll, maxScroll));

        g.method_44379(this.catListX + 1, this.catListY + 1, this.catListX + this.catListW - 1, this.catListY + this.catListH - 1);
        for (int i = 0; i < visible && (this.catScroll + i) < total; i++) {
            PhraseCatalog.Entry e = this.filteredCategories.get(this.catScroll + i);
            int ry = this.catListY + 2 + i * rowH;
            boolean sel = e.id().equals(this.selectedCategoryId);
            boolean hov = mouseX >= this.catListX + 2 && mouseX <= this.catListX + this.catListW - 2
                    && mouseY >= ry && mouseY < ry + rowH;
            if (sel) {
                g.method_25294(this.catListX + 2, ry, this.catListX + this.catListW - 2, ry + rowH - 1, COL_ROW_SEL);
            } else if (hov) {
                g.method_25294(this.catListX + 2, ry, this.catListX + this.catListW - 2, ry + rowH - 1, COL_ROW_HOV);
            }
            boolean hasJson = cfg.phraseOverrides.containsKey(e.id());
            boolean hasMd = PhraseEngine.hasDiskOverride(e.id());
            String prefix = hasJson ? "★ " : (hasMd ? "▪ " : "");
            String label = prefix + e.title();
            String fitted = this.field_22793.method_27523(label, this.catListW - 10);
            int col = sel ? COL_TEXT_GOLD : (hasJson || hasMd ? COL_STATUS_OK : COL_TEXT_MAIN);
            g.method_25303(this.field_22793, fitted, this.catListX + 4, ry + 3, col);
        }
        g.method_44380();
        drawScrollbar(g, this.catListX + this.catListW - 4, this.catListY + 2, this.catListH - 4, this.catScroll, total, visible);

        // Right category heading
        int rightX = this.catListX + this.catListW + 8;
        int rightW = (this.panelX + this.panelW - 10) - rightX;
        PhraseCatalog.Entry cur = PhraseCatalog.get(this.selectedCategoryId);
        String header = cur != null
                ? cur.title() + " (" + cur.id() + ")"
                : this.selectedCategoryId;
        g.method_25303(this.field_22793, this.field_22793.method_27523(header, rightW), rightX, this.contentTop + 3, COL_TEXT_GOLD);
    }

    private void renderProfilesTabContent(class_332 g, int mouseX, int mouseY) {
        g.method_25303(this.field_22793, "Сохраняйте готовых персонажей (имя + скин + фразы) в 1 клик:", this.panelX + 12, this.contentTop + 1, COL_TEXT_MUTED);

        // Left profiles list
        g.method_25294(this.profListX, this.profListY, this.profListX + this.profListW, this.profListY + this.profListH, COL_PANEL_DARK);
        g.method_49601(this.profListX, this.profListY, this.profListW, this.profListH, COL_BORDER_SUB);

        int rowH = 16;
        int total = this.profileList.size();
        int visible = Math.max(1, (this.profListH - 4) / rowH);
        int maxScroll = Math.max(0, total - visible);
        this.profileScroll = Math.max(0, Math.min(this.profileScroll, maxScroll));

        if (total == 0) {
            g.method_25303(this.field_22793, "Пока нет профилей", this.profListX + 8, this.profListY + 10, COL_TEXT_MUTED);
        } else {
            g.method_44379(this.profListX + 1, this.profListY + 1, this.profListX + this.profListW - 1, this.profListY + this.profListH - 1);
            for (int i = 0; i < visible && (this.profileScroll + i) < total; i++) {
                String pName = this.profileList.get(this.profileScroll + i);
                int ry = this.profListY + 2 + i * rowH;
                boolean sel = pName.equals(this.selectedProfile);
                boolean hov = mouseX >= this.profListX + 2 && mouseX <= this.profListX + this.profListW - 2
                        && mouseY >= ry && mouseY < ry + rowH;
                if (sel) {
                    g.method_25294(this.profListX + 2, ry, this.profListX + this.profListW - 2, ry + rowH - 1, COL_ROW_SEL);
                } else if (hov) {
                    g.method_25294(this.profListX + 2, ry, this.profListX + this.profListW - 2, ry + rowH - 1, COL_ROW_HOV);
                }
                g.method_25303(this.field_22793, this.field_22793.method_27523(pName, this.profListW - 10),
                        this.profListX + 6, ry + 4, sel ? COL_TEXT_GOLD : COL_TEXT_MAIN);
            }
            g.method_44380();
            drawScrollbar(g, this.profListX + this.profListW - 4, this.profListY + 2, this.profListH - 4, this.profileScroll, total, visible);
        }

        // Right card: Active config summary
        int rightX = this.profListX + this.profListW + 10;
        int rightW = (this.panelX + this.panelW - 12) - rightX;
        g.method_25294(rightX, this.profListY, rightX + rightW, this.profListY + this.profListH, COL_PANEL_DARK);
        g.method_49601(rightX, this.profListY, rightW, this.profListH, COL_BORDER_SUB);

        CustomizerConfig cfg = CustomizerConfig.get();
        int ty = this.profListY + 8;
        g.method_25303(this.field_22793, "Текущий образ:", rightX + 8, ty, COL_TEXT_GOLD);
        ty += 15;
        g.method_25303(this.field_22793, this.field_22793.method_27523("Имя: " + cfg.getEffectiveName(), rightW - 14), rightX + 8, ty, COL_TEXT_MAIN);
        ty += 13;
        String sk = cfg.skinFile.isEmpty() ? "Стандартный" : cfg.skinFile;
        g.method_25303(this.field_22793, this.field_22793.method_27523("Скин: " + sk, rightW - 14), rightX + 8, ty, COL_TEXT_MAIN);
        ty += 13;
        g.method_25303(this.field_22793, "Руки: " + ("classic".equalsIgnoreCase(cfg.skinModel) ? "Steve" : "Alex")
                + " • Глаза: " + (cfg.showCatEyes ? "ВКЛ" : "ВЫКЛ"), rightX + 8, ty, COL_TEXT_MAIN);
        ty += 13;
        g.method_25303(this.field_22793, "Свои фразы: " + cfg.phraseOverrides.size() + " тем", rightX + 8, ty, COL_TEXT_MAIN);
        ty += 13;
        g.method_25303(this.field_22793, "Автозамены слов: " + cfg.globalReplacements.size(), rightX + 8, ty, COL_TEXT_MAIN);
        if (!cfg.globalReplacements.isEmpty() && ty + 16 < this.profListY + this.profListH - 4) {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> e : cfg.globalReplacements.entrySet()) {
                if (!sb.isEmpty()) sb.append(", ");
                sb.append(e.getKey()).append("→").append(e.getValue());
            }
            g.method_25303(this.field_22793, this.field_22793.method_27523(sb.toString(), rightW - 14), rightX + 8, ty + 14, COL_TEXT_MUTED);
        }
    }

    private static void drawScrollbar(class_332 g, int x, int y, int h, int scroll, int total, int visible) {
        if (total <= visible || h <= 6) return;
        int barH = Math.max(12, (h * visible) / total);
        int maxScroll = total - visible;
        int barY = y + ((h - barH) * scroll) / Math.max(1, maxScroll);
        g.method_25294(x, y, x + 2, y + h, 0x44FFFFFF);
        g.method_25294(x, barY, x + 2, barY + barH, COL_BORDER_GOLD);
    }

    // ==================== INPUT ====================
    @Override
    public boolean method_25402(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if ("skin".equals(this.currentTab) && inside(mouseX, mouseY, this.skinListX, this.skinListY, this.skinListW, this.skinListH)) {
                int idx = this.skinScroll + (int) ((mouseY - (this.skinListY + 2)) / 16.0);
                int total = 1 + this.skinFiles.size();
                if (idx >= 0 && idx < total) {
                    CustomizerConfig updated = CustomizerConfig.get().copy();
                    if (idx == 0) {
                        updated.skinFile = "";
                        updated.skinModel = "slim";
                        updated.showCatEyes = true;
                        setStatus("Выбран стандартный скин Бори!", COL_STATUS_OK);
                    } else {
                        String chosen = this.skinFiles.get(idx - 1);
                        updated.skinFile = chosen;
                        updated.showCatEyes = false;
                        setStatus("Применён скин: " + chosen, COL_STATUS_OK);
                    }
                    McFriendCustomizerClient.applyFromClient(updated);
                    rebuildWidgets();
                    return true;
                }
            } else if ("phrases".equals(this.currentTab) && inside(mouseX, mouseY, this.catListX, this.catListY, this.catListW, this.catListH)) {
                int idx = this.catScroll + (int) ((mouseY - (this.catListY + 2)) / 15.0);
                if (idx >= 0 && idx < this.filteredCategories.size()) {
                    PhraseCatalog.Entry chosen = this.filteredCategories.get(idx);
                    this.selectedCategoryId = chosen.id();
                    CustomizerConfig.PhraseOverride ov = CustomizerConfig.get().phraseOverrides.get(this.selectedCategoryId);
                    this.editorMode = (ov != null && "APPEND".equalsIgnoreCase(ov.mode)) ? "APPEND" : "REPLACE";
                    if (this.phraseModeBtn != null) {
                        this.phraseModeBtn.method_25355(class_2561.method_43470("APPEND".equals(this.editorMode) ? "+Добав." : "Замена"));
                    }
                    loadCategoryIntoEditor();
                    setStatus("Выбрана тема: " + chosen.title() + " (" + chosen.defaultPhrases().size() + " фраз)", COL_STATUS_INFO);
                    return true;
                }
            } else if ("profiles".equals(this.currentTab) && inside(mouseX, mouseY, this.profListX, this.profListY, this.profListW, this.profListH)) {
                int idx = this.profileScroll + (int) ((mouseY - (this.profListY + 2)) / 16.0);
                if (idx >= 0 && idx < this.profileList.size()) {
                    this.selectedProfile = this.profileList.get(idx);
                    rebuildWidgets();
                    return true;
                }
            }
        }
        return super.method_25402(mouseX, mouseY, button);
    }

    @Override
    public boolean method_25401(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int step = verticalAmount > 0 ? -1 : 1;
        if ("skin".equals(this.currentTab) && inside(mouseX, mouseY, this.skinListX, this.skinListY, this.skinListW, this.skinListH)) {
            this.skinScroll = Math.max(0, this.skinScroll + step);
            return true;
        }
        if ("phrases".equals(this.currentTab) && inside(mouseX, mouseY, this.catListX, this.catListY, this.catListW, this.catListH)) {
            this.catScroll = Math.max(0, this.catScroll + step * 2);
            return true;
        }
        if ("profiles".equals(this.currentTab) && inside(mouseX, mouseY, this.profListX, this.profListY, this.profListW, this.profListH)) {
            this.profileScroll = Math.max(0, this.profileScroll + step);
            return true;
        }
        return super.method_25401(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean method_25404(int keyCode, int scanCode, int modifiers) {
        // Enter key in Name tab or Skin download field applies immediately!
        if (keyCode == 257 || keyCode == 335) {
            if ("name".equals(this.currentTab) && this.nameField != null && this.nameField.method_25370()) {
                applyNameSettings();
                return true;
            }
            if ("skin".equals(this.currentTab) && this.skinDownloadField != null && this.skinDownloadField.method_25370()) {
                startSkinDownload();
                return true;
            }
        }
        return super.method_25404(keyCode, scanCode, modifiers);
    }

    private static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    private void setStatus(String text, int color) {
        this.statusText = text;
        this.statusColor = color;
    }

    private static String trimShort(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}
