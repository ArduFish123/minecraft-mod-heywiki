package wiki.minecraft.heywiki.wiki;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class WikiIndividual {
    public static Codec<WikiIndividual> CODEC = RecordCodecBuilder.create(builder ->
            builder
                    .group(
                            Codec.STRING.fieldOf("article_url").forGetter(wiki -> wiki.articleUrl),
                            Codec.STRING.fieldOf("mw_api_url").forGetter(wiki -> wiki.mwApiUrl),
                            Codec.STRING.fieldOf("random_article").forGetter(wiki -> wiki.randomArticle),
                            LanguageMatcher.CODEC.fieldOf("language").forGetter(wiki -> wiki.language)
                          )
                    .apply(builder, WikiIndividual::new));
    public String articleUrl;
    public String mwApiUrl;
    public String randomArticle;
    public LanguageMatcher language;

    public WikiIndividual(String articleUrl, String mwApiUrl, String randomArticle, LanguageMatcher language) {
        this.articleUrl = articleUrl;
        this.mwApiUrl = mwApiUrl;
        this.randomArticle = randomArticle;
        this.language = language;
    }
}
