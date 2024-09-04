package com.soufianeTr.book_network.book;

import com.soufianeTr.book_network.common.PageResponse;
import com.soufianeTr.book_network.exception.OperationNotPermittedException;
import com.soufianeTr.book_network.file.FileStorageService;
import com.soufianeTr.book_network.history.BookTransactionHistory;
import com.soufianeTr.book_network.history.IBookTransactionHistory;
import com.soufianeTr.book_network.user.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class BookService {
    private final BookMapper bookMapper;
    private final BookRepository bookRepository;
    private final IBookTransactionHistory bookTransactionHistory;
    private final FileStorageService fileStorageService;
    public Integer save(BookRequest bookRequest, Authentication connectedUser) {

        User user = ((User) connectedUser.getPrincipal());
        Book book=bookMapper.toBook(bookRequest);
        book.setOwner(user);
        return bookRepository.save(book).getId();
    }
    public BookResponse findBookById(Integer bookId){
        return bookRepository.findById(bookId)
                .map(bookMapper::toBookResponse)
                        .orElseThrow(()-> new EntityNotFoundException("Book not found with ID ::"+bookId));
    }

    public PageResponse<BookResponse> findAllBooks(int page, int size, Authentication connectedUser) {
        User user = ((User) connectedUser.getPrincipal());
        Pageable pageable= PageRequest.of(page,size, Sort.by("createdDate").descending());
        Page<Book> books=bookRepository.findAllDisplayableBooks(pageable,user.getId());
        List<BookResponse> booksResponse = books.stream()
                .map(bookMapper::toBookResponse)
                .toList();

        return new PageResponse<>(
                booksResponse,
                books.getNumber(),
                books.getSize(),
                books.getTotalElements(),
                books.getTotalPages(),
                books.isFirst(),
                books.isLast()
        );
    }

    public PageResponse<BookResponse> findAllBooksByOwner(int page, int size, Authentication connectedUser) {
        User user = ((User) connectedUser.getPrincipal());
        Pageable pageable= PageRequest.of(page,size, Sort.by("createdDate").descending());
        Page<Book> books=bookRepository.findAll(BookSpecification.withOwnerId(user.getId()),pageable);
        List<BookResponse> booksResponse = books.stream()
                .map(bookMapper::toBookResponse)
                .toList();

        return new PageResponse<>(
                booksResponse,
                books.getNumber(),
                books.getSize(),
                books.getTotalElements(),
                books.getTotalPages(),
                books.isFirst(),
                books.isLast()
        );
    }

    public PageResponse<BookBorrowedResponse> findAllBooksBorrowed(int page, int size, Authentication connectedUser) {
        User user = ((User) connectedUser.getPrincipal());
        Pageable pageable= PageRequest.of(page,size, Sort.by("createdDate").descending());

        Page<BookTransactionHistory> allBorrowedBook=bookTransactionHistory.findBorrowedBooks(pageable,user.getId());
        List<BookBorrowedResponse> borrowedResponses=allBorrowedBook.
                stream().
                map(bookMapper::toBorrowedBookResponse).
                collect(Collectors.toList());
        return new PageResponse<>(
                borrowedResponses,
                allBorrowedBook.getNumber(),
                allBorrowedBook.getSize(),
                allBorrowedBook.getTotalElements(),
                allBorrowedBook.getTotalPages(),
                allBorrowedBook.isFirst(),
                allBorrowedBook.isLast()
        );
    }
    public PageResponse<BookBorrowedResponse> AllBookReturned(int page, int size, Authentication connectedUser) {
        User user = ((User) connectedUser.getPrincipal());
        Pageable pageable= PageRequest.of(page,size, Sort.by("createdDate").descending());

        Page<BookTransactionHistory> allBorrowedBook=bookTransactionHistory.findBooksReturned(pageable,user.getId());
        List<BookBorrowedResponse> borrowedResponses=allBorrowedBook.
                stream().
                map(bookMapper::toBorrowedBookResponse).
                collect(Collectors.toList());
        return new PageResponse<>(
                borrowedResponses,
                allBorrowedBook.getNumber(),
                allBorrowedBook.getSize(),
                allBorrowedBook.getTotalElements(),
                allBorrowedBook.getTotalPages(),
                allBorrowedBook.isFirst(),
                allBorrowedBook.isLast()
        );
    }

    public Integer updateShareableStatus(Integer bookId, Authentication connectedUser) throws OperationNotPermittedException {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("No book found with ID:: " + bookId));
        User user = ((User) connectedUser.getPrincipal());
        if (!Objects.equals(book.getOwner().getId(), user.getId())) {
            throw new OperationNotPermittedException("You cannot update others books shareable status");
        }
        book.setShareable(!book.isShareable());
        bookRepository.save(book);
        return bookId;
    }

    public Integer updatedArchivedStatus(Integer bookId, Authentication connectedUser) throws OperationNotPermittedException {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("No book found with ID:: " + bookId));
        User user = ((User) connectedUser.getPrincipal());
        if (!Objects.equals(book.getOwner().getId(), user.getId())) {
            throw new OperationNotPermittedException("You cannot update others books Archived status");
        }
        book.setArchived(!book.isArchived());
        bookRepository.save(book);

        return bookId;
    }

    public Integer borrowBook(Integer bookId, Authentication connectedUser) throws OperationNotPermittedException {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("No book found with ID:: " + bookId));
        if (book.isArchived() || !book.isShareable()) {
            throw new OperationNotPermittedException("The requested book cannot be borrowed since it is archived or not shareable");
        }
        User user = ((User) connectedUser.getPrincipal());
        if (Objects.equals(book.getOwner().getId(), user.getId())) {
            throw new OperationNotPermittedException("You cannot borrow your own book");
        }
        final boolean isAlreadyBorrowedByUser = bookTransactionHistory.isAlreadyBorrowedByUser(bookId, user.getId());
        if (isAlreadyBorrowedByUser) {
            throw new OperationNotPermittedException("You already borrowed this book and it is still not returned or the return is not approved by the owner");
        }
        BookTransactionHistory bookTransactionHistory1=BookTransactionHistory.builder()
                .user(user)
                .book(book)
                .returned(false)
                .returnApproved(false)
                .build();
        return  bookTransactionHistory.save(bookTransactionHistory1).getId();
    }
    public Integer returnBorrowedBook(Integer bookId, Authentication connectedUser) throws OperationNotPermittedException {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("No book found with ID:: " + bookId));
        if (book.isArchived() || !book.isShareable()) {
            throw new OperationNotPermittedException("The requested book is archived or not shareable");
        }
        User user = ((User) connectedUser.getPrincipal());
        if (Objects.equals(book.getOwner().getId(), user.getId())) {
            throw new OperationNotPermittedException("You cannot borrow or return your own book");
        }

        BookTransactionHistory bookTransactionHistory1 = bookTransactionHistory.findByBookIdAndUserId(bookId, user.getId())
                .orElseThrow(() -> new OperationNotPermittedException("You did not borrow this book"));

        bookTransactionHistory1.setReturned(true);
        return bookTransactionHistory.save(bookTransactionHistory1).getId();
    }
    public Integer returnedApprovedBook(Integer bookId, Authentication connectedUser) throws OperationNotPermittedException {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("No book found with ID:: " + bookId));
        if (book.isArchived() || !book.isShareable()) {
            throw new OperationNotPermittedException("The requested book is archived or not shareable");
        }
        User user = ((User) connectedUser.getPrincipal());
        if (Objects.equals(book.getOwner().getId(), user.getId())) {
            throw new OperationNotPermittedException("You cannot borrow or return your own book");
        }

        BookTransactionHistory bookTransactionHistory1 = bookTransactionHistory.findByBookIdAndOwnerId(bookId, user.getId())
                .orElseThrow(() -> new OperationNotPermittedException("The book is not returned yet. You cannot approve its return"));

        bookTransactionHistory1.setReturnApproved(true);
        return bookTransactionHistory.save(bookTransactionHistory1).getId();
    }

    public void uploadBookCoverPicture(MultipartFile file, Authentication connectedUser, Integer bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("No book found with ID:: " + bookId));
        User user = ((User) connectedUser.getPrincipal());
        var bookCover=fileStorageService.saveFile(file,user.getId());
        book.setBookCover(bookCover);
        bookRepository.save(book);
    }
}
