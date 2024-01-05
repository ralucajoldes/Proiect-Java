package socialnetwork.repository.database;
import socialnetwork.domain.Message;
import socialnetwork.domain.User;
import socialnetwork.domain.validators.Validator;
import socialnetwork.exceptions.ValidationException;
import socialnetwork.repository.PaginatedRepository;

import java.awt.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class MessageDbRepository implements PaginatedRepository<Long, Message> {
    private String url;
    private String username;
    private String password;
    private Validator<Message> validator;
    private Integer pageSize;
    private Integer currentPage;

    public MessageDbRepository(String url, String username, String password, Validator<Message> validator, Integer pageSize) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.validator = validator;
        this.pageSize = pageSize;
        this.currentPage = 1;
    }

    @Override
    public Message findOne(Long id) {
        if (id == null)
            throw new IllegalArgumentException("The id must be not null!");
        for (Message m : findAll())
            if (m.getId().equals(id)) return m;
        return null;
    }


    @Override
    public Iterable<Message> findAll() {
        Set<Message> messages = new HashSet<>();
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * from messages INNER JOIN message_user ON messages.id=message_id");
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next())
            {
                Long id = resultSet.getLong("message_id");
                Long user_from = resultSet.getLong("user_from");
                User from = new User(null, null,null,null);
                from.setId(user_from);
                Long user_to = resultSet.getLong("user_to");
                User to = new User(null, null,null,null);
                to.setId(user_to);
                LocalDateTime date = LocalDateTime.parse(resultSet.getString("date_m"));
                String message = resultSet.getString("message");
                Message m = new Message(message, from);
                m.setDate(date);
                m.setId(id);
                if (!messages.contains(m))
                {

                    m.addReceiver(to);
                    messages.add(m);
                } else
                    for (Message mes : messages)
                        if (mes.equals(m)) mes.addReceiver(to);
            }
            return messages;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    public Long findMyMessage(String date_mes, String message_m, Long id_user) {

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM messages WHERE user_from=? AND date_m=? AND message=?")) {
            statement.setLong(1, id_user);
            statement.setString(2, date_mes);
            statement.setString(3, message_m);
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            return resultSet.getLong(1);

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Message save(Message entity) {

        if (entity == null)
            throw new IllegalArgumentException("Entity must be not null!");
        String message = entity.getMessage();
        Long id_f = entity.getFrom().getId();
        ArrayList<Long> id_t = (ArrayList<Long>) entity.getTo().stream().map(user -> user.getId()).collect(Collectors.toList());
        String date = entity.getDate().toString();


        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("INSERT INTO messages(user_from,message,date_m) VALUES (?,?,?)")) {

            statement.setLong(1, id_f);
            statement.setString(2, message);
            statement.setString(3, date);
            validator.validate(entity);
            statement.executeUpdate();
            for (Long x : id_t) {
                PreparedStatement statement2 = connection.prepareStatement("INSERT INTO message_user(user_to,message_id) VALUES (?,?)");
                statement2.setLong(1, x);
                Long id_m = findMyMessage(date, message, id_f);
                statement2.setLong(2, id_m);
                statement2.executeUpdate();
            }
        } catch (ValidationException e)
        {
            System.out.println(e.getMessage());
        } catch (SQLException e) {

            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Message delete(Long id) {
        Message message;
        try {
            message = findOne(id);
        } catch (Exception e) {

            System.out.println(e.getMessage());
            return null;
        }
        if (message == null) return null;

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("DELETE FROM messages WHERE user_from=? AND id=?");) {
            statement.setLong(1, message.getFrom().getId());
            statement.setLong(2, message.getId());
            statement.executeUpdate();
            PreparedStatement statement2 = connection.prepareStatement("DELETE FROM message_user WHERE message_id=?");
            statement2.setLong(1, message.getId());
            statement2.executeUpdate();

        } catch (SQLException e) {

            e.printStackTrace();
        }
        return message;
    }


    @Override
    public Message update(Message entity) {
        if (entity == null)
            throw new IllegalArgumentException("Entity must be not null!");
        Message message;
        try {
            message = findOne(entity.getId());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return entity;
        }

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("DELETE FROM message_user WHERE user_to=? AND message_id=?");) {


            statement.setLong(1, entity.getTo().get(0).getId());
            statement.setLong(2, entity.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Iterable<Message> nextPage(User user)
    {
        if(currentPage<totalPages(user))
        {
            currentPage++;
            return getCurrentPage(user);
        }
        return null;
    }


    @Override
    public Iterable<Message> previousPage(User user)
    {
        if(currentPage>1)
        {
            currentPage--;
            return getCurrentPage(user);
        }
        return null;
    }
    private boolean check(Message message, User user)
    {
        Long filter= (long) -1;
        if(user!=null)filter=user.getId();
        if(!message.getFrom().getId().equals(filter))
            for(User user1 : message.getTo())
            {
                if(user1.getId().equals(filter))
                    return true;
            }
        else
            return true;
        return false;
    }
    @Override
    public Iterable<Message> getCurrentPage(User user)
    {
        Long filter= (long) -1;
        if(user!=null)filter=user.getId();
        List<Message> messages = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * from messages INNER JOIN message_user ON messages.id=message_id");
             ResultSet resultSet = statement.executeQuery())
        {

            while (resultSet.next())
            {
                Long id = resultSet.getLong("message_id");
                Long user_from = resultSet.getLong("user_from");
                User from = new User(null, null,null,null);
                from.setId(user_from);
                Long user_to = resultSet.getLong("user_to");
                User to = new User(null, null,null,null);
                to.setId(user_to);
                LocalDateTime date = LocalDateTime.parse(resultSet.getString("date_m"));
                String message = resultSet.getString("message");
                Message m = new Message(message, from);
                m.setDate(date);
                m.setId(id);
                if (!messages.contains(m))
                {
                    m.addReceiver(to);
                    messages.add(m);
                } else
                    for (Message mes : messages)
                        if (mes.equals(m)) mes.addReceiver(to);
            }

            messages.removeIf(m -> !check(m, user));
            messages.sort((x,y)->{return -x.getDate().compareTo(y.getDate());});
            return messages.stream().skip(pageSize*(currentPage-1)).limit(pageSize).collect(Collectors.toList());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    @Override
    public void setPage(Integer page)
    {
        this.currentPage=page;
    }

    @Override
    public Integer getPageNumber() {
        return currentPage;
    }

    @Override
    public Integer getPageSize() {
        return pageSize;
    }

    @Override
    public Integer totalPages(User user)
    {
        Long filter= (long) -1;
        if(user!=null)filter=user.getId();
        Set<Message> messages = new HashSet<>();
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * from messages INNER JOIN message_user ON messages.id=message_id");
             ResultSet resultSet = statement.executeQuery())
        {

            while (resultSet.next())
            {
                Long id = resultSet.getLong("message_id");
                Long user_from = resultSet.getLong("user_from");
                User from = new User(null, null,null,null);
                from.setId(user_from);
                Long user_to = resultSet.getLong("user_to");
                User to = new User(null, null,null,null);
                to.setId(user_to);
                LocalDateTime date = LocalDateTime.parse(resultSet.getString("date_m"));
                String message = resultSet.getString("message");
                Message m = new Message(message, from);
                m.setDate(date);
                m.setId(id);
                if (!messages.contains(m))
                {
                    m.addReceiver(to);
                    messages.add(m);
                } else
                    for (Message mes : messages)
                        if (mes.equals(m)) mes.addReceiver(to);
            }

            messages.removeIf(m -> !check(m, user));
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        Integer total=messages.size();
        if(total%pageSize!=0)
            return total/pageSize+1;
        else
            return total/pageSize;
    }
}
