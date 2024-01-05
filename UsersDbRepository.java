package socialnetwork.repository.database;

import socialnetwork.domain.User;
import socialnetwork.exceptions.AlreadyExistsException;
import socialnetwork.exceptions.ValidationException;
import socialnetwork.domain.validators.Validator;
import socialnetwork.repository.PaginatedRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UsersDbRepository implements PaginatedRepository<Long, User>
{
    private String url;
    private String username;
    private String password;
    private Validator<User> validator;
    private Integer pageSize;
    private Integer currentPage;
    private User user;



    public void setUser(User user)
    {
        this.user = user;
    }

    public UsersDbRepository(String url, String username, String password, Validator<User> validator, Integer pageSize)
    {
        this.url = url;
        this.username = username;
        this.password = password;
        this.validator = validator;
        this.pageSize=pageSize;
        this.currentPage=1;
    }
    @Override
    public User findOne(Long id) {
        if (id==null)
            throw new IllegalArgumentException("The id must be not null!");
        for(User u:findAll())
            if(u.getId().equals(id))return u;
        return null;
    }


    @Override
    public Iterable<User> findAll() {
        Set<User> users = new HashSet<>();
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * from users");
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next())
            {
                Long id = resultSet.getLong("id");
                String lastName = resultSet.getString("last_name");
                String firstName = resultSet.getString("first_name");
                String user_name = resultSet.getString("user_name");
                String password_key = resultSet.getString("password_key");
                String path = resultSet.getString("path");
                User user = new User(lastName,firstName,user_name,password_key);
                user.setId(id);
                user.setPath(path);
                users.add(user);
            }
            return users;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    @Override
    public User save(User entity)
    {

        if (entity==null)
            throw new IllegalArgumentException("Entity must be not null!");

        String user_last=entity.getLastName();
        String user_first=entity.getFirstName();
        String user_name = entity.getUsername();
        String password_key = entity.getPassword();
        String path=entity.getPath();
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("INSERT INTO users(last_name,first_name,user_name,password_key,path) VALUES (?,?,?,?,?)");)
        {

            statement.setString(1,user_last);
            statement.setString(2,user_first);
            statement.setString(3,user_name);
            statement.setString(4,password_key);
            statement.setString(5,path);
            validator.validate(entity);
            for(User x:findAll())
                if(x.equals(entity))return entity;
           statement.executeUpdate();
        }
        catch (ValidationException e)
        {
            throw new ValidationException(e.getMessage());
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public User delete(Long id)
    {
        User user;
       try
       {
           user = findOne(id);
       }
       catch (Exception e)
       {

           System.out.println(e.getMessage());
           return null;
       }
       if(user==null)return null;
        String user_name=user.getUsername();
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("DELETE FROM users WHERE user_name=?");)
        {
            statement.setString(1,user_name);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        return user;
    }

    @Override
    public User update(User entity)
    {
        if (entity==null)
            throw new IllegalArgumentException("Entity must be not null!");
        User user;
        try
        {
            user = findOne(entity.getId());
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            return null;
        }
        if(user==null)return entity;
        Long id=user.getId();
        String path=entity.getPath();
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("UPDATE users SET path=? WHERE id=?");)
        {

            statement.setString(1,path);
            statement.setLong(2,id);
            validator.validate(entity);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        catch (AlreadyExistsException e)
        {
            return entity;
        }
        return null;
    }

    @Override
    public Iterable<User> nextPage(User user)
    {
        if(currentPage<totalPages(user))
        {
            currentPage++;
            return getCurrentPage(user);
        }
        return null;
    }


    @Override
    public Iterable<User> previousPage(User user)
    {
        if(currentPage>1)
        {
            currentPage--;
            return getCurrentPage(user);
        }
        return null;
    }

    @Override
    public Iterable<User> getCurrentPage(User user)
    {
        List<User> users = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * from users order by user_name limit "+pageSize+" offset "+pageSize*(currentPage-1));
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next())
            {
                Long id = resultSet.getLong("id");
                String lastName = resultSet.getString("last_name");
                String firstName = resultSet.getString("first_name");
                String user_name = resultSet.getString("user_name");
                String password_key = resultSet.getString("password_key");
                String path = resultSet.getString("path");
                User u = new User(lastName,firstName,user_name,password_key);
                u.setId(id);
                u.setPath(path);
                users.add(u);
            }
            return users;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    @Override
    public Integer getPageNumber()
    {
        return currentPage;
    }

    @Override
    public void setPage(Integer page)
    {
        this.currentPage=page;
    }

    @Override
    public Integer getPageSize()
    {
        return pageSize;
    }

    @Override
    public Integer totalPages(User user)
    {

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT COUNT (*) AS total from users");
             ResultSet resultSet = statement.executeQuery())
        {
            resultSet.next();
            Integer friendships=resultSet.getInt("total");
            if(friendships%pageSize!=0)
                return friendships/pageSize+1;
            else
                return friendships/pageSize;
        }
        catch (SQLException throwables)
        {
            throwables.printStackTrace();
        }
        return null;
    }

}
