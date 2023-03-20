package ru.job4j.grabber;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store {
    private Connection cnn;

    public PsqlStore(Properties cfg) {
        try {
            Class.forName(cfg.getProperty("jdbc.driver"));
            cnn = DriverManager.getConnection(
                    cfg.getProperty("url"),
                    cfg.getProperty("login"),
                    cfg.getProperty("password")
            );
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void save(Post post) {
        try (PreparedStatement ps = cnn.prepareStatement(
                "insert into post(title, description, link, created) values (?, ?, ?, ?)"
                        + "on conflict (link) do nothing",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, post.getTitle());
            ps.setString(2, post.getDescription());
            ps.setString(3, post.getLink());
            ps.setTimestamp(4, Timestamp.valueOf(post.getCreated()));
            ps.execute();
            try (ResultSet generatedKey = ps.getGeneratedKeys()) {
                if (generatedKey.next()) {
                    post.setId(generatedKey.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new IllegalArgumentException("Exception in save() + ",  e);
        }
    }

    @Override
    public List<Post> getAll() {
        List<Post> rsl = new ArrayList<>();
        try (PreparedStatement ps = cnn.prepareStatement("select * from post")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rsl.add(createPost(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalArgumentException("Exception in getAll()");
        }
        return rsl;
    }

    @Override
    public Post findById(int id) {
        Post rsl = null;
        try (PreparedStatement ps = cnn.prepareStatement("select * from post where id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    rsl = createPost(rs);
                }
            }
        } catch (SQLException e) {
            throw new IllegalArgumentException("Exception in findById() + ", e);
        }
        return rsl;
    }

    public void close() throws Exception {
        if (cnn != null) {
            cnn.close();
        }
    }

    private Post createPost(ResultSet rs) throws SQLException {
        return new Post(rs.getInt("id"), rs.getString("title"),
                        rs.getString("link"), rs.getString("description"),
                        rs.getTimestamp("created").toLocalDateTime());
    }

    public static void main(String[] args) {
        Properties config = new Properties();
        try (InputStream in = PsqlStore.class.getClassLoader().getResourceAsStream("grabber.properties")) {
            config.load(in);
        } catch (IOException e) {
            throw new IllegalArgumentException("Exception in InputStream");
        }
        try (Store store = new PsqlStore(config)) {
            store.save(new Post("title1", "link1", "description1", LocalDateTime.now()));
            store.save(new Post("title2", "link2", "description2", LocalDateTime.now()));
            System.out.println(store.findById(1));
            store.getAll().forEach(System.out::println);
        } catch (Exception e) {
            throw new IllegalArgumentException("Exception in new PsqlStore + ", e);
        }
    }
}
