package devacc11011.spring.controller;

import devacc11011.spring.dto.NoticeRequest;
import devacc11011.spring.dto.NoticeResponse;
import devacc11011.spring.entity.Notice;
import devacc11011.spring.exception.UnauthorizedException;
import devacc11011.spring.security.CustomOAuth2User;
import devacc11011.spring.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class NoticeController {

	private final NoticeService noticeService;

	@GetMapping
	public List<NoticeResponse> getAllNotices() {
		List<Notice> notices = noticeService.findAllNotices();
		return notices.stream()
			.map(NoticeResponse::from)
			.collect(Collectors.toList());
	}

	@GetMapping("/{id}")
	public NoticeResponse getNoticeById(@PathVariable Long id) {
		Notice notice = noticeService.findById(id);
		return NoticeResponse.from(notice);
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.CREATED)
	public NoticeResponse createNotice(
		@RequestBody NoticeRequest request,
		@AuthenticationPrincipal CustomOAuth2User oAuth2User
	) {
		if (oAuth2User == null) {
			throw new UnauthorizedException();
		}

		Notice notice = noticeService.createNotice(request, oAuth2User.getUser());
		return NoticeResponse.from(notice);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public NoticeResponse updateNotice(
		@PathVariable Long id,
		@RequestBody NoticeRequest request
	) {
		Notice notice = noticeService.updateNotice(id, request);
		return NoticeResponse.from(notice);
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteNotice(@PathVariable Long id) {
		noticeService.deleteNotice(id);
	}
}
