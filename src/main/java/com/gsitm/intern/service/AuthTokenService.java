package com.gsitm.intern.service;

import com.gsitm.intern.entity.AuthToken;
import com.gsitm.intern.entity.Member;
import com.gsitm.intern.repository.AuthTokenRepository;
import com.gsitm.intern.repository.MemberRepository;
import com.gsitm.intern.utils.EncryptionUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthTokenService {

    private final AuthTokenRepository authTokenRepository;
    private final MemberRepository memberRepository;

    public String getTokenCode(String email) {
        //해당 Email에 해당하는 Member에 할당된 code 가져오기(해쉬값)
        String code = EncryptionUtils.encryptMD5(email + LocalDateTime.now());

        return code;
    }
    public AuthToken getToken(String email) {
        Member member = memberRepository.findByEmail(email);
        AuthToken authtoken = authTokenRepository.findFirstByMemberOrderByRegTimeDesc(member);
        return authtoken;
    }

    public AuthToken createToken(String email) {
        AuthToken authToken = new AuthToken();
        //토큰 코드 생성(호출)
        authToken.setCode(getTokenCode(email));
        //사용자 정보 등록(Member)
        authToken.setMember(memberRepository.findByEmail(email));
        //만료 시간 설정
        authToken.setExpireDate(LocalDateTime.now().plusHours(1));

        return authTokenRepository.save(authToken);
    }

    public void invalidateToken(String email) {
        //해당 Email에 해당하는 Member에 할당된 토큰 사용여부 설정
        AuthToken authToken = getToken(email);
        authToken.setUseYn("Y");
    }

    public boolean validateExpireToken(String email, String code) {
        AuthToken authToken = getToken(email);
        // AuthCode의 code와 매개변수의 code가 같은지 비교
        if (!code.equals(authToken.getCode())) {
            return false;
        }
        if (!authToken.getUseYn().equals("N")) {
            return false;
        }
        // AuthCode의 expireDate가 현재 시간보다 작은지 비교
        if (authToken.getExpireDate().isBefore(LocalDateTime.now())) {
            return false;
        }
        return true;
    }

    public AuthToken getTokenByCode(String code){
        AuthToken authToken = authTokenRepository.findFirstByCodeOrderByRegTimeDesc(code);

        return authToken;
    }
}
