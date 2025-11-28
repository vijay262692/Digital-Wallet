package com.digitalwallet.service;
import com.digitalwallet.model.RefreshToken;
import  com.digitalwallet.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import com.digitalwallet.model.User;
import java.util.UUID;



@Service
public class RefreshTokenService {

    @Autowired
    private RefreshTokenRepository refreshTokenRepo;

    
    @Transactional
    public RefreshToken createRefreshToken(User user) {
        refreshTokenRepo.deleteByUser(user); // remove old token

        RefreshToken rt = new RefreshToken();
        rt.setUser(user);
        rt.setToken(UUID.randomUUID().toString());
        rt.setExpiryDate(LocalDateTime.now().plusDays(7)); // valid 7 days

        return refreshTokenRepo.save(rt); 
    }

    public boolean isExpired(RefreshToken rt) {
        return rt.getExpiryDate().isBefore(LocalDateTime.now());
    }
}

