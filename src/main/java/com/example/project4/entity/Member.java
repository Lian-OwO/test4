package com.example.project4.entity;

import com.example.project4.constant.Role;
import com.example.project4.dto.MemberFormDto;
import com.example.project4.repository.MemberRepository;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.tomcat.jni.Address;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.*;
import java.net.PasswordAuthentication;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "member")
@Getter @Setter
@ToString
public class Member extends BaseEntity{
    @Id
    @Column(name = "member_id")
    @GeneratedValue(strategy = GenerationType.AUTO)


    private Long id;
    private String name;
    @Column(unique = true)
    private String email;
    private String password;
    private String update_pw;
    private String address;
//    private String detail_Address;
//    @Column(name = "zipCode")
//    private int zipCode;
    @Enumerated(EnumType.STRING)
    private Role role;
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<Like> likeList = new ArrayList<>();


    public static Member createMember(MemberFormDto memberFormDto, PasswordEncoder passwordEncoder){
        Member member = new Member();
        member.setName(memberFormDto.getName());
        member.setEmail(memberFormDto.getEmail());
        member.setAddress(memberFormDto.getAddress());
//        member.setDetail_Address(memberFormDto.getDetail_Address());
//        member.setZipCode(memberFormDto.getZipCode());
        String password = passwordEncoder.encode(memberFormDto.getPassword());
        member.setPassword(password);
        member.setRole(Role.ADMIN);
        return member;
    }

    public static Member updateMember(MemberFormDto memberFormDto, PasswordEncoder passwordEncoder, MemberRepository memberRepository){
        Member member = memberRepository.findByEmail(memberFormDto.getEmail()).orElse(null);
        if (member != null) {
            member.setName(memberFormDto.getName());
            member.setEmail(memberFormDto.getEmail());
            member.setAddress(memberFormDto.getAddress());
//            member.setDetail_Address(memberFormDto.getDetail_Address());
//            member.setZipCode(memberFormDto.getZipCode());

            // 비밀번호 업데이트가 필요한 경우:
            if (!memberFormDto.getPassword().isEmpty()) {
                String password = passwordEncoder.encode(memberFormDto.getPassword());
                member.setPassword(password);
            }

            // 업데이트된 멤버 엔티티 저장
            return memberRepository.save(member);
        }
        return null;
    }

    public class loadMember{
        private Long id;
        private String name;
        private String email;
        private String password;
        private String address;
        private String detail_Address;
        private LocalDate registrationDate;
    }

}