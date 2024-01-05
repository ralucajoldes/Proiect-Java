package socialnetwork.repository.database;

import socialnetwork.domain.FriendRequest;
import socialnetwork.domain.Friendship;
import socialnetwork.domain.Tuple;
import socialnetwork.domain.User;
import socialnetwork.domain.validators.Validator;
import socialnetwork.exceptions.AlreadyExistsException;
import socialnetwork.exceptions.ValidationException;
import socialnetwork.repository.PaginatedRepository;
import socialnetwork.repository.Repository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class FriendshipsDbRepository implements PaginatedRepository<Tuple<Long,Long>, Friendship>
{
    private String url;
    private String username;
    private String password;
    private Validator<Friendship> validator;
    private Integer pageSize;
    private Integer currentPage;

    public void setCurrentPage(Integer currentPage)
    {
        this.currentPage = currentPage;
    }
    public FriendshipsDbRepository(String url, String username, String password, Validator<Friendship> validator, Integer pageSize)
    {
        this.url = url;
        this.username = username;
        this.password = password;
        this.validator = validator;
        this.pageSize = pageSize;
        this.currentPage = 1;

    }
    @Override
    public Iterable<Friendship> findAll()
    {
        Set<Friendship> friendships = new HashSet<>();
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * from friendships");
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                Long id1 = resultSet.getLong("id_friend1");
                Long id2 = resultSet.getLong("id_friend2");
                String date = resultSet.getString("date");
                Friendship friendship = new Friendship(id1, id2);
                friendship.setDate(LocalDateTime.parse(date));
                friendships.add(friendship);
            }
            return friendships;
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
        return friendships;
    }
    @Override
    public Friendship findOne(Tuple<Long,Long>id)
    {
        if (id==null)
            throw new IllegalArgumentException("The id must be not null!");
        for(Friendship f:findAll())
            if(f.getId().equals(id))return f;
        return null;
    }

    @Override
    public Friendship save(Friendship entity)
    {

        if (entity==null)
            throw new IllegalArgumentException("Entity must be not null!");

        Long user1_id=entity.getId().getLeft();
        Long user2_id=entity.getId().getRight();
        String date = LocalDateTime.now().toString();
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("INSERT INTO friendships(id_friend1,id_friend2,date) VALUES (?,?,?)");)
        {

            statement.setLong(1,user1_id);
            statement.setLong(2,user2_id);
            statement.setString(3,date);
            validator.validate(entity);
            for(Friendship x:findAll())
                if(x.equals(entity))return entity;
            statement.executeUpdate();
        }
        catch (ValidationException e)
        {
            System.out.println(e.getMessage());
        }
        catch (SQLException e)
        {

            e.printStackTrace();
        }

        return null;
    }
    @Override
    public Friendship delete(Tuple<Long,Long>id)
    {
        Friendship friendship;
        try
        {
            friendship = findOne(id);
        }
        catch (Exception e)
        {

            System.out.println(e.getMessage());
            return null;
        }
        if(friendship==null)return null;
        Long user1_id=friendship.getId().getLeft();
        Long user2_id=friendship.getId().getRight();
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("DELETE FROM friendships WHERE id_friend1=? AND id_friend2=? OR id_friend1=? AND id_friend2=?");)
        {

            statement.setLong(1,user1_id);
            statement.setLong(2,user2_id);
            statement.setLong(3,user2_id);
            statement.setLong(4,user1_id);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {

            e.printStackTrace();
        }

        return friendship;
    }

    @Override
    public Friendship update(Friendship entity)
    {
        return null;
    }

    @Override
    public Iterable<Friendship> nextPage(User user)
    {
        if(currentPage<totalPages(user))
        {
            currentPage++;
            return getCurrentPage(user);
        }
        return null;
    }

    @Override
    public void setPage(Integer page)
    {
        this.currentPage=page;
    }

    @Override
    public Iterable<Friendship> previousPage(User user)
    {
        if(currentPage>1)
        {
            currentPage--;
            return getCurrentPage(user);
        }
        return null;
    }

    @Override
    public Iterable<Friendship> getCurrentPage(User user)
    {
        Long filter= (long) -1;
        if(user!=null)filter=user.getId();
        Set<Friendship> friendships = new HashSet<>();
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * from friendships where id_friend1=? OR id_friend2=? limit "+pageSize+" offset "+pageSize*(currentPage-1));
             )
        {
            statement.setLong(1,filter);
            statement.setLong(2,filter);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next())
            {
                Long id1 = resultSet.getLong("id_friend1");
                Long id2 = resultSet.getLong("id_friend2");
                String date = resultSet.getString("date");
                Friendship friendship = new Friendship(id1, id2);
                friendship.setDate(LocalDateTime.parse(date));
                friendships.add(friendship);
            }
            return friendships;
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
        return friendships;
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
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT COUNT (*) AS total from friendships where id_friend1=? OR id_friend2=?");
            )
        {
            statement.setLong(1,filter);
            statement.setLong(2,filter);
            ResultSet resultSet = statement.executeQuery();
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