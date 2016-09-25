package com.tchepannou.kiosk.bot.service;

import com.tchepannou.kiosk.bot.domain.RssItem;
import com.tchepannou.kiosk.client.dto.WebsiteDto;
import com.tchepannou.kiosk.core.service.TimeService;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.tchepannou.kiosk.bot.Fixture.createWebsite;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HtmlServiceTest {

    @Mock
    TimeService timeService;

    @InjectMocks
    HtmlService service = new HtmlService();

    Date now;

    @Before
    public void setUp() {
        now = new Date();
        when(timeService.now()).thenReturn(now);
    }

    //-- ExtractURLs
    @Test
    public void shouldExtractHttpUrls() throws Exception {
        final List<String> hrefs = Arrays.asList("http://www.google.ca", "http://www.yahoo.ca");
        final String html = generateHtml(hrefs);
        final WebsiteDto website = createWebsite();

        final List<String> result = service.extractUrls(html, website);

        assertThat(result).containsAll(hrefs);
    }

    @Test
    public void shouldExtractHttpsUrls() throws Exception {
        final List<String> hrefs = Arrays.asList("https://www.google.ca", "http://www.yahoo.ca");
        final String html = generateHtml(hrefs);
        final WebsiteDto website = createWebsite();

        final List<String> result = service.extractUrls(html, website);

        assertThat(result).containsAll(hrefs);
    }

    @Test
    public void shouldNotExtractNonHttpUrls() throws Exception {
        final List<String> hrefs = Arrays.asList("http://www.google.ca", "mailto:ray.sponsible@gmail.com", "#", "javascript:open(1)");
        final String html = generateHtml(hrefs);
        final WebsiteDto website = createWebsite();

        final List<String> result = service.extractUrls(html, website);

        assertThat(result).containsAll(Collections.singletonList("http://www.google.ca"));
    }

    private String generateHtml(final List<String> hrefs) {
        return "<html><body>"
                + String.join(
                "\n",
                hrefs
                        .stream()
                        .map(href -> "<a href=\"" + href + "\">" + href + "</a>")
                        .collect(Collectors.toList())
        )
                + "</body></html>";
    }

    //-- toRssItem
    @Test
    public void showCreateRssItemForCamfoot() throws Exception {
        // Given
        final WebsiteDto website = createWebsite();
        final String url = "http://goo.com/foo.html";
        final String html = IOUtils.toString(getClass().getResourceAsStream("/articles/camfoot.html"));

        // When
        final RssItem item = service.toRssItem(url, html, website);

        // Then
        assertThat(item.getCreationDate()).isEqualTo(now);
        assertThat(item.getPublishedDate()).isEqualTo(now);
        assertThat(item.getDescription()).isEqualTo(
                "Entame timide pour Fédéral Fc. En match aller des ½ finales des barrages de la ligue régionale de football de l‘Ouest, le club des serpents à deux têtes n’a pas pu sortir son venin face à Red star de Bafoussam lundi 19 septembre dernier, au stade municipal de Bafoussam. Pourtant, ce ne sont pas les occasions qui ont manqué. Mais la ligne offensive des bleu et jaune du Noun, conduite par Minla Francis manquait d’efficacité au niveau de la finition pendant toute la première période. La seconde manche (...)");
        assertThat(item.getLink()).isEqualTo(url);
        assertThat(item.getTitle()).isEqualTo("D2 Ouest : Fédéral accroché, As Menoua domine Renaissance");
    }

    @Test
    public void showCreateRssItemForCameroonInfoNet() throws Exception {
        // Given
        final WebsiteDto website = createWebsite();
        website.setTitleCssSelector(".cp-post-content h3");

        final String url = "http://googl.com/foo.html";
        final String html = IOUtils.toString(getClass().getResourceAsStream("/articles/cameroon-info.net.html"));

        // When
        final RssItem item = service.toRssItem(url, html, website);

        // Then
        assertThat(item.getCreationDate()).isEqualTo(now);
        assertThat(item.getPublishedDate()).isEqualTo(now);
        assertThat(item.getDescription()).isNull();
        assertThat(item.getLink()).isEqualTo(url);
        assertThat(item.getTitle()).isEqualTo("Gabon - Contentieux électoral: Ali Bongo et Jean Ping sont d'accord pour recompter des voix de l'élection présidentielle du 27 août");
    }
}
