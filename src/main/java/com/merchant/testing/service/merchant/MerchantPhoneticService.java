package com.merchant.testing.service.merchant;

import com.merchant.testing.service.external.DictionaryApiService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
class MerchantPhoneticService {
    private final DictionaryApiService dictionaryApiService;

    MerchantPhoneticService(DictionaryApiService dictionaryApiService) {
        this.dictionaryApiService = dictionaryApiService;
    }

    public Optional<String> getPhonetics(String name) {
        List<Map<String, String>> dictionary = dictionaryApiService.getDictionary(name);
        if (dictionary.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(dictionary.getFirst().get("phonetic"));
        
    }
}
