package socialnetwork.repository;

import socialnetwork.domain.Entity;
import socialnetwork.domain.User;

public interface PaginatedRepository<ID, E extends Entity<ID>> extends  Repository<ID,E>
{
    Iterable<E> nextPage(User user);
    Iterable<E> previousPage(User user);
    Iterable<E> getCurrentPage(User user);
    Integer getPageNumber();
    void setPage(Integer page);
    Integer getPageSize();
    Integer totalPages(User user);
}
