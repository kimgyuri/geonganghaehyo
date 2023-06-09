package com.example.main.jwt;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import com.example.main.Manager.dto.Auth;
import com.example.main.Manager.dto.ROLE;
import com.example.main.SetTop.dto.SETTOP_ROLE;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.DecodingException;
import io.jsonwebtoken.security.Keys;

/**
 * JWT 토큰 처리기
 */
@Component
public class JwtTokenProvider {
    /**
     * Secret Key
     */
    @Value("${jwt.token.secret}")
    private String secretKey = "fdjsfkjldfjwikwl1234456789120djfkdjsaf";
    /**
     * encrypted Secret Key
     */
    private Key key;
    /**
     * AccessToken 유효시간(10분)
     */
    @Value("${jwt.access-token.expire-length}")
    private String accessTokenValidMilSecond = "21600000";
    /**
     * RefreshToken 유효시간(일주일)
     */
    @Value("${jwt.refresh-token.expire-length}")
    private String refreshTokenValidMilSecond = "1209600000";

    /**
     * SecretKey 암호화 하면서 초기화
     */


    public JwtTokenProvider() {
        this.key = Keys.hmacShaKeyFor(this.secretKey.getBytes());
    }

    /**
     * AccessToken 생성
     *
     * @param email 발급할 사용자의 아이디
     * @param role  사용자에게 허용할 권한
     * @return AccessToken
     */
    public String createAccessToken(String email, String name, String location, ROLE role) {
        return generateToken(email, name, location, role, Long.parseLong(accessTokenValidMilSecond));
    }

    /**
     * RefreshToken 생성
     *
     * @param email 발급할 사용자의 아이디
     * @param role  사용자에게 허용할 권한
     * @return AccessToken
     */
    public String createRefreshToken(String email, String name, String location, ROLE role) {
        return generateToken(email, name, location, role, Long.parseLong(refreshTokenValidMilSecond));
    }

    /**
     * JWTToken 생성
     *
     * @param email              발급할 사용자의 아이디
     * @param role               사용자에게 허용할 권한
     * @param tokenValidMilSecond 토큰 유효시간
     * @return AccessToken
     */
    protected String generateToken(String email, String name, String location, ROLE role, long tokenValidMilSecond) {
        Date now = new Date();
//        System.out.println("generateToken location : "+location);
        return Jwts.builder()
                .claim("email", email)
                .claim("name", name)
                .claim("location", location)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + tokenValidMilSecond))
                .signWith(this.key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Http Request 에서 JWT 토큰의 데이터 추출
     * Authorization 헤더에 Bearer [Token] 형태로 되어야 함
     *
     * @param req Http 요청
     * @return 토큰 데이터
     */
    public Claims resolveToken(HttpServletRequest req) {
        String token = req.getHeader("Authorization");
        if (token == null)
            return null;
        else if (token.contains("Bearer"))
            token = token.replace("Bearer ", "");
        else
            throw new DecodingException("");

        return getClaimsFromToken(token);
    }

    /**
     * 토큰에서 토큰 데이터를 추출
     *
     * @param token JWT 토큰
     * @return 토큰 데이터
     */
    public Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Spring Security 인증토큰 발급
     * accessToken 은 주기가 짧기 때문에 검사없이 허용한다.
     * 매번 DB에 검증 하기엔 OverHead 가 너무 큼
     *
     * @param claims JWT 토큰 데이터
     * @return Spring Security 인증토큰
     */
    public Authentication getAuthentication(Claims claims) {
        return new UsernamePasswordAuthenticationToken(new Auth(claims), "", getAuthorities(claims));
    }

    /**
     * JWT 토큰 데이터 에서 UserID 추출
     *
     * @param claims JWT 토큰 데이터
     * @return UserId
     */
    public String getUserId(Claims claims) {
        return (String) claims.get("id");
    }

    /**
     * JWT 토큰 데이터 Roles 추출
     *
     * @param claims JWT 토큰 데이터
     * @return 사용자 권한 정보
     */
    private Collection<? extends GrantedAuthority> getAuthorities(Claims claims) {
        return Arrays.stream(new String[]{claims.get("role").toString()})
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
    }
}
