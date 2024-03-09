package com.cn.auth.config.jwt;

import com.alibaba.fastjson.JSONObject;
import com.cn.auth.config.Constant;
import com.cn.auth.entity.User;
import com.pub.core.utils.DateUtils;
import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class TokenProvider {

    public static final String AUTHORIZATION_HEADER = "Authorization";

    public static final String AUTHORIZATION_HEADER_ONLINE = "Authorization_Online";

    public static final String AUTHORIZATION_HEADER_HEAVEN = "Authorization_HEAVEN";

    private final Logger log = LoggerFactory.getLogger(TokenProvider.class);

    private static final String AUTHORITIES_KEY = "auth";

    private static final String ID_KEY = "id";

    private String secretKey="32165321513123165iuiuhiu";

    private long tokenValidityInMilliseconds=30;

    private long tokenValidityInMillisecondsForRememberMe=30;



    public String createToken(User user) {
        long now = (new Date()).getTime();

        /**
         * 五分钟
         */
        Date validity = new Date(now + 1000 * 10 *60);

        return Jwts.builder()
                .setSubject(user.getLoginName())
                .claim(ID_KEY, user.getId())
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .setExpiration(validity)
                .compact();
    }


    public String createTokenNewONline(User user) {
        long now = (new Date()).getTime();
        /**
         * 10分钟
         */
        Date validity = new Date(now + 1000  *10*60);


        return Jwts.builder()
                .setSubject(user.getLoginName())
                .claim(ID_KEY, user.getId())
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .setExpiration(validity)
                .compact();
    }
    public String createTokenNewSchool(User user) {
        long now = (new Date()).getTime();
        /**
         * 10分钟
         */
        Date validity = new Date(now + 1000*60*10);


        return Jwts.builder()
                .setSubject(user.getLoginName())
                .claim(ID_KEY, user.getId())
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .setExpiration(validity)
                .compact();
    }


    public String getUserId(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();

        return claims.get(ID_KEY).toString();
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(authToken);
            return true;
        } catch (SignatureException e) {
            log.info("Invalid JWT signature.");
            log.trace("Invalid JWT signature trace: {}", e);
        } catch (MalformedJwtException e) {
            log.info("Invalid JWT token.");
            log.trace("Invalid JWT token trace: {}", e);
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT token.");
            log.trace("Expired JWT token trace: {}", e);
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT token.");
            log.trace("Unsupported JWT token trace: {}", e);
        } catch (IllegalArgumentException e) {
            log.info("JWT token compact of handler are invalid.");
            log.trace("JWT token compact of handler are invalid trace: {}", e);
        } catch (Exception e) {
            log.info("JWT token compact of handler are invalid.");
            log.trace("JWT token compact of handler are invalid trace: {}", e);
        }
        return false;
    }

    public JSONObject validateTokenNew(String authToken) {
        JSONObject js=new JSONObject();
        js.put("code", Constant.Online.Jwt.sucess_code);
        js.put("msg",Constant.Online.Jwt.sucess_msg);
        try {
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(authToken);
            return js;
        } catch (SignatureException e) {
            log.error("Invalid JWT signature.");
            log.trace("Invalid JWT signature trace: {}", e);
            js.put("code", Constant.Online.Jwt.SignatureException_err_code);
            js.put("msg",Constant.Online.Jwt.SignatureException_err_msg);
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token.");
            log.trace("Invalid JWT token trace: {}", e);
            js.put("code", Constant.Online.Jwt.MalformedJwtException_err_code);
            js.put("msg",Constant.Online.Jwt.MalformedJwtException_err_msg);
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token.");
            log.trace("Expired JWT token trace: {}", e);
            js.put("code", Constant.Online.Jwt.ExpiredJwtException_err_code);
            js.put("msg",Constant.Online.Jwt.ExpiredJwtException_err_msg);
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token.");
            log.trace("Unsupported JWT token trace: {}", e);
            js.put("code", Constant.Online.Jwt.UnsupportedJwtException_err_code);
            js.put("msg",Constant.Online.Jwt.UnsupportedJwtException_err_msg);
        } catch (IllegalArgumentException e) {
            log.error("JWT token compact of handler are invalid.============"+e.getMessage());
            log.trace("JWT token compact of handler are invalid trace: {}", e);
            js.put("code", Constant.Online.Jwt.IllegalArgumentException_err_code);
            js.put("msg",Constant.Online.Jwt.IllegalArgumentException_err_msg);
        } catch (Exception e) {
            log.error("Exception JWT token compact of handler are invalid.----------------"+e.getMessage());
            log.trace("JWT token compact of handler are invalid trace: {}", e);
            js.put("code", Constant.Online.Jwt.Exception_err_code);
            js.put("msg",Constant.Online.Jwt.Exception_err_msg);
        }
        return js;
    }

}
