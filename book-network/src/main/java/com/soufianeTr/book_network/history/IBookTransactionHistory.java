package com.soufianeTr.book_network.history;

import com.soufianeTr.book_network.book.BookResponse;
import com.soufianeTr.book_network.common.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface IBookTransactionHistory  extends JpaRepository<BookTransactionHistory, Integer> {
    @Query("""
            SELECT
            (COUNT (*) > 0) AS isBorrowed
            FROM BookTransactionHistory bookTransactionHistory
            WHERE bookTransactionHistory.user.id = :userId
            AND bookTransactionHistory.book.id = :bookId
            AND bookTransactionHistory.returnApproved = false
            """)
    Page<BookTransactionHistory> findBorrowedBooks(Pageable page, Integer userId);
    @Query("""
        SELECT history
        FROM BookTransactionHistory history
        WHERE history.book.owner.id = :userId
        """)
    Page<BookTransactionHistory> findBooksReturned(Pageable pageable, @Param("userId") Integer userId);

    @Query("SELECT (COUNT(*) > 0) AS isBorrowed " +
            "FROM BookTransactionHistory bookTransactionHistory " +
            "WHERE bookTransactionHistory.book.id = :bookId " +
            "AND bookTransactionHistory.user.id = :userId " +
            "AND bookTransactionHistory.returnApproved = false")
    boolean isAlreadyBorrowedByUser(@Param("userId") Integer userId, @Param("bookId") Integer bookId);


    @Query("""
            SELECT transaction
            FROM BookTransactionHistory  transaction
            WHERE transaction.user.id = :userId
            AND transaction.book.id = :bookId
            AND transaction.returned = false
            AND transaction.returnApproved = false
            """)
    Optional<BookTransactionHistory> findByBookIdAndUserId( Integer bookId, Integer userId);

    @Query("""
            SELECT transaction
            FROM BookTransactionHistory  transaction
            WHERE transaction.book.owner.id = :userId
            AND transaction.book.id = :bookId
            AND transaction.returned = true
            AND transaction.returnApproved = false
            """)
    Optional<BookTransactionHistory> findByBookIdAndOwnerId(@Param("bookId") Integer bookId, @Param("userId") Integer userId);
}
