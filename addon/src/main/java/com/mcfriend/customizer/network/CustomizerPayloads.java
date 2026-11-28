package com.mcfriend.customizer.network;

import com.mcfriend.customizer.McFriendCustomizer;
import net.minecraft.class_2540;
import net.minecraft.class_8710;
import net.minecraft.class_9139;

public final class CustomizerPayloads {
    private static final int MAX_JSON_LEN = 262144;
    private static final int MAX_SHORT_LEN = 2048;

    private CustomizerPayloads() {
    }

    public record SyncConfigS2C(String configJson) implements class_8710 {
        public static final class_8710.class_9154<SyncConfigS2C> ID =
                new class_8710.class_9154<>(McFriendCustomizer.id("sync_config"));
        public static final class_9139<class_2540, SyncConfigS2C> CODEC =
                class_8710.method_56484(SyncConfigS2C::write, SyncConfigS2C::read);

        @Override
        public class_8710.class_9154<? extends class_8710> method_56479() {
            return ID;
        }

        private void write(class_2540 buf) {
            buf.method_10788(this.configJson == null ? "{}" : this.configJson, MAX_JSON_LEN);
        }

        private static SyncConfigS2C read(class_2540 buf) {
            return new SyncConfigS2C(buf.method_10800(MAX_JSON_LEN));
        }
    }

    public record UpdateConfigC2S(String configJson) implements class_8710 {
        public static final class_8710.class_9154<UpdateConfigC2S> ID =
                new class_8710.class_9154<>(McFriendCustomizer.id("update_config"));
        public static final class_9139<class_2540, UpdateConfigC2S> CODEC =
                class_8710.method_56484(UpdateConfigC2S::write, UpdateConfigC2S::read);

        @Override
        public class_8710.class_9154<? extends class_8710> method_56479() {
            return ID;
        }

        private void write(class_2540 buf) {
            buf.method_10788(this.configJson == null ? "{}" : this.configJson, MAX_JSON_LEN);
        }

        private static UpdateConfigC2S read(class_2540 buf) {
            return new UpdateConfigC2S(buf.method_10800(MAX_JSON_LEN));
        }
    }

    public record TestPhraseC2S(String text) implements class_8710 {
        public static final class_8710.class_9154<TestPhraseC2S> ID =
                new class_8710.class_9154<>(McFriendCustomizer.id("test_phrase"));
        public static final class_9139<class_2540, TestPhraseC2S> CODEC =
                class_8710.method_56484(TestPhraseC2S::write, TestPhraseC2S::read);

        @Override
        public class_8710.class_9154<? extends class_8710> method_56479() {
            return ID;
        }

        private void write(class_2540 buf) {
            buf.method_10788(this.text == null ? "" : this.text, MAX_SHORT_LEN);
        }

        private static TestPhraseC2S read(class_2540 buf) {
            return new TestPhraseC2S(buf.method_10800(MAX_SHORT_LEN));
        }
    }

    public record OpenGuiS2C(String initialTab) implements class_8710 {
        public static final class_8710.class_9154<OpenGuiS2C> ID =
                new class_8710.class_9154<>(McFriendCustomizer.id("open_gui"));
        public static final class_9139<class_2540, OpenGuiS2C> CODEC =
                class_8710.method_56484(OpenGuiS2C::write, OpenGuiS2C::read);

        @Override
        public class_8710.class_9154<? extends class_8710> method_56479() {
            return ID;
        }

        private void write(class_2540 buf) {
            buf.method_10788(this.initialTab == null ? "name" : this.initialTab, 64);
        }

        private static OpenGuiS2C read(class_2540 buf) {
            return new OpenGuiS2C(buf.method_10800(64));
        }
    }

    public record DownloadSkinS2C(String source) implements class_8710 {
        public static final class_8710.class_9154<DownloadSkinS2C> ID =
                new class_8710.class_9154<>(McFriendCustomizer.id("download_skin"));
        public static final class_9139<class_2540, DownloadSkinS2C> CODEC =
                class_8710.method_56484(DownloadSkinS2C::write, DownloadSkinS2C::read);

        @Override
        public class_8710.class_9154<? extends class_8710> method_56479() {
            return ID;
        }

        private void write(class_2540 buf) {
            buf.method_10788(this.source == null ? "" : this.source, MAX_SHORT_LEN);
        }

        private static DownloadSkinS2C read(class_2540 buf) {
            return new DownloadSkinS2C(buf.method_10800(MAX_SHORT_LEN));
        }
    }
}
