package sac;

import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;

import java.util.*;

public class EmojiData {

    private static final Collection<Emoji> emojis = EmojiManager.getAll();
    private static final EmojiCategorizer emojiCategorizer = new EmojiCategorizer();

    static {
        for (Emoji emoji : emojis) {
            emojiCategorizer.categorizeEmoji(emoji, emoji.getDescription(), emoji.getTags());
        }
    }

    public static Map<String, List<String>> getEmojis() {
        return emojiCategorizer.emojiCategories;
    }
}