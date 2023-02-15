package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.time.LocalDateTime;

public class HabrCareerParse {

    private static final String SOURCE_LINK = "https://career.habr.com";

    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer?page=", SOURCE_LINK);
    private static final int COUNT_OF_PAGES = 5;

    public static void main(String[] args) throws IOException {
        for (int i = 1; i <= COUNT_OF_PAGES; i++) {
            Connection  connection = Jsoup.connect(PAGE_LINK.concat(String.valueOf(i)));
            Document document = connection.get();
            Elements rows = document.select(".vacancy-card__inner");
            rows.forEach(row -> {
                Element titleElement = row.select(".vacancy-card__title").first();
                Element linkElement = titleElement.child(0);
                Element innerElement = row.select(".vacancy-card__date").first();
                Element dateElement = innerElement.child(0);
                String vacancyName = titleElement.text();
                LocalDateTime date = new HabrCareerDateTimeParser().parse(dateElement.attr("datetime"));
                String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
                System.out.printf("%s %s %s%n", date, vacancyName, link);
            });
        }
    }
}