package wiki.minecraft.heywiki;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.DropdownMenuBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static dev.architectury.platform.Platform.getConfigFolder;

/**
 * The configuration for the Hey Wiki mod.
 */
public class HeyWikiConfig {
    private static final HeyWikiClient MOD = HeyWikiClient.getInstance();

    /**
     * Whether the user should be prompted for confirmation before opening a wiki page.
     */
    public static boolean requiresConfirmation = true;
    /**
     * Whether the user should be prompted for confirmation before opening a wiki page using a command.
     */
    public static boolean requiresConfirmationCommand = false;
    /**
     * The distance at which the player can raycast to find a {@link wiki.minecraft.heywiki.wiki.Target Target}.
     */
    public static double raycastReach = 5.2D; // Use creative mode reach distance
    /**
     * When true, the raycast will hit fluid blocks.
     */
    public static boolean raycastAllowFluid = false;
    /**
     * The language to use for wiki pages.
     */
    public static String language = "auto";
    /**
     * The variant of Chinese to use for wiki pages.
     */
    public static String zhVariant = "auto";

    /**
     * Creates a GUI for the configuration.
     *
     * @param parent The parent screen.
     * @return The configuration screen.
     */
    public static Screen createGui(Screen parent) {
        AtomicReference<Boolean> requireReload = new AtomicReference<>(false);

        List<String> languages = new ArrayList<>(MOD.wikiFamilyConfigManager().getAllAvailableLanguages());
        languages.addFirst("auto");

        ConfigBuilder builder = ConfigBuilder.create()
                                             .setParentScreen(parent)
                                             .setTitle(Text.translatable("options.heywiki.title"));

        ConfigCategory general = builder.getOrCreateCategory(Text.translatable("options.heywiki.general"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        general.addEntry(entryBuilder
                                 .startBooleanToggle(Text.translatable("options.heywiki.requires_confirmation.name"),
                                                     HeyWikiConfig.requiresConfirmation)
                                 .setDefaultValue(true)
                                 .setTooltip(Text.translatable("options.heywiki.requires_confirmation.description"))
                                 .setSaveConsumer(newValue -> HeyWikiConfig.requiresConfirmation = newValue)
                                 .build());
        general.addEntry(entryBuilder
                                 .startBooleanToggle(
                                         Text.translatable("options.heywiki.requires_confirmation_command.name"),
                                         HeyWikiConfig.requiresConfirmationCommand)
                                 .setDefaultValue(true)
                                 .setTooltip(
                                         Text.translatable("options.heywiki.requires_confirmation_command.description"))
                                 .setSaveConsumer(newValue -> HeyWikiConfig.requiresConfirmationCommand = newValue)
                                 .build());
        general.addEntry(entryBuilder
                                 .startDoubleField(Text.translatable("options.heywiki.raycast_reach.name"),
                                                   HeyWikiConfig.raycastReach)
                                 .setDefaultValue(5.2D)
                                 .setMin(0D)
                                 .setMax(64D)
                                 .setTooltip(Text.translatable("options.heywiki.raycast_reach.description"))
                                 .setSaveConsumer(newValue -> HeyWikiConfig.raycastReach = newValue)
                                 .build());
        general.addEntry(entryBuilder
                                 .startBooleanToggle(Text.translatable("options.heywiki.raycast_allow_fluid.name"),
                                                     HeyWikiConfig.raycastAllowFluid)
                                 .setDefaultValue(false)
                                 .setTooltip(Text.translatable("options.heywiki.raycast_allow_fluid.description"))
                                 .setSaveConsumer(newValue -> HeyWikiConfig.raycastAllowFluid = newValue)
                                 .build());
        general.addEntry(entryBuilder
                                 .startDropdownMenu(Text.translatable("options.heywiki.language.name"),
                                                    DropdownMenuBuilder.TopCellElementBuilder.of(language,
                                                                                                 HeyWikiConfig::normalizeLanguageName,
                                                                                                 HeyWikiConfig::languageDescription),
                                                    DropdownMenuBuilder.CellCreatorBuilder.of(
                                                            HeyWikiConfig::languageDescription))
                                 .setSelections(languages)
                                 .setDefaultValue("auto")
                                 .setSuggestionMode(false)
                                 .setTooltip(Text.translatable("options.heywiki.language.description"))
                                 .setSaveConsumer(newValue -> {
                                     if (!(newValue).equals(HeyWikiConfig.language))
                                         requireReload.set(true);
                                     HeyWikiConfig.language = newValue;
                                 })
                                 .build());
        general.addEntry(entryBuilder
                                 .startDropdownMenu(Text.translatable("options.heywiki.zh_variant.name"),
                                                    DropdownMenuBuilder.TopCellElementBuilder.of(zhVariant,
                                                                                                 HeyWikiConfig::normalizeLanguageName,
                                                                                                 HeyWikiConfig::zhVariantDescription),
                                                    DropdownMenuBuilder.CellCreatorBuilder.of(
                                                            HeyWikiConfig::zhVariantDescription))
                                 .setDisplayRequirement(() -> {
                                     for (var wiki : MOD.wikiFamilyConfigManager().activeWikis().values()) {
                                         if (wiki.language().wikiLanguage().startsWith("zh")) {
                                             return true;
                                         }
                                     }
                                     return false;
                                 })
                                 .setSelections(List.of("auto", "zh", "zh-cn", "zh-tw", "zh-hk"))
                                 .setDefaultValue("auto")
                                 .setSuggestionMode(false)
                                 .setTooltip(Text.translatable("options.heywiki.zh_variant.description"))
                                 .setSaveConsumer(newValue -> HeyWikiConfig.zhVariant = newValue)
                                 .build());
        general.addEntry(entryBuilder
                                 .fillKeybindingField(Text.translatable("key.heywiki.open"), HeyWikiClient.openWikiKey)
                                 .setTooltip(Text.translatable("options.heywiki.open_key.description"))
                                 .build());
        general.addEntry(entryBuilder
                                 .fillKeybindingField(Text.translatable("key.heywiki.open_search"),
                                                      HeyWikiClient.openWikiSearchKey)
                                 .setTooltip(Text.translatable("options.heywiki.open_key.description"))
                                 .build());

        builder.setSavingRunnable(() -> save(requireReload.get()));

        return builder.build();
    }

    private static String normalizeLanguageName(String name) {
        return name.split(":")[0];
    }

    private static Text languageDescription(String lang) {
        if (lang.equals("auto")) return Text.translatable("options.heywiki.language.auto");

        return Text.literal(lang + ": " + getLanguageName(lang));
    }

    private static Text zhVariantDescription(String lang) {
        return switch (lang) {
            case "auto" -> Text.translatable("options.heywiki.language.auto");
            case "zh" -> Text.literal("zh: 不转换");
            case "zh-cn" -> Text.literal("zh-cn: 大陆简体");
            case "zh-tw" -> Text.literal("zh-tw: 臺灣正體");
            case "zh-hk" -> Text.literal("zh-hk: 香港繁體");
            default -> throw new IllegalStateException("Unexpected value: " + lang);
        };
    }

    /**
     * Saves the configuration to a file.
     *
     * @param requireReload Whether the game should be reloaded after saving.
     */
    public static void save(Boolean requireReload) {
        if (requireReload) MinecraftClient.getInstance().reloadResourcesConcurrently();
        Path configPath = getConfigFolder().resolve("heywiki.json");
        try {
            Files.createDirectories(configPath.getParent());
            BufferedWriter writer = Files.newBufferedWriter(configPath);

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonWriter jsonWriter = gson.newJsonWriter(writer);
            try {
                jsonWriter.beginObject()
                          .name("requiresConfirmation").value(requiresConfirmation)
                          .name("requiresConfirmationCommand").value(requiresConfirmationCommand)
                          .name("language").value(language)
                          .name("zhVariant").value(zhVariant)
                          .name("raycastReach").value(raycastReach)
                          .name("raycastAllowFluid").value(raycastAllowFluid)
                          .endObject().close();
            } catch (IOException e) {
                jsonWriter.close();
                throw new RuntimeException("Failed to write config file", e);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to write config file", e);
        }
    }

    private static String getLanguageName(String lang) {
        Map<String, String> languageNames =
                Map.of(
                        "lzh", "文言"
                      );
        if (languageNames.containsKey(lang)) return languageNames.get(lang);

        var locale = Locale.of(lang);
        return locale.getDisplayLanguage(locale);
    }

    /**
     * Loads the configuration from a file.
     *
     * @throws RuntimeException if the file cannot be read
     */
    public static void load() {
        Path configPath = getConfigFolder().resolve("heywiki.json");
        if (!configPath.toFile().exists()) {
            return;
        }
        try {
            JsonParser.parseReader(new Gson().newJsonReader(Files.newBufferedReader(configPath))).getAsJsonObject()
                      .entrySet().forEach(entry -> {
                          switch (entry.getKey()) {
                              case "requiresConfirmation":
                                  requiresConfirmation = entry.getValue().getAsBoolean();
                                  break;
                              case "requiresConfirmationCommand":
                                  requiresConfirmationCommand = entry.getValue().getAsBoolean();
                                  break;
                              case "language":
                                  language = entry.getValue().getAsString();
                                  break;
                              case "zhVariant":
                                  zhVariant = entry.getValue().getAsString();
                                  break;
                              case "raycastReach":
                                  raycastReach = entry.getValue().getAsDouble();
                                  break;
                              case "raycastAllowFluid":
                                  raycastAllowFluid = entry.getValue().getAsBoolean();
                                  break;
                          }
                      });
        } catch (IOException e) {
            throw new RuntimeException("Failed to read config file", e);
        }
    }
}
