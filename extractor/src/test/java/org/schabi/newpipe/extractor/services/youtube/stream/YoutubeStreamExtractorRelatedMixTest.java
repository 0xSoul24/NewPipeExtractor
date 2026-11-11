package org.schabi.newpipe.extractor.services.youtube.stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertGreaterOrEqual;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;
import static org.schabi.newpipe.extractor.services.youtube.stream.YoutubeStreamExtractorDefaultTest.YOUTUBE_LICENCE;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.DownloaderFactory;
import org.schabi.newpipe.downloader.DownloaderType;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.playlist.PlaylistInfo.PlaylistType;
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItem;
import org.schabi.newpipe.extractor.services.DefaultStreamExtractorTest;
import org.schabi.newpipe.extractor.services.youtube.InitYoutubeTest;
import org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper;
import org.schabi.newpipe.extractor.services.youtube.YoutubeTestsUtils;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

public class YoutubeStreamExtractorRelatedMixTest extends DefaultStreamExtractorTest
    implements InitYoutubeTest {
    private static final String ID = "K4DyBUG242c";
    private static final String URL = YoutubeStreamExtractorDefaultTest.BASE_URL + ID;
    private static final String TITLE = "Cartoon, Jéja - On & On (feat. Daniel Levi) | Electronic Pop | NCS - Copyright Free Music";

    @BeforeAll
    @Override
    public void setUp() throws Exception {
        InitYoutubeTest.super.setUp();
        YoutubeParsingHelper.setConsentAccepted(true);
    }

    @Override
    protected StreamExtractor createExtractor() throws Exception {
        return YouTube.getStreamExtractor(URL);
    }

    // @formatter:off
    @Override public StreamingService expectedService() { return YouTube; }
    @Override public String expectedName() { return TITLE; }
    @Override public String expectedId() { return ID; }
    @Override public String expectedUrlContains() { return URL; }
    @Override public String expectedOriginalUrlContains() { return URL; }

    @Override public StreamType expectedStreamType() { return StreamType.VIDEO_STREAM; }
    @Override public String expectedUploaderName() { return "NoCopyrightSounds"; }
    @Override public String expectedUploaderUrl() { return "https://www.youtube.com/channel/UC_aEa8K-EOJ3D6gOs7HcyNg"; }
    @Override public List<String> expectedDescriptionContains() {
        return Arrays.asList("https://www.youtube.com/user/danielleviband/", "©");
    }
    @Override public boolean expectedUploaderVerified() { return true; }
    @Override public long expectedUploaderSubscriberCountAtLeast() { return 32_000_000; }
    @Override public long expectedLength() { return 208; }
    @Override public long expectedViewCountAtLeast() { return 449_000_000; }
    @Nullable @Override public String expectedUploadDate() { return "2015-07-09 16:34:35.000"; }
    @Nullable @Override public String expectedTextualUploadDate() { return "2015-07-09T09:34:35-07:00"; }
    @Override public long expectedLikeCountAtLeast() { return 6_400_000; }
    @Override public long expectedDislikeCountAtLeast() { return -1; }
    @Override public int expectedStreamSegmentsCount() { return 0; }
    @Override public String expectedLicence() { return YOUTUBE_LICENCE; }
    @Override public String expectedCategory() { return "Music"; }
    @Override public List<String> expectedTags() {
        return Arrays.asList("Cartoon", "Cartoon - On & On", "Cartoon On & On (feat. Daniel Levi)",
                "Daniel Levi", "NCS Best Songs", "NCS Cartoon Daniel Levi", "NCS Cartoon On & On",
                "NCS On & On", "NCS On and On", "NCS Release Daniel Levi", "NCS release Cartoon",
                "On & On", "best music", "club music", "copyright free music", "dance music", "edm",
                "electronic music", "electronic pop", "free music", "gaming music", "music", "ncs",
                "no copyright music", "no copyright sounds", "nocopyrightsounds",
                "royalty free music", "songs", "top music");
    }
    // @formatter:on

    @Test
    @Override
    public void testRelatedItems() throws Exception {
        super.testRelatedItems();

        final List<PlaylistInfoItem> playlists = Objects.requireNonNull(extractor().getRelatedItems())
                .getItems()
                .stream()
                .filter(PlaylistInfoItem.class::isInstance)
                .map(PlaylistInfoItem.class::cast)
                .collect(Collectors.toList());

        // There can also be other playlists that are not mixes!
        final List<PlaylistInfoItem> streamMixes = playlists.stream()
                .filter(item -> item.getPlaylistType().equals(PlaylistType.MIX_STREAM))
                .collect(Collectors.toList());
        // YouTube puts the mix in the first batch of related items for only some visitors (about
        // half of the time); for the others, it is in the second batch, which is not extracted.
        // The mocks contain a mix in the first batch, so only skip this test on live runs.
        assumeFalse(DownloaderFactory.getDownloaderType() == DownloaderType.REAL
                        && streamMixes.isEmpty(),
                "YouTube didn't return a stream mix in the first batch of related items");
        assertGreaterOrEqual(1, streamMixes.size(), "Not found one or more stream mix in related items");

        final PlaylistInfoItem streamMix = streamMixes.get(0);
        assertSame(InfoItem.InfoType.PLAYLIST, streamMix.getInfoType());
        assertEquals(YouTube.getServiceId(), streamMix.getServiceId());
        // YouTube doesn't always offer the mix of the current video, but may offer the mix of
        // another (related) video instead, so only check that the mix is well-formed
        assertTrue(streamMix.getUrl().matches(
                        "https://www\\.youtube\\.com/watch\\?v=([\\w-]{11})&list=RD\\1"),
                "Unexpected stream mix URL: " + streamMix.getUrl());
        assertTrue(streamMix.getName().startsWith("Mix – "),
                "Unexpected stream mix name: " + streamMix.getName());
        YoutubeTestsUtils.testImages(streamMix.getThumbnails());
    }
}
