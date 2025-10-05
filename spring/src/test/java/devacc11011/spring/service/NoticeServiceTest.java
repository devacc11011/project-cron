package devacc11011.spring.service;

import devacc11011.spring.dto.NoticeRequest;
import devacc11011.spring.entity.Notice;
import devacc11011.spring.entity.User;
import devacc11011.spring.exception.NotFoundException;
import devacc11011.spring.repository.NoticeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoticeServiceTest {

	@Mock
	private NoticeRepository noticeRepository;

	@InjectMocks
	private NoticeService noticeService;

	private User testUser;
	private Notice testNotice;

	@BeforeEach
	void setUp() {
		testUser = User.builder()
			.id(1L)
			.discordId("123456789")
			.username("testuser")
			.email("test@example.com")
			.build();

		testNotice = Notice.builder()
			.id(1L)
			.title("Test Notice")
			.content("Test Content")
			.author(testUser)
			.createdAt(LocalDateTime.now())
			.updatedAt(LocalDateTime.now())
			.build();
	}

	@Test
	@DisplayName("모든 공지사항 조회")
	void findAllNotices() {
		when(noticeRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(testNotice));

		List<Notice> notices = noticeService.findAllNotices();

		assertThat(notices).hasSize(1);
		assertThat(notices.get(0).getTitle()).isEqualTo("Test Notice");
		verify(noticeRepository, times(1)).findAllByOrderByCreatedAtDesc();
	}

	@Test
	@DisplayName("ID로 공지사항 조회 성공")
	void findById_Success() {
		when(noticeRepository.findById(1L)).thenReturn(Optional.of(testNotice));

		Notice notice = noticeService.findById(1L);

		assertThat(notice.getTitle()).isEqualTo("Test Notice");
		verify(noticeRepository, times(1)).findById(1L);
	}

	@Test
	@DisplayName("ID로 공지사항 조회 실패 - 존재하지 않음")
	void findById_NotFound() {
		when(noticeRepository.findById(999L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> noticeService.findById(999L))
			.isInstanceOf(NotFoundException.class)
			.hasMessageContaining("Notice not found");

		verify(noticeRepository, times(1)).findById(999L);
	}

	@Test
	@DisplayName("공지사항 생성")
	void createNotice() {
		NoticeRequest request = NoticeRequest.builder()
			.title("New Notice")
			.content("New Content")
			.build();

		when(noticeRepository.save(any(Notice.class))).thenReturn(testNotice);

		Notice notice = noticeService.createNotice(request, testUser);

		assertThat(notice).isNotNull();
		verify(noticeRepository, times(1)).save(any(Notice.class));
	}

	@Test
	@DisplayName("공지사항 수정")
	void updateNotice() {
		NoticeRequest request = NoticeRequest.builder()
			.title("Updated Notice")
			.content("Updated Content")
			.build();

		when(noticeRepository.findById(1L)).thenReturn(Optional.of(testNotice));
		when(noticeRepository.save(any(Notice.class))).thenReturn(testNotice);

		Notice notice = noticeService.updateNotice(1L, request);

		assertThat(notice.getTitle()).isEqualTo("Updated Notice");
		assertThat(notice.getContent()).isEqualTo("Updated Content");
		verify(noticeRepository, times(1)).findById(1L);
		verify(noticeRepository, times(1)).save(any(Notice.class));
	}

	@Test
	@DisplayName("공지사항 삭제")
	void deleteNotice() {
		when(noticeRepository.findById(1L)).thenReturn(Optional.of(testNotice));
		doNothing().when(noticeRepository).delete(any(Notice.class));

		noticeService.deleteNotice(1L);

		verify(noticeRepository, times(1)).findById(1L);
		verify(noticeRepository, times(1)).delete(testNotice);
	}
}
