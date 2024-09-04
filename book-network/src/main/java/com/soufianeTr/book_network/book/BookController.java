package com.soufianeTr.book_network.book;

import com.soufianeTr.book_network.common.PageResponse;
import com.soufianeTr.book_network.exception.OperationNotPermittedException;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("books")
@RequiredArgsConstructor
@Tag(name = "Book")
public class BookController {
    private final BookService bookService;
    @PostMapping("/saveBook")
    public ResponseEntity<Integer> saveBook(@Valid @RequestBody BookRequest bookRequest,
                                            // for get the connected user
                                            Authentication connectedUser){
        return  ResponseEntity.ok(bookService.save(bookRequest,connectedUser));

    }
    @GetMapping("/{bookId}")
    public ResponseEntity<BookResponse> findBookById(@RequestParam Integer bookId){
        return  ResponseEntity.ok(bookService.findBookById(bookId));

    }
    @GetMapping
    public ResponseEntity <PageResponse<BookResponse>> findAllBooks(
            @RequestParam(name = "page",defaultValue = "0",required = false) int page,
            @RequestParam(name = "size",defaultValue = "10",required = false) int size ,Authentication connectedUser){
        return  ResponseEntity.ok(bookService.findAllBooks(page,size,connectedUser));


    } @GetMapping("/owner")
    public ResponseEntity <PageResponse<BookResponse>> findAllBooksByOwner(
            @RequestParam(name = "page",defaultValue = "0",required = false) int page,
            @RequestParam(name = "size",defaultValue = "10",required = false) int size ,Authentication connectedUser){
        return  ResponseEntity.ok(bookService.findAllBooksByOwner(page,size,connectedUser));

    }
    @GetMapping("/borrowed")
    public ResponseEntity <PageResponse<BookBorrowedResponse>> findAllBooksBorrowed(
            @RequestParam(name = "page",defaultValue = "0",required = false) int page,
            @RequestParam(name = "size",defaultValue = "10",required = false) int size ,Authentication connectedUser){
        return  ResponseEntity.ok(bookService.findAllBooksBorrowed(page,size,connectedUser));

    }
    @GetMapping("/returned")
    public ResponseEntity <PageResponse<BookBorrowedResponse>> findAllBooksReturned(
            @RequestParam(name = "page",defaultValue = "0",required = false) int page,
            @RequestParam(name = "size",defaultValue = "10",required = false) int size ,Authentication connectedUser){
        return  ResponseEntity.ok(bookService.AllBookReturned(page,size,connectedUser));

    }
    @PatchMapping("/shareable/{book-id}")
    public ResponseEntity<Integer> updateShareableStatus(
            @PathVariable("book-id") Integer bookId,
            Authentication connectedUser
    ) throws OperationNotPermittedException {
        return ResponseEntity.ok(bookService.updateShareableStatus(bookId, connectedUser));
    }

    @PatchMapping("/archived/{book-id}")
    public ResponseEntity<Integer> updatedArchivedStatus(
            @PathVariable("book-id") Integer bookId,
            Authentication connectedUser
    ) throws OperationNotPermittedException {
        return ResponseEntity.ok(bookService.updatedArchivedStatus(bookId, connectedUser));
    }
    @PostMapping("/borrowed/{book-id}")
    public ResponseEntity<Integer> borrowBook(
            @PathVariable("book-id") Integer bookId,
            Authentication connectedUser
    ) throws OperationNotPermittedException {

        return ResponseEntity.ok(bookService.borrowBook(bookId, connectedUser));
    }

    @PatchMapping("/borrowed/returned/{book-id}")
    public ResponseEntity<Integer> returnedBorrowBook(
            @PathVariable("book-id") Integer bookId,
            Authentication connectedUser
    ) throws OperationNotPermittedException {
        return ResponseEntity.ok(bookService.returnBorrowedBook(bookId, connectedUser));
    }
    @PatchMapping("/borrowed/returned/approved/{book-id}")
    public ResponseEntity<Integer> returnedApprovedBook(
            @PathVariable("book-id") Integer bookId,
            Authentication connectedUser
    ) throws OperationNotPermittedException {
        return ResponseEntity.ok(bookService.returnedApprovedBook(bookId, connectedUser));
    }
    @PostMapping(value = "/cover/{book-id}", consumes = "multipart/form-data")
    public ResponseEntity<?> uploadBookCoverPicture(

            @PathVariable("book-id") Integer bookId,
            @Parameter()
            @RequestPart("file") MultipartFile file,
            Authentication connectedUser

    ){
        bookService.uploadBookCoverPicture(file, connectedUser, bookId);

        return ResponseEntity.accepted().build();
    }


}
