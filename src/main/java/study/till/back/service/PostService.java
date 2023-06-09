package study.till.back.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import study.till.back.dto.FindPostResponse;
import study.till.back.dto.PostRequest;
import study.till.back.dto.PostResponse;
import study.till.back.entity.Member;
import study.till.back.entity.Post;
import study.till.back.repository.MemberRepository;
import study.till.back.repository.PostRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {
    private final MemberRepository memberRepository;
    private final PostRepository postRepository;

    public ResponseEntity<List<FindPostResponse>> findPosts() {
        List<Post> posts = postRepository.findAll();

        List<FindPostResponse> findPostResponses = posts.stream().map(post -> FindPostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .contents(post.getContents())
                .createdDate(post.getCreatedDate())
                .build()
        ).collect(Collectors.toList());
        return ResponseEntity.ok(findPostResponses);
    }

    public ResponseEntity<FindPostResponse> findPost(Long postId) {
        Post post = postRepository.findById(postId).get();

        FindPostResponse findPostResponse = FindPostResponse.builder()
                .id(post.getId())
                .member_id(post.getMember().getId())
                .title(post.getTitle())
                .contents(post.getContents())
                .createdDate(post.getCreatedDate())
                .build();

        return ResponseEntity.ok(findPostResponse);
    }

    public ResponseEntity<PostResponse> createPost(PostRequest postRequest) {
        Optional<ResponseEntity<PostResponse>> memberCheckResult = validateMember(postRequest.getMember_id());
        if (memberCheckResult.isPresent()) {
            return memberCheckResult.get();
        }

        Member member = memberRepository.findById(postRequest.getMember_id()).orElse(null);

        Post post = Post.builder()
                .title(postRequest.getTitle())
                .contents(postRequest.getContents())
                .member(member)
                .createdDate(LocalDateTime.now())
                .build();
        postRepository.save(post);

        PostResponse postResponse = PostResponse.builder()
                .status("SUCCESS")
                .message("게시글 저장 성공")
                .build();
        return ResponseEntity.ok(postResponse);
    }

    public ResponseEntity<PostResponse> updatePost(PostRequest postRequest) {
        Optional<ResponseEntity<PostResponse>> memberCheckResult = validateMember(postRequest.getMember_id());
        if (memberCheckResult.isPresent()) {
            return memberCheckResult.get();
        }

        Optional<ResponseEntity<PostResponse>> postCheckResult = validatePost(postRequest.getId());
        if (postCheckResult.isPresent()) {
            return postCheckResult.get();
        }

        Post post = postRepository.findById(postRequest.getId()).orElse(null);
        post.setTitle(postRequest.getTitle());
        post.setContents(postRequest.getContents());
        post.setUpdateDate(LocalDateTime.now());
        postRepository.save(post);

        PostResponse postResponse = PostResponse.builder()
                .status("SUCCESS")
                .message("게시글이 수정되었습니다.")
                .build();
        return ResponseEntity.ok(postResponse);
    }

    public ResponseEntity<PostResponse> deletePost(Long id) {
        Optional<ResponseEntity<PostResponse>> postCheckResult = validatePost(id);
        if (postCheckResult.isPresent()) {
            return postCheckResult.get();
        }

        postRepository.deleteById(id);

        PostResponse postResponse = PostResponse.builder()
                .status("SUCCESS")
                .message("게시글이 삭제되었습니다.")
                .build();
        return ResponseEntity.ok(postResponse);
    }

    public Optional<ResponseEntity<PostResponse>> validateMember(Long memberId) {
        Member member = memberRepository.findById(memberId).orElse(null);
        if (member == null) {
            PostResponse postResponse = PostResponse.builder()
                    .status("FAIL")
                    .message("등록된 회원 정보가 없습니다.")
                    .build();
            return Optional.of(ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(postResponse));
        }
        return Optional.empty();
    }

    public Optional<ResponseEntity<PostResponse>> validatePost(Long postId) {
        Post post = postRepository.findById(postId).orElse(null);
        if (post == null) {
            PostResponse postResponse = PostResponse.builder()
                    .status("FAIL")
                    .message("존재하지 않는 게시글입니다.")
                    .build();
            return Optional.of(ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(postResponse));
        }
        return Optional.empty();
    }
}
