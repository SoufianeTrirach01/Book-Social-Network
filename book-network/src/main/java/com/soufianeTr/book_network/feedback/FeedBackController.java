package com.soufianeTr.book_network.feedback;

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
@RequestMapping("feedbacks")
@RequiredArgsConstructor
@Tag(name = "feedback")
public class FeedBackController {
    private  final FeedBackService   feedBackService;

    @RequestMapping("/")
    public ResponseEntity<Integer> saveFeedBack(@RequestBody @Valid FeedbackRequest request,
                                                Authentication connectedUser) throws OperationNotPermittedException {
        return ResponseEntity.ok(feedBackService.saveFeedBack(request,connectedUser));

    }



    public ResponseEntity<PageResponse<FeedbackResponse>> findAllFeedbacksByBook(
            @PathVariable("book-id") Integer bookId,
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) int size,
            Authentication connectedUser
    ) {
        return ResponseEntity.ok(feedBackService.findAllFeedbacksByBook(bookId, page, size, connectedUser));
    }


}
