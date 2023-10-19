package com.example.project4.controller;

import com.example.project4.Exeption.SomeSpecificException;
import com.example.project4.dto.NotificationFormDto;
import com.example.project4.entity.Member;
import com.example.project4.dto.MemberFormDto;
import com.example.project4.entity.Notification;
import com.example.project4.repository.MemberRepository;
import com.example.project4.service.BoardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.Model;
import com.example.project4.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import java.util.List;

@RequestMapping("/members")
@Controller
@RequiredArgsConstructor
public class MemberController {
    private final PasswordEncoder passwordEncoder;
    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final BoardService boardService;

    @GetMapping(value = "/mypage")
    public String myPage(Model model) {
        try {
            // 현재 사용자의 이메일 가져오기
            String email = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();

            Member member = memberService.getMemberData(email);
            model.addAttribute("myPage", member);
            return "member/mypage"; // 정보 출력 폼
        } catch (EntityNotFoundException ex) {
            // 회원을 찾을 수 없는 경우에 대한 예외 처리해서 오류페이지로 넘기기
            model.addAttribute("error", "Member not found");
            return "errorPage"; // 오류 페이지 템플릿 이름
        }
    }

    @PostMapping(value="/mypage")
    public String updateMember(@Valid MemberFormDto memberFormDto, BindingResult bindingResult, Model model) {
        // "password" 필드의 유효성 검사 오류를 제거
        bindingResult.getFieldErrors("password").clear();
        if (bindingResult.hasErrors()) {
            // 유효성 검사 에러가 있는 경우
            return "member/mypage";
        }

        try {
            Member member = Member.updateMember(memberFormDto, passwordEncoder, memberRepository);
            if (passwordEncoder.matches(memberFormDto.getUpdate_pw(), member.getPassword())) {
                // 비밀번호가 일치하면 정보 업데이트
                memberService.saveMember(member);
                model.addAttribute("successMessage", "회원 정보가 성공적으로 업데이트되었습니다.");
            } else {
                // 비밀번호가 일치하지 않으면 에러 메시지 설정
                model.addAttribute("passwordError", "비밀번호가 일치하지 않습니다.");
            }
        } catch (SomeSpecificException e) {
            // 수정이 실패한 경우, 해당 예외에 따라 에러 메시지를 추가하거나 다른 처리를 수행
            model.addAttribute("errorMessage", "수정 중 오류가 발생했습니다: " + e.getMessage());
        }
        // 필요한 데이터나 폼 필드의 값을 모델에 추가
        model.addAttribute("myPage", memberFormDto);

//        return "member/mypage";
        return "redirect:/members/mypage";
    }


    @GetMapping("/checkEmailDuplicate")
    public ResponseEntity<String> checkEmailDuplicate() {
        // GET 요청 처리 로직을 여기에 추가
        return ResponseEntity.ok("GET 요청이 처리되었습니다.");
    }

    @GetMapping(value ="/new")
    public String memberForm(Model model){
        model.addAttribute("memberFormDto", new MemberFormDto());
        return "member/createAccount";

    }
    @PostMapping(value = "/new")
    public String newMember(@Valid MemberFormDto memberFormDto, BindingResult bindingResult, Model model) {
        System.out.println("-----------폼 제출 후 이메일 확인");
        System.out.println(memberFormDto.getEmail());

        if (bindingResult.hasErrors()) {
            return "member/createAccount";
        }

        try {
            memberService.checkEmailDuplicate(memberFormDto.getEmail());
            Member member = Member.createMember(memberFormDto, passwordEncoder);
            memberService.saveMember(member);
        } catch (IllegalStateException e) {
            model.addAttribute("errorMessage", e.getMessage());  // 오타 수정
            return "member/createAccount";
        }

        return "redirect:/";
    }

    @GetMapping(value="/new/error")
    public  String createError(Model model){
        model.addAttribute("createErrorMsg", "비밀번호를 맞게 입력해주세요");
        return "member/createAccount";
    }

    @GetMapping(value = "/login")
    public String loginMember() {return  "member/login";}


    @GetMapping(value = "/login/error")
    public String loginError(Model model){
        model.addAttribute("loginErrorMsg", "아이디 혹은 비밀번호를 맞게 입력해주세요");
        return "member/login";
    }





}