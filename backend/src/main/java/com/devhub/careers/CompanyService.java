package com.devhub.careers;

import com.devhub.users.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;

    @Transactional
    public Company findOrCreate(User currentUser, String name, String website) {
        return companyRepository.findByUserIdAndNameIgnoreCase(currentUser.getId(), name)
                .orElseGet(() -> companyRepository.save(
                        Company.builder()
                                .user(currentUser)
                                .name(name)
                                .website(website)
                                .build()));
    }
}
