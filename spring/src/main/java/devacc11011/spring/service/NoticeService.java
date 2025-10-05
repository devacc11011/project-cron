package devacc11011.spring.service;

import devacc11011.spring.dto.NoticeRequest;
import devacc11011.spring.entity.Notice;
import devacc11011.spring.entity.User;
import devacc11011.spring.exception.NotFoundException;
import devacc11011.spring.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {

	private final NoticeRepository noticeRepository;

	public List<Notice> findAllNotices() {
		return noticeRepository.findAllByOrderByCreatedAtDesc();
	}

	public Notice findById(Long id) {
		return noticeRepository.findById(id)
			.orElseThrow(() -> new NotFoundException("Notice not found with id: " + id));
	}

	@Transactional
	public Notice createNotice(NoticeRequest request, User author) {
		Notice notice = Notice.builder()
			.title(request.getTitle())
			.content(request.getContent())
			.author(author)
			.build();

		return noticeRepository.save(notice);
	}

	@Transactional
	public Notice updateNotice(Long id, NoticeRequest request) {
		Notice notice = findById(id);
		notice.setTitle(request.getTitle());
		notice.setContent(request.getContent());

		return noticeRepository.save(notice);
	}

	@Transactional
	public void deleteNotice(Long id) {
		Notice notice = findById(id);
		noticeRepository.delete(notice);
	}
}
