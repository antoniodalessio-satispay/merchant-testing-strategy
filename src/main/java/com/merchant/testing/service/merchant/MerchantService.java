package com.merchant.testing.service.merchant;

import com.merchant.testing.entity.Merchant;
import com.merchant.testing.entity.MerchantBuilder;
import com.merchant.testing.repository.MerchantRepository;
import com.merchant.testing.service.merchant.bean.MerchantCreateBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class MerchantService {

    private final MerchantLoadServiceStrategy merchantLoadServiceStrategy;
    private final MerchantRepository merchantRepository;
    private final MerchantPhoneticService merchantPhoneticService;

    public MerchantService(
            MerchantLoadServiceStrategy merchantLoadServiceStrategy, MerchantRepository merchantRepository,
            MerchantPhoneticService merchantPhoneticService) {
        this.merchantLoadServiceStrategy = merchantLoadServiceStrategy;
        this.merchantRepository = merchantRepository;
        this.merchantPhoneticService = merchantPhoneticService;
    }

    @Transactional
    public Merchant createMerchant(MerchantCreateBean merchantCreateBean) {
        if (merchantRepository.existsByEmail(merchantCreateBean.email())) {
            throw new IllegalArgumentException("Merchant with email already exists");
        }
        String phonetics = merchantPhoneticService.getPhonetics(merchantCreateBean.name()).orElse("");
        Merchant merchant = MerchantBuilder.aMerchant()
                .withEmail(merchantCreateBean.email())
                .withName(merchantCreateBean.name())
                .withPhonetics(phonetics)
                .build();
        return merchantRepository.save(merchant);
    }

    public List<Merchant> getAllMerchants() {
        return merchantRepository.findAll();
    }

    public Optional<Merchant> getMerchantById(Long id) {
        return merchantLoadServiceStrategy.loadMerchant(id.toString());
    }

    public Optional<Merchant> getMerchantByEmail(String email) {
        return merchantLoadServiceStrategy.loadMerchantByEmail(email);
    }

    @Transactional
    public Merchant updateMerchant(Long id, Merchant merchantDetails) {
        Merchant merchant = merchantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Merchant not found"));

        merchant.setName(merchantDetails.getName());
        merchant.setBusinessType(merchantDetails.getBusinessType());

        return merchantRepository.save(merchant);
    }

    @Transactional
    public void deleteMerchant(Long id) {
        merchantRepository.deleteById(id);
    }
}
