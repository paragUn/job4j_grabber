package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HabrCareerParse implements Parse {
    private static final String SOURCE_LINK = "https://career.habr.com";
    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer?page=", SOURCE_LINK);
    private static final int COUNT_OF_PAGES = 5;

    private final DateTimeParser dateTimeParser;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    @Override
    public List<Post> list(String link) {
        List<Post> listOfPosts = new ArrayList<>();
        for (int i = 1; i <= COUNT_OF_PAGES; i++) {
            String pageLink = String.format("%s%d", PAGE_LINK, i);
            Connection connection = Jsoup.connect(String.format("%s%s", pageLink, "?page=" + i));
            try {
                Document document = connection.get();
                Elements rows = document.select(".vacancy-card__inner");
                rows.forEach(row -> listOfPosts.add(post(row)));
            } catch (IOException e) {
                throw new IllegalArgumentException("Exception in method 'list()'");
            }
        }
        return listOfPosts;
    }

    private static String retrieveDescription(String link) {
        String result = "";
        try {
            Document document = Jsoup.connect(link).get();
            Element descriptionElement = document.selectFirst(".vacancy-description__text");
            result = Objects.requireNonNull(descriptionElement.text());

        } catch (IOException e) {
            throw new IllegalArgumentException("Exception in method 'retrieveDescription()'");
        }
        return result;
    }

    private Post post(Element row) {
        Element titleElement = row.select(".vacancy-card__title").first();
        Element dateElement = row.select(".vacancy-card__date").first().child(0);
        String vacancyName = titleElement.text();
        LocalDateTime date = new HabrCareerDateTimeParser().parse(dateElement.attr("datetime"));
        String vacancyLink = String.format("%s%s", SOURCE_LINK, titleElement.child(0).attr("href"));
        String description = retrieveDescription(vacancyLink);
        return new Post(vacancyName, vacancyLink, description, date);
    }
}