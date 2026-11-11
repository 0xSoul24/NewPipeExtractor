package org.schabi.newpipe.extractor.services.youtube;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonParser;

import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.MetaInfo;
import org.schabi.newpipe.extractor.stream.Description;

import java.net.URL;
import java.util.List;

class YoutubeMetaInfoHelperTest {

    private static List<MetaInfo> getMetaInfoOfInfoPanel(final String infoPanelContentRenderer)
            throws Exception {
        final JsonArray contents = JsonParser.array().from("[{\"itemSectionRenderer\":"
                + "{\"contents\":[{\"infoPanelContentRenderer\":" + infoPanelContentRenderer
                + "}]}}]");
        return YoutubeMetaInfoHelper.getMetaInfo(contents);
    }

    @Test
    void testInfoPanelWithParagraphs() throws Exception {
        final List<MetaInfo> metaInfo = getMetaInfoOfInfoPanel("{"
                + "\"paragraphs\":[{\"runs\":[{\"text\":"
                + "\"Arte is a French/German public broadcast service.\"}]}],"
                + "\"sourceEndpoint\":{\"urlEndpoint\":{\"url\":"
                + "\"https://en.wikipedia.org/wiki/Arte?wprov=yicw1\"}},"
                + "\"inlineSource\":{\"runs\":[{\"text\":\"Wikipedia\"}]}}");

        assertEquals(1, metaInfo.size());
        assertEquals("", metaInfo.get(0).getTitle());
        assertEquals(new Description("Arte is a French/German public broadcast service.",
                Description.Type.HTML), metaInfo.get(0).getContent());
        assertEquals(List.of(new URL("https://en.wikipedia.org/wiki/Arte?wprov=yicw1")),
                metaInfo.get(0).getUrls());
        assertEquals(List.of("Wikipedia"), metaInfo.get(0).getUrlTexts());
    }

    @Test
    void testInfoPanelWithAttributedParagraphs() throws Exception {
        // Info panel returned for a topic (climate change) in November 2026
        final List<MetaInfo> metaInfo = getMetaInfoOfInfoPanel("{"
                + "\"sourceEndpoint\":{\"commandMetadata\":{\"webCommandMetadata\":{\"url\":"
                + "\"https://www.un.org/en/climatechange/what-is-climate-change\"}},"
                + "\"urlEndpoint\":{\"url\":"
                + "\"https://www.un.org/en/climatechange/what-is-climate-change\","
                + "\"target\":\"TARGET_NEW_WINDOW\"}},"
                + "\"attributedParagraphs\":[{\"content\":\"United Nations • Climate change "
                + "refers to long-term shifts in temperatures and weather patterns.\","
                + "\"styleRuns\":[{\"startIndex\":0,\"length\":17,"
                + "\"weightLabel\":\"FONT_WEIGHT_MEDIUM\"}]}]}");

        assertEquals(1, metaInfo.size());
        assertEquals("", metaInfo.get(0).getTitle());
        assertEquals(new Description("Climate change refers to long-term shifts in temperatures"
                        + " and weather patterns.", Description.Type.PLAIN_TEXT),
                metaInfo.get(0).getContent());
        assertEquals(List.of(new URL(
                        "https://www.un.org/en/climatechange/what-is-climate-change")),
                metaInfo.get(0).getUrls());
        assertEquals(List.of("United Nations"), metaInfo.get(0).getUrlTexts());
    }
}
